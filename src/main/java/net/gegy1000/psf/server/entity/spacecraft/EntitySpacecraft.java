package net.gegy1000.psf.server.entity.spacecraft;

import io.netty.buffer.ByteBuf;
import net.gegy1000.psf.client.render.spacecraft.model.SpacecraftModel;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntitySpacecraft extends Entity implements IEntityAdditionalSpawnData {
    @SideOnly(Side.CLIENT)
    public SpacecraftModel model;

    private SpacecraftBlockAccess blockAccess;

    public EntitySpacecraft(World world) {
        super(world);
        this.blockAccess = new SpacecraftBlockAccess.Builder()
                .setBlockState(BlockPos.ORIGIN, Blocks.GRASS.getDefaultState())
                .setBlockState(BlockPos.ORIGIN.up(), Blocks.BIRCH_FENCE.getDefaultState())
                .build();
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.blockAccess = SpacecraftBlockAccess.deserialize(compound);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound = this.blockAccess.serialize(compound);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        this.blockAccess.serialize(buffer);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.blockAccess = SpacecraftBlockAccess.deserialize(buffer);
    }

    public SpacecraftBlockAccess getBlockAccess() {
        return this.blockAccess;
    }
}
