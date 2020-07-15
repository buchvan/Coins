package io.neolab.internship.coins.common.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.service.GameInitializer;
import io.neolab.internship.coins.utils.AvailabilityType;
import org.junit.Test;

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
    public void testEquivalentCustomPlayer() throws CoinsException, JsonProcessingException {
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

    // TODO тест с (де)сериализацией вопроса
}