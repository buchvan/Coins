package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.ChangeRaceAnswer;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.utils.AvailabilityType;
import org.junit.Test;

import java.util.List;

import static io.neolab.internship.coins.server.game.service.GameInitializer.gameInit;
import static org.junit.Assert.*;

public class GameChangeRaceAnswerProcessorTests {

    private final GameAnswerProcessor gameAnswerProcessor = new GameAnswerProcessor();

    @Test
    public void changeRaceUnavailableNewRaceTest() throws CoinsException {
        IGame game = gameInit(2,2);
        game.getRacesPool().remove(Race.ELF);
        Question question = new Question(QuestionType.CHANGE_RACE, game, game.getPlayers().get(0));
        Answer answer = new ChangeRaceAnswer(Race.ELF);
        CoinsException exception = assertThrows(CoinsException.class,
                () -> gameAnswerProcessor.process(question, answer));
        assertEquals(ErrorCode.UNAVAILABLE_NEW_RACE, exception.getErrorCode());
    }

    @Test
    public void changeRaceUnavailableNewRaceDeletedFromPoolTest() throws CoinsException {
        IGame game = gameInit(2,2);
        game.getRacesPool().remove(Race.ELF);
        Question question = new Question(QuestionType.CHANGE_RACE, game, game.getPlayers().get(0));
        Answer answer = new ChangeRaceAnswer(Race.MUSHROOM);
        gameAnswerProcessor.process(question, answer);
        assertFalse(game.getRacesPool().contains(Race.MUSHROOM));
    }

    @Test
    public void changeRaceOldRaceReturnsTest() throws CoinsException {
        IGame game = gameInit(2,2);
        List<Race> races = game.getRacesPool();
        races.remove(Race.ELF);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.ELF);
        Question question = new Question(QuestionType.CHANGE_RACE, game, player);
        Answer answer = new ChangeRaceAnswer(Race.AMPHIBIAN);
        gameAnswerProcessor.process(question, answer);
        assertTrue(races.contains(Race.ELF));
    }

    @Test
    public void changeRaceRightNewRacesTest() throws CoinsException {
        IGame game = gameInit(2,2);
        List<Race> races = game.getRacesPool();
        races.remove(Race.ELF);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.ELF);
        Question question = new Question(QuestionType.CHANGE_RACE, game, player);
        Answer answer = new ChangeRaceAnswer(Race.AMPHIBIAN);
        gameAnswerProcessor.process(question, answer);
        assertSame(player.getRace(), Race.AMPHIBIAN);
    }

    @Test
    public void changeRaceAmphibianRightAvailableUnitsTest() throws CoinsException {
        IGame game = gameInit(2,2);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.ELF);
        Question question = new Question(QuestionType.CHANGE_RACE, game, player);
        Answer answer = new ChangeRaceAnswer(Race.AMPHIBIAN);
        gameAnswerProcessor.process(question, answer);
        assertEquals(Race.AMPHIBIAN.getUnitsAmount(),
                player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).size());
    }

    @Test
    public void changeRaceAmphibianRightUnavailableUnitsTest() throws CoinsException {
        IGame game = gameInit(2,2);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.ELF);
        Question question = new Question(QuestionType.CHANGE_RACE, game, player);
        Answer answer = new ChangeRaceAnswer(Race.AMPHIBIAN);
        gameAnswerProcessor.process(question, answer);
        assertEquals(0, player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).size());
    }


    @Test
    public void changeRaceElfRightAvailableUnitsTest() throws CoinsException {
        IGame game = gameInit(2,2);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.MUSHROOM);
        Question question = new Question(QuestionType.CHANGE_RACE, game, player);
        Answer answer = new ChangeRaceAnswer(Race.ELF);
        gameAnswerProcessor.process(question, answer);
        assertEquals(Race.ELF.getUnitsAmount(), player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).size());
    }

    @Test
    public void changeRaceElfRightUnavailableUnitsTest() throws CoinsException {
        IGame game = gameInit(2,2);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.MUSHROOM);
        Question question = new Question(QuestionType.CHANGE_RACE, game, player);
        Answer answer = new ChangeRaceAnswer(Race.ELF);
        gameAnswerProcessor.process(question, answer);
        assertEquals(0, player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).size());
    }

    @Test
    public void changeRaceOrcRightAvailableUnitsTest() throws CoinsException {
        IGame game = gameInit(2,2);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.ELF);
        Question question = new Question(QuestionType.CHANGE_RACE, game, player);
        Answer answer = new ChangeRaceAnswer(Race.ORC);
        gameAnswerProcessor.process(question, answer);
        assertEquals(Race.ORC.getUnitsAmount(), player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).size());
    }

    @Test
    public void changeRaceOrcRightUnavailableUnitsTest() throws CoinsException {
        IGame game = gameInit(2,2);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.ELF);
        Question question = new Question(QuestionType.CHANGE_RACE, game, player);
        Answer answer = new ChangeRaceAnswer(Race.ORC);
        gameAnswerProcessor.process(question, answer);
        assertEquals(0, player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).size());
    }

    @Test
    public void changeRaceGnomeRightAvailableUnitsTest() throws CoinsException {
        IGame game = gameInit(2,2);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.ELF);
        Question question = new Question(QuestionType.CHANGE_RACE, game, player);
        Answer answer = new ChangeRaceAnswer(Race.GNOME);
        gameAnswerProcessor.process(question, answer);
        assertEquals(Race.GNOME.getUnitsAmount(), player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).size());
    }

    @Test
    public void changeRaceGnomeRightUnavailableUnitsTest() throws CoinsException {
        IGame game = gameInit(2,2);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.ELF);
        Question question = new Question(QuestionType.CHANGE_RACE, game, player);
        Answer answer = new ChangeRaceAnswer(Race.GNOME);
        gameAnswerProcessor.process(question, answer);
        assertEquals(0, player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).size());
    }

    @Test
    public void changeRaceUndeadRightAvailableUnitsTest() throws CoinsException {
        IGame game = gameInit(2,2);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.ELF);
        Question question = new Question(QuestionType.CHANGE_RACE, game, player);
        Answer answer = new ChangeRaceAnswer(Race.UNDEAD);
        gameAnswerProcessor.process(question, answer);
        assertEquals(Race.UNDEAD.getUnitsAmount(), player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).size());
    }

    @Test
    public void changeRaceUndeadRightUnavailableUnitsTest() throws CoinsException {
        IGame game = gameInit(2,2);
        Player player = game.getPlayers().get(0);
        player.setRace(Race.ELF);
        Question question = new Question(QuestionType.CHANGE_RACE, game, player);
        Answer answer = new ChangeRaceAnswer(Race.UNDEAD);
        gameAnswerProcessor.process(question, answer);
        assertEquals(0, player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).size());
    }
}
