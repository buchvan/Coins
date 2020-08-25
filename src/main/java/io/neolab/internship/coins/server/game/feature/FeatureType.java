package io.neolab.internship.coins.server.game.feature;

import java.io.Serializable;

/**
 * Тип особенности пары раса-тип_клетки (Race-CellType)
 */
public enum FeatureType implements Serializable {
    CATCH_CELL_CHANGING_UNITS_NUMBER, // Изменение числа юнитов для захвата клетки
    DEFENSE_CELL_CHANGING_UNITS_NUMBER, // Изменение числа юнитов для обороны клетки
    DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, // Число погибших юнитов игрока после захвата клетки соперником
    CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL, // Изменение получаемых с клетки монеток
    CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL_GROUP, // Изменение получаемых с группы клеток монеток
    CATCH_CELL_IMPOSSIBLE, // Невозможность захвата клетки
    ;
}
