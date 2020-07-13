package io.neolab.internship.coins.server.game.feature;

/**
 * Интерфейс особенности с целочисленным коэффициентом пары раса-тип_клетки (Race-CellType)
 */
public interface ICoefficientlyFeature extends IFeature {
    int getCoefficient();
}
