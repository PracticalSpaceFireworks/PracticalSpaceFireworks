package net.gegy1000.psf.server.entity.spacecraft;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.gegy1000.psf.client.render.spacecraft.model.SpacecraftModel;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.capability.world.SatelliteWorldData;
import net.gegy1000.psf.server.satellite.EntityBoundSatellite;
import net.gegy1000.psf.server.util.Matrix;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class EntitySpacecraft extends Entity implements IEntityAdditionalSpawnData {
    private static final double AIR_RESISTANCE = 0.98;
    private static final double GRAVITY = 1.6;

    private final Matrix rotationMatrix = new Matrix(3);

    @Getter
    private final EntityBoundSatellite satellite = new EntityBoundSatellite(this);

    @SideOnly(Side.CLIENT)
    public SpacecraftModel model;

    private SpacecraftBlockAccess blockAccess;

    @Getter
    private State state = new Static();

    public EntitySpacecraft(World world) {
        this(world, Collections.emptySet(), BlockPos.ORIGIN);
    }

    public EntitySpacecraft(World world, Set<BlockPos> positions, @Nonnull BlockPos origin) {
        super(world);
        this.setSize(1, 1);

        SpacecraftBuilder builder = new SpacecraftBuilder();
        builder.copyFrom(world, origin, positions);
        this.blockAccess = builder.buildBlockAccess(this);

        this.satellite.detectModules();
        this.recalculateRotation();
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        this.motionX *= AIR_RESISTANCE;
        this.motionY *= AIR_RESISTANCE;
        this.motionZ *= AIR_RESISTANCE;

        this.motionY -= GRAVITY / 20.0;

        this.state = this.state.update();

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

        if (Math.abs(this.rotationYaw - this.prevRotationYaw) > 1e-3 || Math.abs(this.rotationPitch - this.prevRotationPitch) > 1e-3) {
            this.recalculateRotation();
        }

        super.onUpdate();
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);

        this.setEntityBoundingBox(this.calculateEncompassingBounds());
    }

    @Override
    @Nonnull
    public AxisAlignedBB getRenderBoundingBox() {
        return this.calculateEncompassingBounds();
    }

    @Nonnull
    private AxisAlignedBB calculateEncompassingBounds() {
        List<AxisAlignedBB> bounds = new ArrayList<>();
        this.collectTransformedBlockBounds(null, bounds);

        AxisAlignedBB ret = new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX, this.posY, this.posZ);
        for (AxisAlignedBB bb : bounds) {
            ret = ret.union(bb);
        }

        return ret;
    }

    @Override
    public void resetPositionToBB() {
        AxisAlignedBB bounds = this.calculateEncompassingBounds();
        AxisAlignedBB updatedBounds = this.getEntityBoundingBox();
        this.posX += updatedBounds.minX - bounds.minX;
        this.posY += updatedBounds.minY - bounds.minY;
        this.posZ += updatedBounds.minZ - bounds.minZ;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return super.isInRangeToRenderDist(distance / 8);
    }

    public void collectTransformedBlockBounds(@Nullable AxisAlignedBB reference, @Nonnull List<AxisAlignedBB> bounds) {
        if (this.blockAccess != null) {
            for (BlockPos pos : BlockPos.getAllInBoxMutable(this.blockAccess.getMinPos(), this.blockAccess.getMaxPos())) {
                IBlockState state = this.blockAccess.getBlockState(pos);
                AxisAlignedBB bb = state.getCollisionBoundingBox(this.blockAccess, pos);
                if (bb != null) {
                    AxisAlignedBB transformed = this.rotateBoundsEncompassing(bb, pos).offset(this.posX, this.posY, this.posZ);
                    if (reference == null || reference.intersects(transformed)) {
                        bounds.add(transformed);
                    }
                }
            }
        }
    }

    private AxisAlignedBB rotateBoundsEncompassing(AxisAlignedBB bounds, BlockPos pos) {
        bounds = bounds.offset(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5);
        return this.rotationMatrix.transform(bounds);
    }

    private void recalculateRotation() {
        this.rotationMatrix.identity();
        this.rotationMatrix.rotate(180.0F - this.rotationYaw, 0.0F, 1.0F, 0.0F);
        this.rotationMatrix.rotate(this.rotationPitch, 1.0F, 0.0F, 0.0F);

        this.setEntityBoundingBox(this.calculateEncompassingBounds());
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        this.state = new Launch(this);
        return true;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.blockAccess = SpacecraftBlockAccess.deserialize(compound.getCompoundTag("block_data"));
        this.satellite.deserializeNBT(compound.getCompoundTag("satellite"));

        String state = compound.getString("state");
        switch (state) {
            case "launch":
                this.state = new Launch(this);
                break;
            default:
                this.state = new Static();
                break;
        }

        this.satellite.detectModules();
        this.recalculateRotation();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("block_data", this.blockAccess.serialize(new NBTTagCompound()));
        compound.setTag("satellite", this.satellite.serializeNBT());

        compound.setString("state", this.state.serializeName());
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        this.blockAccess.serialize(buffer);
        ByteBufUtils.writeTag(buffer, this.satellite.serializeNBT());

        buffer.writeBoolean(this.state instanceof Launch);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.blockAccess = SpacecraftBlockAccess.deserialize(buffer);
        this.satellite.deserializeNBT(ByteBufUtils.readTag(buffer));
        this.model = null;

        if (buffer.readBoolean()) {
            this.state = new Launch(this);
        } else {
            this.state = new Static();
        }

        this.satellite.detectModules();

        this.recalculateRotation();
    }

    public SpacecraftBlockAccess getBlockAccess() {
        return this.blockAccess;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilitySatellite.INSTANCE ||
                super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilitySatellite.INSTANCE) {
            return CapabilitySatellite.INSTANCE.cast(this.satellite);
        }
        return super.getCapability(capability, facing);
    }

    public interface State {
        default State update() {
            return this;
        }

        default double getCameraShake() {
            return 0.0;
        }

        String serializeName();
    }

    public static class Static implements State {
        @Override
        public String serializeName() {
            return "static";
        }
    }

    public static class Launch implements State {
        private final EntitySpacecraft entity;
        private final LaunchMetadata metadata;
        private final IFluidHandler fuelHandler;

        private double force;

        public Launch(EntitySpacecraft entity) {
            this.entity = entity;
            this.metadata = this.entity.blockAccess.buildLaunchMetadata();
            this.fuelHandler = this.metadata.buildFuelHandler();
        }

        @Override
        public State update() {
            World world = this.entity.getEntityWorld();

            if (this.entity.posY > 1000) {
                this.entity.setDead();

                if (!world.isRemote && world.hasCapability(CapabilityWorldData.SATELLITE_INSTANCE, null)) {
                    SatelliteWorldData capability = world.getCapability(CapabilityWorldData.SATELLITE_INSTANCE, null);
                    capability.addSatellite(this.entity.satellite.toOrbiting());
                }
            }

            int totalDrain = this.metadata.getTotalFuelDrain() / 20;
            FluidStack result = this.fuelHandler.drain(totalDrain, true);
            if (result != null && result.amount > 0) {
                this.force = this.metadata.getTotalForce();
                double acceleration = this.force / this.metadata.getMass() / 20.0;
                this.entity.motionY += acceleration;

                this.entity.rotationYaw += Math.max(this.entity.motionY, 0.0F) * 0.5F;

                if (world.isRemote) {
                    for (LaunchMetadata.Thruster thruster : this.metadata.getThrusters()) {
                        BlockPos thrusterPos = thruster.getPos();
                        Point3d thrusterPoint = new Point3d(thrusterPos.getX(), thrusterPos.getY(), thrusterPos.getZ());
                        this.entity.rotationMatrix.transform(thrusterPoint);
                        double posX = this.entity.posX + thrusterPoint.x;
                        double posY = this.entity.posY + thrusterPoint.y;
                        double posZ = this.entity.posZ + thrusterPoint.z;
                        for (int i = 0; i < 30; i++) {
                            double motionX = (this.entity.rand.nextDouble() * 2.0 - 1) * 0.3;
                            double motionY = -acceleration;
                            double motionZ = (this.entity.rand.nextDouble() * 2.0 - 1) * 0.3;
                            world.spawnParticle(EnumParticleTypes.FLAME, true, posX + motionX, posY, posZ + motionZ, motionX, motionY, motionZ);
                        }
                    }
                }

                return this;
            } else {
                return new Static();
            }
        }

        @Override
        public double getCameraShake() {
            return this.force * 5e-7;
        }

        @Override
        public String serializeName() {
            return "launch";
        }
    }
}
