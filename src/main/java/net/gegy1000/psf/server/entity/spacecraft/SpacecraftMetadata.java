package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class SpacecraftMetadata {
    private final ImmutableList<Thruster> thrusters;

    public SpacecraftMetadata(ImmutableList<Thruster> thrusters) {
        this.thrusters = thrusters;
    }

    public NBTTagCompound serialize(NBTTagCompound compound) {
        NBTTagList thrusterList = new NBTTagList();
        for (Thruster thruster : this.thrusters) {
            thrusterList.appendTag(thruster.serialize(new NBTTagCompound()));
        }

        compound.setTag("thrusters", thrusterList);

        return compound;
    }

    public static SpacecraftMetadata deserialize(NBTTagCompound compound) {
        ImmutableList.Builder<Thruster> thrusters = ImmutableList.builder();
        NBTTagList thrusterList = compound.getTagList("thrusters", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < thrusterList.tagCount(); i++) {
            NBTTagCompound thrusterTag = thrusterList.getCompoundTagAt(i);
            thrusters.add(new Thruster(new BlockPos(thrusterTag.getInteger("x"), thrusterTag.getInteger("y"), thrusterTag.getInteger("z"))));
        }

        return new SpacecraftMetadata(thrusters.build());
    }

    public List<Thruster> getThrusters() {
        return this.thrusters;
    }

    public static class Thruster {
        private final BlockPos pos;

        public Thruster(BlockPos pos) {
            this.pos = pos;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public NBTTagCompound serialize(NBTTagCompound compound) {
            compound.setInteger("x", this.pos.getX());
            compound.setInteger("y", this.pos.getY());
            compound.setInteger("z", this.pos.getZ());

            return compound;
        }

        public static Thruster deserialize(NBTTagCompound compound) {
            BlockPos pos = new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
            return new Thruster(pos);
        }
    }
}
