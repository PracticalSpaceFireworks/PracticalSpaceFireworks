package net.gegy1000.psf.server.block.module;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.sound.PSFSounds;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

public class BlockStrut extends BlockModule {

    private static final ImmutableMap<EnumFacing, PropertyBool> SIDE_PROPS = Arrays.stream(EnumFacing.VALUES)
            .collect(Maps.toImmutableEnumMap(Function.identity(), f -> PropertyBool.create(f.getName())));

    private static final AxisAlignedBB STRUT_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 1.0D, 0.9375D);

    public BlockStrut() {
        super(Material.IRON, "strut_cube");
        setSoundType(PSFSounds.METAL);
        setHardness(2.0f);
        setCreativeTab(PracticalSpaceFireworks.TAB);
        setLightOpacity(1);
    }

    @Override
    public @Nullable AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return ForgeModContainer.fullBoundingBoxLadders ? FULL_BLOCK_AABB : STRUT_AABB;
    }

    @Override
    protected @Nonnull
    BlockStateContainer createBlockState() {
        BlockStateContainer.Builder builder = new BlockStateContainer.Builder(this);
        for (PropertyBool prop : SIDE_PROPS.values()) {
            builder.add(prop);
        }
        return builder.add(DIRECTION).build();
    }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) {
        return true;
    }

    @Override
    public @Nonnull
    BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public int getLightOpacity(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return super.getLightOpacity(state, world, pos);
    }

    @Override
    protected boolean isDirectional(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    public @Nonnull IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, @Nonnull EnumHand hand) {
        return getDefaultState().withProperty(DIRECTION, EnumFacing.UP);
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(@Nonnull IBlockState state) {
        return 0.5F;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Random rand) {
        if (world.isRainingAt(pos.up())) {
            IBlockState below = world.getBlockState(pos.down());
            BlockFaceShape shape = below.getBlockFaceShape(world, pos.down(), EnumFacing.UP);
            if (shape != BlockFaceShape.SOLID && rand.nextInt(7) == 1) {
                double x = (double) ((float) pos.getX() + rand.nextFloat());
                double y = (double) pos.getY() - 0.05D;
                double z = (double) ((float) pos.getZ() + rand.nextFloat());
                world.spawnParticle(EnumParticleTypes.DRIP_WATER, x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return 0;
    }

    @Override
    public @Nonnull IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(DIRECTION, EnumFacing.UP);
    }

    @Override
    public boolean isStructuralModule(@Nullable IBlockState connecting, @Nonnull IBlockState state) {
        return true;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        for (EnumFacing side : EnumFacing.VALUES) {
            PropertyBool property = SIDE_PROPS.get(side);
            BlockPos offset = pos.offset(side);
            IBlockState target = world.getBlockState(offset);
            state = state.withProperty(property, target.getBlock() != this);
        }
        return state;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return true;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity instanceof EntityLivingBase && (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isFlying)) {
            EntityLivingBase living = (EntityLivingBase) entity;

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
}
