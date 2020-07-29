package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.utils.LoggerFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.neolab.internship.coins.server.service.GameInitializer.initTestPlayers;
import static io.neolab.internship.coins.server.service.SelfPlay.selfPlayByPlayers;

/**
 * Класс, обеспечивающий сбор стастистики(процент побед и поражений)
 */
//FIXME: на большом количество итераций в конце игры у игроков слишком много монет(несколько тысяч)
public class GameStatistic {

    private static Map<Player, Integer> playersStatistic = new HashMap<>();
    private static final int GAME_AMOUNT = 10;
    private static final int PLAYERS_AMOUNT = 2;
    private static final String DELIMITER = "\n";
    private static int winCounter = 0;


    private static void play() {
        final List<Player> players = initPlayers();
        initStatisticMap(players);
        for (int i = 0; i < GAME_AMOUNT; i++) {
            final List<Player> winners;
            winners = selfPlayByPlayers(players);
            for (final Player winner : winners) {
                winCounter++;
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

    public static void main(final String[] args) {
        play();
        try (final LoggerFile ignored = new LoggerFile("game-statistic")) {
            GameStatisticLogger.printLogStatisticFileLog(ignored.toString());
            playersStatistic
                    .forEach(((player, playerWinAmount)
                            -> GameStatisticLogger.printPlayerStatisticLog(player, playerWinAmount,
                            (double) playerWinAmount * 100 / winCounter)));
        }
    }
}
