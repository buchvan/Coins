package io.neolab.internship.coins.server.game.feature;

import java.io.Serializable;

/**
 * Интерфейс особенности пары раса-тип_клетки (Race-CellType)
 */
public interface IFeature extends Serializable {
    /**
     * @return тип особенности
     */
    FeatureType getType();
}
