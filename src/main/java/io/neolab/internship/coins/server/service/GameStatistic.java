package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.server.game.player.Player;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.neolab.internship.coins.server.service.GameInitializer.initTestPlayers;
import static io.neolab.internship.coins.server.service.SelfPlay.selfPlayByPlayers;

/**
 * Класс, обеспечивающий сбор стастистики(процент побед и поражений)
 */
//FIXME: на большом количество итераций в конце игры у игроков слишком много монет(несколько тысяч)
//TODO: use file path
public class GameStatistic {

    private static Map<Player, Integer> playersStatistic = new HashMap<>();
    private static final int GAME_AMOUNT = 2;
    private static final int PLAYERS_AMOUNT = 2;
    private static final String STATISTIC_FILE_PATH = "./src/statistic/";
    private static final String STATISTIC_BASE_FILE_NAME = "game-statistic-";
    private static final String DELIMITER = "\n";


    public static void play() {
        final List<Player> players = initPlayers();
        initStatisticMap(players);
        for (int i = 0; i < GAME_AMOUNT; i++) {
            final List<Player> winners;
            winners = selfPlayByPlayers(players);
            for (final Player winner : winners) {
                int currentWinAmount = playersStatistic.get(winner);
                playersStatistic.put(winner, ++currentWinAmount);
            }
            clearPlayerInfo();
        }
    }

    /**
     * Сброс параметров игроков после каждой игры в начальное состояние
     */
    private static void clearPlayerInfo() {
        for (final Player player : playersStatistic.keySet()) {
            player.getUnitStateToUnits().forEach((key, value) -> value.clear());
            player.setCoins(0);
            player.setRace(null);
        }
    }

    /**
     * Иннициализация тестовых игроков
     */
    private static List<Player> initPlayers() {
        return initTestPlayers(PLAYERS_AMOUNT);

    }

    /**
     * Заполнение мапы со статистикой
     */
    private static void initStatisticMap(final List<Player> players) {
        for (final Player player : players) {
            playersStatistic.put(player, 0);
        }
    }

    /**
     * Генерация имени файла
     */
    private static String generateStatisticFileName() {
        return STATISTIC_BASE_FILE_NAME + new Date().toString();
    }

    /**
     * Вывести статистику на консоль
     */
    private static void writeStatisticToConsole(final String playerStr, final String winStr, final String percentStr) {
        System.out.print(playerStr);
        System.out.print(winStr);
        System.out.print(percentStr);
    }

    /**
     * Записать статистику в файл
     */
    private static void writeStatisticIntoFile(final FileWriter file, final String playerStr,
                                               final String winStr, final String percentStr) throws IOException {
        file.write(playerStr);
        file.write(winStr);
        file.write(percentStr);
    }

    /**
     * Возвращает строку с именем игрока
     */
    private static String getPlayerString(final Player player) {
        return "PLAYER: " + player.getNickname() + DELIMITER;
    }

    /**
     * Возвращает строку с количеством побед игрока
     */
    private static String getPlayerWinString(final int winAmount) {
        return "WIN : " + winAmount + DELIMITER;
    }

    /**
     * Возвращает строку с процентом побед
     */
    private static String getPlayerWinPercentStr(final double percent) {
        return "% : " + percent + DELIMITER;
    }

    /**
     * Запись результатов в консоль
     */
    public static void main(final String[] args) {
        play();
        try (final FileWriter file = new FileWriter("test")) {
            playersStatistic.forEach(((player, winAmount) -> {
                final String playerStr = getPlayerString(player);
                final String winStr = getPlayerWinString(winAmount);
                final String percentStr = getPlayerWinPercentStr((double) winAmount * 100 / GAME_AMOUNT);
                writeStatisticToConsole(playerStr, winStr, percentStr);
                try {
                    writeStatisticIntoFile(file, playerStr, winStr, percentStr);
                } catch (final IOException exception) {
                    System.out.println("Statistic writing failed");
                }
            }));
        } catch (final IOException exception) {
            System.out.println("Statistic file failed");
        }

    }
}
