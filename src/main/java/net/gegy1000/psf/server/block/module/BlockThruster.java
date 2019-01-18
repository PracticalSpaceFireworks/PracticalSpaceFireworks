package net.gegy1000.psf.server.block.module;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

// todo ambient sound

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockThruster extends BlockModule {
    public BlockThruster(String module) {
        super(Material.IRON, MapColor.SILVER, module);
        setLightLevel(0.4F);
        setLightOpacity(4);
    }

    @Override
    protected boolean canAttachOnSide(World world, BlockPos pos, IBlockState state, IBlockState on, EnumFacing side) {
        if (EnumFacing.UP != side) {
            BlockPos offset = pos.offset(side.getOpposite());
            IBlockState other = world.getBlockState(offset);
            BlockFaceShape shape = other.getBlockFaceShape(world, offset, side);
            return BlockFaceShape.SOLID == shape;
        }
        return false;
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 1.0F;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return true;
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!entity.isImmuneToFire() && entity instanceof EntityLivingBase) {
            if (!EnchantmentHelper.hasFrostWalkerEnchantment((EntityLivingBase) entity)) {
                entity.attackEntityFrom(DamageSource.HOT_FLOOR, 1.0F);
            }
        }
        super.onEntityWalk(world, pos, entity);
    }

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
        return BlockFaceShape.UNDEFINED;
    }
}
