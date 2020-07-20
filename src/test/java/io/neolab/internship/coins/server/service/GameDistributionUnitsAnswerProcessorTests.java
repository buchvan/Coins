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
        Map<Position, List<Unit>> resolution = new HashMap<>();
        BidiMap<Position, Cell> positionCellBidiMap = game.getBoard().getPositionToCellMap();
        System.out.println(player);

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
}
