package net.gegy1000.psf.server.block.remote;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ResourceAmount {
    private int capacity;
    private int amount;

    public void add(int amount, int capacity) {
        this.amount += amount;
        this.capacity += capacity;
    }

    public void clear() {
        this.amount = 0;
        this.capacity = 0;
    }
}