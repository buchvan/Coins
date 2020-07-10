package io.neolab.internship.coins.server.game.service;

import io.neolab.internship.coins.server.game.Player;

import java.util.LinkedList;
import java.util.List;

public class GameFinalizer {
    /**
     * Финализатор игры. Выводит победителей в лог.
     *
     * @param playerList - список игроков.
     */
    public static void finalize(final List<Player> playerList) {
        final int maxCoinsCount = getMaxCoinsCount(playerList);
        GameLogger.printResultsInGameEnd(getWinners(maxCoinsCount, playerList), playerList);
    }

    /**
     * @param playerList - список игроков
     * @return максимальное кол-во монет, имеющихся у одного игрока
     */
    private static int getMaxCoinsCount(final List<Player> playerList) {
        return playerList.stream()
                .map(Player::getCoins)
                .max(Integer::compareTo)
                .orElse(-1);
    }

    /**
     * @param maxCoinsCount - максимальное кол-во монет, имеющихся у одного игрока
     * @param playerList    - - список игроков
     * @return список победителей (игроков, имеющих монет в кол-ве maxCoinsCount)
     */
    private static List<Player> getWinners(final int maxCoinsCount, final List<Player> playerList) {
        final List<Player> winners = new LinkedList<>();
        playerList.forEach(player -> {
            if (player.getCoins() == maxCoinsCount) {
                winners.add(player);
            }
        });
        return winners;
    }
}
