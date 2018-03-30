package net.gegy1000.psf.server.capability;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.IModule;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CapabilityModule {

    @SuppressWarnings("null")
    @CapabilityInject(IModule.class)
    @Nonnull
    public static final Capability<IModule> INSTANCE = null;
    
    public static void register() {
        // TODO default IStorage ?
        CapabilityManager.INSTANCE.register(IModule.class, new IStorage<IModule>() {
          @Override
          public NBTBase writeNBT(Capability<IModule> capability, IModule instance, EnumFacing side) {
            return null;
          }

          @Override
          public void readNBT(Capability<IModule> capability, IModule instance, EnumFacing side, NBTBase nbt) {
          }
        }, () -> null); // FIXME
    }
    
}
