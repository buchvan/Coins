package io.neolab.internship.coins.server.game.service;

import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.GameFeatures;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.server.game.board.Cell;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class GameInitializerTest {

    @Test
    public void gameInit() throws CoinsException {
        final Game game = GameInitializer.gameInit(2, 2, 2);
        assertNotNull(game);
        assertNotNull(game.getBoard()); // Тестируется в BoardFactoryTest.class
        game.getPlayers().forEach(player -> {
            assertNotNull(player.getNickname());
            assertNotNull(player.getUnitStateToUnits());
            assertNull(player.getRace());
        });
        game.getFeudalToCells().forEach((feudal, cells) -> {
            assertNotNull(feudal);
            assertTrue(game.getPlayers().contains(feudal));
            assertNotNull(cells);
            assertTrue(cells.isEmpty());
        });
        game.getOwnToCells().forEach((feudal, cells) -> {
            assertNotNull(feudal);
            assertTrue(game.getPlayers().contains(feudal));
            assertNotNull(cells);
            assertTrue(cells.isEmpty());
        });
        game.getPlayerToTransitCells().forEach((feudal, cells) -> {
            assertNotNull(feudal);
            assertTrue(game.getPlayers().contains(feudal));
            assertNotNull(cells);
            assertTrue(cells.isEmpty());
        });
        game.getGameFeatures().getRaceCellTypeFeatures().forEach((raceCellTypePair, features) -> {
            assertNotNull(raceCellTypePair.getFirst());
            assertNotNull(raceCellTypePair.getSecond());
            assertNotNull(features);
            assertFalse(features.isEmpty());
        });
        game.getRacesPool().forEach(Assert::assertNotNull);
        assertTrue(Arrays.asList(Race.values()).containsAll(game.getRacesPool()));
    }

    @Test(expected = CoinsException.class)
    public void gameInitFail1() throws CoinsException {
        GameInitializer.gameInit(0, 0, 2);
    }

    @Test(expected = CoinsException.class)
    public void gameInitFail2() throws CoinsException {
        GameInitializer.gameInit(2, 1, 2);
    }
}
