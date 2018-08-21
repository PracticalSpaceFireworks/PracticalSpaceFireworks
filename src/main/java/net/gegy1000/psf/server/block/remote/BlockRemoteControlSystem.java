package net.gegy1000.psf.server.block.remote;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class BlockRemoteControlSystem extends BlockHorizontal implements RegisterItemModel, RegisterItemBlock, RegisterTileEntity {

    public BlockRemoteControlSystem() {
        super(Material.IRON);
        this.setHarvestLevel("pickaxe", 1);
        this.setSoundType(SoundType.METAL);
        this.setHardness(2.0F);
        this.setResistance(3.0F);
        this.setCreativeTab(PracticalSpaceFireworks.TAB);

        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileRemoteControlSystem) {
            ((TileRemoteControlSystem) te).rebuildCraftList();
        }
        if (!world.isRemote && player instanceof EntityPlayerMP) {
            EnumMap<SatelliteState, List<IListedSpacecraft>> satellites = new EnumMap<>(SatelliteState.class);
            satellites.putAll(PracticalSpaceFireworks.PROXY.getSatellites().getAll().stream()
                    .filter(s -> s.getWorld() == world)
                    .map(ISatellite::toListedCraft)
                    .collect(Collectors.groupingBy(c -> SatelliteState.byClass(c.getClass()))));
            PSFNetworkHandler.network.sendTo(new PacketOpenRemoteControl(pos, satellites), (EntityPlayerMP) player);
        }
        return true;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
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
    public Class<? extends TileEntity> getEntityClass() {
        return TileRemoteControlSystem.class;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }
}
