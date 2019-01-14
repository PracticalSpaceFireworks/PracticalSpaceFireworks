package net.gegy1000.psf.api;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiButton;

public interface IModuleConfigButton<T extends GuiButton> {

    @SuppressWarnings("unchecked")
    @Nonnull
    default T getButton() {
        return (T) this;
    }

    /**
     * Used for scrolling, should return the INITIAL y value, do not just return GuiButton.y
     */
    int getY();

    /* Extra GUI hooks */

    default boolean keyTyped(char typedChar, int keyCode) {
        return false;
    }

}
