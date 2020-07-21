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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class GameCatchCellAnswerProcessorTests {
    @Test
    public void catchCellWrongPositionTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        Pair<Position, List<Unit>> resolution  = new Pair<>(new Position(100, 100), Collections.emptyList());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                ()-> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.WRONG_POSITION, exception.getErrorCode());
    }

    @Test
    public void catchCellNoCellForCatching() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        Pair<Position, List<Unit>> resolution  = new Pair<>(new Position(1, 1), Collections.emptyList());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                ()-> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.NO_ACHIEVABLE_CELL, exception.getErrorCode());
    }

    @Test
    public void catchCellNoUnitsForCatching() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);

        Position somePosition = getSomeBoardPosition(game.getBoard().getPositionToCellMap());
        Cell someCell = game.getBoard().getCellByPosition(somePosition);
        Set<Cell> achievableCells = new HashSet<>();
        achievableCells.add(someCell);
        game.getPlayerToAchievableCells().put(player, achievableCells);

        Pair<Position, List<Unit>> resolution  = new Pair<>(somePosition, Collections.emptyList());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                ()-> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.NO_AVAILABLE_UNITS, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellLandNotEnoughUnits() throws CoinsException {
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

        Pair<Position, List<Unit>> resolution  = new Pair<>(landPosition, new ArrayList<>());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                ()-> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellMushroomNotEnoughUnits() throws CoinsException {
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

        Pair<Position, List<Unit>> resolution  = new Pair<>(mushroomPosition, new ArrayList<>());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                ()-> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellMountainNotEnoughUnits() throws CoinsException {
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

        Pair<Position, List<Unit>> resolution  = new Pair<>(mountainPosition, new ArrayList<>());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                ()-> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellWaterNotEnoughUnits() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
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

        Pair<Position, List<Unit>> resolution  = new Pair<>(waterPosition, new ArrayList<>());
        PlayerQuestion question = new PlayerQuestion(QuestionType.CATCH_CELL, game, player);
        Answer catchCellAnswer = new CatchCellAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                ()-> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(ErrorCode.ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
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
        for(int i =0; i < unitsAmount; i++) {
            playerUnits.add(new Unit());
        }
        player.getUnitStateToUnits().put(AvailabilityType.AVAILABLE, playerUnits);
    }
}
