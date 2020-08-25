package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.server.game.player.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Класс, содержащий основные функции вывода логов об игровой статистике
 */
public class GameStatisticLogger {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(GameStatisticLogger.class);

    /**
     * Запись в лог информации об игроке
     */
    private static void printPlayerLog(final @NotNull Player player) {
        LOGGER.info("PLAYER: " + player.getNickname());
    }

    /**
     * Запись в лог информации о количестве побед
     */
    private static void printPlayerWinAmountLog(final int winAmount) {
        LOGGER.info("WIN: " + winAmount);
    }

    /**
     * Запись в лог информации о проценте побед игрока
     */
    private static void printPLayerWinPercentLog(final double percent) {
        LOGGER.info("% : " + percent);
    }

    /**
     * Запись в лог полной статистики об игроке
     */
    public static void printPlayerStatisticLog(final @NotNull Player player, final int winAmount, final double percent) {
        printPlayerLog(player);
        printPlayerWinAmountLog(winAmount);
        printPLayerWinPercentLog(percent);
    }
}
