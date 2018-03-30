package net.gegy1000.psf.server.entity.spacecraft;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import io.netty.buffer.ByteBuf;
import net.gegy1000.psf.client.render.spacecraft.model.SpacecraftModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntitySpacecraft extends Entity implements IEntityAdditionalSpawnData {
    @SideOnly(Side.CLIENT)
    public SpacecraftModel model;

    private SpacecraftBlockAccess blockAccess;
    private SpacecraftMetadata metadata;

    private boolean testLaunched;

    public EntitySpacecraft(World world) {
        this(world, Collections.emptySet(), BlockPos.ORIGIN);
    }
    
    public EntitySpacecraft(World world, Set<BlockPos> positions, @Nonnull BlockPos origin) {
        super(world);

        SpacecraftBuilder builder = new SpacecraftBuilder();
        for (BlockPos pos : positions) {
            builder.setBlockState(pos.subtract(origin), world.getBlockState(pos));
        }
        this.blockAccess = builder.buildBlockAccess();
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
                double posY = this.posY + thrusterPos.getY() - 1.0;
                double posZ = this.posZ + thrusterPos.getZ();
                for (int i = 0; i < 10; i++) {
                    double motionX = this.rand.nextDouble() * 2.0 - 0.5;
                    double motionY = -acc;
                    double motionZ = this.rand.nextDouble() * 2.0 - 0.5;
                    this.world.spawnParticle(EnumParticleTypes.FLAME, posX, posY, posZ, motionX * 0.1, motionY, motionZ * 0.1);
                }
            }
        }

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.getEntityBoundingBox();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        this.testLaunched = true;
        return true;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.blockAccess = SpacecraftBlockAccess.deserialize(compound.getCompoundTag("block_data"));
        this.metadata = SpacecraftMetadata.deserialize(compound.getCompoundTag("metadata"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("block_data", this.blockAccess.serialize(new NBTTagCompound()));
        compound.setTag("metadata", this.metadata.serialize(new NBTTagCompound()));
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        this.blockAccess.serialize(buffer);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.blockAccess = SpacecraftBlockAccess.deserialize(buffer);
        this.model = null;
    }

    public SpacecraftBlockAccess getBlockAccess() {
        return this.blockAccess;
    }
}
