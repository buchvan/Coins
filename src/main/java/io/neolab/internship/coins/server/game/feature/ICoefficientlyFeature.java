package io.neolab.internship.coins.server.game.feature;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Интерфейс особенности с целочисленным коэффициентом пары раса-тип_клетки (Race-CellType)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CoefficientlyFeature.class, name = "CoefficientlyFeature"),
})
public interface ICoefficientlyFeature extends IFeature {
    int getCoefficient();
}
