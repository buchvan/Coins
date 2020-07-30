package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.message.client.answer.Answer;
import io.neolab.internship.coins.common.message.client.answer.DeclineRaceAnswer;
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static io.neolab.internship.coins.server.service.GameInitializer.gameInit;
import static org.junit.Assert.*;

public class GameDeclineRaceAnswerProcessorTests extends GameAnswerProcessorTests {
    @BeforeClass
    public static void before() {
        MDC.put("logFileName", testFileName);
    }

    @Test
    public void emptyAnswerTest() throws CoinsException {
        final List<Cell> controlledCells = new LinkedList<>();
        controlledCells.add(new Cell(CellType.LAND));
        controlledCells.add(new Cell(CellType.WATER));
        final IGame game = gameInit(2, 2, 2);
        final List<Player> players = game.getPlayers();
        final Player player = players.get(0);
        game.getOwnToCells().get(player).addAll(controlledCells);
        final PlayerQuestion PlayerQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DECLINE_RACE, game, player);
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(PlayerQuestion, null));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_ERROR_EMPTY_ANSWER, exception.getErrorCode());
    }

    @Test
    public void declineRaceTrueRightControlledCellsTest() throws CoinsException {
        final List<Cell> controlledCells = new LinkedList<>();
        controlledCells.add(new Cell(CellType.LAND));
        controlledCells.add(new Cell(CellType.WATER));
        final IGame game = gameInit(2, 2, 2);
        final Player declineRacePlayer = getSomePlayer(game);
        game.getOwnToCells().get(declineRacePlayer).addAll(controlledCells);
        final PlayerQuestion PlayerQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DECLINE_RACE, game, declineRacePlayer);
        final Answer answer = new DeclineRaceAnswer(true);
        GameAnswerProcessor.process(PlayerQuestion, answer);
        assertEquals(0, game.getOwnToCells().get(declineRacePlayer).size());
    }

    @Test
    public void declineRaceTrueRightFeudalCellsOwnersTest() throws CoinsException {
        final List<Cell> feudalCells = new LinkedList<>();
        final Cell feudalCell = new Cell(CellType.MOUNTAIN);
        final Cell feudalCell1 = new Cell(CellType.MUSHROOM);
        feudalCells.add(feudalCell);
        feudalCells.add(feudalCell1);
        final IGame game = gameInit(2, 2, 2);
        final Player declineRacePlayer = getSomePlayer(game);
        feudalCell.setFeudal(declineRacePlayer);
        feudalCell1.setFeudal(declineRacePlayer);
        game.getFeudalToCells().get(declineRacePlayer).addAll(feudalCells);
        final PlayerQuestion PlayerQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DECLINE_RACE, game, declineRacePlayer);
        final Answer answer = new DeclineRaceAnswer(true);
        GameAnswerProcessor.process(PlayerQuestion, answer);
        feudalCells.forEach(cell -> assertNotNull(cell.getFeudal()));
    }

    @Test
    public void declineRaceTrueFeudalCellsTest() throws CoinsException {
        final List<Cell> feudalCells = new LinkedList<>();
        final Cell feudalCell = new Cell(CellType.MOUNTAIN);
        final Cell feudalCell1 = new Cell(CellType.MUSHROOM);
        feudalCells.add(feudalCell);
        feudalCells.add(feudalCell1);
        final IGame game = gameInit(2, 2, 2);
        final Player declineRacePlayer = getSomePlayer(game);
        feudalCell.setFeudal(declineRacePlayer);
        feudalCell1.setFeudal(declineRacePlayer);
        game.getFeudalToCells().get(declineRacePlayer).addAll(feudalCells);
        final PlayerQuestion PlayerQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DECLINE_RACE, game, declineRacePlayer);
        final Answer answer = new DeclineRaceAnswer(true);
        GameAnswerProcessor.process(PlayerQuestion, answer);
        assertTrue(feudalCells.contains(feudalCell));
        assertTrue(feudalCells.contains(feudalCell1));
        assertEquals(2, feudalCells.size());
    }

    @Test
    public void declineRaceFalseRightControlledCellsTest() throws CoinsException {
        final List<Cell> controlledCells = new LinkedList<>();
        controlledCells.add(new Cell(CellType.LAND));
        controlledCells.add(new Cell(CellType.WATER));
        final IGame game = gameInit(2, 2, 2);
        final Player declineRacePlayer = getSomePlayer(game);
        game.getOwnToCells().get(declineRacePlayer).addAll(controlledCells);
        final PlayerQuestion PlayerQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DECLINE_RACE, game, declineRacePlayer);
        final Answer answer = new DeclineRaceAnswer(false);
        GameAnswerProcessor.process(PlayerQuestion, answer);
        assertEquals(2, game.getOwnToCells().get(declineRacePlayer).size());
    }

    @Test
    public void declineRaceFalseRightFeudalCellsOwnersTest() throws CoinsException {
        final List<Cell> feudalCells = new LinkedList<>();
        final Cell feudalCell = new Cell(CellType.MOUNTAIN);
        final Cell feudalCell1 = new Cell(CellType.MUSHROOM);
        feudalCells.add(feudalCell);
        feudalCells.add(feudalCell1);
        final IGame game = gameInit(2, 2, 2);
        final Player declineRacePlayer = getSomePlayer(game);
        feudalCell.setFeudal(declineRacePlayer);
        feudalCell1.setFeudal(declineRacePlayer);
        game.getFeudalToCells().get(declineRacePlayer).addAll(feudalCells);
        final PlayerQuestion PlayerQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DECLINE_RACE, game, declineRacePlayer);
        final Answer answer = new DeclineRaceAnswer(false);
        GameAnswerProcessor.process(PlayerQuestion, answer);
        feudalCells.forEach(cell -> assertNotNull(cell.getFeudal()));
    }

    @Test
    public void declineRaceFalseFeudalCellsTest() throws CoinsException {
        final List<Cell> feudalCells = new LinkedList<>();
        final Cell feudalCell = new Cell(CellType.MOUNTAIN);
        final Cell feudalCell1 = new Cell(CellType.MUSHROOM);
        feudalCells.add(feudalCell);
        feudalCells.add(feudalCell1);
        final IGame game = gameInit(2, 2, 2);
        final Player declineRacePlayer = getSomePlayer(game);
        feudalCell.setFeudal(declineRacePlayer);
        feudalCell1.setFeudal(declineRacePlayer);
        game.getFeudalToCells().get(declineRacePlayer).addAll(feudalCells);
        final PlayerQuestion PlayerQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DECLINE_RACE, game, declineRacePlayer);
        final Answer answer = new DeclineRaceAnswer(false);
        GameAnswerProcessor.process(PlayerQuestion, answer);
        assertTrue(feudalCells.contains(feudalCell));
        assertTrue(feudalCells.contains(feudalCell1));
        assertEquals(2, feudalCells.size());
    }

    @Test
    public void declineRaceTrueSavedUnitsCellsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);

        final Position somePosition = getSomeBoardPosition(game.getBoard().getPositionToCellMap());
        final Cell someCellByPosition = game.getBoard().getCellByPosition(somePosition);
        final List<Unit> cellUnits = new ArrayList<>();
        final Unit someUnit1 = new Unit();
        final Unit someUnit2 = new Unit();
        cellUnits.add(someUnit1);
        cellUnits.add(someUnit2);
        Objects.requireNonNull(someCellByPosition).getUnits().addAll(cellUnits);

        final List<Cell> feudalCells = new LinkedList<>();
        feudalCells.add(someCellByPosition);
        final Player declineRacePlayer = getSomePlayer(game);
        someCellByPosition.setFeudal(declineRacePlayer);
        game.getFeudalToCells().get(declineRacePlayer).addAll(feudalCells);

        final PlayerQuestion PlayerQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DECLINE_RACE, game, declineRacePlayer);
        final Answer answer = new DeclineRaceAnswer(true);
        GameAnswerProcessor.process(PlayerQuestion, answer);

        final List<Unit> cellUnitsAfterDeclining = someCellByPosition.getUnits();
        assertTrue(cellUnitsAfterDeclining.contains(someUnit1));
        assertTrue(cellUnitsAfterDeclining.contains(someUnit2));
        assertEquals(2, cellUnitsAfterDeclining.size());
    }
}
