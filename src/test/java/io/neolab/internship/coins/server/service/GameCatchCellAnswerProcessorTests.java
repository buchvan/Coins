package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.message.client.answer.Answer;
import io.neolab.internship.coins.common.message.client.answer.CatchCellAnswer;
import io.neolab.internship.coins.common.message.server.question.PlayerQuestion;
import io.neolab.internship.coins.common.message.server.question.PlayerQuestionType;
import io.neolab.internship.coins.common.message.server.ServerMessageType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.neolab.internship.coins.server.service.GameInitializer.gameInit;
import static org.junit.Assert.*;

public class GameCatchCellAnswerProcessorTests extends GameAnswerProcessorTests {
    @BeforeClass
    public static void before() {
        MDC.put("logFileName", testFileName);
    }

    @Test
    public void catchCellWrongPositionTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(
                new Pair<>(new Position(100, 100), Collections.emptyList()));

        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_WRONG_POSITION, exception.getErrorCode());
    }

    @Test
    public void catchCellNoCellForCatchingTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(
                new Pair<>(new Position(1, 1), Collections.emptyList()));

        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_UNREACHABLE_CELL, exception.getErrorCode());
    }

    @Test
    public void catchCellNoUnitsForCatchingTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);

        final IBoard board = game.getBoard();
        final Position somePosition = getSomeBoardPosition(board.getPositionToCellMap());
        final Cell someCell = board.getCellByPosition(somePosition);
        Objects.requireNonNull(someCell).setFeudal(player);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(someCell));

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(new Pair<>(somePosition, Collections.emptyList()));

        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_NO_AVAILABLE_UNITS, exception.getErrorCode());
    }

    @Test
    public void catchCellCaptureImpossibleTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 1, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell landCell = getCellFromBoardByCellType(CellType.LAND, board);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(landCell));

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(new Pair<>(board.getPositionByCell(landCell),
                player.getUnitsByState(AvailabilityType.AVAILABLE)));

        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_CELL_CAPTURE_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellLandNotEnoughUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 1, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell landCell = getCellFromBoardByCellType(CellType.LAND, board);
        landCell.setFeudal(player);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(landCell));
        setCellAsControlled(landCell, game, player);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(
                new Pair<>(board.getPositionByCell(landCell), Collections.emptyList()));

        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellMushroomNotEnoughUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 1, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell mushroomCell = getCellFromBoardByCellType(CellType.MUSHROOM, board);
        mushroomCell.setFeudal(player);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(mushroomCell));
        setCellAsControlled(mushroomCell, game, player);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(
                new Pair<>(board.getPositionByCell(mushroomCell), Collections.emptyList()));

        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellMountainNotEnoughUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 1, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell mountainCell = getCellFromBoardByCellType(CellType.MOUNTAIN, board);
        mountainCell.setFeudal(player);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(mountainCell));
        setCellAsControlled(mountainCell, game, player);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(
                new Pair<>(board.getPositionByCell(mountainCell), Collections.emptyList()));

        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchControlledCellWaterNotEnoughUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.AMPHIBIAN);
        setPlayerUnits(player, 1, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell waterCell = getCellFromBoardByCellType(CellType.WATER, board);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(waterCell));
        waterCell.setFeudal(player);
        setCellAsControlled(waterCell, game, player);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        final Answer catchCellAnswer = new CatchCellAnswer(
                new Pair<>(board.getPositionByCell(waterCell), Collections.emptyList()));

        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, catchCellAnswer));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_ENTER_CELL_IMPOSSIBLE, exception.getErrorCode());
    }

    @Test
    public void catchLandCellSucceedTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 2, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell landCell = getCellFromBoardByCellType(CellType.LAND, board);
        landCell.setFeudal(player);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(landCell));

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        GameAnswerProcessor.process(question,
                createCatchCellAnswer(board.getPositionByCell(landCell), player, 2));
        assertEquals(player, landCell.getFeudal());
        assertTrue(game.getFeudalToCells().get(player).contains(landCell));
        assertFalse(game.getPlayerToTransitCells().get(player).contains(landCell));
        assertTrue(game.getOwnToCells().get(player).contains(landCell));
    }

    @Test
    public void catchMountainCellSucceedTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 3, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell cell = getCellFromBoardByCellType(CellType.MOUNTAIN, board);
        cell.setFeudal(player);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(cell));

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        GameAnswerProcessor.process(question,
                createCatchCellAnswer(board.getPositionByCell(cell), player, 3));
        assertEquals(player, cell.getFeudal());
        assertTrue(game.getFeudalToCells().get(player).contains(cell));
        assertFalse(game.getPlayerToTransitCells().get(player).contains(cell));
        assertTrue(game.getOwnToCells().get(player).contains(cell));
    }

    @Test
    public void catchMushroomCellSucceedTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 3, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell cell = getCellFromBoardByCellType(CellType.MUSHROOM, board);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(cell));
        cell.setFeudal(player);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        GameAnswerProcessor.process(question,
                createCatchCellAnswer(board.getPositionByCell(cell), player, 2));
        assertEquals(player, cell.getFeudal());
        assertTrue(game.getFeudalToCells().get(player).contains(cell));
        assertFalse(game.getPlayerToTransitCells().get(player).contains(cell));
        assertTrue(game.getOwnToCells().get(player).contains(cell));
    }

    @Test
    public void catchWaterCellByAmphibianSucceedTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.AMPHIBIAN);
        setPlayerUnits(player, 1, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell cell = getCellFromBoardByCellType(CellType.WATER, board);
        cell.setFeudal(player);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(cell));

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        GameAnswerProcessor.process(question,
                createCatchCellAnswer(board.getPositionByCell(cell), player, 2));
        assertEquals(player, cell.getFeudal());
        assertTrue(game.getFeudalToCells().get(player).contains(cell));
        assertFalse(game.getPlayerToTransitCells().get(player).contains(cell));
        assertTrue(game.getOwnToCells().get(player).contains(cell));
    }

    @Test
    public void catchWaterCellNotByAmphibianTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ORC);
        setPlayerUnits(player, 3, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell cell = getCellFromBoardByCellType(CellType.WATER, board);
        cell.setFeudal(player);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(cell));

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        GameAnswerProcessor.process(question,
                createCatchCellAnswer(board.getPositionByCell(cell), player, 2));
        assertNull(cell.getFeudal());
        assertFalse(game.getFeudalToCells().get(player).contains(cell));
        assertTrue(game.getPlayerToTransitCells().get(player).contains(cell));
        assertTrue(game.getOwnToCells().get(player).contains(cell));

    }

    @Test
    public void catchCellByOrc() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ORC);
        setPlayerUnits(player, 3, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell cell = getCellFromBoardByCellType(CellType.MUSHROOM, board);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(cell));

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        GameAnswerProcessor.process(question,
                createCatchCellAnswer(board.getPositionByCell(cell), player, 1));
        assertEquals(player, cell.getFeudal());
        assertTrue(game.getFeudalToCells().get(player).contains(cell));
        assertFalse(game.getPlayerToTransitCells().get(player).contains(cell));
        assertTrue(game.getOwnToCells().get(player).contains(cell));
    }

    @Test
    public void catchOtherPlayerCell() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = game.getPlayers().get(0);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 5, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell cell = getCellFromBoardByCellType(CellType.MUSHROOM, board);
        final Player otherPlayer = game.getPlayers().get(1);
        otherPlayer.setRace(Race.MUSHROOM);
        setCellAsControlled(cell, game, otherPlayer);
        cell.setFeudal(otherPlayer);
        setUnitToCell(cell, 2);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(cell));

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        GameAnswerProcessor.process(question,
                createCatchCellAnswer(board.getPositionByCell(cell), player, 5));
        assertEquals(player, cell.getFeudal());
        assertTrue(game.getFeudalToCells().get(player).contains(cell));
        assertFalse(game.getPlayerToTransitCells().get(player).contains(cell));
        assertTrue(game.getOwnToCells().get(player).contains(cell));
    }

    @Test
    public void catchGnomeCellByGnome() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = game.getPlayers().get(0);
        player.setRace(Race.GNOME);
        setPlayerUnits(player, 6, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell cell = getCellFromBoardByCellType(CellType.MUSHROOM, board);
        setUnitToCell(cell, 2);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(cell));

        final Player otherPlayer = game.getPlayers().get(1);
        otherPlayer.setRace(Race.GNOME);
        setCellAsControlled(cell, game, otherPlayer);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        GameAnswerProcessor.process(question,
                createCatchCellAnswer(board.getPositionByCell(cell), player, 6));
        assertEquals(player, cell.getFeudal());
        assertTrue(game.getFeudalToCells().get(player).contains(cell));
        assertFalse(game.getPlayerToTransitCells().get(player).contains(cell));
        assertTrue(game.getOwnToCells().get(player).contains(cell));
    }

    @Test
    public void catchGnomeCellByGnomeOwnersTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = game.getPlayers().get(0);
        player.setRace(Race.GNOME);
        setPlayerUnits(player, 6, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell cell = getCellFromBoardByCellType(CellType.MUSHROOM, board);
        setUnitToCell(cell, 2);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(cell));

        final Player otherPlayer = game.getPlayers().get(1);
        otherPlayer.setRace(Race.GNOME);
        final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        final List<Cell> controlledCells = ownToCells.get(otherPlayer);
        controlledCells.add(cell);
        cell.setRace(Race.GNOME);
        cell.setFeudal(otherPlayer);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        GameAnswerProcessor.process(question,
                createCatchCellAnswer(board.getPositionByCell(cell), player, 6));
        assertTrue(controlledCells.isEmpty());
    }

    @Test
    public void catchCellCheckOpponentUnitsDeadTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = game.getPlayers().get(0);
        player.setRace(Race.GNOME);
        setPlayerUnits(player, 6, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell cell = getCellFromBoardByCellType(CellType.MUSHROOM, board);
        setUnitToCell(cell, 2);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(cell));

        final Player otherPlayer = game.getPlayers().get(1);
        otherPlayer.setRace(Race.GNOME);
        setCellAsControlled(cell, game, otherPlayer);
        cell.setRace(Race.GNOME);
        cell.setFeudal(otherPlayer);
        setPlayerUnits(otherPlayer, 2, AvailabilityType.NOT_AVAILABLE);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        GameAnswerProcessor.process(question,
                createCatchCellAnswer(board.getPositionByCell(cell), player, 6));
        assertEquals(player, cell.getFeudal());
        assertEquals(1, otherPlayer.getUnitsByState(AvailabilityType.NOT_AVAILABLE).size());
    }

    @Test
    public void catchNeutralCellSucceedTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 5, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell landCell = getCellFromBoardByCellType(CellType.LAND, board);
        setUnitToCell(landCell, 2);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(landCell));
        landCell.setRace(Race.MUSHROOM);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        GameAnswerProcessor.process(question,
                createCatchCellAnswer(board.getPositionByCell(landCell), player, 5));
        assertEquals(player, landCell.getFeudal());
        assertTrue(game.getFeudalToCells().get(player).contains(landCell));
        assertFalse(game.getPlayerToTransitCells().get(player).contains(landCell));
        assertTrue(game.getOwnToCells().get(player).contains(landCell));
    }

    @Test
    public void catchCellByUnitsFromFarCells() throws CoinsException {
        final IGame game = gameInit(3, 3, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 2, AvailabilityType.NOT_AVAILABLE);

        final Position from = new Position(0, 0);
        final Cell cell = Objects.requireNonNull(game.getBoard().getCellByPosition(from));
        setUnitToCell(cell, 2);
        cell.setFeudal(player);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        final CoinsException coinsException = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question,
                        createCatchCellAnswer(new Position(2, 2), player, 1)));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_UNREACHABLE_CELL, coinsException.getErrorCode());
    }

    @Test
    public void enterToCellTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 2, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell landCell = getCellFromBoardByCellType(CellType.LAND, board);
        landCell.setFeudal(player);

        game.getOwnToCells().get(player).add(landCell);
        game.getFeudalToCells().get(player).add(landCell);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(landCell));

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        GameAnswerProcessor.process(question,
                createCatchCellAnswer(board.getPositionByCell(landCell), player,3));
        assertNull(landCell.getFeudal());
        assertFalse(game.getFeudalToCells().get(player).contains(landCell));
        assertFalse(game.getPlayerToTransitCells().get(player).contains(landCell));
        assertFalse(game.getOwnToCells().get(player).contains(landCell));
        assertTrue(player.getUnitsByState(AvailabilityType.AVAILABLE).isEmpty());
        assertEquals(2, player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).size());
    }

    @Test
    public void enterToCellWithUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.UNDEAD);
        setPlayerUnits(player, 3, AvailabilityType.AVAILABLE);

        final IBoard board = game.getBoard();
        final Cell landCell = getCellFromBoardByCellType(CellType.LAND, board);
        landCell.setFeudal(player);
        landCell.getUnits().add(player.getUnitsByState(AvailabilityType.AVAILABLE).get(0));

        game.getOwnToCells().get(player).add(landCell);
        game.getFeudalToCells().get(player).add(landCell);
        game.getPlayerToAchievableCells().put(player, getAchievableCellSet(landCell));

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question,
                        createCatchCellAnswer(board.getPositionByCell(landCell), player,3)));
        assertEquals(CoinsErrorCode.ANSWER_VALIDATION_ENTER_CELL_INVALID_UNITS, exception.getErrorCode());
    }
}
