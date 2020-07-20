package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.DistributionUnitsAnswer;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.utils.AvailabilityType;
import org.apache.commons.collections4.BidiMap;
import org.junit.Test;

import java.util.*;

import static io.neolab.internship.coins.server.game.service.GameInitializer.gameInit;
import static org.junit.Assert.*;

public class GameDistributionUnitsAnswerProcessorTests {

    private final GameAnswerProcessor gameAnswerProcessor = new GameAnswerProcessor();

    @Test
    public void distributionUnitsNoAvailableUnitsTest() throws CoinsException {
        IGame game = gameInit(2, 2, 2);
        List<Player> players = game.getPlayers();
        Player player = players.get(0);
        game.getOwnToCells().get(player).addAll(Collections.emptyList());
        PlayerQuestion question = new PlayerQuestion(QuestionType.DISTRIBUTION_UNITS, game, player);
        Answer answer = new DistributionUnitsAnswer();
        CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, answer));
        assertEquals(ErrorCode.NO_PLACE_FOR_DISTRIBUTION, exception.getErrorCode());
    }

    @Test
    public void distributionUnitsNotEnoughUnitsTest() throws CoinsException {
        IGame game = gameInit(2,2, 2);

        List<Player> players = game.getPlayers();
        Player player = players.get(0);
        List<Unit> playerUnits = new ArrayList<>();
        playerUnits.add(new Unit());
        player.getUnitStateToUnits().put(AvailabilityType.NOT_AVAILABLE, playerUnits);

        List<Cell> controlledCells = new LinkedList<>();
        controlledCells.add(new Cell(CellType.LAND));
        controlledCells.add(new Cell(CellType.WATER));
        game.getOwnToCells().get(player).addAll(controlledCells);

        Map<Position, List<Unit>> resolution = new HashMap<>();
        BidiMap<Position, Cell> positionCellBidiMap = game.getBoard().getPositionToCellMap();
        List<Cell> cells = new ArrayList<>(positionCellBidiMap.values());
        Position somePosition = positionCellBidiMap.getKey(cells.get(0));

        List<Unit> unitsForResolutions = new ArrayList<>();
        unitsForResolutions.add(new Unit());
        unitsForResolutions.add(new Unit());

        resolution.put(somePosition, unitsForResolutions);
        PlayerQuestion question = new PlayerQuestion(QuestionType.DISTRIBUTION_UNITS, game, player);
        Answer answer = new DistributionUnitsAnswer(resolution);

        CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, answer));
        assertEquals(ErrorCode.NOT_ENOUGH_UNITS, exception.getErrorCode());
    }

    @Test
    public void distributionUnitsWrongPositionTest() throws CoinsException {
        IGame game = gameInit(2,2, 2);
        List<Player> players = game.getPlayers();
        List<Cell> controlledCells = new LinkedList<>();
        controlledCells.add(new Cell(CellType.LAND));
        controlledCells.add(new Cell(CellType.WATER));
        Player player = players.get(0);
        game.getOwnToCells().get(player).addAll(controlledCells);
        Map<Position, List<Unit>> resolution = new HashMap<>();
        resolution.put(new Position(100, 100), new ArrayList<>());
        PlayerQuestion question = new PlayerQuestion(QuestionType.DISTRIBUTION_UNITS, game, player);
        Answer answer = new DistributionUnitsAnswer(resolution);
        CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, answer));
        assertEquals(ErrorCode.WRONG_POSITION, exception.getErrorCode());
    }

    @Test
    public void distributionUnitsRightUnitsInsertionsToCellTest() throws CoinsException {
        IGame game = gameInit(2,2, 2);

        List<Player> players = game.getPlayers();
        Player player = players.get(0);
        List<Unit> playerUnits = new ArrayList<>();
        playerUnits.add(new Unit());
        player.getUnitStateToUnits().put(AvailabilityType.NOT_AVAILABLE, playerUnits);

        List<Cell> controlledCells = new LinkedList<>();
        controlledCells.add(new Cell(CellType.LAND));
        controlledCells.add(new Cell(CellType.WATER));
        game.getOwnToCells().get(player).addAll(controlledCells);

        Map<Position, List<Unit>> resolution = new HashMap<>();
        BidiMap<Position, Cell> positionCellBidiMap = game.getBoard().getPositionToCellMap();
        List<Cell> cells = new ArrayList<>(positionCellBidiMap.values());
        Position somePosition = positionCellBidiMap.getKey(cells.get(0));

        List<Unit> unitsForResolutions = new ArrayList<>();
        Unit someUnit = new Unit();
        unitsForResolutions.add(someUnit);

        resolution.put(somePosition, unitsForResolutions);
        PlayerQuestion question = new PlayerQuestion(QuestionType.DISTRIBUTION_UNITS, game, player);
        Answer answer = new DistributionUnitsAnswer(resolution);

        GameAnswerProcessor.process(question, answer);
        assertEquals(1, positionCellBidiMap.get(somePosition).getUnits().size());
        assertEquals(someUnit.getId(), positionCellBidiMap.get(somePosition).getUnits().get(0).getId());
    }

    @Test
    public void distributionUnitsNewUnitsBecomeNotUnavailable() throws CoinsException {
        IGame game = gameInit(2,2, 2);

        List<Player> players = game.getPlayers();
        Player player = players.get(0);
        List<Unit> playerUnits = new ArrayList<>();
        playerUnits.add(new Unit());
        player.getUnitStateToUnits().put(AvailabilityType.NOT_AVAILABLE, playerUnits);

        List<Cell> controlledCells = new LinkedList<>();
        controlledCells.add(new Cell(CellType.LAND));
        controlledCells.add(new Cell(CellType.WATER));
        game.getOwnToCells().get(player).addAll(controlledCells);

        Map<Position, List<Unit>> resolution = new HashMap<>();
        BidiMap<Position, Cell> positionCellBidiMap = game.getBoard().getPositionToCellMap();
        List<Cell> cells = new ArrayList<>(positionCellBidiMap.values());
        Position somePosition = positionCellBidiMap.getKey(cells.get(0));

        List<Unit> unitsForResolutions = new ArrayList<>();
        Unit someUnit = new Unit();
        unitsForResolutions.add(someUnit);

        resolution.put(somePosition, unitsForResolutions);
        PlayerQuestion question = new PlayerQuestion(QuestionType.DISTRIBUTION_UNITS, game, player);
        Answer answer = new DistributionUnitsAnswer(resolution);

        GameAnswerProcessor.process(question, answer);

        assertTrue( player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).contains(someUnit));
    }
}
