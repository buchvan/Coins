package io.neolab.internship.coins.server.feature;

/**
 * Тип особенности пары раса-тип_клетки (Race-CellType)
 */
public enum FeatureType {
    CATCH_CELL_CHANGING_UNITS_NUMBER(true), // Изменение числа юнитов для захвата клетки
    CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL(true), // Изменение получаемых с клетки монеток
    CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL_GROUP(true), // Изменение получаемых с группы клеток монеток
    CATCH_CELL_IMPOSSIBLE(false), // Невозможность захвата клетки
    ;

    private final boolean isCoefficiently;

    FeatureType(final boolean isCoefficiently) {
        this.isCoefficiently = isCoefficiently;
    }

    public boolean isCoefficiently() {
        return isCoefficiently;
    }

    @Override
    public String toString() {
        return "FeatureType{" +
                "isCoefficiently=" + isCoefficiently +
                '}';
    }
}
