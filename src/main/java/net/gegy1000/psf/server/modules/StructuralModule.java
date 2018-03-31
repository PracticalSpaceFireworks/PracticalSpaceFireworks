package net.gegy1000.psf.server.modules;

public class StructuralModule extends EmptyModule {

    public StructuralModule(String name) {
        super(name);
    }

    @Override
    public boolean isStructuralModule() {
        return true;
    }
}
