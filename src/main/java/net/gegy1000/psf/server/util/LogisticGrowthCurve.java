package net.gegy1000.psf.server.util;

import lombok.Value;

/**
 * This class represents a <a href="https://en.wikipedia.org/wiki/Logistic_function">logistic growth curve</a>, for use in physics calculations where a value chages logarithmically over time.
 */
@Value
public class LogisticGrowthCurve {

    private final double capacity, a, b;
    
    public double get(double x) {
        return capacity / (1 + Math.exp(a + (b * x)));
    }
}
