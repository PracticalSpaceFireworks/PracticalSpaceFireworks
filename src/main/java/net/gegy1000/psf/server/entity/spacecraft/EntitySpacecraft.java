package net.gegy1000.psf.server.entity.spacecraft;

import io.netty.buffer.ByteBuf;
import net.gegy1000.psf.client.render.spacecraft.model.SpacecraftModel;
import net.gegy1000.psf.server.util.Matrix;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class EntitySpacecraft extends Entity implements IEntityAdditionalSpawnData {
    private final Matrix rotationMatrix = new Matrix(3);

    @SideOnly(Side.CLIENT)
    public SpacecraftModel model;

    private SpacecraftBlockAccess blockAccess;
    private SpacecraftMetadata metadata;

    private boolean testLaunched;

    private boolean resetMatrix = true;

    public EntitySpacecraft(World world) {
        this(world, Collections.emptySet(), BlockPos.ORIGIN);
    }

    public EntitySpacecraft(World world, Set<BlockPos> positions, @Nonnull BlockPos origin) {
        super(world);
        setSize(1, 1);

        SpacecraftBuilder builder = new SpacecraftBuilder();
        for (BlockPos pos : positions) {
            builder.setBlockState(pos.subtract(origin), world.getBlockState(pos));
        }
        this.blockAccess = builder.buildBlockAccess(this);
        this.metadata = builder.buildMetadata();
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.testLaunched) {
            double acc = 0.05;
            this.motionY += acc;

            for (SpacecraftMetadata.Thruster thruster : this.metadata.getThrusters()) {
                BlockPos thrusterPos = thruster.getPos();
                double posX = this.posX + thrusterPos.getX();
                double posY = this.posY + thrusterPos.getY() + 0.5;
                double posZ = this.posZ + thrusterPos.getZ();
                for (int i = 0; i < 10; i++) {
                    double motionX = this.rand.nextDouble() * 2.0 - 1;
                    double motionY = -acc;
                    double motionZ = this.rand.nextDouble() * 2.0 - 1;
                    this.world.spawnParticle(EnumParticleTypes.FLAME, posX, posY, posZ, motionX * 0.1, motionY, motionZ * 0.1);
                }
            }
        }

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

        if (this.posY > 1000) {
            setDead();
        }

        this.rotationYaw += 1;
        this.rotationPitch = 0;

        if (Math.abs(this.rotationYaw - this.prevRotationYaw) > 1e-3 || Math.abs(this.rotationPitch - this.prevRotationPitch) > 1e-3 || this.resetMatrix) {
            this.rotationMatrix.identity();
            this.rotationMatrix.rotate(this.rotationYaw, 0.0F, 1.0F, 0.0F);
            this.rotationMatrix.rotate(this.rotationPitch, 1.0F, 0.0F, 0.0F);

            this.setEntityBoundingBox(this.calculateEncompassingBounds());

            this.resetMatrix = false;
        }
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);

        this.setEntityBoundingBox(this.calculateEncompassingBounds());
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.calculateEncompassingBounds();
    }

    @Override
    public AxisAlignedBB getEntityBoundingBox() {
        return super.getEntityBoundingBox();
    }

    @Override
    public @Nonnull AxisAlignedBB getRenderBoundingBox() {
        return this.calculateEncompassingBounds();
    }

    private @Nonnull AxisAlignedBB calculateEncompassingBounds() {
        AxisAlignedBB ret = new AxisAlignedBB(BlockPos.ORIGIN);
        for (AxisAlignedBB bb : this.collectTransformedBlockBounds()) {
            ret = ret.union(bb);
        }

        return ret.offset(posX - 0.5, posY, posZ - 0.5);
    }

    @Override
    public void resetPositionToBB() {
        AxisAlignedBB bounds = this.calculateEncompassingBounds();
        AxisAlignedBB updatedBounds = this.getEntityBoundingBox();
        this.posX += updatedBounds.minX - bounds.minX;
        this.posY += updatedBounds.minY - bounds.minY;
        this.posZ += updatedBounds.minZ - bounds.minZ;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return super.isInRangeToRenderDist(distance / 8);
    }

    private List<AxisAlignedBB> collectTransformedBlockBounds() {
        List<AxisAlignedBB> bounds = new ArrayList<>();
        if (this.blockAccess != null) {
            for (BlockPos pos : BlockPos.getAllInBoxMutable(this.blockAccess.getMinPos(), this.blockAccess.getMaxPos())) {
                IBlockState state = this.blockAccess.getBlockState(pos);
                AxisAlignedBB bb = state.getCollisionBoundingBox(this.blockAccess, pos);
                if (bb != null) {
                    bounds.add(this.rotateBoundsEncompassing(bb, pos));
                }
            }
        }
        return bounds;
    }

    private AxisAlignedBB rotateBoundsEncompassing(AxisAlignedBB bounds, BlockPos pos) {
        bounds = bounds.offset(pos);

        Point3d min = new Point3d(bounds.minX, bounds.minY, bounds.minZ);
        Point3d max = new Point3d(bounds.maxX, bounds.maxY, bounds.maxZ);

        this.rotationMatrix.transform(min);
        this.rotationMatrix.transform(max);

        return new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        this.testLaunched = true;
        return true;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.blockAccess = SpacecraftBlockAccess.deserialize(compound.getCompoundTag("block_data"), getEntityWorld());
        this.metadata = SpacecraftMetadata.deserialize(compound.getCompoundTag("metadata"));

        this.resetMatrix = true;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("block_data", this.blockAccess.serialize(new NBTTagCompound()));
        compound.setTag("metadata", this.metadata.serialize(new NBTTagCompound()));
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        this.blockAccess.serialize(buffer);
        ByteBufUtils.writeTag(buffer, this.metadata.serialize(new NBTTagCompound()));
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.blockAccess = SpacecraftBlockAccess.deserialize(buffer, getEntityWorld());
        this.metadata = SpacecraftMetadata.deserialize(ByteBufUtils.readTag(buffer));
        this.model = null;

        this.resetMatrix = true;
    }

    public SpacecraftBlockAccess getBlockAccess() {
        return this.blockAccess;
    }
}
