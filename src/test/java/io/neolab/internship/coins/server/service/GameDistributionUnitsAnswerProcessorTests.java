package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.DistributionUnitsAnswer;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.utils.AvailabilityType;
import org.apache.commons.collections4.BidiMap;
import org.junit.Test;

import java.util.*;

import static io.neolab.internship.coins.server.service.GameInitializer.gameInit;
import static org.junit.Assert.*;

public class GameDistributionUnitsAnswerProcessorTests {

    @Test
    public void distributionUnitsNoAvailableUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        game.getOwnToCells().get(player).addAll(Collections.emptyList());
        final PlayerQuestion question = new PlayerQuestion(QuestionType.DISTRIBUTION_UNITS, game, player);
        final Answer answer = new DistributionUnitsAnswer();
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, answer));
        assertEquals(ErrorCode.NO_PLACE_FOR_DISTRIBUTION, exception.getErrorCode());
    }

    @Test
    public void distributionUnitsNotEnoughUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);

        final Player player = getSomePlayer(game);
        setPlayerUnits(player);
        setControlledPlayerCells(game, player);

        final Position somePosition = getSomeBoardPosition(game.getBoard().getPositionToCellMap());
        final List<Unit> unitsForResolutions = new ArrayList<>();
        unitsForResolutions.add(new Unit());
        unitsForResolutions.add(new Unit());
        final Map<Position, List<Unit>> resolution = new HashMap<>();
        resolution.put(somePosition, unitsForResolutions);

        final PlayerQuestion question = new PlayerQuestion(QuestionType.DISTRIBUTION_UNITS, game, player);
        final Answer answer = new DistributionUnitsAnswer(resolution);

        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, answer));
        assertEquals(ErrorCode.NOT_ENOUGH_UNITS, exception.getErrorCode());
    }

    @Test
    public void distributionUnitsWrongPositionTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        setControlledPlayerCells(game, player);
        final Map<Position, List<Unit>> resolution = new HashMap<>();
        resolution.put(new Position(100, 100), Collections.emptyList());
        final PlayerQuestion question = new PlayerQuestion(QuestionType.DISTRIBUTION_UNITS, game, player);
        final Answer answer = new DistributionUnitsAnswer(resolution);
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, answer));
        assertEquals(ErrorCode.WRONG_POSITION, exception.getErrorCode());
    }

    @Test
    public void distributionUnitsRightUnitsInsertionsToCellTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);

        final Player player = getSomePlayer(game);
        setPlayerUnits(player);
        setControlledPlayerCells(game, player);

        final BidiMap<Position, Cell> positionCellBidiMap = game.getBoard().getPositionToCellMap();
        final Position somePosition = getSomeBoardPosition(positionCellBidiMap);
        final List<Unit> unitsForResolutions = new ArrayList<>();
        final Unit someUnit = new Unit();
        unitsForResolutions.add(someUnit);
        final Map<Position, List<Unit>> resolution = new HashMap<>();
        resolution.put(somePosition, unitsForResolutions);

        final PlayerQuestion question = new PlayerQuestion(QuestionType.DISTRIBUTION_UNITS, game, player);
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
        setPlayerUnits(player);
        setControlledPlayerCells(game, player);

        final Position somePosition = getSomeBoardPosition(game.getBoard().getPositionToCellMap());
        final List<Unit> unitsForResolutions = new ArrayList<>();
        final Unit someUnit = new Unit();
        unitsForResolutions.add(someUnit);
        final Map<Position, List<Unit>> resolution = new HashMap<>();
        resolution.put(somePosition, unitsForResolutions);

        final PlayerQuestion question = new PlayerQuestion(QuestionType.DISTRIBUTION_UNITS, game, player);
        final Answer answer = new DistributionUnitsAnswer(resolution);

        GameAnswerProcessor.process(question, answer);

        assertTrue(player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).contains(someUnit));
    }

    private Player getSomePlayer(final IGame game) {
        return game.getPlayers().get(0);
    }

    private void setControlledPlayerCells(final IGame game, final Player player) {
        final List<Cell> controlledCells = new LinkedList<>();
        controlledCells.add(new Cell(CellType.LAND));
        controlledCells.add(new Cell(CellType.WATER));
        game.getOwnToCells().get(player).addAll(controlledCells);
    }

    private void setPlayerUnits(final Player player) {
        final List<Unit> playerUnits = new ArrayList<>();
        playerUnits.add(new Unit());
        player.getUnitStateToUnits().put(AvailabilityType.NOT_AVAILABLE, playerUnits);
    }

    private Position getSomeBoardPosition(final BidiMap<Position, Cell> positionCellBidiMap) {
        final List<Cell> cells = new ArrayList<>(positionCellBidiMap.values());
        return positionCellBidiMap.getKey(cells.get(0));
    }
}
