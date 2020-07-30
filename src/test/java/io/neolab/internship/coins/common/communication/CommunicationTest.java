package io.neolab.internship.coins.common.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.TestUtils;
import io.neolab.internship.coins.common.serialization.Communication;
import io.neolab.internship.coins.common.message.client.answer.CatchCellAnswer;
import io.neolab.internship.coins.common.message.client.answer.ChangeRaceAnswer;
import io.neolab.internship.coins.common.message.client.answer.DeclineRaceAnswer;
import io.neolab.internship.coins.common.message.client.answer.DistributionUnitsAnswer;
import io.neolab.internship.coins.common.message.server.GameOverMessage;
import io.neolab.internship.coins.common.message.server.question.PlayerQuestion;
import io.neolab.internship.coins.common.message.server.question.PlayerQuestionType;
import io.neolab.internship.coins.common.message.server.ServerMessageType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.service.GameInitializer;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CommunicationTest extends TestUtils {
    @BeforeClass
    public static void before() {
        MDC.put("logFileName", testFileName);
    }

    @Test
    public void testEquivalentDefaultGame() throws CoinsException, JsonProcessingException {
        final IGame expected = GameInitializer.gameInit(3, 4, 2);
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
        final IGame expected = GameInitializer.gameInit(3, 4, 2);

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
        final PlayerQuestionType expected = PlayerQuestionType.CATCH_CELL;
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final PlayerQuestionType actual = mapper.readValue(json, PlayerQuestionType.class);
        assertEquals(expected, actual);
    }

    private @NotNull PlayerQuestion getTestPlayerQuestion() throws CoinsException {
        final IGame game = GameInitializer.gameInit(3, 4, 2);

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

        final PlayerQuestionType playerQuestionType = PlayerQuestionType.CATCH_CELL;

        return new PlayerQuestion(ServerMessageType.GAME_QUESTION, playerQuestionType, game, player);
    }

    @Test
    public void testEquivalentPlayerQuestion() throws JsonProcessingException, CoinsException {
        final PlayerQuestion expected = getTestPlayerQuestion();
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final PlayerQuestion actual = mapper.readValue(json, PlayerQuestion.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentPlayerQuestionCommunication1() throws CoinsException, JsonProcessingException {
        final PlayerQuestion expected = getTestPlayerQuestion();
        final ObjectMapper mapper = new ObjectMapper();
        final String json = Communication.serializeServerMessage(expected);
        final PlayerQuestion actual = mapper.readValue(json, PlayerQuestion.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentPlayerQuestionCommunication2() throws CoinsException, JsonProcessingException {
        final PlayerQuestion expected = getTestPlayerQuestion();
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final PlayerQuestion actual = (PlayerQuestion) Communication.deserializeServerMessage(json);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentPlayerQuestionCommunication3() throws CoinsException, JsonProcessingException {
        final PlayerQuestion expected = getTestPlayerQuestion();
        final String json = Communication.serializeServerMessage(expected);
        final PlayerQuestion actual = (PlayerQuestion) Communication.deserializeServerMessage(json);
        assertEquals(expected, actual);
    }

    private GameOverMessage getTestGameOverQuestion() {
        final List<Player> playerList = new LinkedList<>();

        final Player kvs = new Player("kvs");
        kvs.setRace(Race.ELF);
        kvs.setCoins(12);
        kvs.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());
        kvs.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());
        playerList.add(kvs);

        final Player bim = new Player("bim");
        kvs.setRace(Race.UNDEAD);
        kvs.setCoins(5);
        kvs.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());
        kvs.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());
        playerList.add(bim);

        final ServerMessageType serverMessageType = ServerMessageType.GAME_OVER;
        final List<Player> winners = new LinkedList<>();
        winners.add(kvs);

        return new GameOverMessage(serverMessageType, winners, playerList);
    }

    @Test
    public void testEquivalentGameOverQuestion() throws JsonProcessingException {
        final GameOverMessage expected = getTestGameOverQuestion();
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final GameOverMessage actual = mapper.readValue(json, GameOverMessage.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentGameOverQuestionCommunication1() throws JsonProcessingException {
        final GameOverMessage expected = getTestGameOverQuestion();
        final ObjectMapper mapper = new ObjectMapper();
        final String json = Communication.serializeServerMessage(expected);
        final GameOverMessage actual = mapper.readValue(json, GameOverMessage.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentGameOverQuestionCommunication2() throws JsonProcessingException {
        final GameOverMessage expected = getTestGameOverQuestion();
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final GameOverMessage actual = (GameOverMessage) Communication.deserializeServerMessage(json);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentGameOverQuestionCommunication3() throws JsonProcessingException {
        final GameOverMessage expected = getTestGameOverQuestion();
        final String json = Communication.serializeServerMessage(expected);
        final GameOverMessage actual = (GameOverMessage) Communication.deserializeServerMessage(json);
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
        final String json = Communication.serializeClientMessage(expected);
        final CatchCellAnswer actual = (CatchCellAnswer) Communication.deserializeClientMessage(json);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentChangeRaceAnswerCommunication1() throws JsonProcessingException {
        final ChangeRaceAnswer expected = new ChangeRaceAnswer(Race.ELF);
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);
        final ChangeRaceAnswer actual = mapper.readValue(json, ChangeRaceAnswer.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testEquivalentChangeRaceAnswerCommunication2() throws JsonProcessingException {
        final ChangeRaceAnswer expected = new ChangeRaceAnswer(Race.ELF);
        final String json = Communication.serializeClientMessage(expected);
        final ChangeRaceAnswer actual = (ChangeRaceAnswer) Communication.deserializeClientMessage(json);
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
        final String json = Communication.serializeClientMessage(expected);
        final DeclineRaceAnswer actual = (DeclineRaceAnswer) Communication.deserializeClientMessage(json);
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

        final String json = Communication.serializeClientMessage(expected);
        final DistributionUnitsAnswer actual = (DistributionUnitsAnswer) Communication.deserializeClientMessage(json);
        assertEquals(expected, actual);
    }

}
