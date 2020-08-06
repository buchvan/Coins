package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.client.bot.IBot;
import io.neolab.internship.coins.client.bot.SimpleBot;
import io.neolab.internship.coins.client.bot.SmartBot;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.utils.LoggerFile;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.neolab.internship.coins.server.service.SelfPlay.selfPlayByBotToPlayers;

/**
 * Класс, обеспечивающий сбор стастистики(процент побед и поражений)
 */
public class GameStatistic {

    private static final @NotNull Map<Player, Integer> playersStatistic = new HashMap<>();
    private static final int GAME_AMOUNT = 2;
    private static final int PLAYERS_AMOUNT = 2;
    private static int winCounter = 0;


    private static void play() throws InterruptedException {
        final List<Player> players = initPlayers();
        initStatisticMap(players);
        final ExecutorService executorService = Executors.newFixedThreadPool(GAME_AMOUNT);
        for (int i = 0; i < GAME_AMOUNT; i++) {
            final int index = i;
            executorService.execute(() -> {
                final List<Player> playersCopy = new LinkedList<>();
                players.forEach(player -> playersCopy.add(player.getCopy()));
                final List<Pair<IBot, Player>> botToPlayer = initBotPlayerPair(playersCopy);
                final List<Player> winners = selfPlayByBotToPlayers(index, botToPlayer);
                synchronized (playersStatistic) {
                    for (final Player winner : winners) {
                        winCounter++;
                        int currentWinAmount = playersStatistic.get(winner);
                        playersStatistic.put(winner, ++currentWinAmount);
                    }
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
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

    private static @NotNull List<Pair<IBot, Player>> initBotPlayerPair(final List<Player> players) {
        final List<Pair<IBot, Player>> botToPlayer = new LinkedList<>();
        final SmartBot smartBot = new SmartBot();
        botToPlayer.add(new Pair<>(smartBot, players.get(0)));
        for (int i = 1; i < PLAYERS_AMOUNT; i++) {
            botToPlayer.add(new Pair<>(new SimpleBot(), players.get(i)));
        }
        return botToPlayer;
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

    public static void main(final String[] args) throws InterruptedException {
        play();
        collectStatistic();
    }
}
