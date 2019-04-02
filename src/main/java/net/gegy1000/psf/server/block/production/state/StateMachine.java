package net.gegy1000.psf.server.block.production.state;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Supplier;

public final class StateMachine<C> {
    private final StateSet<C> stateSet;
    private final Supplier<C> contextSupplier;

    @Getter
    private StateType state;

    @Setter
    private long stateChangeInterval;

    private long nextStateTick;

    public StateMachine(StateSet<C> stateSet, Supplier<C> contextSupplier) {
        this.stateSet = stateSet;
        this.state = stateSet.getInitState();
        this.contextSupplier = contextSupplier;
    }

    public void update(long time) {
        if (time >= nextStateTick) {
            StateType last = state;
            StateStepFunction<C> function = stateSet.getStepFunction(state);
            state = function.step(contextSupplier.get());
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
        StateType parsedState = stateSet.byId(compound.getString("id"));
        if (parsedState != null) {
            state = parsedState;
        }
    }
}
