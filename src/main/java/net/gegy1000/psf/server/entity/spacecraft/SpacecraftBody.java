package net.gegy1000.psf.server.entity.spacecraft;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.gegy1000.psf.api.spacecraft.ISpacecraftBodyData;
import net.gegy1000.psf.server.entity.world.DelegatedWorld;
import net.gegy1000.psf.server.util.Matrix;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpacecraftBody {
    @Getter
    private final DelegatedWorld world;
    @Getter
    private final ISpacecraftBodyData data;

    @Getter
    private final Matrix rotationMatrix = new Matrix(3);

    @Getter
    private final Matrix inverseMatrix = new Matrix(3);

    private AxisAlignedBB calculatedBounds = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

    private float lastRecalcYaw = Float.MAX_VALUE;
    private float lastRecalcPitch = Float.MAX_VALUE;

    public SpacecraftBody(ISpacecraftBodyData data) {
        this.data = data;
        this.world = new DelegatedWorld(data);
    }

    public static SpacecraftBody empty() {
        return new SpacecraftBody(SpacecraftBodyData.empty());
    }

    public boolean updateRotation(float yaw, float pitch) {
        if (Math.abs(yaw - this.lastRecalcYaw) > 1e-3 || Math.abs(pitch - this.lastRecalcPitch) > 1e-3) {
            this.recalculateRotation(yaw, pitch);
            this.lastRecalcYaw = yaw;
            this.lastRecalcPitch = pitch;
            return true;
        }
        return false;
    }

    public void apply(Entity entity) {
        entity.setEntityBoundingBox(computeAppliedBounds(entity));
    }

    public AxisAlignedBB computeAppliedBounds(Entity entity) {
        return calculatedBounds.offset(entity.posX, entity.posY, entity.posZ);
    }

    private void recalculateRotation(float yaw, float pitch) {
        this.rotationMatrix.identity();
        this.rotationMatrix.rotate(180.0F - yaw, 0.0F, 1.0F, 0.0F);
        this.rotationMatrix.rotate(pitch, 1.0F, 0.0F, 0.0F);

        this.inverseMatrix.identity();
        this.inverseMatrix.multiply(this.rotationMatrix);
        this.inverseMatrix.inverse();

        this.calculatedBounds = this.computeEncompassingBounds();
    }

    @Nonnull
    private AxisAlignedBB computeEncompassingBounds() {
        List<AxisAlignedBB> bounds = this.collectTransformedBlockBounds();

        AxisAlignedBB ret = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
        for (AxisAlignedBB bb : bounds) {
            ret = ret.union(bb);
        }

        return ret;
    }

    public List<AxisAlignedBB> collectTransformedBlockBounds() {
        List<AxisAlignedBB> bounds = new ArrayList<>();
        if (data != null) {
            for (BlockPos pos : BlockPos.getAllInBoxMutable(data.getMinPos(), data.getMaxPos())) {
                IBlockState state = data.getBlockState(pos);
                if (state.getBlock() != Blocks.AIR) {
                    List<AxisAlignedBB> collisionBounds = new ArrayList<>();
                    state.addCollisionBoxToList(world, pos, TileEntity.INFINITE_EXTENT_AABB, collisionBounds, null, false);
                    for (AxisAlignedBB bb : collisionBounds) {
                        if (bb == null) {
                            continue;
                        }
                        bounds.add(rotateBoundsEncompassing(bb));
                    }
                }
            }
        }
        return bounds;
    }

    private AxisAlignedBB rotateBoundsEncompassing(AxisAlignedBB bounds) {
        bounds = bounds.offset(-0.5, 0, -0.5);
        return this.rotationMatrix.transform(bounds);
    }

    public BlockPos getMinPos() {
        return data.getMinPos();
    }

    public BlockPos getMaxPos() {
        return data.getMaxPos();
    }

    public Optional<SpacecraftDeconstructor.Result> deconstruct(World world, double x, double y, double z) {
        return SpacecraftDeconstructor.deconstruct(world, data, x, y, z, rotationMatrix);
    }

    public Vec3d toLocalPoint(Vec3d point) {
        return inverseMatrix.transformPoint(point);
    }

    public Vec3d toGlobalPoint(Vec3d point) {
        return rotationMatrix.transformPoint(point);
    }

    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setTag("body_data", data.serializeNBT());
        return compound;
    }

    public static SpacecraftBody deserialize(NBTTagCompound compound) {
        SpacecraftBodyData bodyData = SpacecraftBodyData.deserializeCraft(compound.getCompoundTag("body_data"));
        return new SpacecraftBody(bodyData);
    }

    public void serialize(ByteBuf buf) {
        data.serialize(buf);
    }

    public static SpacecraftBody deserialize(ByteBuf buf) {
        SpacecraftBodyData bodyData = SpacecraftBodyData.deserializeCraft(buf);
        return new SpacecraftBody(bodyData);
    }
}
