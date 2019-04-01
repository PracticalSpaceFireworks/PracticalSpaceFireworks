package net.gegy1000.psf.server.block.module;

import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.PSFSoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class BlockStrutAbstract extends BlockModule {
    protected static final AxisAlignedBB COLLISION_AABB =
        new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 1.0, 0.9375);

    public BlockStrutAbstract(String module) {
        super(Material.IRON, module);
        setSoundType(PSFSoundType.SCAFFOLD);
        setHardness(2.0F);
        setCreativeTab(PracticalSpaceFireworks.TAB);
        setLightOpacity(1);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DIRECTION);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(DIRECTION, EnumFacing.UP);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(DIRECTION, EnumFacing.UP);
    }

    @Override
    public boolean isStructuralModule(@Nullable IBlockState connecting, IBlockState state) {
        return true;
    }

    @Override
    @Deprecated
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    @Nullable
    @Deprecated
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
        return ForgeModContainer.fullBoundingBoxLadders ? FULL_BLOCK_AABB : COLLISION_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (world.isRainingAt(pos.up())) {
            val below = world.getBlockState(pos.down());
            val shape = below.getBlockFaceShape(world, pos.down(), EnumFacing.UP);
            if (shape != BlockFaceShape.SOLID && rand.nextInt(7) == 1) {
                val x = (double) ((float) pos.getX() + rand.nextFloat());
                val y = (double) pos.getY() - 0.05;
                val z = (double) ((float) pos.getZ() + rand.nextFloat());
                world.spawnParticle(EnumParticleTypes.DRIP_WATER, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity instanceof EntityLivingBase && (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isFlying)) {
            val living = (EntityLivingBase) entity;
            if (!living.isOnLadder() || !isLadder(state, world, pos, living)) {
                living.motionX = MathHelper.clamp(living.motionX, -0.15D, 0.15D);
                living.motionY = Math.max(living.motionY, -0.15D);
                living.motionZ = MathHelper.clamp(living.motionZ, -0.15D, 0.15D);
                living.fallDistance = 0.0F;
                if (living.collidedHorizontally) {
                    living.motionY = 0.2D;
                }
                if (living.isSneaking()) {
                    living.motionY = Math.max(living.motionY, 0.08D);
                }
            }
        }
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 0.5F;
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess access, BlockPos pos, @Nullable EntityLivingBase entity) {
        return true;
    }
}
