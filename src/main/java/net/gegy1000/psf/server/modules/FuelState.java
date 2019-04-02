package net.gegy1000.psf.server.modules;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraftforge.fluids.Fluid;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class FuelState {
    private int amount, capacity;

    FuelState(Fluid fluid) {
        super();
    }

    public FuelState addAmount(int amount) {
        this.amount += amount;
        return this;
    }

    public FuelState addCapacity(int capacity) {
        this.capacity += capacity;
        return this;
    }
}
