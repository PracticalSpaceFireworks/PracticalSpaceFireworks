package net.gegy1000.psf.server.modules;

import java.util.Optional;

import javax.annotation.Nonnull;

import lombok.Setter;
import net.gegy1000.psf.api.IController;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class ModuleController extends EmptyModule implements IController {
    
    @Setter
    @Nonnull
    private BlockPos pos = BlockPos.ORIGIN;
    
    public ModuleController() {
        super("controller.simple");
    }

    public ModuleController(BlockPos pos) {
        this();
        this.pos = pos;
    }

    @Override
    public Optional<BlockPos> getPosition() {
        return Optional.of(pos);
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound ret = super.serializeNBT();
        ret.setInteger("controller_x", pos.getX());
        ret.setInteger("controller_y", pos.getY());
        ret.setInteger("controller_z", pos.getZ());
        return ret;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        setPos(new BlockPos(nbt.getInteger("controller_x"), nbt.getInteger("controller_y"), nbt.getInteger("controller_z")));
    }
}
