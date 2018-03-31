package net.gegy1000.psf.server.satellite;

import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class OrbitingSatellite {
    private final String name;
    private final UUID uuid;

    private final BlockPos position;
    private final SpacecraftBlockAccess blockAccess;

    public OrbitingSatellite(String name, UUID uuid, BlockPos position, SpacecraftBlockAccess blockAccess) {
        this.name = name;
        this.uuid = uuid;
        this.position = position;
        this.blockAccess = blockAccess;
    }

    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("name", this.name);
        compound.setUniqueId("uuid", this.uuid);

        compound.setInteger("x", this.position.getX());
        compound.setInteger("y", this.position.getY());
        compound.setInteger("z", this.position.getZ());

        compound.setTag("block_data", this.blockAccess.serialize(new NBTTagCompound()));

        return compound;
    }

    public static OrbitingSatellite deserialize(NBTTagCompound compound) {
        String name = compound.getString("name");
        UUID uuid = compound.getUniqueId("uuid");

        BlockPos pos = new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
        SpacecraftBlockAccess blockAccess = SpacecraftBlockAccess.deserialize(compound);

        return new OrbitingSatellite(name, uuid, pos, blockAccess);
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OrbitingSatellite && ((OrbitingSatellite) obj).uuid.equals(this.uuid);
    }
}
