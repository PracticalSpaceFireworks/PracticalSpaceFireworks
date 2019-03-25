package net.gegy1000.psf.server.block.module;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.server.sound.PSFSounds;
import net.gegy1000.psf.server.util.AxisDirectionalBB;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

// todo ambient sound

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockThruster extends BlockModule {
    private static final AxisAlignedBB BOUNDING_BOX_CENTER = new AxisAlignedBB(0.0625, 0.125, 0.0625, 0.9375, 1.0, 0.9375);
    private static final AxisDirectionalBB BOUNDING_BOX_WALL = AxisDirectionalBB.of(0.0625, 0.125, 0.3125, 0.9375, 1.0, 1.0);

    public BlockThruster(String module) {
        super(Material.IRON, MapColor.SILVER, module);
        setSoundType(SoundType.METAL);
        setLightOpacity(4);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return BlockRenderLayer.SOLID == layer || BlockRenderLayer.CUTOUT == layer;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(DIRECTION);
        return facing.getAxis().isVertical() ? BOUNDING_BOX_CENTER : BOUNDING_BOX_WALL.withFacing(facing);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (world.getTotalWorldTime() % 200 == 0) {
            double x = (float) pos.getX() + 0.5F;
            double y = (float) pos.getY();
            double z = (float) pos.getZ() + 0.5F;
            float v = 0.5F;
            float p = 1.0F;
            world.playSound(x, y, z, PSFSounds.THRUSTER_AMBIENT, SoundCategory.BLOCKS, v, p, false);
        }
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
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!entity.isImmuneToFire() && entity instanceof EntityLivingBase) {
            if (!EnchantmentHelper.hasFrostWalkerEnchantment((EntityLivingBase) entity)) {
                entity.attackEntityFrom(DamageSource.HOT_FLOOR, 1.0F);
            }
        }
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 1.0F;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
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
