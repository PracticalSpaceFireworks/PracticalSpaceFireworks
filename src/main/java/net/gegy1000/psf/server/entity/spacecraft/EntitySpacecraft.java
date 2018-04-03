package net.gegy1000.psf.server.entity.spacecraft;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.client.render.spacecraft.model.SpacecraftModel;
import net.gegy1000.psf.server.block.remote.packet.PacketCraftState;
import net.gegy1000.psf.server.block.remote.packet.PacketOpenRemoteControl;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.capability.world.SatelliteWorldData;
import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.gegy1000.psf.server.satellite.EntityBoundSatellite;
import net.gegy1000.psf.server.util.Matrix;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Rotation;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class EntitySpacecraft extends Entity implements IEntityAdditionalSpawnData {
    private static final double AIR_RESISTANCE = 0.98;
    private static final double GRAVITY = 1.6;

    private static final DataParameter<Byte> STATE = EntityDataManager.createKey(EntitySpacecraft.class, DataSerializers.BYTE);
    private static final DataParameter<Float> ACCELERATION = EntityDataManager.createKey(EntitySpacecraft.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> FORCE = EntityDataManager.createKey(EntitySpacecraft.class, DataSerializers.FLOAT);

    private final Matrix rotationMatrix = new Matrix(3);

    static {
        PSFNetworkHandler.network.registerMessage(PacketLaunchCraft.Handler.class, PacketLaunchCraft.class, PSFNetworkHandler.nextID(), Side.SERVER);
    }

    @Getter
    private final EntityBoundSatellite satellite;

    @SideOnly(Side.CLIENT)
    public SpacecraftModel model;

    private SpacecraftBlockAccess blockAccess;

    @Getter
    private State state = new Static(this);

    private boolean converted;
    private LaunchMetadata metadata;

    public EntitySpacecraft(World world) {
        this(world, Collections.emptySet(), BlockPos.ORIGIN, null);
    }

    public EntitySpacecraft(World world, Set<BlockPos> positions, @Nonnull BlockPos origin, @Nullable UUID id) {
        super(world);
        this.setSize(1, 1);

        SpacecraftBuilder builder = new SpacecraftBuilder();
        builder.copyFrom(world, origin, positions);
        this.blockAccess = builder.buildBlockAccess(origin, this.world);

        this.satellite = new EntityBoundSatellite(this, id);
        this.satellite.detectModules();
        this.recalculateRotation();
        this.metadata = this.blockAccess.buildLaunchMetadata();

        if (!world.isRemote) {
            PracticalSpaceFireworks.PROXY.getSatellites().register(satellite);
        }
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(STATE, (byte) StateType.STATIC.ordinal());
        this.dataManager.register(FORCE, 0.0F);
        this.dataManager.register(ACCELERATION, 0.0F);
    }

    @Override
    public void onUpdate() {
        this.motionX *= AIR_RESISTANCE;
        this.motionY *= AIR_RESISTANCE;
        this.motionZ *= AIR_RESISTANCE;

        this.motionY -= GRAVITY / 20.0;

        State newState = this.state.update();

        if (!this.world.isRemote) {
            if (this.state.getType() != newState.getType()) {
                this.state = newState;
                this.dataManager.set(STATE, (byte) newState.getType().ordinal());
            }
        } else {
            StateType syncedStateType = StateType.values()[this.dataManager.get(STATE) % StateType.values().length];
            if (syncedStateType != this.state.getType()) {
                this.state = syncedStateType.create(this);
            }
        }

        double lastMotionY = motionY;

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

        if (lastMotionY <= -1 && this.collidedVertically) {
            if (!world.isRemote) {
                world.createExplosion(this, posX, getEntityBoundingBox().minY, posZ, (float) Math.log10(-lastMotionY * metadata.getMass()) + 1, true);
            }
            setDead();
        }

        if (posY > 1000) {
            setDead();

            if (!world.isRemote && world.hasCapability(CapabilityWorldData.SATELLITE_INSTANCE, null)) {
                SatelliteWorldData capability = world.getCapability(CapabilityWorldData.SATELLITE_INSTANCE, null);
                ISatellite orbiting = satellite.toOrbiting();
                capability.addSatellite(orbiting);
                converted = true;
                for (EntityPlayerMP player : orbiting.getTrackingPlayers()) {
                    PSFNetworkHandler.network.sendTo(new PacketCraftState(PacketOpenRemoteControl.SatelliteState.ORBIT, orbiting.toListedCraft()), player);
                }
            }
        }

        if (Math.abs(this.rotationYaw - this.prevRotationYaw) > 1e-3 || Math.abs(this.rotationPitch - this.prevRotationPitch) > 1e-3) {
            this.recalculateRotation();
        }

        if (!world.isRemote) {
            satellite.tickSatellite(ticksExisted);
        }

        super.onUpdate();
    }

    @Override
    public void setDead() {
        super.setDead();
        if (!converted && !world.isRemote) {
            PracticalSpaceFireworks.PROXY.getSatellites().remove(satellite);
        }
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
        if (!world.isRemote) {
            float prevRotationYaw = this.rotationYaw;
            float prevRotationPitch = this.rotationPitch;

            int yaw = Math.round((this.rotationYaw % 360) / 90.0F) * 90;
            while (yaw < 0) {
                yaw += 360;
            }

            this.rotationYaw = yaw;
            this.rotationPitch = Math.round(this.rotationPitch / 90.0F) * 90.0F;
            this.recalculateRotation();

            Rotation rotation = Rotation.values()[(yaw / 90 + 2) % 4];

            Optional<SpacecraftDeconstructor.Result> result = SpacecraftDeconstructor.deconstruct(world, blockAccess, posX, posY, posZ, rotationMatrix);
            if (result.isPresent()) {
                Map<BlockPos, IBlockState> blocks = result.get().getBlocks();
                Map<BlockPos, TileEntity> entities = result.get().getEntities();

                for (Map.Entry<BlockPos, IBlockState> entry : blocks.entrySet()) {
                    world.setBlockState(entry.getKey(), entry.getValue().withRotation(rotation), 10);
                }

                for (Map.Entry<BlockPos, TileEntity> entry : entities.entrySet()) {
                    world.setTileEntity(entry.getKey(), entry.getValue());
                }

                setDead();
            } else {
                this.rotationYaw = prevRotationYaw;
                this.rotationPitch = prevRotationPitch;
                this.recalculateRotation();
                return false;
            }
        }

        return true;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("block_data", this.blockAccess.serialize(new NBTTagCompound()));
        compound.setTag("satellite", this.satellite.serializeNBT());

        compound.setString("state", this.state.getType().name());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.blockAccess = SpacecraftBlockAccess.deserialize(compound.getCompoundTag("block_data"));
        this.satellite.deserializeNBT(compound.getCompoundTag("satellite"));
        this.metadata = this.blockAccess.buildLaunchMetadata();

        String state = compound.getString("state");
        this.state = StateType.valueOf(state).create(this);

        this.satellite.detectModules();
        this.recalculateRotation();
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

        this.satellite.detectModules();

        this.recalculateRotation();
        this.metadata = this.blockAccess.buildLaunchMetadata();
    }

    public void setState(StateType state) {
        this.state = state.create(this);
        if (!world.isRemote) {
            this.dataManager.set(STATE, (byte) state.ordinal());
        }
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

    public enum StateType {
        STATIC {
            @Override
            protected State create(EntitySpacecraft entity) {
                return new Static(entity);
            }
        },
        LAUNCH {
            @Override
            protected State create(EntitySpacecraft entity) {
                return new Launch(entity);
            }
        };

        protected abstract State create(EntitySpacecraft entity);
    }

    public interface State {
        default State update() {
            return this;
        }

        default double getCameraShake() {
            return 0.0;
        }

        StateType getType();
    }

    public static class Static implements State {
        private final EntitySpacecraft entity;

        public Static(EntitySpacecraft entity) {
            this.entity = entity;
        }

        @Override
        public State update() {
            if (!entity.world.isRemote) {
                this.entity.dataManager.set(ACCELERATION, 0.0F);
            }
            return this;
        }

        @Override
        public StateType getType() {
            return StateType.STATIC;
        }
    }

    public static class Launch implements State {
        private final EntitySpacecraft entity;
        private final IFluidHandler fuelHandler;

        private double lastForce;

        public Launch(EntitySpacecraft entity) {
            this.entity = entity;
            this.fuelHandler = entity.metadata.buildFuelHandler();
        }

        @Override
        public State update() {
            World world = this.entity.getEntityWorld();

            double acceleration = 0.0;
            double force = 0.0;
            if (world.isRemote) {
                acceleration = entity.dataManager.get(ACCELERATION);
                force = entity.dataManager.get(FORCE);
            } else {
                int totalDrain = entity.metadata.getTotalFuelDrain() / 20;
                FluidStack keroseneResult = this.fuelHandler.drain(new FluidStack(PSFFluidRegistry.KEROSENE, totalDrain), true);
                FluidStack liquidOxygenResult = this.fuelHandler.drain(new FluidStack(PSFFluidRegistry.LIQUID_OXYGEN, totalDrain), true);
                if (keroseneResult != null && keroseneResult.amount > 0 && liquidOxygenResult != null && liquidOxygenResult.amount > 0) {
                    force = entity.metadata.getTotalForce();
                    acceleration = force / entity.metadata.getMass() / 20.0;
                }

                entity.dataManager.set(ACCELERATION, (float) acceleration);
                entity.dataManager.set(FORCE, (float) force);
            }

            this.lastForce = force;

            if (acceleration > 1e-4) {
                this.entity.motionY += acceleration;
                this.entity.rotationYaw += Math.max(this.entity.motionY, 0.0F) * 0.5F;

                if (world.isRemote) {
                    for (LaunchMetadata.Thruster thruster : entity.metadata.getThrusters()) {
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
                return StateType.STATIC.create(entity);
            }
        }

        @Override
        public double getCameraShake() {
            return this.lastForce * 5e-7;
        }

        @Override
        public StateType getType() {
            return StateType.LAUNCH;
        }
    }
}
