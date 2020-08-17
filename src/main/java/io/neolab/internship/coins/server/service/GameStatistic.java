package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.client.bot.IBot;
import io.neolab.internship.coins.client.bot.SmartBot;
import io.neolab.internship.coins.client.bot.FunctionType;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.LoggerFile;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Класс, обеспечивающий сбор стастистики(процент побед и поражений)
 */
public class GameStatistic {
    // Самый худший случай для глубины 4 - примерно 30 секунд, для глубины 3 - примерно 7 секунд

    private static final @NotNull Map<Player, Statistic> playersStatistic = new HashMap<>();
    private static final int GAME_AMOUNT = 10;
    private static final int PLAYERS_AMOUNT = 2;
    private static final int BOT1_MAX_DEPTH = 2;
    private static final FunctionType BOT1_TYPE = FunctionType.MIN_MAX_VALUE_DIFFERENCE;
    private static final int BOT2_MAX_DEPTH = 2;
    private static final FunctionType BOT2_TYPE = FunctionType.MAX_VALUE_DIFFERENCE;
    private static int winCounter = 0;
    private static final boolean isParallel = false;

    static class Statistic {
        private int winAmount = 0;
        private final @NotNull Map<Pair<Race, CellType>, Integer> capturesNumber = new HashMap<>();
        private final @NotNull List<Race> firstRaces = new ArrayList<>(GAME_AMOUNT);
        private final @NotNull List<Race> lastRaces = new ArrayList<>(GAME_AMOUNT);

        int getWinAmount() {
            return winAmount;
        }

        @NotNull Map<Pair<Race, CellType>, Integer> getCapturesNumber() {
            return capturesNumber;
        }

        void incrementWinAmount() {
            winAmount++;
        }

        void incrementCapturesNumber(final @NotNull Race race, final @NotNull CellType cellType) {
            final Pair<Race, CellType> pair = new Pair<>(race, cellType);
            final Integer number = capturesNumber.get(pair);
            if (number != null) {
                capturesNumber.replace(pair, number + 1);
                return;
            }
            capturesNumber.put(pair, 1);
        }

        @NotNull List<Race> getFirstRaces() {
            return firstRaces;
        }

        void addFirstRace(final @NotNull Race lastRace) {
            firstRaces.add(lastRace);
        }

        @NotNull List<Race> getLastRaces() {
            return lastRaces;
        }

        void addLastRace(final @NotNull Race lastRace) {
            lastRaces.add(lastRace);
        }

        @Contract(value = "null -> false", pure = true)
        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Statistic statistic = (Statistic) o;
            return winAmount == statistic.winAmount &&
                    capturesNumber.equals(statistic.capturesNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(winAmount, capturesNumber);
        }

        @Override
        public String toString() {
            return "Statistic{" +
                    "winAmount=" + winAmount +
                    ", capturesNumber=" + capturesNumber +
                    '}';
        }
    }

    private static void play() throws InterruptedException {
        final List<Player> players = initPlayers();
        initStatisticMap(players);
        if (isParallel) {
            playParallel(players);
            return;
        }
        playNotParallel(players);
    }

    private static void playParallel(final @NotNull List<Player> players) throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(GAME_AMOUNT);
        for (int i = 0; i < GAME_AMOUNT; i++) {
            final int index = i;
            executorService.execute(() -> {
                final List<Player> playersCopy = new LinkedList<>();
                players.forEach(player -> playersCopy.add(player.getCopy()));
                final List<Pair<IBot, Player>> botToPlayer = initBotPlayerPair(playersCopy);
                final List<Player> winners =
                        SelfPlay.selfPlayByBotToPlayersWithStatistic(index, botToPlayer, playersStatistic);
                synchronized (playersStatistic) {
                    players.forEach(player ->
                            playersStatistic.get(player).addLastRace(Objects.requireNonNull(player.getRace())));
                    for (final Player winner : winners) {
                        winCounter++;
                        playersStatistic.get(winner).incrementWinAmount();
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
        botToPlayer.add(new Pair<>(new SmartBot(BOT1_MAX_DEPTH, BOT1_TYPE), players.get(0)));
        botToPlayer.add(new Pair<>(new SmartBot(BOT2_MAX_DEPTH, BOT2_TYPE), players.get(players.size() - 1)));
        return botToPlayer;
    }

    /**
     * Заполнение мапы со статистикой
     */
    private static void initStatisticMap(final @NotNull List<Player> players) {
        for (final Player player : players) {
            playersStatistic.put(player, new Statistic());
        }
    }

    /**
     * Сбор статистики и её вывод в лог
     */
    private static void collectStatistic() {
        try (final LoggerFile ignored = new LoggerFile("game-statistic")) {
            playersStatistic.forEach(((player, statistic) ->
                    GameStatisticLogger.printPlayerStatisticLog(player, statistic, winCounter)));
        }
    }

    private static void playNotParallel(final @NotNull List<Player> players) {
        for (int i = 0; i < GAME_AMOUNT; i++) {
            final List<Pair<IBot, Player>> botToPlayer = initBotPlayerPair(players);
            final List<Player> winners =
                    SelfPlay.selfPlayByBotToPlayersWithStatistic(i, botToPlayer, playersStatistic);
            players.forEach(player ->
                    playersStatistic.get(player).addLastRace(Objects.requireNonNull(player.getRace())));
            for (final Player winner : winners) {
                winCounter++;
                playersStatistic.get(winner).incrementWinAmount();
            }
            toDefault(players);
        }
    }

    private static void toDefault(final @NotNull List<Player> players) {
        players.forEach(player -> {
            player.setCoins(0);
            player.setRace(null);
            Arrays.stream(AvailabilityType.values()).forEach(availabilityType ->
                    player.getUnitsByState(availabilityType).clear());
        });
    }

    public static void main(final String[] args) throws InterruptedException {
        play();
        collectStatistic();
    }
}
