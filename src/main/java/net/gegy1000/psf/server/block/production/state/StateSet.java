package net.gegy1000.psf.server.block.production.state;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;

public final class StateSet<C> {
    @Getter
    private final StateType initState;
    private final ImmutableMap<String, StateType> idToState;
    private final ImmutableMap<StateType, StateStepFunction<C>> stepFunctions;

    private StateSet(StateType initState, ImmutableMap<String,StateType> idToState, ImmutableMap<StateType, StateStepFunction<C>> stepFunctions) {
        this.initState = initState;
        this.idToState = idToState;
        this.stepFunctions = stepFunctions;
    }

    public StateType byId(String id) {
        return idToState.get(id);
    }

    public StateStepFunction<C> getStepFunction(StateType state) {
        return stepFunctions.get(state);
    }

    public static final class Builder<C> {
        private StateType initState;

        private final ImmutableMap.Builder<String, StateType> idToState = ImmutableMap.builder();
        private final ImmutableMap.Builder<StateType, StateStepFunction<C>> stepFunctions = ImmutableMap.builder();

        public Builder<C> withInitState(StateType state) {
            this.initState = state;
            return this;
        }

        public Builder<C> withStep(StateType state, StateStepFunction<C> function) {
            this.idToState.put(state.getId(), state);
            this.stepFunctions.put(state, function);
            return this;
        }

        public StateSet<C> build() {
            Preconditions.checkNotNull(initState, "init state must be set!");
            return new StateSet<>(initState, idToState.build(), stepFunctions.build());
        }
    }
}
