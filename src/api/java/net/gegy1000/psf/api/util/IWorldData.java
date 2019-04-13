package net.gegy1000.psf.api.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IWorldData extends IBlockAccess, INBTSerializable<NBTTagCompound>, IByteBufSerializeable {
    void setBlockState(BlockPos pos, IBlockState state);

    void setTileEntity(BlockPos pos, @Nullable TileEntity entity);

    boolean containsBlock(BlockPos pos);

    World asWorld();

    @Override
    @Deprecated
    @SideOnly(Side.CLIENT)
    default @Nonnull Biome getBiome(@Nonnull BlockPos pos) {
        return Biomes.DEFAULT;
    }
}
