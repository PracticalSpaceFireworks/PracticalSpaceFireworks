package net.gegy1000.psf.server.block.remote;

import javax.annotation.ParametersAreNonnullByDefault;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

@RequiredArgsConstructor
@ParametersAreNonnullByDefault
public class ContainerControlSystem extends Container {
    
    @Getter
    private final TileRemoteControlSystem te;
    
    private final InventoryPlayer playerInv;

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

}
