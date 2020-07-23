package io.neolab.internship.coins.server.game.feature;

import com.fasterxml.jackson.annotation.*;

import java.util.Objects;

/**
 * Особенность пары раса-тип_клетки (Race-CellType)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CoefficientlyFeature.class, name = "CoefficientlyFeature"),
})
public class Feature implements IFeature {
    @JsonProperty
    private final FeatureType type;

    @JsonCreator
    public Feature(@JsonProperty("type") final FeatureType type) {
        this.type = type;
    }

    public Feature getCopy() {
        return new Feature(type);
    }

    @Override
    public FeatureType getType() {
        return type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Feature feature = (Feature) o;
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
