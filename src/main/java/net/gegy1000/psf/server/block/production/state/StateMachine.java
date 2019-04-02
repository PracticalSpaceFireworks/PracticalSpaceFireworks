package net.gegy1000.psf.server.block.production.state;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;

public final class StateMachine<C> {
    private final ImmutableMap<String, StateType> idToState;
    private final ImmutableMap<StateType, StateStepFunction<C>> stepFunctions;
    private final long stateChangeInterval;

    @Getter
    private StateType state;

    private long nextStateTick;

    StateMachine(StateType initState, ImmutableMap<String, StateType> idToState, ImmutableMap<StateType, StateStepFunction<C>> stepFunctions, long stateChangeInterval) {
        this.state = initState;
        this.idToState = idToState;
        this.stepFunctions = stepFunctions;
        this.stateChangeInterval = stateChangeInterval;
    }

    public void update(C ctx, long time) {
        if (time >= nextStateTick) {
            StateType last = state;
            StateStepFunction<C> function = stepFunctions.get(state);
            state = function.step(ctx);
            if (last != state) {
                nextStateTick = time + stateChangeInterval;
            }
        }
    }

    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("id", state.getId());
        return compound;
    }

    public void deserialize(NBTTagCompound compound) {
        StateType parsedState = idToState.get(compound.getString("id"));
        if (parsedState != null) {
            state = parsedState;
        }
    }
}
