package net.gegy1000.psf.server.block.module;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.val;
import lombok.var;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.server.block.Machine;
import net.gegy1000.psf.server.init.PSFBlocks;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.gegy1000.psf.server.network.PacketDisplayContainerMessage;
import net.gegy1000.psf.server.util.AxisDirectionalBB;
import net.gegy1000.psf.server.util.PSFGuiHandler;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockFuelValve extends BlockModule implements Machine {
    private static final ImmutableMap<EnumFacing, PropertyBool> SIDES = Arrays.stream(EnumFacing.values())
            .collect(Maps.toImmutableEnumMap(Function.identity(), f -> PropertyBool.create(f.getName())));

    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    private static final AxisDirectionalBB AABB_SIDE = new AxisDirectionalBB(0.25, 0.25, 0.0, 0.75, 0.75, 0.25);

    public BlockFuelValve() {
        super(Material.IRON, "fuel_valve");
        setSoundType(SoundType.METAL);
        setHardness(3.0F);
        setLightOpacity(2);
        setCreativeTab(PracticalSpaceFireworks.TAB);
        var state = getDefaultState();
        for (val property : SIDES.values()) {
            state = state.withProperty(property, false);
        }
        setDefaultState(state.withProperty(ACTIVE, false));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        IModule module = TileModule.getModule(world.getTileEntity(pos));
        if (module == null) return false;

        if (world.isRemote) return true;

        if (module.getOwner() != null) {
            player.openGui(PracticalSpaceFireworks.getInstance(), PSFGuiHandler.ID_FUEL_VALVE, world, pos.getX(), pos.getY(), pos.getZ());
        } else if (player instanceof EntityPlayerMP) {
            ITextComponent title = new TextComponentTranslation(PSFBlocks.FUEL_VALVE.getTranslationKey() + ".name");
            Style importantStyle = new Style().setBold(true).setColor(TextFormatting.RED);
            ITextComponent[] lines = {
                    new TextComponentTranslation("gui.psf.fuel_valve.controller_not_present.0").setStyle(importantStyle),
                    new TextComponentTranslation("gui.psf.fuel_valve.controller_not_present.1")
            };
            PSFNetworkHandler.network.sendTo(new PacketDisplayContainerMessage(title, lines), (EntityPlayerMP) player);
        }

        return true;
    }

    @Override
    @Deprecated
    public IBlockState getActualState(IBlockState state, IBlockAccess access, BlockPos pos) {
        boolean active = false;
        for (val entry : SIDES.entrySet()) {
            val canConnect = canConnect(access, pos, entry.getKey());
            state = state.withProperty(entry.getValue(), canConnect);
            active |= canConnect;
        }
        return state.withProperty(ACTIVE, active);
    }

    private boolean canConnect(IBlockAccess access, BlockPos pos, EnumFacing side) {
        @Nullable val te = access.getTileEntity(pos.offset(side));
        if (te != null && te.getBlockType() != this) {
            return te.hasCapability(FLUID_HANDLER_CAPABILITY, side.getOpposite());
        }
        return false;
    }

    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
        state = state.getActualState(access, pos);
        AxisAlignedBB box = AABB;
        for (val entry : SIDES.entrySet()) {
            if (state.getValue(entry.getValue())) {
                box = box.union(AABB_SIDE.withDirection(entry.getKey()));
            }
        }
        return box;
    }

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing side) {
        return canConnect(access, pos, side) ? BlockFaceShape.CENTER_BIG : BlockFaceShape.UNDEFINED;
    }

    @Override
    @Deprecated
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> boxes, @Nullable Entity entity, boolean isActualState) {
        if (!isActualState) state = state.getActualState(world, pos);
        addCollisionBoxToList(pos, entityBox, boxes, AABB);
        for (val entry : SIDES.entrySet()) {
            if (state.getValue(entry.getValue())) {
                addCollisionBoxToList(pos, entityBox, boxes, AABB_SIDE.withDirection(entry.getKey()));
            }
        }
    }

    @Override
    @Nullable
    @Deprecated
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        state = state.getActualState(world, pos);
        val boxes = Sets.newHashSet(AABB);
        for (val e : SIDES.entrySet()) {
            if (state.getValue(e.getValue())) {
                boxes.add(AABB_SIDE.withDirection(e.getKey()));
            }
        }
        val results = new HashSet<RayTraceResult>();
        val min = start.subtract(pos.getX(), pos.getY(), pos.getZ());
        val max = end.subtract(pos.getX(), pos.getY(), pos.getZ());
        for (val box : boxes) {
            @Nullable val result = box.calculateIntercept(min, max);
            if (result != null) {
                val vec = result.hitVec.add(pos.getX(), pos.getY(), pos.getZ());
                results.add(new RayTraceResult(vec, result.sideHit, pos));
            }
        }
        @Nullable RayTraceResult nearest = null;
        var sqrDis = 0.0;
        for (val result : results) {
            val newSqrDis = result.hitVec.squareDistanceTo(end);
            if (newSqrDis > sqrDis) {
                nearest = result;
                sqrDis = newSqrDis;
            }
        }
        return nearest;
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 1.0F;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess access, BlockPos pos, EnumFacing side) {
        return this == access.getBlockState(pos.offset(side)).getBlock();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        val builder = new BlockStateContainer.Builder(this);
        SIDES.values().forEach(builder::add);
        return builder.add(ACTIVE, DIRECTION).build();
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public boolean isStructuralModule(@Nullable IBlockState connecting, IBlockState state) {
        return true;
    }
}
