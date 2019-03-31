package net.gegy1000.psf.server.block.module;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.module.IModuleFactory;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.modules.Modules;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@NoArgsConstructor
@AllArgsConstructor
public class TileModule extends TileEntity {

    @Getter
    @Nullable
    private IModule module;

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        IModule module = getModule();
        return super.hasCapability(capability, facing) || (module != null && module.hasCapability(capability, facing));
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        IModule module = getModule();
        if (module != null && module.hasCapability(capability, facing)) {
            return module.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Override
    public @Nonnull
    NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        IModule module = this.module;
        if (module != null) {
            ResourceLocation moduleID = module.getRegistryName();
            Preconditions.checkNotNull(moduleID, "Module does not have registry name set!");
            compound.setString("moduleID", moduleID.toString());
            compound.setTag("moduleData", module.serializeNBT());
        }
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("moduleID")) {
            String id = Strings.emptyToNull(compound.getString("moduleID"));
            Preconditions.checkNotNull(id, "No module data found!");
            IModuleFactory factory = Modules.get().getValue(new ResourceLocation(id));
            if (factory != null) {
                this.module = factory.get();
                this.module.deserializeNBT(compound.getCompoundTag("moduleData"));
            } else {
                PracticalSpaceFireworks.LOGGER.warn("Unknown module type '" + id + "'");
            }
        }
    }

    @Nullable
    public static IModule getModule(TileEntity entity) {
        if (entity != null && entity.hasCapability(CapabilityModule.INSTANCE, null)) {
            return entity.getCapability(CapabilityModule.INSTANCE, null);
        }
        return null;
    }
}
