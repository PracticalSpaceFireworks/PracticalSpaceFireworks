package net.gegy1000.psf.server.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;

public class ModuleThruster extends EmptyModule {

    @RequiredArgsConstructor
    public enum ThrusterTier implements IStringSerializable {
        // TODO: Different names for thrusters
        SIMPLE("simple", 420000.0),
        ADVANCED("advanced", 1267500.0);

        @Getter
        private final String name;
        @Getter
        private final double thrust;
    }

    @Getter
    private ThrusterTier tier;

    public ModuleThruster(ThrusterTier tier) {
        super("thruster." + tier.getName());
        this.tier = tier;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("tier", (byte) (this.tier.ordinal() & 0xFF));
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        int tier = nbt.getByte("tier") & 0xFF;
        this.tier = ThrusterTier.values()[tier % ThrusterTier.values().length];
    }
}
