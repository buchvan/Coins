package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.server.game.player.Player;


import java.util.*;

import static io.neolab.internship.coins.server.service.GameInitializer.initTestPlayers;
import static io.neolab.internship.coins.server.service.SelfPlay.*;

public class GameStatistic {

    // или бот вместо Player ?
    private static Map<Player, Integer> playersStatistic = new HashMap<>();


    public static void play(final int gameAmount) {
        final List<Player> players = initPlayers();
        initStatisticMap(players);
        for (int i = 0; i < gameAmount; i++) {
            List<Player> winners;
            winners = selfPlayByPlayers(players);
            for(final Player winner: winners) {
                int currentWinAmount = playersStatistic.get(winner);
                playersStatistic.put(winner, ++currentWinAmount);
            }
            clearPlayerInfo();
        }
    }

    private static void clearPlayerInfo() {
        for(final Player player : playersStatistic.keySet()) {
            player.getUnitStateToUnits().forEach((key, value) -> value.clear());
            player.setCoins(0);
            player.setRace(null);
        }
    }

    private static List<Player> initPlayers() {
        return initTestPlayers(2);

    }

    private static void initStatisticMap(final List<Player> players) {
        for(final Player player: players) {
            playersStatistic.put(player, 0);
        }
    }

    public static void main(final String[] args) {
        final int gameAmount = 100;
        play(gameAmount);
       playersStatistic.forEach(((player, winAmount) -> {
            System.out.println("PLAYER: " + player.getNickname());
            System.out.println("WIN : " + winAmount);
        }));
    }
}
