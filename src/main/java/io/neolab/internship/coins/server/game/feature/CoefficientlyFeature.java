package io.neolab.internship.coins.server.game.feature;

import java.util.Objects;

/**
 * Особенность с целочисленным коэффициентом пары раса-тип_клетки (Race-CellType)
 */
public class CoefficientlyFeature extends Feature implements ICoefficientlyFeature {
    private static final int coefficient = 1;

    public CoefficientlyFeature(final FeatureType type) {
        super(type);
    }

    @Override
    public int getCoefficient() {
        return coefficient;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), coefficient);
    }

    @Override
    public String toString() {
        return "CoefficientlyFeature{" +
                "coefficient=" + coefficient +
                '}';
    }
}
