package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.CatchCellAnswer;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.apache.commons.collections4.BidiMap;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static io.neolab.internship.coins.server.game.service.GameInitializer.gameInit;
import static org.junit.Assert.*;

public class GameCatchCellAnswerProcessorTests {
    @Test
    public void catchCellWrongPositionTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        final Pair<Position, List<Unit>> resolution = new Pair<>(new Position(100, 100), Collections.emptyList());
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.WRONG_POSITION, exception.getErrorCode());
    }

    @Test
    public void catchCellNoCellForCatchingTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        final Pair<Position, List<Unit>> resolution = new Pair<>(new Position(1, 1), Collections.emptyList());
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.NO_ACHIEVABLE_CELL, exception.getErrorCode());
    }

    @Test
    public void catchCellNoUnitsForCatchingTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);

        final Position somePosition = getSomeBoardPosition(game.getBoard().getPositionToCellMap());
        final Cell someCell = game.getBoard().getCellByPosition(somePosition);
        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(someCell);
        someCell.setFeudal(player);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        final Pair<Position, List<Unit>> resolution = new Pair<>(somePosition, Collections.emptyList());
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.NO_AVAILABLE_UNITS, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellLandNotEnoughUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 1);

        final IBoard board = game.getBoard();
        final Cell landCell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(cell -> cell.getType() == CellType.LAND)
                .collect(Collectors.toList())
                .get(0);
        final Position landPosition = board.getPositionByCell(landCell);

        landCell.setFeudal(player);
        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(landCell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        final List<Cell> controlledCells = ownToCells.get(player);
        controlledCells.add(landCell);

        final Pair<Position, List<Unit>> resolution = new Pair<>(landPosition, new ArrayList<>());
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellMushroomNotEnoughUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 1);

        final IBoard board = game.getBoard();
        final Cell mushroomCell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(cell -> cell.getType() == CellType.MUSHROOM)
                .collect(Collectors.toList())
                .get(0);
        final Position mushroomPosition = board.getPositionByCell(mushroomCell);

        mushroomCell.setFeudal(player);
        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(mushroomCell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        final List<Cell> controlledCells = ownToCells.get(player);
        controlledCells.add(mushroomCell);

        final Pair<Position, List<Unit>> resolution = new Pair<>(mushroomPosition, new ArrayList<>());
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellMountainNotEnoughUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 1);

        final IBoard board = game.getBoard();
        final Cell mountainCell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(cell -> cell.getType() == CellType.MOUNTAIN)
                .collect(Collectors.toList())
                .get(0);
        final Position mountainPosition = board.getPositionByCell(mountainCell);

        mountainCell.setFeudal(player);
        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(mountainCell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        final List<Cell> controlledCells = ownToCells.get(player);
        controlledCells.add(mountainCell);

        final Pair<Position, List<Unit>> resolution = new Pair<>(mountainPosition, new ArrayList<>());
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellWaterNotEnoughUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.AMPHIBIAN);
        setPlayerUnits(player, 1);

        final IBoard board = game.getBoard();
        final Cell waterCell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(cell -> cell.getType() == CellType.WATER)
                .collect(Collectors.toList())
                .get(0);
        final Position waterPosition = board.getPositionByCell(waterCell);

        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(waterCell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        waterCell.setFeudal(player);
        final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        final List<Cell> controlledCells = ownToCells.get(player);
        controlledCells.add(waterCell);

        final Pair<Position, List<Unit>> resolution = new Pair<>(waterPosition, new ArrayList<>());
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchLandCellSucceedTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 2);

        final IBoard board = game.getBoard();
        final Cell landCell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(cell -> cell.getType() == CellType.LAND)
                .collect(Collectors.toList())
                .get(0);
        final Position landPosition = board.getPositionByCell(landCell);

        landCell.setFeudal(player);
        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(landCell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        final List<Unit> units = new ArrayList<>();
        units.add(new Unit());
        units.add(new Unit());
        final Pair<Position, List<Unit>> resolution = new Pair<>(landPosition, units);
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, landCell.getFeudal());
    }

    @Test
    public void catchMountainCellSucceedTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 3);

        final IBoard board = game.getBoard();
        final Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.MOUNTAIN)
                .collect(Collectors.toList())
                .get(0);
        final Position position = board.getPositionByCell(cell);

        cell.setFeudal(player);
        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        final List<Unit> units = new ArrayList<>();
        units.add(new Unit());
        units.add(new Unit());
        units.add(new Unit());
        final Pair<Position, List<Unit>> resolution = new Pair<>(position, units);
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, cell.getFeudal());
    }

    @Test
    public void catchMushroomCellSucceedTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 3);

        final IBoard board = game.getBoard();
        final Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.MUSHROOM)
                .collect(Collectors.toList())
                .get(0);
        final Position position = board.getPositionByCell(cell);

        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        cell.setFeudal(player);
        final List<Unit> units = new ArrayList<>();
        units.add(new Unit());
        units.add(new Unit());
        final Pair<Position, List<Unit>> resolution = new Pair<>(position, units);
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, cell.getFeudal());
    }

    @Test
    public void catchWaterCellByAmphibianSucceedTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.AMPHIBIAN);
        setPlayerUnits(player, 1);

        final IBoard board = game.getBoard();
        final Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.WATER)
                .collect(Collectors.toList())
                .get(0);
        final Position position = board.getPositionByCell(cell);

        cell.setFeudal(player);
        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        final List<Unit> units = new ArrayList<>();
        units.add(new Unit());
        units.add(new Unit());
        final Pair<Position, List<Unit>> resolution = new Pair<>(position, units);
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, cell.getFeudal());
    }

    @Test
    public void catchWaterCellByAmphibianFailedTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ORC);
        setPlayerUnits(player, 3);

        final IBoard board = game.getBoard();
        final Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.WATER)
                .collect(Collectors.toList())
                .get(0);
        final Position position = board.getPositionByCell(cell);

        cell.setFeudal(player);
        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        final List<Unit> units = new ArrayList<>();
        units.add(new Unit());
        units.add(new Unit());
        final Pair<Position, List<Unit>> resolution = new Pair<>(position, units);
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.CELL_CAPTURE_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchCellByOrc() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ORC);
        setPlayerUnits(player, 3);

        final IBoard board = game.getBoard();
        final Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.MUSHROOM)
                .collect(Collectors.toList())
                .get(0);
        final Position position = board.getPositionByCell(cell);

        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        final List<Unit> units = new ArrayList<>();
        units.add(new Unit());
        final Pair<Position, List<Unit>> resolution = new Pair<>(position, units);
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, cell.getFeudal());
    }

    @Test
    public void catchOtherPlayerCell() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = game.getPlayers().get(0);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 5);

        final IBoard board = game.getBoard();
        final Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.MUSHROOM)
                .collect(Collectors.toList())
                .get(0);

        final Player otherPlayer = game.getPlayers().get(1);
        otherPlayer.setRace(Race.MUSHROOM);
        final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        final List<Cell> controlledCells = ownToCells.get(otherPlayer);
        controlledCells.add(cell);
        cell.setFeudal(otherPlayer);
        final List<Unit> cellUnits = new ArrayList<>();
        cellUnits.add(new Unit());
        cellUnits.add(new Unit());
        cell.setUnits(cellUnits);

        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        final Position position = board.getPositionByCell(cell);

        final List<Unit> resolutionUnits = new ArrayList<>();
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        final Pair<Position, List<Unit>> resolution = new Pair<>(position, resolutionUnits);
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);

        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, cell.getFeudal());
    }

    @Test
    public void catchGnomeCellByGnome() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = game.getPlayers().get(0);
        player.setRace(Race.GNOME);
        setPlayerUnits(player, 6);

        final IBoard board = game.getBoard();
        final Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.MUSHROOM)
                .collect(Collectors.toList())
                .get(0);

        final List<Unit> cellUnits = new ArrayList<>();
        cellUnits.add(new Unit());
        cellUnits.add(new Unit());
        cell.setUnits(cellUnits);

        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        final Player otherPlayer = game.getPlayers().get(1);
        otherPlayer.setRace(Race.GNOME);
        final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        final List<Cell> controlledCells = ownToCells.get(otherPlayer);
        controlledCells.add(cell);
        cell.setFeudal(otherPlayer);

        final Position position = board.getPositionByCell(cell);

        final List<Unit> resolutionUnits = new ArrayList<>();
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        final Pair<Position, List<Unit>> resolution = new Pair<>(position, resolutionUnits);
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);

        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, cell.getFeudal());
    }

    @Test
    public void catchGnomeCellByGnomeOwnersTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = game.getPlayers().get(0);
        player.setRace(Race.GNOME);
        setPlayerUnits(player, 6);

        final IBoard board = game.getBoard();
        final Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.MUSHROOM)
                .collect(Collectors.toList())
                .get(0);

        final List<Unit> cellUnits = new ArrayList<>();
        cellUnits.add(new Unit());
        cellUnits.add(new Unit());
        cell.setUnits(cellUnits);

        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        final Player otherPlayer = game.getPlayers().get(1);
        otherPlayer.setRace(Race.GNOME);
        final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        final List<Cell> controlledCells = ownToCells.get(otherPlayer);
        controlledCells.add(cell);
        cell.setRace(Race.GNOME);
        final Position position = board.getPositionByCell(cell);
        cell.setFeudal(otherPlayer);
        final List<Unit> resolutionUnits = new ArrayList<>();
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        final Pair<Position, List<Unit>> resolution = new Pair<>(position, resolutionUnits);
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        System.out.println("CONTROLLED CELLS BEFORE: " + controlledCells.size());
        GameAnswerProcessor.process(question, catchCellAnswer);
        System.out.println("CONTROLLED CELLS AFTER: " + controlledCells.size());
        assertTrue(controlledCells.isEmpty());
    }

    @Test
    public void catchGnomeCellsUnitsDeadTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = game.getPlayers().get(0);
        player.setRace(Race.GNOME);
        setPlayerUnits(player, 6);

        final IBoard board = game.getBoard();
        final Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.MUSHROOM)
                .collect(Collectors.toList())
                .get(0);

        final List<Unit> cellUnits = new ArrayList<>();
        cellUnits.add(new Unit());
        cellUnits.add(new Unit());
        cell.setUnits(cellUnits);

        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        final Player otherPlayer = game.getPlayers().get(1);
        otherPlayer.setRace(Race.GNOME);
        final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        final List<Cell> controlledCells = ownToCells.get(otherPlayer);
        controlledCells.add(cell);
        cell.setRace(Race.GNOME);
        final Position position = board.getPositionByCell(cell);
        cell.setFeudal(otherPlayer);
        final List<Unit> resolutionUnits = new ArrayList<>();
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        final Pair<Position, List<Unit>> resolution = new Pair<>(position, resolutionUnits);
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        System.out.println("CONTROLLED CELLS BEFORE: " + controlledCells.size());
        GameAnswerProcessor.process(question, catchCellAnswer);
        System.out.println("CONTROLLED CELLS AFTER: " + controlledCells.size());
        assertEquals(player, cell.getFeudal());
    }

    @Test
    public void catchNeutralCellSucceedTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 2);

        final IBoard board = game.getBoard();
        final Cell landCell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(cell -> cell.getType() == CellType.LAND)
                .collect(Collectors.toList())
                .get(0);
        final Position landPosition = board.getPositionByCell(landCell);
        final List<Unit> cellUnits = new ArrayList<>();
        cellUnits.add(new Unit());
        cellUnits.add(new Unit());
        final Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(landCell);
        game.getPlayerToAchievableCells().put(player, achievableCells);
        landCell.setRace(Race.MUSHROOM);
        landCell.setUnits(cellUnits);
        final List<Unit> units = new ArrayList<>();
        units.add(new Unit());
        units.add(new Unit());
        units.add(new Unit());
        units.add(new Unit());
        units.add(new Unit());
        final Pair<Position, List<Unit>> resolution = new Pair<>(landPosition, units);
        final PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(resolution);
        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, landCell.getFeudal());
    }

    private Player getSomePlayer(final IGame game) {
        return game.getPlayers().get(0);
    }

    private Position getSomeBoardPosition(final BidiMap<Position, Cell> positionCellBidiMap) {
        final List<Cell> cells = new ArrayList<>(positionCellBidiMap.values());
        return positionCellBidiMap.getKey(cells.get(0));
    }

    private void setPlayerUnits(final Player player, final int unitsAmount) {
        final List<Unit> playerUnits = new ArrayList<>();
        for (int i = 0; i < unitsAmount; i++) {
            playerUnits.add(new Unit());
        }
        player.getUnitStateToUnits().put(AvailabilityType.AVAILABLE, playerUnits);
    }
}
