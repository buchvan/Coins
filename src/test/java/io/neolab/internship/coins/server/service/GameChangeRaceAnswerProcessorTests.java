package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.ChangeRaceAnswer;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.question.PlayerQuestionType;
import io.neolab.internship.coins.common.question.ServerMessageType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.utils.AvailabilityType;
import org.junit.Test;

import java.util.List;

import static io.neolab.internship.coins.server.game.service.GameInitializer.gameInit;
import static io.neolab.internship.coins.server.service.TestUtils.getSomePlayer;
import static org.junit.Assert.*;

public class GameChangeRaceAnswerProcessorTests {

    @Test
    public void changeRaceUnavailableNewRaceTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        game.getRacesPool().remove(Race.ELF);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, game.getPlayers().get(0));
        final Answer answer = new ChangeRaceAnswer(Race.ELF);
        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, answer));
        assertEquals(ErrorCode.ANSWER_VALIDATION_UNAVAILABLE_NEW_RACE, exception.getErrorCode());
    }

    @Test
    public void changeRaceUnavailableNewRaceDeletedFromPoolTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        game.getRacesPool().remove(Race.ELF);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, game.getPlayers().get(0));
        final Answer answer = new ChangeRaceAnswer(Race.MUSHROOM);
        GameAnswerProcessor.process(question, answer);
        assertFalse(game.getRacesPool().contains(Race.MUSHROOM));
    }

    @Test
    public void changeRaceOldRaceReturnsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final List<Race> races = game.getRacesPool();
        races.remove(Race.ELF);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ELF);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, player);
        final Answer answer = new ChangeRaceAnswer(Race.AMPHIBIAN);
        GameAnswerProcessor.process(question, answer);
        assertTrue(races.contains(Race.ELF));
    }

    @Test
    public void changeRaceRightNewRacesTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final List<Race> races = game.getRacesPool();
        races.remove(Race.ELF);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ELF);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, player);
        final Answer answer = new ChangeRaceAnswer(Race.AMPHIBIAN);
        GameAnswerProcessor.process(question, answer);
        assertSame(player.getRace(), Race.AMPHIBIAN);
    }

    @Test
    public void changeRaceAmphibianRightAvailableUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ELF);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, player);
        final Answer answer = new ChangeRaceAnswer(Race.AMPHIBIAN);
        GameAnswerProcessor.process(question, answer);
        assertEquals(Race.AMPHIBIAN.getUnitsAmount(),
                player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).size());
    }

    @Test
    public void changeRaceAmphibianRightUnavailableUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ELF);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, player);
        final Answer answer = new ChangeRaceAnswer(Race.AMPHIBIAN);
        GameAnswerProcessor.process(question, answer);
        assertEquals(0, player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).size());
    }


    @Test
    public void changeRaceElfRightAvailableUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.MUSHROOM);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, player);
        final Answer answer = new ChangeRaceAnswer(Race.ELF);
        GameAnswerProcessor.process(question, answer);
        assertEquals(Race.ELF.getUnitsAmount(), player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).size());
    }

    @Test
    public void changeRaceElfRightUnavailableUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.MUSHROOM);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, player);
        final Answer answer = new ChangeRaceAnswer(Race.ELF);
        GameAnswerProcessor.process(question, answer);
        assertEquals(0, player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).size());
    }

    @Test
    public void changeRaceOrcRightAvailableUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ELF);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, player);
        final Answer answer = new ChangeRaceAnswer(Race.ORC);
        GameAnswerProcessor.process(question, answer);
        assertEquals(Race.ORC.getUnitsAmount(), player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).size());
    }

    @Test
    public void changeRaceOrcRightUnavailableUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ELF);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, player);
        final Answer answer = new ChangeRaceAnswer(Race.ORC);
        GameAnswerProcessor.process(question, answer);
        assertEquals(0, player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).size());
    }

    @Test
    public void changeRaceGnomeRightAvailableUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ELF);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, player);
        final Answer answer = new ChangeRaceAnswer(Race.GNOME);
        GameAnswerProcessor.process(question, answer);
        assertEquals(Race.GNOME.getUnitsAmount(), player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).size());
    }

    @Test
    public void changeRaceGnomeRightUnavailableUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ELF);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, player);
        final Answer answer = new ChangeRaceAnswer(Race.GNOME);
        GameAnswerProcessor.process(question, answer);
        assertEquals(0, player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).size());
    }

    @Test
    public void changeRaceUndeadRightAvailableUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ELF);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, player);
        final Answer answer = new ChangeRaceAnswer(Race.UNDEAD);
        GameAnswerProcessor.process(question, answer);
        assertEquals(Race.UNDEAD.getUnitsAmount(), player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).size());
    }

    @Test
    public void changeRaceUndeadRightUnavailableUnitsTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);
        player.setRace(Race.ELF);
        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, player);
        final Answer answer = new ChangeRaceAnswer(Race.UNDEAD);
        GameAnswerProcessor.process(question, answer);
        assertEquals(0, player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).size());
    }
}
