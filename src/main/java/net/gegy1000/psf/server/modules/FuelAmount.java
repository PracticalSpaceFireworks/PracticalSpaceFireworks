package net.gegy1000.psf.server.modules;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public class FuelAmount {

    @Getter
    @Setter
    private int amount;
    
    @Getter
    @Setter
    private int capacity;
    
    public FuelAmount addAmount(int amount) {
        this.amount += amount;
        return this;
    }
    
    public FuelAmount addCapacity(int cap) {
        this.capacity += cap;
        return this;
    }
}
