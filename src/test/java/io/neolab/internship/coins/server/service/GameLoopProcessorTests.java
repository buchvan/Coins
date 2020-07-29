package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.TestUtils;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.factory.BoardFactory;
import io.neolab.internship.coins.server.game.feature.CoefficientlyFeature;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.feature.FeatureType;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class GameLoopProcessorTests extends TestUtils {
    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testPlayerRoundBeginUpdateNull() {
        GameLoopProcessor.playerRoundBeginUpdate(null);
    }

    @Test
    public void testPlayerRoundBeginUpdate() {
        final Player player = new Player("F1");
        player.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());
        player.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());
        player.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());
        final Unit unit1 = new Unit();
        player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).add(unit1);
        final Unit unit2 = new Unit();
        player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).add(unit2);

        GameLoopProcessor.playerRoundBeginUpdate(player);

        assertTrue(player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).isEmpty());
        assertTrue(player.getUnitsByState(AvailabilityType.AVAILABLE).contains(unit1));
        assertTrue(player.getUnitsByState(AvailabilityType.AVAILABLE).contains(unit2));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testPlayerRoundEndUpdateNull() {
        GameLoopProcessor.playerRoundEndUpdate(null);
    }

    @Test
    public void testPlayerRoundEndUpdate() {
        final Player player = new Player("F1");
        player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).add(new Unit());
        player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).add(new Unit());
        player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).add(new Unit());
        final Unit unit1 = new Unit();
        player.getUnitsByState(AvailabilityType.AVAILABLE).add(unit1);
        final Unit unit2 = new Unit();
        player.getUnitsByState(AvailabilityType.AVAILABLE).add(unit2);

        GameLoopProcessor.playerRoundEndUpdate(player);

        assertTrue(player.getUnitsByState(AvailabilityType.AVAILABLE).isEmpty());
        assertTrue(player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).contains(unit1));
        assertTrue(player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).contains(unit2));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAchievableCellsNull1() throws CoinsException {
        final IBoard board = new BoardFactory().generateBoard(3, 3);
        GameLoopProcessor.updateAchievableCells(null, board, new HashSet<>(), new LinkedList<>());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAchievableCellsNull2() {
        GameLoopProcessor.updateAchievableCells(new Player("F1"), null, new HashSet<>(), new LinkedList<>());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAchievableCellsNull3() throws CoinsException {
        final IBoard board = new BoardFactory().generateBoard(3, 3);
        GameLoopProcessor.updateAchievableCells(new Player("F1"), board, null, new LinkedList<>());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAchievableCellsNull4() throws CoinsException {
        final IBoard board = new BoardFactory().generateBoard(3, 3);
        GameLoopProcessor.updateAchievableCells(new Player("F1"), board, new HashSet<>(), null);
    }

    @Test
    public void testUpdateAchievableCellsBoardEdge() throws CoinsException {
        final Player player = new Player("F1");
        final IBoard board = new BoardFactory().generateBoard(3, 3);
        final Set<Cell> achievableCells = new HashSet<>();
        final List<Cell> controlledCells = new LinkedList<>();
        final Cell someCell = getCellFromBoardByCellType(CellType.LAND, board);
        controlledCells.add(someCell);
        GameLoopProcessor.updateAchievableCells(player, board, achievableCells, controlledCells);
        assertNotNull(board.getNeighboringCells(someCell));
        assertTrue(achievableCells.containsAll(Objects.requireNonNull(board.getNeighboringCells(someCell))));
        assertEquals(
                Objects.requireNonNull(board.getNeighboringCells(someCell)).size() + 1, achievableCells.size());
    }

    @Test
    public void testUpdateAchievableCellsNotBoardEdge() throws CoinsException {
        final Player player = new Player("F1");
        final IBoard board = new BoardFactory().generateBoard(3, 3);
        final Set<Cell> achievableCells = new HashSet<>();
        final List<Cell> controlledCells = new LinkedList<>();
        GameLoopProcessor.updateAchievableCells(player, board, achievableCells, controlledCells);
        assertTrue(achievableCells.containsAll(board.getEdgeCells()));
        assertEquals(board.getEdgeCells().size(), achievableCells.size());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testGetUnitsNeededToCatchNull1() {
        GameLoopProcessor.getUnitsCountNeededToCatchCell(null, new Cell(CellType.LAND));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testGetUnitsNeededToCatchNull2() {
        GameLoopProcessor.getUnitsCountNeededToCatchCell(new GameFeatures(), null);
    }

    @Test
    public void testGetUnitsNeededToCatch() {
        final int bonusDefense = 3;
        final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures = new HashMap<>(2);
        List<Feature> features = new LinkedList<>();
        features.add(new CoefficientlyFeature(FeatureType.CATCH_CELL_CHANGING_UNITS_NUMBER, 2));
        features.add(new CoefficientlyFeature(FeatureType.DEFENSE_CELL_CHANGING_UNITS_NUMBER, bonusDefense));
        features.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        features.add(new CoefficientlyFeature(FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL, 1));
        features.add(new CoefficientlyFeature(FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL_GROUP, 1));
        features.add(new Feature(FeatureType.CATCH_CELL_IMPOSSIBLE));
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.LAND), features);
        features = new LinkedList<>();
        features.add(new CoefficientlyFeature(FeatureType.CATCH_CELL_CHANGING_UNITS_NUMBER, 2));
        features.add(new CoefficientlyFeature(FeatureType.DEFENSE_CELL_CHANGING_UNITS_NUMBER, 3));
        features.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        features.add(new CoefficientlyFeature(FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL, 1));
        features.add(new CoefficientlyFeature(FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL_GROUP, 1));
        features.add(new Feature(FeatureType.CATCH_CELL_IMPOSSIBLE));
        raceCellTypeFeatures.put(new Pair<>(Race.ELF, CellType.LAND), features);
        final GameFeatures gameFeatures = new GameFeatures(raceCellTypeFeatures);
        final Cell cell = new Cell(CellType.LAND);
        cell.setRace(Race.ORC);
        cell.getUnits().add(new Unit());
        final int actual = GameLoopProcessor.getUnitsCountNeededToCatchCell(gameFeatures, cell);

        assertEquals(cell.getType().getCatchDifficulty() + 3 + 1 + 1, actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testLoseCellsNull1() {
        GameLoopProcessor.loseCells(null, new LinkedList<>(), new HashSet<>());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testLoseCellsNull2() {
        GameLoopProcessor.loseCells(new LinkedList<>(), null, new HashSet<>());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testLoseCellsNull3() {
        GameLoopProcessor.loseCells(new LinkedList<>(), new LinkedList<>(), null);
    }

    @Test
    public void testLoseCells() {
        final Cell cell1 = new Cell(CellType.LAND);
        final Cell cell2 = new Cell(CellType.WATER);
        cell2.getUnits().add(new Unit());
        final Cell cell3 = new Cell(CellType.MOUNTAIN);
        cell3.getUnits().add(new Unit());

        final List<Cell> cells = new LinkedList<>();
        cells.add(cell1);
        cells.add(cell2);
        final List<Cell> controlledCells = new LinkedList<>();
        controlledCells.add(cell1);
        controlledCells.add(cell2);
        final Set<Cell> feudalCells = new HashSet<>();
        feudalCells.add(cell1);
        feudalCells.add(cell2);
        feudalCells.add(cell3);

        GameLoopProcessor.loseCells(cells, controlledCells, feudalCells);

        final List<Cell> expected1 = new LinkedList<>();
        expected1.add(cell2);
        assertEquals(expected1, controlledCells);

        final Set<Cell> expected2 = new HashSet<>();
        expected2.add(cell2);
        expected2.add(cell3);
        assertEquals(expected2, feudalCells);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testFreeTransitCellsNull1() {
        GameLoopProcessor.freeTransitCells(null, new LinkedList<>(), new LinkedList<>());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testFreeTransitCellsNull2() {
        GameLoopProcessor.freeTransitCells(new Player("F1"), null, new LinkedList<>());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testFreeTransitCellsNull3() {
        GameLoopProcessor.freeTransitCells(new Player("F1"), new LinkedList<>(), null);
    }

    @Test
    public void testFreeTransitCells() {
        final Player player = new Player("F1");
        final List<Cell> transitCells = new LinkedList<>();
        transitCells.add(new Cell(CellType.MUSHROOM));
        transitCells.add(new Cell(CellType.LAND));
        transitCells.add(new Cell(CellType.MOUNTAIN));

        final List<Cell> controlledCells = new LinkedList<>(transitCells);
        final Cell cell1 = new Cell(CellType.WATER);
        controlledCells.add(cell1);
        final Cell cell2 = new Cell(CellType.LAND);
        controlledCells.add(cell2);
        GameLoopProcessor.freeTransitCells(player, transitCells, controlledCells);
        assertTrue(transitCells.isEmpty());
        final List<Cell> expected = new LinkedList<>();
        expected.add(cell1);
        expected.add(cell2);
        assertEquals(expected, controlledCells);
    }
}