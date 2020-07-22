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
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        Pair<Position, List<Unit>> resolution = new Pair<>(new Position(100, 100), Collections.emptyList());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.WRONG_POSITION, exception.getErrorCode());
    }

    @Test
    public void catchCellNoCellForCatchingTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        Pair<Position, List<Unit>> resolution = new Pair<>(new Position(1, 1), Collections.emptyList());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.NO_ACHIEVABLE_CELL, exception.getErrorCode());
    }

    @Test
    public void catchCellNoUnitsForCatchingTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);

        Position somePosition = getSomeBoardPosition(game.getBoard().getPositionToCellMap());
        Cell someCell = game.getBoard().getCellByPosition(somePosition);
        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(someCell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        Pair<Position, List<Unit>> resolution = new Pair<>(somePosition, Collections.emptyList());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.NO_AVAILABLE_UNITS, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellLandNotEnoughUnitsTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 1);

        IBoard board = game.getBoard();
        Cell landCell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(cell -> cell.getType() == CellType.LAND)
                .collect(Collectors.toList())
                .get(0);
        Position landPosition = board.getPositionByCell(landCell);

        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(landCell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        List<Cell> controlledCells = ownToCells.get(player);
        controlledCells.add(landCell);

        Pair<Position, List<Unit>> resolution = new Pair<>(landPosition, new ArrayList<>());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellMushroomNotEnoughUnitsTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 1);

        IBoard board = game.getBoard();
        Cell mushroomCell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(cell -> cell.getType() == CellType.MUSHROOM)
                .collect(Collectors.toList())
                .get(0);
        Position mushroomPosition = board.getPositionByCell(mushroomCell);

        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(mushroomCell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        List<Cell> controlledCells = ownToCells.get(player);
        controlledCells.add(mushroomCell);

        Pair<Position, List<Unit>> resolution = new Pair<>(mushroomPosition, new ArrayList<>());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellMountainNotEnoughUnitsTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 1);

        IBoard board = game.getBoard();
        Cell mountainCell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(cell -> cell.getType() == CellType.MOUNTAIN)
                .collect(Collectors.toList())
                .get(0);
        Position mountainPosition = board.getPositionByCell(mountainCell);

        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(mountainCell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        List<Cell> controlledCells = ownToCells.get(player);
        controlledCells.add(mountainCell);

        Pair<Position, List<Unit>> resolution = new Pair<>(mountainPosition, new ArrayList<>());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellWaterNotEnoughUnitsTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        player.setRace(Race.AMPHIBIAN);
        setPlayerUnits(player, 1);

        IBoard board = game.getBoard();
        Cell waterCell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(cell -> cell.getType() == CellType.WATER)
                .collect(Collectors.toList())
                .get(0);
        Position waterPosition = board.getPositionByCell(waterCell);

        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(waterCell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        List<Cell> controlledCells = ownToCells.get(player);
        controlledCells.add(waterCell);

        Pair<Position, List<Unit>> resolution = new Pair<>(waterPosition, new ArrayList<>());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchLandCellSucceedTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 2);

        IBoard board = game.getBoard();
        Cell landCell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(cell -> cell.getType() == CellType.LAND)
                .collect(Collectors.toList())
                .get(0);
        Position landPosition = board.getPositionByCell(landCell);

        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(landCell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        List<Unit> units = new ArrayList<>();
        units.add(new Unit());
        units.add(new Unit());
        Pair<Position, List<Unit>> resolution = new Pair<>(landPosition, units);
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, landCell.getFeudal());
    }

    @Test
    public void catchMountainCellSucceedTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 3);

        IBoard board = game.getBoard();
        Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.MOUNTAIN)
                .collect(Collectors.toList())
                .get(0);
        Position position = board.getPositionByCell(cell);

        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        List<Unit> units = new ArrayList<>();
        units.add(new Unit());
        units.add(new Unit());
        units.add(new Unit());
        Pair<Position, List<Unit>> resolution = new Pair<>(position, units);
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, cell.getFeudal());
    }

    @Test
    public void catchMushroomCellSucceedTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 3);

        IBoard board = game.getBoard();
        Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.MUSHROOM)
                .collect(Collectors.toList())
                .get(0);
        Position position = board.getPositionByCell(cell);

        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        List<Unit> units = new ArrayList<>();
        units.add(new Unit());
        units.add(new Unit());
        Pair<Position, List<Unit>> resolution = new Pair<>(position, units);
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, cell.getFeudal());
    }

    @Test
    public void catchWaterCellByAmphibianSucceedTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        player.setRace(Race.AMPHIBIAN);
        setPlayerUnits(player, 1);

        IBoard board = game.getBoard();
        Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.WATER)
                .collect(Collectors.toList())
                .get(0);
        Position position = board.getPositionByCell(cell);

        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        List<Unit> units = new ArrayList<>();
        units.add(new Unit());
        units.add(new Unit());
        Pair<Position, List<Unit>> resolution = new Pair<>(position, units);
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, cell.getFeudal());
    }

    @Test
    public void catchWaterCellByAmphibianFailedTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        player.setRace(Race.ORC);
        setPlayerUnits(player, 3);

        IBoard board = game.getBoard();
        Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.WATER)
                .collect(Collectors.toList())
                .get(0);
        Position position = board.getPositionByCell(cell);

        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        List<Unit> units = new ArrayList<>();
        units.add(new Unit());
        units.add(new Unit());
        Pair<Position, List<Unit>> resolution = new Pair<>(position, units);
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.CELL_CAPTURE_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchCellByOrc() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        player.setRace(Race.ORC);
        setPlayerUnits(player, 3);

        IBoard board = game.getBoard();
        Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.MUSHROOM)
                .collect(Collectors.toList())
                .get(0);
        Position position = board.getPositionByCell(cell);

        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        List<Unit> units = new ArrayList<>();
        units.add(new Unit());
        Pair<Position, List<Unit>> resolution = new Pair<>(position, units);
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, cell.getFeudal());
    }

    @Test
    public void catchOtherPlayerCell() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 5);

        IBoard board = game.getBoard();
        Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.MUSHROOM)
                .collect(Collectors.toList())
                .get(0);

        Player otherPlayer = game.getPlayers().get(1);
        otherPlayer.setRace(Race.MUSHROOM);
        Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        List<Cell> controlledCells = ownToCells.get(otherPlayer);
        controlledCells.add(cell);

        List<Unit> cellUnits = new ArrayList<>();
        cellUnits.add(new Unit());
        cellUnits.add(new Unit());
        cell.setUnits(cellUnits);

        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        Position position = board.getPositionByCell(cell);

        List<Unit> resolutionUnits = new ArrayList<>();
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        Pair<Position, List<Unit>> resolution = new Pair<>(position, resolutionUnits);
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);

        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, cell.getFeudal());
    }

    @Test
    public void catchGnomeCellByGnome() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.GNOME);
        setPlayerUnits(player, 6);

        IBoard board = game.getBoard();
        Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.MUSHROOM)
                .collect(Collectors.toList())
                .get(0);

        List<Unit> cellUnits = new ArrayList<>();
        cellUnits.add(new Unit());
        cellUnits.add(new Unit());
        cell.setUnits(cellUnits);

        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        Player otherPlayer = game.getPlayers().get(1);
        otherPlayer.setRace(Race.GNOME);
        Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        List<Cell> controlledCells = ownToCells.get(otherPlayer);
        controlledCells.add(cell);

        Position position = board.getPositionByCell(cell);

        List<Unit> resolutionUnits = new ArrayList<>();
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        Pair<Position, List<Unit>> resolution = new Pair<>(position, resolutionUnits);
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);

        GameAnswerProcessor.process(question, catchCellAnswer);
        assertEquals(player, cell.getFeudal());
    }
    
    @Test
    public void catchGnomeCellByGnomeOwnersUnitsDeadTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.GNOME);
        setPlayerUnits(player, 6);

        IBoard board = game.getBoard();
        Cell cell = board.getPositionToCellMap()
                .values()
                .stream()
                .filter(c -> c.getType() == CellType.MUSHROOM)
                .collect(Collectors.toList())
                .get(0);

        List<Unit> cellUnits = new ArrayList<>();
        cellUnits.add(new Unit());
        cellUnits.add(new Unit());
        cell.setUnits(cellUnits);

        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(cell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        Player otherPlayer = game.getPlayers().get(1);
        otherPlayer.setRace(Race.GNOME);
        Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        List<Cell> controlledCells = ownToCells.get(otherPlayer);
        controlledCells.add(cell);
        cell.setRace(Race.GNOME);
        Position position = board.getPositionByCell(cell);
        cell.setFeudal(otherPlayer);
        List<Unit> resolutionUnits = new ArrayList<>();
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        resolutionUnits.add(new Unit());
        Pair<Position, List<Unit>> resolution = new Pair<>(position, resolutionUnits);
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        System.out.println("CONTROLLED CELLS BEFORE: " + controlledCells.size());
        GameAnswerProcessor.process(question, catchCellAnswer);
        System.out.println("CONTROLLED CELLS AFTER: " + controlledCells.size());
        assertTrue(controlledCells.isEmpty());
    }

    private Player getSomePlayer(IGame game) {
        return game.getPlayers().get(0);
    }

    private Position getSomeBoardPosition(BidiMap<Position, Cell> positionCellBidiMap) {
        List<Cell> cells = new ArrayList<>(positionCellBidiMap.values());
        return positionCellBidiMap.getKey(cells.get(0));
    }

    private void setPlayerUnits(Player player, int unitsAmount) {
        List<Unit> playerUnits = new ArrayList<>();
        for (int i = 0; i < unitsAmount; i++) {
            playerUnits.add(new Unit());
        }
        player.getUnitStateToUnits().put(AvailabilityType.AVAILABLE, playerUnits);
    }
}
