package net.gegy1000.psf.server.entity.spacecraft;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.client.particle.PSFParticles;
import net.gegy1000.psf.client.render.spacecraft.model.SpacecraftModel;
import net.gegy1000.psf.server.block.controller.CraftGraph;
import net.gegy1000.psf.server.block.module.BlockModule;
import net.gegy1000.psf.server.block.remote.packet.PacketCraftState;
import net.gegy1000.psf.server.block.remote.packet.PacketOpenRemoteControl;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.capability.world.SatelliteWorldData;
import net.gegy1000.psf.server.entity.world.DelegatedWorld;
import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.gegy1000.psf.server.satellite.EntityBoundSatellite;
import net.gegy1000.psf.server.util.Matrix;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
import java.util.Random;
import java.util.Set;

public class EntitySpacecraft extends Entity implements IEntityAdditionalSpawnData {
    public static final double AIR_RESISTANCE = 0.98;
    public static final double GRAVITY = 1.6;

    private static final DataParameter<Byte> STATE = EntityDataManager.createKey(EntitySpacecraft.class, DataSerializers.BYTE);
    private static final DataParameter<Float> ACCELERATION = EntityDataManager.createKey(EntitySpacecraft.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> FORCE = EntityDataManager.createKey(EntitySpacecraft.class, DataSerializers.FLOAT);

    @Getter
    private final Matrix rotationMatrix = new Matrix(3);

    @Getter
    private final Matrix inverseMatrix = new Matrix(3);

    private float lastRecalcYaw = Float.MAX_VALUE;
    private float lastRecalcPitch = Float.MAX_VALUE;

    static {
        PSFNetworkHandler.network.registerMessage(PacketLaunchCraft.Handler.class, PacketLaunchCraft.class, PSFNetworkHandler.nextID(), Side.SERVER);
    }

    @Getter
    private final EntityBoundSatellite satellite;

    @SideOnly(Side.CLIENT)
    public SpacecraftModel model;

    private DelegatedWorld delegatedWorld;
    private SpacecraftWorldHandler worldHandler;

    @Getter
    private State state = new Static(this);

    private boolean converted;
    private SpacecraftMetadata metadata;

    public EntitySpacecraft(World world) {
        this(world, null, BlockPos.ORIGIN, null);
    }

    public EntitySpacecraft(World world, @Nullable CraftGraph positions, @Nonnull BlockPos origin, @Nullable ISatellite parent) {
        super(world);
        this.setSize(1, 1);

        SpacecraftBuilder builder = new SpacecraftBuilder();
        builder.copyFrom(world, origin, positions);
        this.worldHandler = builder.buildWorldHandler(origin, this.world);
        this.delegatedWorld = new DelegatedWorld(this.world, this.worldHandler);

        if (parent != null) {
            this.satellite = new EntityBoundSatellite(this, parent.getId(), parent.getName());
            parent.getTrackingPlayers().forEach(satellite::track);

            if (!world.isRemote) {
                initSpacecraft();
            }
        } else {
            this.satellite = new EntityBoundSatellite(this, getUniqueID(), "Unnamed Craft #" + getUniqueID().hashCode() % 1000);
        }
    }

    public EntitySpacecraft(ISatellite craft) {
        super(craft.getWorld());
        this.setSize(1, 1);

        this.worldHandler = craft.buildWorldHandler(craft.getWorld());
        this.delegatedWorld = new DelegatedWorld(this.world, this.worldHandler);

        this.satellite = new EntityBoundSatellite(this, craft.getId(), craft.getName());
        initSpacecraft();
    }

    private void initSpacecraft() {
        this.satellite.detectModules();
        this.recalculateRotation();
        this.metadata = this.worldHandler.buildSpacecraftMetadata();

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

        if (Math.abs(this.rotationYaw - this.lastRecalcYaw) > 1e-3 || Math.abs(this.rotationPitch - this.lastRecalcPitch) > 1e-3) {
            this.recalculateRotation();
            this.lastRecalcYaw = this.rotationYaw;
            this.lastRecalcPitch = this.rotationPitch;
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
        if (worldHandler != null) {
            if (reference == null) {
                reference = TileEntity.INFINITE_EXTENT_AABB;
            }
            for (BlockPos pos : BlockPos.getAllInBoxMutable(worldHandler.getMinPos(), worldHandler.getMaxPos())) {
                IBlockState state = worldHandler.getBlockState(pos);
                if (state.getBlock() != Blocks.AIR) {
                    List<AxisAlignedBB> collisionBounds = new ArrayList<>();
                    state.addCollisionBoxToList(delegatedWorld, pos, TileEntity.INFINITE_EXTENT_AABB, collisionBounds, null, false);
                    for (AxisAlignedBB bb : collisionBounds) {
                        if (bb == null) {
                            continue;
                        }
                        AxisAlignedBB transformed = this.rotateBoundsEncompassing(bb, pos).offset(this.posX, this.posY, this.posZ);
                        if (reference.intersects(transformed)) {
                            bounds.add(transformed);
                        }
                    }
                }
            }
        }
    }

    private AxisAlignedBB rotateBoundsEncompassing(AxisAlignedBB bounds, BlockPos pos) {
        bounds = bounds.offset(-0.5, 0, -0.5);
        return this.rotationMatrix.transform(bounds);
    }

    private void recalculateRotation() {
        this.rotationMatrix.identity();
        this.rotationMatrix.rotate(180.0F - this.rotationYaw, 0.0F, 1.0F, 0.0F);
        this.rotationMatrix.rotate(this.rotationPitch, 1.0F, 0.0F, 0.0F);

        this.inverseMatrix.identity();
        this.inverseMatrix.multiply(this.rotationMatrix);
        this.inverseMatrix.inverse();

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

            Optional<SpacecraftDeconstructor.Result> result = SpacecraftDeconstructor.deconstruct(world, worldHandler, posX, posY, posZ, rotationMatrix);
            if (result.isPresent()) {
                Map<BlockPos, IBlockState> blocks = result.get().getBlocks();
                Map<BlockPos, TileEntity> entities = result.get().getEntities();

                BlockModule.CONVERTING.set(true);
                try {
                    for (Map.Entry<BlockPos, IBlockState> entry : blocks.entrySet()) {
                        world.setBlockState(entry.getKey(), entry.getValue().withRotation(rotation), 10);
                    }

                    for (Map.Entry<BlockPos, TileEntity> entry : entities.entrySet()) {
                        world.setTileEntity(entry.getKey(), entry.getValue());
                    }
                } finally {
                    BlockModule.CONVERTING.set(false);
                }

                this.converted = true;
                for (EntityPlayerMP p : satellite.getTrackingPlayers()) {
                    PSFNetworkHandler.network.sendTo(new PacketCraftState(PacketOpenRemoteControl.SatelliteState.TILE, satellite.toListedCraft()), p);
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
        compound.setTag("block_data", worldHandler.serialize(new NBTTagCompound()));
        compound.setTag("satellite", this.satellite.serializeNBT());

        compound.setString("state", this.state.getType().name());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.worldHandler = SpacecraftWorldHandler.deserializeCraft(compound.getCompoundTag("block_data"));
        this.delegatedWorld = new DelegatedWorld(this.world, worldHandler);

        this.satellite.deserializeNBT(compound.getCompoundTag("satellite"));

        String state = compound.getString("state");
        this.state = StateType.valueOf(state).create(this);

        initSpacecraft();
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        this.worldHandler.serialize(buffer);
        ByteBufUtils.writeTag(buffer, this.satellite.serializeNBT());

        buffer.writeBoolean(this.state instanceof Launch);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.worldHandler = SpacecraftWorldHandler.deserializeCraft(buffer);
        this.delegatedWorld = new DelegatedWorld(this.world, worldHandler);

        this.satellite.deserializeNBT(ByteBufUtils.readTag(buffer));
        this.model = null;

        initSpacecraft();

        prevRotationYaw = rotationYaw;
    }

    public void setState(StateType state) {
        this.state = state.create(this);
        if (!world.isRemote) {
            this.dataManager.set(STATE, (byte) state.ordinal());
        }
    }

    public DelegatedWorld getDelegatedWorld() {
        return delegatedWorld;
    }

    public SpacecraftWorldHandler getWorldHandler() {
        return worldHandler;
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
        private static final int ENGINE_WARMUP = 80;
        private static final int MIN_ACC = ENGINE_WARMUP / 4;

        private final EntitySpacecraft entity;
        private final IFluidHandler fuelHandler;

        private int stateTicks;

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
                    double totalForce = entity.metadata.getTotalForce();
                    force = (totalForce - MIN_ACC) * Math.pow((double) MathHelper.clamp(stateTicks, 0, ENGINE_WARMUP) / ENGINE_WARMUP, 0.5) + MIN_ACC;
                    acceleration = force / entity.metadata.getMass() / 20.0;
                }

                entity.dataManager.set(ACCELERATION, (float) acceleration);
                entity.dataManager.set(FORCE, (float) force);
            }

            this.lastForce = force;

            if (acceleration > 1e-4 || stateTicks < ENGINE_WARMUP) {
                this.entity.motionY += acceleration;
                this.entity.rotationYaw += Math.max(this.entity.motionY, 0.0F) * 0.5F;

                if (world.isRemote) {
                    Random rand = this.entity.rand;
                    for (SpacecraftMetadata.Thruster thruster : entity.metadata.getThrusters()) {
                        BlockPos thrusterPos = thruster.getPos();
                        Point3d thrusterPoint = new Point3d(thrusterPos.getX(), thrusterPos.getY(), thrusterPos.getZ());
                        this.entity.rotationMatrix.transform(thrusterPoint);
                        double posX = this.entity.posX + thrusterPoint.x;
                        double posY = this.entity.posY + thrusterPoint.y;
                        double posZ = this.entity.posZ + thrusterPoint.z;
                        for (int i = 0; i < 30; i++) {
                            double motionX = (rand.nextDouble() * 2.0 - 1) * 0.05;
                            double motionY = -Math.min(force / 1e+3, 1.0) - rand.nextDouble() * 0.1;
                            double motionZ = (rand.nextDouble() * 2.0 - 1) * 0.05;
                            PSFParticles.ROCKET_PLUME.spawn(world, posX + motionX, posY + rand.nextDouble() * motionY, posZ + motionZ, motionX, motionY, motionZ);
                        }
                    }
                }

                this.stateTicks++;

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
