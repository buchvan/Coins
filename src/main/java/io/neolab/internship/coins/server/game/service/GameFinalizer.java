package io.neolab.internship.coins.server.game.service;

import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.game.player.Player;

import java.util.LinkedList;
import java.util.List;

public class GameFinalizer {

    /**
     * Финализатор игры. Выводит победителей в лог.
     *
     * @param playerList - список игроков.
     * @return список победителей
     */
    public static List<Player> finalize(final List<Player> playerList) throws CoinsException {
        final int maxCoinsCount = getMaxCoinsCount(playerList);
        final List<Player> winners = getWinners(maxCoinsCount, playerList);
        GameLogger.printResultsInGameEnd(winners, playerList);
        return winners;
    }

    /**
     * @param playerList - список игроков
     * @return максимальное кол-во монет, имеющихся у одного игрока
     */
    public static int getMaxCoinsCount(final List<Player> playerList) throws CoinsException {
        if (playerList == null) {
            throw new CoinsException(ErrorCode.PLAYERS_LIST_IS_NULL);
        }
        return playerList.stream()
                .map(Player::getCoins)
                .max(Integer::compareTo)
                .orElse(-1);
    }

    /**
     * @param maxCoinsCount - максимальное кол-во монет, имеющихся у одного игрока
     * @param playerList    - список игроков
     * @return список победителей (игроков, имеющих монет в кол-ве maxCoinsCount)
     */
    public static List<Player> getWinners(final int maxCoinsCount,
                                          final List<Player> playerList) throws CoinsException {
        if (playerList == null) {
            throw new CoinsException(ErrorCode.PLAYERS_LIST_IS_NULL);
        }
        final List<Player> winners = new LinkedList<>();
        playerList.forEach(player -> {
            if (player.getCoins() == maxCoinsCount) {
                winners.add(player);
            }
        });
        return winners;
    }
}
