package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameLogger {
    public static final Logger LOGGER = LoggerFactory.getLogger(GameLogger.class);

    /**
     * Вывод лога о выборе в начале игры
     */
    public static void writeStartGameChoiceLog() {
        LOGGER.info("--------------------------------------------------");
        LOGGER.info("Choice at the beginning of the game");
    }

    /**
     * Вывод лога в начале игры
     */
    public static void writeStartGame() {
        LOGGER.info("--------------------------------------------------");
        LOGGER.info("* Game is started *");
    }

    /**
     * Вывод информации в начале раунда
     *
     * @param currentRound - номер текущего раунда
     */
    public static void printRoundBeginLog(final int currentRound) {
        LOGGER.info("--------------------------------------------------");
        LOGGER.info("Round {} ", currentRound);
    }

    /**
     * Вывод информации в конце раунда
     *
     * @param currentRound  - номер текущего раунда
     * @param playerList    - список всех игроков (без нейтрального)
     * @param ownToCells    - списки клеток, которыми владеет каждый игрок
     * @param feudalToCells - множества клеток, приносящих каждому игроку монеты
     */
    public static void printRoundEndLog(final int currentRound, final List<Player> playerList,
                                        final Map<Player, List<Cell>> ownToCells,
                                        final Map<Player, Set<Cell>> feudalToCells) {
        LOGGER.debug("* Round {} is end! *", currentRound);
        LOGGER.debug("* Players after {} rounds:", currentRound);
        printPlayersInformation(playerList, ownToCells, feudalToCells);
    }

    /**
     * Вывод информации об игроках
     *
     * @param playerList    - список всех игроков (без нейтрального)
     * @param ownToCells    - списки клеток, которыми владеет каждый игрок
     * @param feudalToCells - множества клеток, приносящих каждому игроку монеты
     */
    public static void printPlayersInformation(final List<Player> playerList,
                                               final Map<Player, List<Cell>> ownToCells,
                                               final Map<Player, Set<Cell>> feudalToCells) {

        for (final Player player : playerList) {
            LOGGER.debug("Player {}: [ coins {}, feudal for: {} cells, controled: {} cells ] ",
                    player.getNickname(), player.getCoins(),
                    ownToCells.get(player).size(), feudalToCells.get(player).size());
        }
    }

    /**
     * Выводит информацию о результатах игры
     *
     * @param winners    - победители
     * @param playerList - список игроков
     */
    public static void printResultsInGameEnd(final List<Player> winners, final List<Player> playerList) {
        LOGGER.info("---------------------------------------");
        LOGGER.info("Game OVER !!!");
        LOGGER.info("Winners: ");
        for (final Player winner : winners) {
            LOGGER.info("Player {} - coins {} ", winner.getNickname(), winner.getCoins());
        }
        LOGGER.info("***************************************");
        LOGGER.info("Results of other players: ");
        for (final Player player : playerList) {
            if (winners.contains(player)) {
                continue;
            }
            LOGGER.info("Player {} - coins {} ", player.getNickname(), player.getCoins());
        }
    }

    /**
     * Вывод лога об ошибке
     *
     * @param message - сообщение об ошибке
     */
    public static void writeErrorLog(final String message) {
        LOGGER.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LOGGER.error(message);
    }

    /**
     * Вывод лога об ошибке
     *
     * @param exception - сопутствующее исключение
     */
    public static void writeErrorLog(final Exception exception) {
        writeErrorLog("ERROR!!!", exception);
    }

    /**
     * Вывод лога об ошибке
     *
     * @param message   - сообщение об ошибке
     * @param exception - сопутствующее исключение
     */
    public static void writeErrorLog(final String message, final Exception exception) {
        LOGGER.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LOGGER.error(message, exception);
    }
}
