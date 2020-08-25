package io.neolab.internship.coins.bim.bot.ai;

import io.neolab.internship.coins.bim.bot.ai.model.Edge;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

class AILogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(AILogger.class);
    private static final boolean isLoggedOn = false;

    /**
     * Вывод лога о новой глубине
     *
     * @param newDepth - новая глубина
     * @param newRound - новый раунд игры
     */
    static void printLogNewDepth(final int newDepth, final int newRound) {
        if (isLoggedOn) {
            LOGGER.debug("New depth: {}", newDepth);
            LOGGER.debug("New round: {}", newRound);
        }
    }

    /**
     * Вывод лога о следующем игроке
     *
     * @param nextPlayer - следующий игрок
     */
    static void printLogNextPlayer(final @NotNull Player nextPlayer) {
        if (isLoggedOn) {
            LOGGER.debug("Next player: {}", nextPlayer.getNickname());
        }
    }

    /**
     * Вывод лога об уходе в упадок игрока
     *
     * @param currentDepth - текущая глубина
     * @param player       - игрока
     */
    static void printLogDeclineRace(final int currentDepth, final @NotNull Player player, final boolean isDeclineRace) {
        if (isLoggedOn) {
            LOGGER.debug("depth {}, player {}, DeclineRace {}", currentDepth, player.getNickname(), isDeclineRace);
        }
    }

    /**
     * Вывод лога об изменении расы
     *
     * @param currentDepth - текущая глубина
     * @param race         - новая раса
     * @param player       - игрок
     */
    static void printLogChangeRace(final int currentDepth, final @NotNull Race race,
                                   final @NotNull Player player) {
        if (isLoggedOn) {
            LOGGER.debug("depth {}, ChangeRace {}, player {}", currentDepth, race, player.getNickname());
        }
    }

    /**
     * Вывод лога о создании нового узла
     *
     * @param currentDepth - текущая глубина
     * @param edges        - список дуг, выходящих из этого нового узла
     */
    static void printLogCreatedNewNode(final int currentDepth, final @NotNull List<Edge> edges) {
        if (isLoggedOn) {
            LOGGER.debug("Created new node in depth {} with edges: {}", currentDepth, edges);
        }
    }

    /**
     * Вывод лога о создании узла с решением по захвату клетки игроком
     *
     * @param currentDepth - текущая глубина
     * @param index        - число юнитов в данном решении
     * @param player       - игрок
     * @param resolution   - решение
     */
    static void printLogCatchCellResolution(final int currentDepth, final int index,
                                            final @NotNull Player player,
                                            final @NotNull Pair<Position, List<Unit>> resolution) {
        if (isLoggedOn) {
            LOGGER.debug("current depth {}, index {}, player {}, resolution {}", currentDepth, index,
                    player.getNickname(), resolution);
        }
    }

    /**
     * Вывод лога о создании нового терминального узла
     *
     * @param map - отображение игрока в число побед в данном узле
     */
    static void printLogNewTerminalNodePercent(final @NotNull Map<Player, Integer> map) {
        if (isLoggedOn) {
            LOGGER.debug("!!!!!!!!!!!!!!!!!!!");
            LOGGER.debug("Created new terminal node:");
            map.forEach((player, integer) -> LOGGER.debug("--- {} -> {}", player.getNickname(), integer));
        }
    }

    /**
     * Вывод лога о создании нового терминального узла
     *
     * @param playerToMaxAndMinCoinsCount - отображение игрока в пару
     *                                    (максимально число монет игрока, минимально число монет)
     */
    static void printLogNewTerminalNodeValue(final @NotNull Map<Player, Pair<Integer, Integer>> playerToMaxAndMinCoinsCount) {
        if (isLoggedOn) {
            LOGGER.debug("!!!!!!!!!!!!!!!!!!!");
            LOGGER.debug("Created new terminal node:");
            playerToMaxAndMinCoinsCount.forEach((player, pair) ->
                    LOGGER.debug("--- {} -> {}", player.getNickname(), pair));
        }
    }

    /**
     * Вывод лога о создании нового терминального узла
     *
     * @param playerToValueDifference - отображение игрока в минимальную разность между его
     *                                числом монет и числом монет остальных
     */
    static void printLogNewTerminalNodeValueDifference(final @NotNull Map<Player, Integer> playerToValueDifference) {
        if (isLoggedOn) {
            LOGGER.debug("!!!!!!!!!!!!!!!!!!!");
            LOGGER.debug("Created new terminal node:");
            playerToValueDifference.forEach((player, integer) ->
                    LOGGER.debug("--- {} -> {}", player.getNickname(), integer));
        }
    }
}
