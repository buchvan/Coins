package io.neolab.internship.coins.server.game.feature;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Интерфейс особенности пары раса-тип_клетки (Race-CellType)
 */
public interface IFeature extends Serializable {
    /**
     * @return тип особенности
     */
    @NotNull FeatureType getType();
}
