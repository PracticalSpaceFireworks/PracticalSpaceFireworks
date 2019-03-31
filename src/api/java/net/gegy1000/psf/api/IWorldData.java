package net.gegy1000.psf.api;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public interface IWorldData extends IBlockAccess, INBTSerializable<NBTTagCompound>, IByteBufSerializeable {
    void setBlockState(BlockPos pos, IBlockState state);

    void setTileEntity(BlockPos pos, @Nullable TileEntity entity);

    boolean containsBlock(BlockPos pos);

    World buildWorld(World parent);
    
    
}