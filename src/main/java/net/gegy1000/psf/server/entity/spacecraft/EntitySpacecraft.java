package net.gegy1000.psf.server.entity.spacecraft;

import io.netty.buffer.ByteBuf;
import net.gegy1000.psf.client.render.spacecraft.model.SpacecraftModel;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
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
    private final EntityBoundSatellite satellite = new EntityBoundSatellite(this);

    @SideOnly(Side.CLIENT)
    public SpacecraftModel model;

    private SpacecraftBlockAccess blockAccess;
    private LaunchMetadata metadata;

    private boolean testLaunched;

    public EntitySpacecraft(World world) {
        this(world, Collections.emptySet(), BlockPos.ORIGIN);
    }

    public EntitySpacecraft(World world, Set<BlockPos> positions, @Nonnull BlockPos origin) {
        super(world);
        this.setSize(1, 1);

        SpacecraftBuilder builder = new SpacecraftBuilder();
        builder.copyFrom(world, origin, positions);
        this.blockAccess = builder.buildBlockAccess(this);
        this.metadata = builder.buildMetadata();

        this.satellite.detectModules();
        this.recalculateRotation();
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        this.motionX *= AIR_RESISTANCE;
        this.motionY *= AIR_RESISTANCE;
        this.motionZ *= AIR_RESISTANCE;

        this.motionY -= GRAVITY / 20.0;

        if (this.posY > 1000) {
            this.setDead();
        }

        if (Math.abs(this.rotationYaw - this.prevRotationYaw) > 1e-3 || Math.abs(this.rotationPitch - this.prevRotationPitch) > 1e-3) {
            this.recalculateRotation();
        }

        if (this.testLaunched) {
            double acceleration = this.metadata.getTotalAcceleration() / 20.0;
            this.motionY += acceleration;

            if (this.world.isRemote) {
                for (LaunchMetadata.Thruster thruster : this.metadata.getThrusters()) {
                    BlockPos thrusterPos = thruster.getPos();
                    Point3d thrusterPoint = new Point3d(thrusterPos.getX(), thrusterPos.getY(), thrusterPos.getZ());
                    this.rotationMatrix.transform(thrusterPoint);
                    double posX = this.posX + thrusterPoint.x;
                    double posY = this.posY + thrusterPoint.y;
                    double posZ = this.posZ + thrusterPoint.z;
                    for (int i = 0; i < 30; i++) {
                        double motionX = (this.rand.nextDouble() * 2.0 - 1) * 0.3;
                        double motionY = -acceleration;
                        double motionZ = (this.rand.nextDouble() * 2.0 - 1) * 0.3;
                        this.world.spawnParticle(EnumParticleTypes.FLAME, true, posX + motionX, posY, posZ + motionZ, motionX, motionY, motionZ);
                    }
                }
            }
        }

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);

        this.setEntityBoundingBox(this.calculateEncompassingBounds());
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.calculateEncompassingBounds();
    }

    @Override
    @Nonnull
    public AxisAlignedBB getRenderBoundingBox() {
        return this.calculateEncompassingBounds();
    }

    @Nonnull
    private AxisAlignedBB calculateEncompassingBounds() {
        AxisAlignedBB ret = new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        for (AxisAlignedBB bb : this.collectTransformedBlockBounds()) {
            ret = ret.union(bb);
        }

        return ret.offset(this.posX, this.posY, this.posZ);
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

    private List<AxisAlignedBB> collectTransformedBlockBounds() {
        List<AxisAlignedBB> bounds = new ArrayList<>();
        if (this.blockAccess != null) {
            for (BlockPos pos : BlockPos.getAllInBoxMutable(this.blockAccess.getMinPos(), this.blockAccess.getMaxPos())) {
                IBlockState state = this.blockAccess.getBlockState(pos);
                AxisAlignedBB bb = state.getCollisionBoundingBox(this.blockAccess, pos);
                if (bb != null) {
                    bounds.add(this.rotateBoundsEncompassing(bb, pos));
                }
            }
        }
        return bounds;
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
        this.testLaunched = true;
        return true;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.blockAccess = SpacecraftBlockAccess.deserialize(this.world, compound.getCompoundTag("block_data"));
        this.metadata = LaunchMetadata.deserialize(compound.getCompoundTag("metadata"));

        this.satellite.detectModules();
        this.recalculateRotation();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("block_data", this.blockAccess.serialize(new NBTTagCompound()));
        compound.setTag("metadata", this.metadata.serialize(new NBTTagCompound()));
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        this.blockAccess.serialize(buffer);
        this.metadata.serialize(buffer);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.blockAccess = SpacecraftBlockAccess.deserialize(this.world, buffer);
        this.metadata = LaunchMetadata.deserialize(buffer);
        this.model = null;

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
            return CapabilitySatellite.INSTANCE.cast(satellite);
        }
        return super.getCapability(capability, facing);
    }
}
