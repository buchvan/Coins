package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.CatchCellAnswer;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.utils.Pair;
import org.apache.commons.collections4.BidiMap;
import org.junit.Test;

import java.util.*;

import static io.neolab.internship.coins.server.game.service.GameInitializer.gameInit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class GamCatchCellAnswerProcessorTests {
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

    private Player getSomePlayer(IGame game) {
        return game.getPlayers().get(0);
    }

    private Position getSomeBoardPosition(BidiMap<Position, Cell> positionCellBidiMap) {
        List<Cell> cells = new ArrayList<>(positionCellBidiMap.values());
        return positionCellBidiMap.getKey(cells.get(0));
    }
}
