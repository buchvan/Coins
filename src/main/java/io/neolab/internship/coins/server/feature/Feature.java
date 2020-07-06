package io.neolab.internship.coins.server.feature;

import java.util.Objects;

/**
 * Особенность пары раса-тип_клетки (Race-CellType)
 */
public class Feature implements IFeature {
    private final FeatureType type;

    public Feature(final FeatureType type) {
        this.type = type;
    }

    public FeatureType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feature feature = (Feature) o;
        return type == feature.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "Feature{" +
                "type=" + type +
                '}';
    }
}
