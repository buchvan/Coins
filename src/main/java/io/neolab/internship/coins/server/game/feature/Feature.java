package io.neolab.internship.coins.server.game.feature;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.neolab.internship.coins.common.deserialize.FeatureDeserializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Особенность пары (раса, тип_клетки) (Race, CellType)
 */
@JsonDeserialize(using = FeatureDeserializer.class)
public class Feature implements IFeature {
    private final @NotNull FeatureType type;

    @Contract(pure = true)
    @JsonCreator
    public Feature(@NotNull @JsonProperty("type") final FeatureType type) {
        this.type = type;
    }

    @Override
    public @NotNull FeatureType getType() {
        return type;
    }

    @Contract(value = "null -> false", pure = true)
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
