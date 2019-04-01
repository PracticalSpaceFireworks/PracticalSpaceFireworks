package net.gegy1000.psf.server.block.remote;

import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.spacecraft.IListedSpacecraft;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.gegy1000.psf.server.api.RegisterTileEntity;
import net.gegy1000.psf.server.block.remote.packet.PacketOpenRemoteControl;
import net.gegy1000.psf.server.block.remote.packet.PacketOpenRemoteControl.SatelliteState;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockRemoteControlSystem extends BlockHorizontal implements RegisterItemModel, RegisterItemBlock, RegisterTileEntity {
    public BlockRemoteControlSystem() {
        super(Material.IRON);
        setHarvestLevel("pickaxe", 1);
        setSoundType(SoundType.METAL);
        setHardness(2.0F);
        setResistance(3.0F);
        setCreativeTab(PracticalSpaceFireworks.TAB);
        setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    @Deprecated
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    @Deprecated
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        @Nullable val te = world.getTileEntity(pos);
        if (te instanceof TileRemoteControlSystem) {
            ((TileRemoteControlSystem) te).rebuildCraftList();
        }
        if (!world.isRemote && player instanceof EntityPlayerMP) {
            val satellites = new EnumMap<SatelliteState, List<IListedSpacecraft>>(SatelliteState.class);
            for (val satellite : PracticalSpaceFireworks.PROXY.getSatellites().getAll()) {
                if (satellite.getWorld() == world) {
                    val listedCraft = satellite.toListedCraft();
                    satellites.computeIfAbsent(
                        SatelliteState.byClass(listedCraft.getClass()),
                        k -> new ArrayList<>()
                    ).add(listedCraft);
                }
            }
            PSFNetworkHandler.network.sendTo(new PacketOpenRemoteControl(pos, satellites), (EntityPlayerMP) player);
        }
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileRemoteControlSystem();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public Class<? extends TileEntity> getEntityClass() {
        return TileRemoteControlSystem.class;
    }
}
