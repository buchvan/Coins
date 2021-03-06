package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.TestUtils;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.player.Race;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.Arrays;

import static org.junit.Assert.*;

public class GameInitializerTest extends TestUtils {
    @BeforeClass
    public static void before() {
        MDC.put("logFileName", testFileName);
    }

    @Test
    public void gameInit() throws CoinsException {
        final IGame game = GameInitializer.gameInit(2, 2, 2);
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
