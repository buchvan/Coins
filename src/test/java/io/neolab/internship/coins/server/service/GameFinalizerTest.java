package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.TestUtils;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.service.GameFinalizer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GameFinalizerTest extends TestUtils {
    @BeforeClass
    public static void before() {
        MDC.put("logFileName", testFileName);
    }

    @Test
    public void testFinalize() throws CoinsException {
        final List<Player> playerList = new LinkedList<>();
        playerList.add(new Player("kvs"));
        playerList.add(new Player("bim"));
        GameFinalizer.finalization(playerList);
    }

    @Test(expected = CoinsException.class)
    public void testFinalizeFail() throws CoinsException {
        GameFinalizer.finalization(null);
    }

    @Test
    public void testGetMaxCoinsCount1() throws CoinsException {
        final List<Player> playerList = new LinkedList<>();
        final Player kvs = new Player("kvs");
        final Player bim = new Player("bim");
        kvs.setCoins(45);
        bim.setCoins(42);
        playerList.add(kvs);
        playerList.add(bim);

        assertEquals(45, GameFinalizer.getMaxCoinsCount(playerList));
    }

    @Test
    public void testGetMaxCoinsCount2() throws CoinsException {
        final List<Player> playerList = new LinkedList<>();
        final Player kvs = new Player("kvs");
        final Player bim = new Player("bim");
        kvs.setCoins(45);
        bim.setCoins(45);
        playerList.add(kvs);
        playerList.add(bim);

        assertEquals(45, GameFinalizer.getMaxCoinsCount(playerList));
    }

    @Test
    public void testGetMaxCoinsCount3() throws CoinsException {
        final List<Player> playerList = new LinkedList<>();
        final Player kvs = new Player("kvs");
        final Player bim = new Player("bim");
        kvs.setCoins(45);
        bim.setCoins(49);
        playerList.add(kvs);
        playerList.add(bim);

        assertEquals(49, GameFinalizer.getMaxCoinsCount(playerList));
    }

    @Test(expected = CoinsException.class)
    public void testGetMaxCoinsCountFail() throws CoinsException {
        GameFinalizer.getMaxCoinsCount(null);
    }

    @Test
    public void testGetWinners1() throws CoinsException {
        final List<Player> playerList = new LinkedList<>();
        final Player kvs = new Player("kvs");
        final Player bim = new Player("bim");
        kvs.setCoins(45);
        bim.setCoins(42);
        playerList.add(kvs);
        playerList.add(bim);

        final List<Player> expected = new LinkedList<>();
        expected.add(kvs);
        assertEquals(expected, GameFinalizer.getWinners(45, playerList));
    }

    @Test
    public void testGetWinners2() throws CoinsException {
        final List<Player> playerList = new LinkedList<>();
        final Player kvs = new Player("kvs");
        final Player bim = new Player("bim");
        kvs.setCoins(45);
        bim.setCoins(45);
        playerList.add(kvs);
        playerList.add(bim);

        assertEquals(playerList, GameFinalizer.getWinners(45, playerList));
    }

    @Test
    public void testGetWinners3() throws CoinsException {
        final List<Player> playerList = new LinkedList<>();
        final Player kvs = new Player("kvs");
        final Player bim = new Player("bim");
        kvs.setCoins(45);
        bim.setCoins(49);
        playerList.add(kvs);
        playerList.add(bim);

        final List<Player> expected = new LinkedList<>();
        expected.add(bim);
        assertEquals(expected, GameFinalizer.getWinners(49, playerList));
    }

    @Test(expected = CoinsException.class)
    public void testGetWinnersFail() throws CoinsException {
        GameFinalizer.getWinners(3, null);
    }
}
