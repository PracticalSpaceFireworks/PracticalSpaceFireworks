package net.gegy1000.psf.server.modules;

import lombok.Setter;
import net.gegy1000.psf.api.spacecraft.IController;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.gegy1000.psf.server.block.remote.packet.PacketCraftState;
import net.gegy1000.psf.server.block.remote.packet.PacketOpenRemoteControl.SatelliteState;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.capability.world.SatelliteWorldData;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.modules.configs.ConfigBasicAction;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ModuleController extends EmptyModule implements IController {
    
    private final ConfigBasicAction deorbit = new ConfigBasicAction("deorbit", TextFormatting.RED + "DEORBIT") {
      
        @Override
        public void modified() {
            deorbiting = true;
        }
    };
    
    @Setter
    @Nonnull
    private BlockPos pos = BlockPos.ORIGIN;
    
    boolean deorbiting;
    
    public ModuleController() {
        super("controller_simple");
        registerConfigs(deorbit);
    }

    public ModuleController(BlockPos pos) {
        this();
        this.pos = pos;
    }

    @Override
    public Optional<BlockPos> getPosition() {
        return Optional.of(pos);
    }
    
    @Override
    public void onSatelliteTick(@Nonnull ISatellite satellite) {
        super.onSatelliteTick(satellite);
        
        if (deorbiting && satellite.isOrbiting()) {
            deorbiting = false;
            
            EntitySpacecraft entity = new EntitySpacecraft(satellite);
            BlockPos pos = getPosition().get();
            entity.setPosition(pos.getX() + 0.5, 1000, pos.getZ() + 0.5);
            satellite.getWorld().spawnEntity(entity);
            
            SatelliteWorldData data = satellite.getWorld().getCapability(CapabilityWorldData.SATELLITE_INSTANCE, null);
            if (data != null) {
                data.removeSatellite(satellite.getId());
            }
            
            for (EntityPlayerMP player : satellite.getTrackingPlayers()) {
                PSFNetworkHandler.network.sendTo(new PacketCraftState(SatelliteState.ENTITY, satellite.toListedCraft()), player);
            }
        }
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound ret = super.serializeNBT();
        ret.setInteger("controller_x", pos.getX());
        ret.setInteger("controller_y", pos.getY());
        ret.setInteger("controller_z", pos.getZ());
        return ret;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        setPos(new BlockPos(nbt.getInteger("controller_x"), nbt.getInteger("controller_y"), nbt.getInteger("controller_z")));
    }
}
