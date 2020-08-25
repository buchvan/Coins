package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.ai_vika.bot.AIBot;
import io.neolab.internship.coins.client.bot.IBot;
import io.neolab.internship.coins.client.bot.SimpleBot;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.utils.LoggerFile;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.neolab.internship.coins.server.service.SelfPlay.selfPlayByBotToPlayers;

/**
 * Класс, обеспечивающий сбор стастистики(процент побед и поражений)
 */
public class GameStatistic {

    private static final @NotNull Map<Player, Integer> playersStatistic = new HashMap<>();
    private static final @NotNull List<Pair<IBot, Player>> simpleBotToPlayer = new LinkedList<>();
    private static final @NotNull List<Integer> aiBotCoins = new LinkedList<>();
    private static final int GAME_AMOUNT = 100;
    private static final int PLAYERS_AMOUNT = 2;
    private static int winCounter = 0;

    private static void play() {
        final List<Player> players = initPlayers();
        initBotPlayerPair(players);
        initStatisticMap(players);
        for (int i = 0; i < GAME_AMOUNT; i++) {
            final List<Player> winners;
            winners = selfPlayByBotToPlayers(simpleBotToPlayer);
            for (final Player winner : winners) {
                winCounter++;
                if(winner.equals(simpleBotToPlayer.get(1).getSecond())){
                    aiBotCoins.add(winner.getCoins());
                }
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
    private static @NotNull List<Player> initPlayers() {
        int i = 0;
        final List<Player> playerList = new LinkedList<>();
        while (i < PLAYERS_AMOUNT) {
            i++;
            playerList.add(new Player("F" + i));
        }
        return playerList;
    }

    private static void initBotPlayerPair(final List<Player> players) {
        simpleBotToPlayer.add(new Pair<>(new SimpleBot(), players.get(0)));
        simpleBotToPlayer.add(new Pair<>(new AIBot(), players.get(1)));
        //players.forEach(player -> simpleBotToPlayer.add(new Pair<>(new SimpleBot(), player)));
    }

    /**
     * Заполнение мапы со статистикой
     */
    private static void initStatisticMap(final @NotNull List<Player> players) {
        for (final Player player : players) {
            playersStatistic.put(player, 0);
        }
    }

    /**
     * Сбор статистики и её вывод в лог
     */
    private static void collectStatistic() {
        try (final LoggerFile ignored = new LoggerFile("game-statistic")) {
            playersStatistic
                    .forEach(((player, playerWinAmount)
                            -> GameStatisticLogger.printPlayerStatisticLog(player, playerWinAmount,
                            (double) playerWinAmount * 100 / winCounter)));
        }
    }

    public static void main(final String[] args) {
        play();
        collectStatistic();
        aiBotCoins.sort(Comparator.comparingInt(Integer::intValue));
        aiBotCoins.forEach(System.out::println);
        System.out.println("MEDIANA: " + ((double)(aiBotCoins.get(50) + aiBotCoins.get(51))) / 2);
    }
}
