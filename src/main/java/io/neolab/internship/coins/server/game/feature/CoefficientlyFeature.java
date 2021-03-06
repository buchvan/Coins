package io.neolab.internship.coins.server.game.feature;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Особенность с целочисленным коэффициентом пары раса-тип_клетки (Race-CellType)
 */
public class CoefficientlyFeature extends Feature implements ICoefficientlyFeature {
    @JsonProperty
    private final int coefficient;

    @JsonCreator
    public CoefficientlyFeature(@NotNull @JsonProperty("type") final FeatureType type,
                                @JsonProperty("coefficient") final int coefficient) {
        super(type);
        this.coefficient = coefficient;
    }

    @Override
    public int getCoefficient() {
        return coefficient;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final CoefficientlyFeature that = (CoefficientlyFeature) o;
        return coefficient == that.coefficient;
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
