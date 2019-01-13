package net.gegy1000.psf.client.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.util.ResourceLocation;

public enum PSFIcons implements IWidgetIcon {
    BAR_BACKGROUND(0, 0, 115, 5),
    BAR_FILL(0, 5, 113, 3),
    WARNING(0, 16, 9, 9),
    ;

    public static final @Nonnull ResourceLocation TEXTURE = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/widgets.png");

    public static final @Nonnull IWidgetMap map = new IWidgetMap.WidgetMapImpl(256, TEXTURE);

    public final int x;
    public final int y;
    public final int width;
    public final int height;
    public final @Nullable IWidgetIcon overlay;

    PSFIcons(int x, int y) {
      this(x, y, null);
    }

    PSFIcons(int x, int y, @Nullable IWidgetIcon overlay) {
      this(x, y, 16, 16, overlay);
    }

    PSFIcons(int x, int y, int width, int height) {
      this(x, y, width, height, null);
    }

    PSFIcons(int x, int y, int width, int height, @Nullable IWidgetIcon overlay) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.overlay = overlay;
    }

    @Override
    public int getX() {
      return x;
    }

    @Override
    public int getY() {
      return y;
    }

    @Override
    public int getWidth() {
      return width;
    }

    @Override
    public int getHeight() {
      return height;
    }

    @Override
    public @Nullable IWidgetIcon getOverlay() {
      return overlay;
    }

    @Override
    public @Nonnull IWidgetMap getMap() {
      return map;
    }
}
