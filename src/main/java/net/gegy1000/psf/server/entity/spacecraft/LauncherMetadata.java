package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class LauncherMetadata {
    private final ImmutableList<Thruster> thrusters;
    private final double mass;

    public LauncherMetadata(ImmutableList<Thruster> thrusters, double mass) {
        this.thrusters = thrusters;
        this.mass = mass;
    }

    public NBTTagCompound serialize(NBTTagCompound compound) {
        NBTTagList thrusterList = new NBTTagList();
        for (Thruster thruster : this.thrusters) {
            thrusterList.appendTag(thruster.serialize(new NBTTagCompound()));
        }

        compound.setTag("thrusters", thrusterList);

        compound.setDouble("mass", this.mass);

        return compound;
    }

    public void serialize(ByteBuf buffer) {
        buffer.writeByte(this.thrusters.size() & 0xFF);
        for (Thruster thruster : this.thrusters) {
            thruster.serialize(buffer);
        }

        buffer.writeDouble(this.mass);
    }

    public static LauncherMetadata deserialize(NBTTagCompound compound) {
        ImmutableList.Builder<Thruster> thrusters = ImmutableList.builder();
        NBTTagList thrusterList = compound.getTagList("thrusters", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < thrusterList.tagCount(); i++) {
            NBTTagCompound thrusterTag = thrusterList.getCompoundTagAt(i);
            thrusters.add(Thruster.deserialize(thrusterTag));
        }

        double mass = compound.getDouble("mass");

        return new LauncherMetadata(thrusters.build(), mass);
    }

    public static LauncherMetadata deserialize(ByteBuf buffer) {
        ImmutableList.Builder<Thruster> thrusters = ImmutableList.builder();

        int thrusterCount = buffer.readUnsignedByte();
        for (int i = 0; i < thrusterCount; i++) {
            thrusters.add(Thruster.deserialize(buffer));
        }

        double mass = buffer.readDouble();

        return new LauncherMetadata(thrusters.build(), mass);
    }

    public List<Thruster> getThrusters() {
        return this.thrusters;
    }

    public double getTotalForce() {
        double force = 0.0;
        for (Thruster thruster : this.thrusters) {
            force += thruster.force;
        }
        return force;
    }

    public double getTotalAcceleration() {
        return this.getTotalForce() / this.mass;
    }

    public double getMass() {
        return this.mass;
    }

    public static class Thruster {
        private final BlockPos pos;
        private final double force;

        public Thruster(BlockPos pos, double force) {
            this.pos = pos;
            this.force = force;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public double getForce() {
            return this.force;
        }

        public NBTTagCompound serialize(NBTTagCompound compound) {
            compound.setInteger("x", this.pos.getX());
            compound.setInteger("y", this.pos.getY());
            compound.setInteger("z", this.pos.getZ());
            compound.setDouble("force", this.force);

            return compound;
        }

        public void serialize(ByteBuf buffer) {
            buffer.writeLong(this.pos.toLong());
            buffer.writeFloat((float) this.force);
        }

        public static Thruster deserialize(NBTTagCompound compound) {
            BlockPos pos = new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
            double force = compound.getDouble("force");
            return new Thruster(pos, force);
        }

        public static Thruster deserialize(ByteBuf buffer) {
            BlockPos pos = BlockPos.fromLong(buffer.readLong());
            double force = buffer.readFloat();
            return new Thruster(pos, force);
        }
    }
}
