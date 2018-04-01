package net.gegy1000.psf.server.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.modules.data.EntityListData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
public class ModuleEntityDetector extends EmptyModule {

    @RequiredArgsConstructor
    public enum EntityDetectorTier implements IStringSerializable {
        SIMPLE("simple", 1),
        ADVANCED("advanced", 5),
        CUSTOM("custom", 0),;

        @Getter
        private final String name;
        @Getter
        private final int chunkRange;
    }

    private final int chunkRange;

    private final EntityListData listData = new EntityListData();

    public ModuleEntityDetector(EntityDetectorTier tier) {
        this(tier, tier.getChunkRange());
    }

    public ModuleEntityDetector(int chunkRange) {
        this(EntityDetectorTier.CUSTOM, chunkRange);
    }

    public ModuleEntityDetector(EntityDetectorTier tier, int chunkRange) {
        super("entity_detector." + tier.getName());
        this.chunkRange = chunkRange;
    }

    @Override
    public void onSatelliteTick(ISatellite satellite) {
        World world = satellite.getWorld();
        if (!world.isRemote) {
            Collection<EntityLivingBase> entities = world.getEntities(EntityLivingBase.class, e -> {
                if (e == null || e instanceof EntityPlayer) {
                    return false;
                }
                return world.canSeeSky(e.getPosition()) && this.withinSatelliteRange(satellite, e.getPosition());
            });

            this.listData.updateEntities(entities);
        }
    }

    @Override
    public int getTickInterval() {
        return 40;
    }

    private boolean withinSatelliteRange(ISatellite satellite, BlockPos pos) {
        BlockPos sat = new BlockPos(satellite.getPosition().getX() >> 4, 0, satellite.getPosition().getZ() >> 4);
        BlockPos entityChunk = new BlockPos(pos.getX() >> 4, 0, pos.getZ() >> 4);
        BlockPos diff = sat.subtract(entityChunk);

        return Math.abs(diff.getX()) <= chunkRange && Math.abs(diff.getZ()) <= chunkRange;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityModuleData.ENTITY_LIST || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityModuleData.ENTITY_LIST) {
            return CapabilityModuleData.ENTITY_LIST.cast(this.listData);
        }
        return super.getCapability(capability, facing);
    }
}
