package io.neolab.internship.coins.server.game.service;

import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.Player;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GameFinalizerTest {

    @Test
    public void testFinalize() throws CoinsException {
        final List<Player> playerList = new LinkedList<>();
        playerList.add(new Player("kvs"));
        playerList.add(new Player("bim"));
        GameFinalizer.finalize(playerList);
    }

    @Test(expected = CoinsException.class)
    public void testFinalizeFail() throws CoinsException {
        GameFinalizer.finalize(null);
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