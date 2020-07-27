package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.service.GameLoopProcessor;
import io.neolab.internship.coins.server.service.TestUtils;
import org.junit.Test;

import java.util.Set;

import static io.neolab.internship.coins.server.service.GameInitializer.gameInit;
import static io.neolab.internship.coins.server.service.TestUtils.getCellFromBoardByCellType;
import static org.junit.Assert.assertEquals;

public class GameLoopProcessorTest {

    /*
     * каждый захваченный регион с грибами приносит в конце хода призовую монетку расе грибов
     */
    @Test
    public void updateCoinsCountWithMushroomFeatures() throws CoinsException {
        final IGame game = gameInit(2,2,2);
        final Player somePlayer = TestUtils.getSomePlayer(game);
        somePlayer.setRace(Race.MUSHROOM);
        somePlayer.setCoins(0);
        final Set<Cell> playerFeudalCells = game.getFeudalToCells().get(somePlayer);
        playerFeudalCells.add(getCellFromBoardByCellType(CellType.MUSHROOM, game.getBoard()));

        GameLoopProcessor.updateCoinsCount(somePlayer, playerFeudalCells, game.getGameFeatures(), game.getBoard());
        assertEquals(2, somePlayer.getCoins());
    }

    /*
     *  каждая отдельная группа регионов приносит одну дополнительную монетку расе эльфов
     */
    @Test
    public void updateCoinsCountWithElfFeatures() throws CoinsException {
        final IGame game = gameInit(2,2,2);
        final Player somePlayer = TestUtils.getSomePlayer(game);
        somePlayer.setRace(Race.ELF);
        somePlayer.setCoins(0);
        final Set<Cell> playerFeudalCells = game.getFeudalToCells().get(somePlayer);
        playerFeudalCells.add(getCellFromBoardByCellType(CellType.MUSHROOM, game.getBoard()));
        playerFeudalCells.add(getCellFromBoardByCellType(CellType.LAND, game.getBoard()));

        GameLoopProcessor.updateCoinsCount(somePlayer, playerFeudalCells, game.getGameFeatures(), game.getBoard());
        assertEquals(4, somePlayer.getCoins());
    }

    /*
     * проверка того, что в произвольном случае монетки считаются верно
     */
    @Test
    public void updateCoinsCountRightCoinsAmount() throws CoinsException {
        final IGame game = gameInit(2,2,2);
        final Player somePlayer = TestUtils.getSomePlayer(game);
        somePlayer.setRace(Race.UNDEAD);
        somePlayer.setCoins(0);
        final Set<Cell> playerFeudalCells = game.getFeudalToCells().get(somePlayer);
        playerFeudalCells.add(getCellFromBoardByCellType(CellType.MUSHROOM, game.getBoard()));
        playerFeudalCells.add(getCellFromBoardByCellType(CellType.LAND, game.getBoard()));

        GameLoopProcessor.updateCoinsCount(somePlayer, playerFeudalCells, game.getGameFeatures(), game.getBoard());
        assertEquals(2, somePlayer.getCoins());
    }

    /*
     * амфибии могут захватить воду
     */
    @Test
    public void updateCoinsCountWaterAmphibianFeature() throws CoinsException {
        final IGame game = gameInit(2,2,2);
        final Player somePlayer = TestUtils.getSomePlayer(game);
        somePlayer.setRace(Race.AMPHIBIAN);
        somePlayer.setCoins(0);
        final Set<Cell> playerFeudalCells = game.getFeudalToCells().get(somePlayer);
        playerFeudalCells.add(getCellFromBoardByCellType(CellType.WATER, game.getBoard()));
        playerFeudalCells.add(getCellFromBoardByCellType(CellType.LAND, game.getBoard()));

        GameLoopProcessor.updateCoinsCount(somePlayer, playerFeudalCells, game.getGameFeatures(), game.getBoard());
        assertEquals(2, somePlayer.getCoins());
    }

}
