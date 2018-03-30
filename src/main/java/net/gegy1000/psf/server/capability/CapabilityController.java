package net.gegy1000.psf.server.capability;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.IController;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CapabilityController {

    @SuppressWarnings("null")
    @CapabilityInject(IController.class)
    @Nonnull
    public static final Capability<IController> INSTANCE = null;
    
    public static void register() {
        // TODO default IStorage ?
        CapabilityManager.INSTANCE.register(IController.class, new IStorage<IController>() {
          @Override
          public NBTBase writeNBT(Capability<IController> capability, IController instance, EnumFacing side) {
            return null;
          }

          @Override
          public void readNBT(Capability<IController> capability, IController instance, EnumFacing side, NBTBase nbt) {
          }
        }, () -> null); // FIXME
    }
    
}
