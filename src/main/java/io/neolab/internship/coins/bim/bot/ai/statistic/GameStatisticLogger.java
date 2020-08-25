package io.neolab.internship.coins.bim.bot.ai.statistic;

import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Класс, содержащий основные функции вывода логов об игровой статистике
 */
public class GameStatisticLogger {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(GameStatisticLogger.class);

    /**
     * Запись в лог пустой строки
     */
    private static void printEmptyLine() {
        LOGGER.info("");
    }

    /**
     * Запись в лог информации об игроке
     */
    private static void printPlayerLog(final @NotNull Player player) {
        LOGGER.info("PLAYER: {}", player.getNickname());
    }

    /**
     * Запись в лог информации о количестве побед
     */
    private static void printPlayerWinAmountLog(final int winAmount) {
        LOGGER.info("WIN: {}", winAmount);
    }

    /**
     * Запись в лог информации о проценте побед игрока
     */
    private static void printPlayerWinPercentLog(final double percent) {
        LOGGER.info("% : {}", percent);
    }

    /**
     * Запись в лог информации о числе захватов типа клетки
     */
    private static void printPlayerCapturesNumberLog(final @NotNull Pair<Race, CellType> pair, final int capturesNumber) {
        LOGGER.info("race {} to cellType {} : {}",
                pair.getFirst().getTitle(), pair.getSecond().getTitle(), capturesNumber);
    }

    /**
     * Запись в лог списка рас
     */
    private static void printPlayerRaceList(final @NotNull String title, final @NotNull List<Race> raceList) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(title);
        raceList.forEach(race -> stringBuilder.append(race.getTitle()).append(" "));
        LOGGER.info(stringBuilder.toString());
    }

    private static void printMaxTime(final long maxTime) {
        LOGGER.info("Max time: {}", maxTime);
    }

    /**
     * Запись в лог полной статистики об игроке
     */
    static void printPlayerStatisticLog(final @NotNull Player player, final @NotNull GameStatistic.Statistic statistic,
                                        final int winCounter) {
        printEmptyLine();
        printPlayerLog(player);
        printPlayerWinAmountLog(statistic.getWinAmount());
        printPlayerWinPercentLog((double) statistic.getWinAmount() * 100 / winCounter);
        statistic.getCapturesNumber().forEach(GameStatisticLogger::printPlayerCapturesNumberLog);
        printPlayerRaceList("First races: ", statistic.getFirstRaces());
        printPlayerRaceList("Last races: ", statistic.getLastRaces());
        printMaxTime(statistic.getMaxTime());
    }
}