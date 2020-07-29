package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.server.game.player.Player;

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
    private static final int GAME_AMOUNT = 3;
    private static final int PLAYERS_AMOUNT = 2;


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
     * Запись результатов в консоль
     */
    public static void main(final String[] args) {
        play();
        playersStatistic.forEach(((player, winAmount) -> {
            System.out.println("PLAYER: " + player.getNickname());
            System.out.println("WIN : " + winAmount);
        }));
    }
}
