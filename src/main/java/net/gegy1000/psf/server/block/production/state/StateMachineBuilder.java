package net.gegy1000.psf.server.block.production.state;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public final class StateMachineBuilder<C> {
    private StateType initState;
    private long stateChangeInterval;

    private final ImmutableMap.Builder<String, StateType> idToState = ImmutableMap.builder();
    private final ImmutableMap.Builder<StateType, StateStepFunction<C>> stepFunctions = ImmutableMap.builder();

    public StateMachineBuilder<C> withInitState(StateType state) {
        this.initState = state;
        return this;
    }

    public StateMachineBuilder<C> withStep(StateType state, StateStepFunction<C> function) {
        this.idToState.put(state.getId(), state);
        this.stepFunctions.put(state, function);
        return this;
    }

    public StateMachineBuilder<C> withStateChangeInterval(long interval) {
        this.stateChangeInterval = interval;
        return this;
    }

    public StateMachine<C> build() {
        Preconditions.checkNotNull(initState, "init state must be set!");
        return new StateMachine<>(initState, idToState.build(), stepFunctions.build(), stateChangeInterval);
    }
}
