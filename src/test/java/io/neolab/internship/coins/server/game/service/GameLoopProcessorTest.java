package io.neolab.internship.coins.server.game.service;

import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.service.TestUtils;
import org.junit.Test;

import java.util.Set;

import static io.neolab.internship.coins.server.game.service.GameInitializer.gameInit;
import static org.junit.Assert.assertEquals;

public class GameLoopProcessorTest {

    /*
     * каждый захваченный регион с грибами приносит в конце хода призовую монетку расе грибов
     */
    @Test
    public void updateCoinsCountWithMushroomFeatures() throws CoinsException {
        IGame game = gameInit(2,2,2);
        Player somePlayer = TestUtils.getSomePlayer(game);
        somePlayer.setRace(Race.MUSHROOM);
        somePlayer.setCoins(0);
        Set<Cell> playerFeudalCells = game.getFeudalToCells().get(somePlayer);
        playerFeudalCells.add(new Cell(CellType.MUSHROOM));

        GameLoopProcessor.updateCoinsCount(somePlayer, playerFeudalCells, game.getGameFeatures(), game.getBoard());
        assertEquals(2, somePlayer.getCoins());
    }

    /*
     *  каждая отдельная группа регионов приносит одну дополнительную монетку расе эльфов
     */
    @Test
    public void updateCoinsCountWithElfFeatures() throws CoinsException {
        IGame game = gameInit(2,2,2);
        Player somePlayer = TestUtils.getSomePlayer(game);
        somePlayer.setRace(Race.ELF);
        somePlayer.setCoins(0);
        Set<Cell> playerFeudalCells = game.getFeudalToCells().get(somePlayer);
        playerFeudalCells.add(new Cell(CellType.MUSHROOM));
        playerFeudalCells.add(new Cell(CellType.LAND));

        GameLoopProcessor.updateCoinsCount(somePlayer, playerFeudalCells, game.getGameFeatures(), game.getBoard());
        assertEquals(4, somePlayer.getCoins());
    }

    /*
     * проверка того, что в произвольном случае монетки считаются верно
     */
    @Test
    public void updateCoinsCountRightCoinsAmount() throws CoinsException {
        IGame game = gameInit(2,2,2);
        Player somePlayer = TestUtils.getSomePlayer(game);
        somePlayer.setRace(Race.UNDEAD);
        somePlayer.setCoins(0);
        Set<Cell> playerFeudalCells = game.getFeudalToCells().get(somePlayer);
        playerFeudalCells.add(new Cell(CellType.MUSHROOM));
        playerFeudalCells.add(new Cell(CellType.LAND));

        GameLoopProcessor.updateCoinsCount(somePlayer, playerFeudalCells, game.getGameFeatures(), game.getBoard());
        assertEquals(2, somePlayer.getCoins());
    }

    /*
     * амфибии могут захватить воду
     */
    @Test
    public void updateCoinsCountWaterAmphibianFeature() throws CoinsException {
        IGame game = gameInit(2,2,2);
        Player somePlayer = TestUtils.getSomePlayer(game);
        somePlayer.setRace(Race.AMPHIBIAN);
        somePlayer.setCoins(0);
        Set<Cell> playerFeudalCells = game.getFeudalToCells().get(somePlayer);
        playerFeudalCells.add(new Cell(CellType.WATER));
        playerFeudalCells.add(new Cell(CellType.LAND));

        GameLoopProcessor.updateCoinsCount(somePlayer, playerFeudalCells, game.getGameFeatures(), game.getBoard());
        assertEquals(2, somePlayer.getCoins());
    }

}
