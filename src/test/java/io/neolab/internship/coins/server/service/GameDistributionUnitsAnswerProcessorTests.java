package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.message.client.answer.Answer;
import io.neolab.internship.coins.common.message.client.answer.DistributionUnitsAnswer;
import io.neolab.internship.coins.common.message.server.question.PlayerQuestion;
import io.neolab.internship.coins.common.message.server.question.PlayerQuestionType;
import io.neolab.internship.coins.common.message.server.ServerMessageType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.utils.AvailabilityType;
import org.apache.commons.collections4.BidiMap;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.*;

import static io.neolab.internship.coins.server.service.GameInitializer.gameInit;
import static org.junit.Assert.*;

public class GameDistributionUnitsAnswerProcessorTests extends GameAnswerProcessorTests {

    @Test
    public void distributionUnitsNoAvailableUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        game.getOwnToCells().get(player).addAll(Collections.emptyList());
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DISTRIBUTION_UNITS, game, player);
        final Answer answer = new DistributionUnitsAnswer(new HashMap<>());
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, answer));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_NO_PLACE_FOR_DISTRIBUTION, exception.getErrorCode());
    }

    @Test
    public void distributionUnitsNotEnoughUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);

        final Player player = getSomePlayer(game);
        setPlayerUnits(player, 1, AvailabilityType.NOT_AVAILABLE);
        setControlledPlayerCells(game, player);

        final Position somePosition = getSomeBoardPosition(game.getBoard().getPositionToCellMap());
        final List<Unit> unitsForResolutions = new ArrayList<>();
        unitsForResolutions.add(new Unit());
        unitsForResolutions.add(new Unit());
        final Map<Position, List<Unit>> resolution = new HashMap<>();
        resolution.put(somePosition, unitsForResolutions);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DISTRIBUTION_UNITS, game, player);
        final Answer answer = new DistributionUnitsAnswer(resolution);

        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, answer));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_NOT_ENOUGH_UNITS, exception.getErrorCode());
    }

    @Test
    public void distributionUnitsWrongPositionTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        setControlledPlayerCells(game, player);
        final Map<Position, List<Unit>> resolution = new HashMap<>();
        resolution.put(new Position(100, 100), Collections.emptyList());
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DISTRIBUTION_UNITS, game, player);
        final Answer answer = new DistributionUnitsAnswer(resolution);
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, answer));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_WRONG_POSITION, exception.getErrorCode());
    }

    @Test
    public void distributionUnitsRightUnitsInsertionsToCellTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);

        final Player player = getSomePlayer(game);
        setPlayerUnits(player, 1, AvailabilityType.NOT_AVAILABLE);
        setControlledPlayerCells(game, player);

        final BidiMap<Position, Cell> positionCellBidiMap = game.getBoard().getPositionToCellMap();
        final Position somePosition = getSomeBoardPosition(positionCellBidiMap);
        final List<Unit> unitsForResolutions = new ArrayList<>();
        final Unit someUnit = new Unit();
        unitsForResolutions.add(someUnit);
        final Map<Position, List<Unit>> resolution = new HashMap<>();
        resolution.put(somePosition, unitsForResolutions);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DISTRIBUTION_UNITS, game, player);
        final Answer answer = new DistributionUnitsAnswer(resolution);

        GameAnswerProcessor.process(question, answer);
        final List<Unit> positionCells = positionCellBidiMap.get(somePosition).getUnits();
        assertEquals(1, positionCells.size());
        assertEquals(someUnit, positionCells.get(0));
    }

    @Test
    public void distributionUnitsNewUnitsBecomeNotUnavailable() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);

        final Player player = getSomePlayer(game);
        setPlayerUnits(player, 1, AvailabilityType.NOT_AVAILABLE);
        setControlledPlayerCells(game, player);

        final Position somePosition = getSomeBoardPosition(game.getBoard().getPositionToCellMap());
        final List<Unit> unitsForResolutions = new ArrayList<>();
        final Unit someUnit = new Unit();
        unitsForResolutions.add(someUnit);
        final Map<Position, List<Unit>> resolution = new HashMap<>();
        resolution.put(somePosition, unitsForResolutions);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DISTRIBUTION_UNITS, game, player);
        final Answer answer = new DistributionUnitsAnswer(resolution);

        GameAnswerProcessor.process(question, answer);

        assertTrue(player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).contains(someUnit));
    }

    @Test
    public void testDistributionUnitsCellEmptyNotOwn() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);

        final Player player = getSomePlayer(game);
        setPlayerUnits(player, 1, AvailabilityType.AVAILABLE);
        setControlledPlayerCells(game, player);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DISTRIBUTION_UNITS, game, player);
        final Answer answer = new DistributionUnitsAnswer(new HashMap<>());

        GameAnswerProcessor.process(question, answer);

        assertTrue(game.getOwnToCells().get(player).isEmpty());
        assertTrue(game.getFeudalToCells().get(player).isEmpty());
    }

    @Test
    public void testDistributionUnitsCellEmptyNotFeudal() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);

        final Player player = getSomePlayer(game);
        setPlayerUnits(player, 1, AvailabilityType.AVAILABLE);
        setControlledPlayerCells(game, player);
        setFeudalPlayerCells(game, player, game.getOwnToCells().get(player));

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DISTRIBUTION_UNITS, game, player);
        final Answer answer = new DistributionUnitsAnswer(new HashMap<>());

        GameAnswerProcessor.process(question, answer);

        assertTrue(game.getOwnToCells().get(player).isEmpty());
        assertTrue(game.getFeudalToCells().get(player).isEmpty());
    }

    private void setControlledPlayerCells(final @NotNull IGame game, final @NotNull Player player) {
        final List<Cell> controlledCells = new LinkedList<>();
        controlledCells.add(new Cell(CellType.LAND));
        controlledCells.add(new Cell(CellType.WATER));
        game.getOwnToCells().get(player).addAll(controlledCells);
    }

    private void setFeudalPlayerCells(final @NotNull IGame game, final @NotNull Player player,
                                      final List<Cell> controlledCells) {
        controlledCells.forEach(cell -> cell.setFeudal(player));
        game.getFeudalToCells().get(player).addAll(controlledCells);
    }
}
