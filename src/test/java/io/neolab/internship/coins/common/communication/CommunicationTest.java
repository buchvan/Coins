package io.neolab.internship.coins.common.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.service.GameInitializer;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommunicationTest {
    @Test
    public void testEquivalent() throws CoinsException, JsonProcessingException {
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
//        assertEquals(expected, actual);
    }
}