package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.DeclineRaceAnswer;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static io.neolab.internship.coins.server.game.service.GameInitializer.gameInit;
import static org.junit.Assert.*;

public class GameDeclineRaceAnswerProcessorTests {

    private final GameAnswerProcessor gameAnswerProcessor = new GameAnswerProcessor();

    @Test
    public void emptyAnswerTest() {
        Question question = new Question(QuestionType.DECLINE_RACE, new Game(), new Player("test"));
        CoinsException exception = assertThrows(CoinsException.class,
                () -> gameAnswerProcessor.process(question, null));
        assertEquals(ErrorCode.EMPTY_ANSWER, exception.getErrorCode());
    }

    @Test
    public void declineRaceTrueRightControlledCells() throws CoinsException {
        List<Cell> controlledCells = new LinkedList<>();
        controlledCells.add(new Cell(CellType.LAND));
        controlledCells.add(new Cell(CellType.WATER));
        IGame game = gameInit(2,2);
        List<Player> players = game.getPlayers();
        Player declineRacePlayer = players.get(0);
        game.getOwnToCells().get(declineRacePlayer).addAll(controlledCells);
        Question question = new Question(QuestionType.DECLINE_RACE, game, declineRacePlayer);
                Answer answer = new DeclineRaceAnswer(true);
        gameAnswerProcessor.process(question, answer);
       assertEquals(0, game.getOwnToCells().get(declineRacePlayer).size());
    }

    @Test
    public void declineRaceTrueRightFeudalCells() throws CoinsException {
        List<Cell> feudalCells = new LinkedList<>();
        Cell feudalCell = new Cell(CellType.MOUNTAIN);
        Cell feudalCell1 = new Cell(CellType.MUSHROOM);
        feudalCells.add(feudalCell);
        feudalCells.add(feudalCell1);
        IGame game = gameInit(2,2);
        List<Player> players = game.getPlayers();
        Player declineRacePlayer = players.get(0);
        feudalCell.setFeudal(declineRacePlayer);
        feudalCell1.setFeudal(declineRacePlayer);
        game.getFeudalToCells().get(declineRacePlayer).addAll(feudalCells);
        Question question = new Question(QuestionType.DECLINE_RACE, game, declineRacePlayer);
        Answer answer = new DeclineRaceAnswer(true);
        gameAnswerProcessor.process(question, answer);
        feudalCells.forEach(cell -> assertNull(cell.getOwn()));
    }

    @Test
    public void declineRaceFalseRightControlledCells() throws CoinsException {
        List<Cell> controlledCells = new LinkedList<>();
        controlledCells.add(new Cell(CellType.LAND));
        controlledCells.add(new Cell(CellType.WATER));
        IGame game = gameInit(2,2);
        List<Player> players = game.getPlayers();
        Player declineRacePlayer = players.get(0);
        game.getOwnToCells().get(declineRacePlayer).addAll(controlledCells);
        Question question = new Question(QuestionType.DECLINE_RACE, game, declineRacePlayer);
        Answer answer = new DeclineRaceAnswer(false);
        gameAnswerProcessor.process(question, answer);
        assertEquals(2, game.getOwnToCells().get(declineRacePlayer).size());
    }

    @Test
    public void declineRaceFalseRightFeudalCells() throws CoinsException {
        List<Cell> feudalCells = new LinkedList<>();
        Cell feudalCell = new Cell(CellType.MOUNTAIN);
        Cell feudalCell1 = new Cell(CellType.MUSHROOM);
        feudalCells.add(feudalCell);
        feudalCells.add(feudalCell1);
        IGame game = gameInit(2,2);
        List<Player> players = game.getPlayers();
        Player declineRacePlayer = players.get(0);
        feudalCell.setFeudal(declineRacePlayer);
        feudalCell1.setFeudal(declineRacePlayer);
        game.getFeudalToCells().get(declineRacePlayer).addAll(feudalCells);
        Question question = new Question(QuestionType.DECLINE_RACE, game, declineRacePlayer);
        Answer answer = new DeclineRaceAnswer(false);
        gameAnswerProcessor.process(question, answer);
        feudalCells.forEach(cell -> assertNull(cell.getOwn()));
    }
}
