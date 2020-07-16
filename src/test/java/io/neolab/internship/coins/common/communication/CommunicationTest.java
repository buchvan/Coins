package io.neolab.internship.coins.common.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.common.Communication;
import io.neolab.internship.coins.common.answer.CatchCellAnswer;
import io.neolab.internship.coins.common.answer.ChooseRaceAnswer;
import io.neolab.internship.coins.common.answer.DeclineRaceAnswer;
import io.neolab.internship.coins.common.answer.DistributionUnitsAnswer;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.service.GameInitializer;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CommunicationTest {
    @Test
    public void testEquivalentDefaultGame() throws CoinsException, JsonProcessingException {
        final IGame expected = GameInitializer.gameInit(3, 4);
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final IGame actual = mapper.readValue(json, Game.class);
        assertEquals(expected.getBoard(), actual.getBoard());
        assertEquals(expected.getCurrentRound(), actual.getCurrentRound());
        assertEquals(expected.getFeudalToCells(), actual.getFeudalToCells());
        assertEquals(expected.getOwnToCells(), actual.getOwnToCells());
        assertEquals(expected.getPlayerToTransitCells(), actual.getPlayerToTransitCells());
        assertEquals(expected.getGameFeatures(), actual.getGameFeatures());
        assertEquals(expected.getRacesPool(), actual.getRacesPool());
        assertEquals(expected.getPlayers(), actual.getPlayers());
    }

    @Test
    public void testEquivalentCustomGame() throws CoinsException, JsonProcessingException {
        final IGame expected = GameInitializer.gameInit(3, 4);

        expected.getPlayerToTransitCells().forEach((player, cells) -> cells.add(new Cell(CellType.MUSHROOM)));
        expected.getPlayerToTransitCells().forEach((player, cells) -> cells.add(new Cell(CellType.LAND)));
        expected.getOwnToCells().forEach((player, cells) -> cells.add(new Cell(CellType.WATER)));
        expected.getOwnToCells().forEach((player, cells) -> cells.add(new Cell(CellType.MOUNTAIN)));
        expected.getPlayerToTransitCells().forEach((player, cells) -> cells.add(new Cell(CellType.WATER)));
        expected.getPlayerToTransitCells().forEach((player, cells) -> cells.add(new Cell(CellType.MOUNTAIN)));
        expected.getFeudalToCells().forEach((player, cells) -> cells.add(new Cell(CellType.WATER)));
        expected.getFeudalToCells().forEach((player, cells) -> cells.add(new Cell(CellType.LAND)));
        expected.getPlayers().forEach(player -> player.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit()));

        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final IGame actual = mapper.readValue(json, Game.class);
        assertEquals(expected.getBoard(), actual.getBoard());
        assertEquals(expected.getCurrentRound(), actual.getCurrentRound());
        assertEquals(expected.getFeudalToCells(), actual.getFeudalToCells());
        assertEquals(expected.getOwnToCells(), actual.getOwnToCells());
        assertEquals(expected.getPlayerToTransitCells(), actual.getPlayerToTransitCells());
        assertEquals(expected.getGameFeatures(), actual.getGameFeatures());
        assertEquals(expected.getRacesPool(), actual.getRacesPool());
        assertEquals(expected.getPlayers(), actual.getPlayers());
    }

    @Test
    public void testEquivalentCustomPlayer() throws JsonProcessingException {
        final Player expected = new Player("kvs");
        expected.setRace(Race.ELF);
        expected.setCoins(12);
        expected.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());
        expected.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());

        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final Player actual = mapper.readValue(json, Player.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentQuestionType() throws JsonProcessingException {
        final QuestionType expected = QuestionType.CATCH_CELL;
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final QuestionType actual = mapper.readValue(json, QuestionType.class);
        assertEquals(expected, actual);
    }

    private Question getTestQuestion() throws CoinsException {
        final IGame game = GameInitializer.gameInit(3, 4);

        game.getPlayerToTransitCells().forEach((player, cells) -> cells.add(new Cell(CellType.MUSHROOM)));
        game.getPlayerToTransitCells().forEach((player, cells) -> cells.add(new Cell(CellType.LAND)));
        game.getOwnToCells().forEach((player, cells) -> cells.add(new Cell(CellType.WATER)));
        game.getOwnToCells().forEach((player, cells) -> cells.add(new Cell(CellType.MOUNTAIN)));
        game.getPlayerToTransitCells().forEach((player, cells) -> cells.add(new Cell(CellType.WATER)));
        game.getPlayerToTransitCells().forEach((player, cells) -> cells.add(new Cell(CellType.MOUNTAIN)));
        game.getFeudalToCells().forEach((player, cells) -> cells.add(new Cell(CellType.WATER)));
        game.getFeudalToCells().forEach((player, cells) -> cells.add(new Cell(CellType.LAND)));
        game.getPlayers().forEach(player -> player.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit()));

        final Player player = new Player("kvs");
        player.setRace(Race.ELF);
        player.setCoins(12);
        player.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());
        player.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());

        final QuestionType questionType = QuestionType.CATCH_CELL;

        return new Question(questionType, game, player);
    }

    @Test
    public void testEquivalentQuestion() throws CoinsException, JsonProcessingException {
        final Question expected = getTestQuestion();
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final Question actual = mapper.readValue(json, Question.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentQuestionCommunication1() throws CoinsException, JsonProcessingException {
        final Question expected = getTestQuestion();
        final ObjectMapper mapper = new ObjectMapper();
        final String json = Communication.serializeQuestion(expected);
        final Question actual = mapper.readValue(json, Question.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentQuestionCommunication2() throws CoinsException, JsonProcessingException {
        final Question expected = getTestQuestion();
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final Question actual = Communication.deserializeQuestion(json);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentQuestionCommunication3() throws CoinsException, JsonProcessingException {
        final Question expected = getTestQuestion();
        final String json = Communication.serializeQuestion(expected);
        final Question actual = Communication.deserializeQuestion(json);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentCatchCellAnswerCommunication1() throws JsonProcessingException {
        final Pair<Position, List<Unit>> pair = new Pair<>(new Position(), new LinkedList<>());
        pair.getSecond().add(new Unit());
        pair.getSecond().add(new Unit());
        pair.getSecond().add(new Unit());
        final CatchCellAnswer expected = new CatchCellAnswer(pair);
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final CatchCellAnswer actual = mapper.readValue(json, CatchCellAnswer.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentCatchCellAnswerCommunication2() throws JsonProcessingException {
        final Pair<Position, List<Unit>> pair = new Pair<>(new Position(), new LinkedList<>());
        pair.getSecond().add(new Unit());
        pair.getSecond().add(new Unit());
        pair.getSecond().add(new Unit());
        final CatchCellAnswer expected = new CatchCellAnswer(pair);
        final String json = Communication.serializeAnswer(expected);
        final CatchCellAnswer actual = Communication.deserializeCatchCellAnswer(json);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentChooseRaceAnswerCommunication1() throws JsonProcessingException {
        final ChooseRaceAnswer expected = new ChooseRaceAnswer(Race.ELF);
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final ChooseRaceAnswer actual = mapper.readValue(json, ChooseRaceAnswer.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentChooseRaceAnswerCommunication2() throws JsonProcessingException {
        final ChooseRaceAnswer expected = new ChooseRaceAnswer(Race.ELF);
        final String json = Communication.serializeAnswer(expected);
        final ChooseRaceAnswer actual = Communication.deserializeChooseRaceAnswer(json);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentDeclineRaceAnswerCommunication1() throws JsonProcessingException {
        final DeclineRaceAnswer expected = new DeclineRaceAnswer(true);
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final DeclineRaceAnswer actual = mapper.readValue(json, DeclineRaceAnswer.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentDeclineRaceAnswerCommunication2() throws JsonProcessingException {
        final DeclineRaceAnswer expected = new DeclineRaceAnswer(true);
        final String json = Communication.serializeAnswer(expected);
        final DeclineRaceAnswer actual = Communication.deserializeDeclineRaceAnswer(json);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentDistributionUnitsAnswerCommunication1() throws JsonProcessingException {
        final Map<Position, List<Unit>> map = new HashMap<>(3);
        final List<Unit> units1 = new LinkedList<>();
        units1.add(new Unit());
        map.put(new Position(), units1);
        final List<Unit> units2 = new LinkedList<>();
        units1.add(new Unit());
        units1.add(new Unit());
        map.put(new Position(2, 3), units2);
        map.put(new Position(2, 3), new LinkedList<>());
        final DistributionUnitsAnswer expected = new DistributionUnitsAnswer(map);

        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final DistributionUnitsAnswer actual = mapper.readValue(json, DistributionUnitsAnswer.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentDistributionUnitsAnswerCommunication2() throws JsonProcessingException {
        final Map<Position, List<Unit>> map = new HashMap<>(3);
        final List<Unit> units1 = new LinkedList<>();
        units1.add(new Unit());
        map.put(new Position(), units1);
        final List<Unit> units2 = new LinkedList<>();
        units1.add(new Unit());
        units1.add(new Unit());
        map.put(new Position(2, 3), units2);
        map.put(new Position(2, 3), new LinkedList<>());
        final DistributionUnitsAnswer expected = new DistributionUnitsAnswer(map);

        final String json = Communication.serializeAnswer(expected);
        final DistributionUnitsAnswer actual = Communication.deserializeDistributionUnitsAnswer(json);
        assertEquals(expected, actual);
    }

}