package net.gegy1000.psf.server.block.remote;

import java.util.List;

public interface ICraftList {

    void rebuildCraftList();

    void provideServerCrafts(List<IListedSpacecraft> crafts);

    void provideSingleCraft(IListedSpacecraft craft);

    List<IListedSpacecraft> getCrafts();

}
