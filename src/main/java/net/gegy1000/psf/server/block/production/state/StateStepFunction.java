package net.gegy1000.psf.server.block.production.state;

public interface StateStepFunction<C> {
    StateType step(C ctx);
}
