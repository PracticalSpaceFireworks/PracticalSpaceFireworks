package net.gegy1000.psf.api.data;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.ParametersAreNonnullByDefault;

import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.registries.IForgeRegistryEntry;

@RequiredArgsConstructor
@ParametersAreNonnullByDefault
public class SimpleModuleDataDisplayFactory extends IForgeRegistryEntry.Impl<IModuleDataDisplayFactory> implements IModuleDataDisplayFactory {
    
    private final Supplier<IModuleDataDisplay> defaultFactory;
    private final UnaryOperator<NBTTagCompound> dataConverter;

    @Override
    public IModuleDataDisplay create() {
        return defaultFactory.get();
    }
    
    @Override
    public NBTTagCompound getUpdateData(NBTTagCompound requestData) {
    	return dataConverter.apply(requestData);
    }
}
