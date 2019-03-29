package net.gegy1000.psf.server.entity.spacecraft;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Point3d;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.client.particle.PSFParticles;
import net.gegy1000.psf.client.render.spacecraft.model.SpacecraftModel;
import net.gegy1000.psf.server.block.controller.CraftGraph;
import net.gegy1000.psf.server.block.controller.TileController;
import net.gegy1000.psf.server.block.module.BlockModule;
import net.gegy1000.psf.server.block.remote.packet.PacketCraftState;
import net.gegy1000.psf.server.block.remote.packet.PacketOpenRemoteControl;
import net.gegy1000.psf.server.block.remote.packet.PacketVisualData;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.capability.world.SatelliteWorldData;
import net.gegy1000.psf.server.init.PSFFluids;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.gegy1000.psf.server.satellite.EntityBoundSatellite;
import net.gegy1000.psf.server.sound.PSFSounds;
import net.gegy1000.psf.server.util.LogisticGrowthCurve;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntitySpacecraft extends Entity implements IEntityAdditionalSpawnData {
    public static final double BASE_AIR_RESISTANCE = 0.98;
    public static final double GRAVITY = 1.6;

    private static final DataParameter<Byte> STATE = EntityDataManager.createKey(EntitySpacecraft.class, DataSerializers.BYTE);
    private static final DataParameter<Float> ACCELERATION = EntityDataManager.createKey(EntitySpacecraft.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> FORCE = EntityDataManager.createKey(EntitySpacecraft.class, DataSerializers.FLOAT);

    static {
        PSFNetworkHandler.network.registerMessage(PacketLaunchCraft.Handler.class, PacketLaunchCraft.class, PSFNetworkHandler.nextID(), Side.SERVER);
        PSFNetworkHandler.network.registerMessage(PacketLaunchTile.Handler.class, PacketLaunchTile.class, PSFNetworkHandler.nextID(), Side.SERVER);
    }

    private SpacecraftBody body;

    @Getter
    private final EntityBoundSatellite satellite;

    @SideOnly(Side.CLIENT)
    public SpacecraftModel model;

    @SideOnly(Side.CLIENT)
    public RayTraceResult pointedBlock;

    @Getter
    private State state = new StaticState(this);

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
        this.body = new SpacecraftBody(world, builder.buildBodyData(origin, world));

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

        this.body = new SpacecraftBody(craft);

        this.satellite = new EntityBoundSatellite(this, craft.getId(), craft.getName());
        initSpacecraft();
    }

    private void initSpacecraft() {
        this.satellite.detectModules();
        this.body.updateRotation(rotationYaw, rotationPitch);
        this.body.apply(this);

        this.metadata = body.buildSpacecraftMetadata();

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
    
    private static final LogisticGrowthCurve AIR_RESISTANCE = new LogisticGrowthCurve(1 - BASE_AIR_RESISTANCE, 10, -0.015);

    @Override
    public void onUpdate() {
        double airResistance = BASE_AIR_RESISTANCE + AIR_RESISTANCE.get(posY);
        this.motionX *= airResistance;
        this.motionY *= airResistance;
        this.motionZ *= airResistance;

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

        if (body.updateRotation(rotationYaw, rotationPitch)) {
            body.apply(this);
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

            if (!world.isRemote) {
                SatelliteWorldData capability = world.getCapability(CapabilityWorldData.SATELLITE_INSTANCE, null);
                if (capability != null) {
                    ISatellite orbiting = satellite.toOrbiting();
                    capability.addSatellite(orbiting);
                    converted = true;
                    for (EntityPlayerMP player : orbiting.getTrackingPlayers()) {
                        PSFNetworkHandler.network.sendTo(new PacketCraftState(PacketOpenRemoteControl.SatelliteState.ORBIT, orbiting.toListedCraft()), player);
                        PSFNetworkHandler.network.sendTo(new PacketVisualData(orbiting.buildBodyData(world)), player);
                    }
                }
            }
        }

        if (!world.isRemote) {
            satellite.tickSatellite(ticksExisted);
        } else {
            updateRayTrace();
        }

        super.onUpdate();
    }

    private void updateRayTrace() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        pointedBlock = playerRayTrace(player).orElse(null);
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
        this.posX = x;
        this.posY = y;
        this.posZ = z;

        if (body != null) {
            body.apply(this);
        }
    }

    @Override
    public void resetPositionToBB() {
        AxisAlignedBB bounds = body.computeAppliedBounds(this);
        AxisAlignedBB updatedBounds = this.getEntityBoundingBox();
        this.posX += updatedBounds.minX - bounds.minX;
        this.posY += updatedBounds.minY - bounds.minY;
        this.posZ += updatedBounds.minZ - bounds.minZ;
    }

    @Override
    public boolean canBeCollidedWith() {
        // If there is no pointed block, return false as we don't want the pointed entity raytracer to detect this
        if (world.isRemote) {
            return pointedBlock != null;
        }
        return true;
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return super.isInRangeToRenderDist(distance / 8);
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        Optional<RayTraceResult> rayTrace = playerRayTrace(player);
        if (!rayTrace.isPresent() || rayTrace.get().typeOfHit != RayTraceResult.Type.BLOCK) {
            return false;
        }

        if (world.isRemote) {
            player.swingArm(hand);
        } else if (hand == EnumHand.MAIN_HAND) {
            float prevRotationYaw = this.rotationYaw;
            float prevRotationPitch = this.rotationPitch;

            int yaw = Math.round((this.rotationYaw % 360) / 90.0F) * 90;
            while (yaw < 0) {
                yaw += 360;
            }

            this.rotationYaw = yaw;
            this.rotationPitch = Math.round(this.rotationPitch / 90.0F) * 90.0F;
            body.updateRotation(rotationYaw, rotationPitch);

            Rotation rotation = Rotation.values()[(yaw / 90 + 2) % 4];

            Optional<SpacecraftDeconstructor.Result> result = body.deconstruct(world, posX, posY, posZ);
            if (result.isPresent()) {
                Map<BlockPos, IBlockState> blocks = result.get().getBlocks();
                Map<BlockPos, TileEntity> entities = result.get().getEntities();

                BlockModule.CONVERTING.set(true);
                try {
                    blocks.forEach((pos, block) -> world.setBlockState(pos, block.withRotation(rotation), 10 | 1));

                    for (Map.Entry<BlockPos, TileEntity> entry : entities.entrySet()) {
                        world.setTileEntity(entry.getKey(), entry.getValue());
                    }
                } finally {
                    BlockModule.CONVERTING.set(false);
                }

                BlockPos min = body.getMinPos().add(getPosition());
                BlockPos max = body.getMaxPos().add(getPosition());
                world.markBlockRangeForRenderUpdate(min, max);

                this.converted = true;
                for (EntityPlayerMP p : satellite.getTrackingPlayers()) {
                    PSFNetworkHandler.network.sendTo(new PacketCraftState(PacketOpenRemoteControl.SatelliteState.TILE, satellite.toListedCraft()), p);
                }

                // TODO improve IController API to avoid this
                satellite.getController().getPosition()
                        .map(world::getTileEntity)
                        .filter(TileController.class::isInstance)
                        .map(TileController.class::cast)
                        .ifPresent(TileController::scanStructure);

                setDead();
            } else {
                this.rotationYaw = prevRotationYaw;
                this.rotationPitch = prevRotationPitch;
                this.body.updateRotation(rotationYaw, rotationPitch);
                this.body.apply(this);

                return false;
            }
        }

        return true;
    }

    public Optional<RayTraceResult> playerRayTrace(EntityPlayer player) {
        double reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();

        Vec3d origin = player.getPositionEyes(1.0F).subtract(posX, posY, posZ);
        Vec3d target = origin.add(player.getLookVec().scale(reach));

        origin = body.toLocalPoint(origin);
        target = body.toLocalPoint(target);

        return Optional.ofNullable(body.getWorld().rayTraceBlocks(origin.add(0.5, 0, 0.5), target.add(0.5, 0.0, 0.5)));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("body", body.serialize(new NBTTagCompound()));
        compound.setTag("satellite", this.satellite.serializeNBT());

        compound.setString("state", this.state.getType().name());
        NBTTagCompound stateData = this.state.serializeNBT();
        if (stateData != null) {
            compound.setTag("state_data", stateData);
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.body = SpacecraftBody.deserialize(world, compound.getCompoundTag("body"));
        this.satellite.deserializeNBT(compound.getCompoundTag("satellite"));

        initSpacecraft();

        String state = compound.getString("state");
        this.state = StateType.valueOf(state).create(this);
        if (compound.hasKey("state_data")) {
            this.state.deserializeNBT(compound.getCompoundTag("state_data"));
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        this.body.serialize(buffer);
        ByteBufUtils.writeTag(buffer, this.satellite.serializeNBT());
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.body = SpacecraftBody.deserialize(world, buffer);

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

    public SpacecraftBody getBody() {
        return body;
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
                return new StaticState(entity);
            }
        },
        LAUNCH {
            @Override
            protected State create(EntitySpacecraft entity) {
                return new LaunchState(entity);
            }
        };

        protected abstract State create(EntitySpacecraft entity);
    }

    public interface State extends INBTSerializable<NBTTagCompound> {
        default State update() {
            return this;
        }

        default double getCameraShake() {
            return 0.0;
        }

        StateType getType();
        
        @Override
        default NBTTagCompound serializeNBT() {
            return null;
        }
        
        @Override
        default void deserializeNBT(NBTTagCompound nbt) {
        }
    }

    private static class StaticState implements State {
        private final EntitySpacecraft entity;

        StaticState(EntitySpacecraft entity) {
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

    private static class LaunchState implements State {
        private static final int IGNITION_TIME = 45;
        private static final int ENGINE_WARMUP = 80;

        private final EntitySpacecraft entity;
        private final IFluidHandler fuelHandler;
        
        @SideOnly(Side.CLIENT)
        private final MovingSound sound;

        private int stateTicks;

        private double lastForce;

        LaunchState(EntitySpacecraft entity) {
            this.entity = entity;
            this.fuelHandler = entity.metadata.buildFuelHandler();
            
            if (entity.world.isRemote) {
                sound = initSound();
            } else {
                sound = null;
            }
        }
        
        private static final float MAX_SOUND_DIST = 128;
        private static final float MAX_SOUND_DIST_SQ = MAX_SOUND_DIST * MAX_SOUND_DIST;
        
        @SideOnly(Side.CLIENT)
        private MovingSound initSound() {
            MovingSound ret = new MovingSound(PSFSounds.SPACECRAFT_LAUNCH, SoundCategory.BLOCKS) {
                {
                    this.attenuationType = AttenuationType.NONE;
                }
                
                float startVol = -1;
                int fadeOut = 20;
                
                @Override
                public void update() {
                    if (entity.isDead || entity.getState() != LaunchState.this) {
                        if (--fadeOut == 0) {
                            this.donePlaying = true;
                        } else {
                            if (startVol == -1) {
                                startVol = this.volume;
                            }
                            this.volume = startVol * (fadeOut / 20F);
                        }
                    } else {
                        this.xPosF = (float) entity.posX;
                        this.yPosF = (float) entity.posY;
                        this.zPosF = (float) entity.posZ;
                        
                        Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
                        if (viewEntity == null) {
                            return;
                        }
                        float distSq = (float) viewEntity.getDistanceSq(entity.posX, viewEntity.posY, entity.posZ);
                        if (distSq > MAX_SOUND_DIST_SQ) {
                            float dist = (float) Math.sqrt(distSq);
                            this.volume = MathHelper.clamp((1 - ((dist - MAX_SOUND_DIST) / 64)), 0, 1);
                        }
                        System.out.println(Math.sqrt(distSq) + " (" + this.volume + ")");
                    }
                }
            };
            Minecraft.getMinecraft().getSoundHandler().playSound(ret);
            return ret;
        }
        
        private static final LogisticGrowthCurve FORCE_CURVE = new LogisticGrowthCurve(1, 8, -1.7);

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
                FluidStack keroseneDrain = new FluidStack(PSFFluids.kerosene(), totalDrain);
                FluidStack liquidOxygenDrain = new FluidStack(PSFFluids.liquidOxygen(), totalDrain);
                FluidStack keroseneResult = this.fuelHandler.drain(keroseneDrain, false);
                FluidStack liquidOxygenResult = this.fuelHandler.drain(liquidOxygenDrain, false);
                if (keroseneResult != null && keroseneResult.amount > 0 && liquidOxygenResult != null && liquidOxygenResult.amount > 0) {
                    double totalForce = entity.metadata.getTotalForce();
                    double forcePercentage = FORCE_CURVE.get(stateTicks / 20D);
                    force = totalForce * forcePercentage;
                    keroseneDrain.amount *= forcePercentage;
                    liquidOxygenDrain.amount *= forcePercentage;
                    this.fuelHandler.drain(keroseneDrain, true);
                    this.fuelHandler.drain(liquidOxygenDrain, true);
                    acceleration = force / entity.metadata.getMass() / 20.0;

                    entity.dataManager.set(ACCELERATION, (float) acceleration);
                    entity.dataManager.set(FORCE, (float) force);
                } else {
                    entity.dataManager.set(ACCELERATION, 0F);
                    entity.dataManager.set(FORCE, 0F);
                    return StateType.STATIC.create(entity);
                }
            }

            this.lastForce = force;
            this.entity.motionY += acceleration;
            this.entity.rotationYaw += Math.max(this.entity.motionY, 0.0F) * 0.5F;

            if (world.isRemote) {
                if (stateTicks < IGNITION_TIME) {
                    Random rand = this.entity.rand;
                    for (SpacecraftMetadata.Thruster thruster : entity.metadata.getThrusters()) {
                        BlockPos thrusterPos = thruster.getPos();
                        Point3d thrusterPoint = new Point3d(thrusterPos.getX(), thrusterPos.getY(), thrusterPos.getZ());
                        this.entity.body.getRotationMatrix().transform(thrusterPoint);
                        double posX = this.entity.posX + thrusterPoint.x + (rand.nextDouble() - 0.5);
                        double posY = this.entity.posY + thrusterPoint.y + 0.5 + (rand.nextDouble() - 0.5);
                        double posZ = this.entity.posZ + thrusterPoint.z + (rand.nextDouble() - 0.5);
                        if (rand.nextBoolean()) {
                            double motionX = (rand.nextDouble() * 2.0 - 1) * 0.05;
                            double motionY = rand.nextDouble() * 0.05;
                            double motionZ = (rand.nextDouble() * 2.0 - 1) * 0.05;
                            this.entity.world.spawnParticle(EnumParticleTypes.CRIT, posX, posY, posZ, motionX, motionY, motionZ);
                        }
                    }
                } else if (stateTicks == IGNITION_TIME) {
                    Random rand = this.entity.rand;
                    for (SpacecraftMetadata.Thruster thruster : entity.metadata.getThrusters()) {
                        BlockPos thrusterPos = thruster.getPos();
                        Point3d thrusterPoint = new Point3d(thrusterPos.getX(), thrusterPos.getY(), thrusterPos.getZ());
                        this.entity.body.getRotationMatrix().transform(thrusterPoint);
                        double posX = this.entity.posX + thrusterPoint.x;
                        double posY = this.entity.posY + thrusterPoint.y;
                        double posZ = this.entity.posZ + thrusterPoint.z;
                        for (int i = 0; i < 60; i++) {
                            double motionX = (rand.nextDouble() * 2.0 - 1) * 0.1;
                            double motionY = -rand.nextDouble() * 0.2;
                            double motionZ = (rand.nextDouble() * 2.0 - 1) * 0.1;
                            PSFParticles.ROCKET_PLUME.spawn(world, posX + motionX, posY + rand.nextDouble() * motionY, posZ + motionZ, motionX, motionY, motionZ);
                        }
                    }
                } else if (acceleration > 1e-4 || stateTicks < IGNITION_TIME + ENGINE_WARMUP) {
                    Random rand = this.entity.rand;
                    for (SpacecraftMetadata.Thruster thruster : entity.metadata.getThrusters()) {
                        BlockPos thrusterPos = thruster.getPos();
                        Point3d thrusterPoint = new Point3d(thrusterPos.getX(), thrusterPos.getY(), thrusterPos.getZ());
                        this.entity.body.getRotationMatrix().transform(thrusterPoint);
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
            }
            this.stateTicks++;
            return this;
        }

        @Override
        public double getCameraShake() {
            return this.lastForce * 5e-7;
        }

        @Override
        public StateType getType() {
            return StateType.LAUNCH;
        }
        
        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound ret = new NBTTagCompound();
            ret.setInteger("prog", stateTicks);
            return ret;
        }
        
        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            this.stateTicks = nbt.getInteger("prog");
        }
    }
}
