package net.gegy1000.psf.api;

import net.gegy1000.psf.server.block.remote.config.GuiModuleConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IModuleConfigDisplay {
    
    IModuleConfigButton<?> createButton(GuiModuleConfig parent, IModule module, int id, int x, int y, int width);

}
