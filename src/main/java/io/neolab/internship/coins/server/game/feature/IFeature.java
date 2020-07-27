package io.neolab.internship.coins.server.game.feature;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Интерфейс особенности пары раса-тип_клетки (Race-CellType)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Feature.class, name = "Feature"),
        @JsonSubTypes.Type(value = ICoefficientlyFeature.class, name = "ICoefficientlyFeature"),
})
public interface IFeature extends Serializable {
    /**
     * @return тип особенности
     */
    @NotNull FeatureType getType();
}
