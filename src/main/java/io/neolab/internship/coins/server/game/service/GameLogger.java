package io.neolab.internship.coins.server.game.service;

import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.utils.AvailabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameLogger {
    public static final Logger LOGGER = LoggerFactory.getLogger(GameLogger.class);

    /**
     * Вывод лога о файле, куда ведётся логгирование
     *
     * @param logFileName - имя файла-лога
     */
    public static void printLogFileLog(final String logFileName) {
        LOGGER.debug("* Logging in file {} *", logFileName);
    }

    /**
     * Вывод лога о создании игры
     *
     * @param game - созданная игра
     */
    public static void printGameCreatedLog(final Game game) {
        LOGGER.debug("Game is created: {} ", game);
    }

    /**
     * Вывод лога о выборе в начале игры
     */
    public static void printStartGameChoiceLog() {
        LOGGER.info("--------------------------------------------------");
        LOGGER.info("Choice at the beginning of the game");
    }

    /**
     * Вывод лога о выборе расы игроком
     *
     * @param player  - игрок, выбравший расу
     * @param newRace - новая раса игрока
     */
    public static void printChooseRaceLog(final Player player, final Race newRace) {
        LOGGER.info("* Player {} choose race {} *", player.getNickname(), newRace);
    }

    /**
     * Вывод лога в начале игры
     */
    public static void printStartGame() {
        LOGGER.info("--------------------------------------------------");
        LOGGER.info("* Game is started *");
    }

    /**
     * Вывод лога в начале раунда
     *
     * @param currentRound - номер текущего раунда
     */
    public static void printRoundBeginLog(final int currentRound) {
        LOGGER.info("--------------------------------------------------");
        LOGGER.info("Round {} ", currentRound);
    }

    /**
     * Вывод лога о следующем игроке
     *
     * @param player - следующий игрок
     */
    public static void printNextPlayerLog(final Player player) {
        LOGGER.info("Next player: {} ", player.getNickname());
    }

    /**
     * Вывод лога об упадке
     *
     * @param player - игрок в упадке
     */
    public static void printDeclineRaceLog(final Player player) {
        LOGGER.info("* Player {} in decline of race! *", player.getNickname());
    }

    /**
     * Вывод лога о начале захвата клеток
     *
     * @param player - игрок, начинающий захватывать клетки
     */
    public static void printBeginCatchCellsLog(final Player player) {
        LOGGER.debug("=================================");
        LOGGER.debug("* Player {} catch cells! *", player.getNickname());
    }

    /**
     * Вывод лога о попытке захвата клетки
     *
     * @param player       - игрок, пытающийся захватить клетку
     * @param cellPosition - позиция клетки на борде
     */
    public static void printCellCatchAttemptLog(final Player player, final Position cellPosition) {
        LOGGER.debug("Player {} catch attempt the cell {} ", player.getNickname(), cellPosition);
    }

    /**
     * Вывод лога о выборе игроком числа юнитов для захвата клетки
     *
     * @param player     - игрок, выбравший число юнитов для захвата
     * @param unitsCount - выбранное число юнитов
     */
    public static void printCatchCellUnitsQuantityLog(final Player player, final int unitsCount) {
        LOGGER.debug("Player {} capture units in quantity {} ", player.getNickname(), unitsCount);
    }

    /**
     * Вывод лога о неудачном захвате клетки
     *
     * @param player - игрок, который не смог захватить клетку
     */
    public static void printCatchCellNotCapturedLog(final Player player) {
        LOGGER.debug("The cell is not captured. The aggressor {} retreated ", player.getNickname());
    }

    /**
     * Вывод лога о применении особенности при обороне клетки
     *
     * @param defendingPlayer - игрок, защищающий клетку
     * @param catchingCell    - захватываемая клетка
     */
    public static void printCatchCellDefenseFeatureLog(final Player defendingPlayer, final Cell catchingCell) {
        LOGGER.debug("Player stumbled upon a defense of {} in cellType {} of defending player {}",
                catchingCell.getRace(), catchingCell.getType(),
                defendingPlayer != null ? defendingPlayer.getNickname() : "NULL");
    }

    /**
     * Вывод лога о количестве юнитов, необходимом для захвата клетки
     *
     * @param unitsCountNeededToCatch - число юнитов, необходимое для захвата клетки
     */
    public static void printCatchCellCountNeededLog(final int unitsCountNeededToCatch) {
        LOGGER.debug("Units count needed to catch: {} ", unitsCountNeededToCatch);
    }

    /**
     * Вывод лога о применении особенности при захвате клетки
     *
     * @param player       - игрок, захватывающий клетку
     * @param catchingCell - захватываемая клетка
     */
    public static void printCatchCellCatchingFeatureLog(final Player player, final Cell catchingCell) {
        LOGGER.debug("Player {} took advantage of the feature race {} and cellType of catchCell {}",
                player.getNickname(), player.getRace().getTitle(), catchingCell.getType().getTitle());
    }

    /**
     * Вывод лога о бонусе к силе атаки
     *
     * @param bonusAttack - бонус к силе атаки
     */
    public static void printCatchCellBonusAttackLog(final int bonusAttack) {
        LOGGER.debug("Bonus attack: {} ", bonusAttack);
    }

    /**
     * Вывод лога о захвате клетки
     *
     * @param player - игрок, захвативший клетку
     */
    public static void printCatchCellBonusAttackLog(final Player player) {
        LOGGER.info("Cell is catched of player {} ", player.getNickname());
    }

    /**
     * Вывод лога о смерти юнитов игрока
     *
     * @param player         - игрок, чьи юниты погибли
     * @param deadUnitsCount - число погибших юнитов игрока
     */
    public static void printCatchCellUnitsDiedLog(final Player player, final int deadUnitsCount) {
        LOGGER.debug("{} units of player {} died ", deadUnitsCount, player.getNickname());
    }

    /**
     * Вывод лога после захвата клетки
     *
     * @param player       - игрок, захвативший клетку
     * @param catchingCell - захваченная клетка
     */
    public static void printAfterCellCatchingLog(final Player player, final Cell catchingCell) {
        LOGGER.debug("+++++++++++++++++++++++++++++++");
        LOGGER.debug("Cell after catching: ");
        LOGGER.debug("CellType: {} ", catchingCell.getType().getTitle());
        LOGGER.debug("Race: {} ", catchingCell.getRace().getTitle());
        LOGGER.debug("Feudal: {} ",
                catchingCell.getFeudal() != null ? catchingCell.getFeudal().getNickname() : "NULL");
        LOGGER.debug("Own: {} ", catchingCell.getOwn().getNickname());
        LOGGER.debug("Units: {} ", catchingCell.getUnits());
        LOGGER.debug("Player after catching: ");
        LOGGER.debug("Available units: {} ", player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE));
        LOGGER.debug("Not available units: {} ", player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE));
        LOGGER.debug("+++++++++++++++++++++++++++++++");
    }

    /**
     * Вывод лога о транизитных клетках игрока
     *
     * @param player       - игрок, чьи транзитные клетки мы логгируем
     * @param transitCells - транзитные клетки игрока
     *                     (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     */
    public static void printTransitCellsLog(final Player player, final List<Cell> transitCells) {
        LOGGER.debug("* Transit cells of player {}: ", player.getNickname());
        transitCells.forEach(transitCell -> {
            LOGGER.debug("--- CellType: {} ", transitCell.getType().getTitle());
            LOGGER.debug("--- Race: {} ", transitCell.getRace().getTitle());
            LOGGER.debug("--- Feudal: {} ",
                    transitCell.getFeudal() != null ? transitCell.getFeudal().getNickname() : "--- NULL");
            LOGGER.debug("--- Own: {} ", transitCell.getOwn().getNickname());
            LOGGER.debug("--- Units: {} ", transitCell.getUnits());
        });
    }

    /**
     * Вывод лога о начале фазы распределения войск
     *
     * @param player - игрок, чьи транзитные клетки мы логгируем
     */
    public static void printBeginUnitsDistributionLog(final Player player) {
        LOGGER.debug("=======================================");
        LOGGER.debug("* Player {} is distributes units! *", player.getNickname());
    }

    /**
     * Вывод лога об освобождении транизитных клеток игроком
     *
     * @param player - игрок, который освободил свои транзитные клетки
     */
    public static void printFreedTransitCellsLog(final Player player) {
        LOGGER.debug("Player {} freed his transit cells ", player.getNickname());
    }

    /**
     * Вывод лога о намерении игрока распределить юниты в клетку
     *
     * @param player     - игрок, распределяющий юнитов в клетку
     * @param unitsCount - число юнитов, направленных в эту клетку
     * @param position   - позиция клетки, в которую распределяют юнитов
     */
    public static void printCellDefendingLog(final Player player, final int unitsCount, final Position position) {
        LOGGER.debug("Player {} protects by {} units the cell in position {}",
                player.getNickname(), unitsCount, position);
    }

    /**
     * Вывод лога о последствиях в данных после распределения войск в клетку
     *
     * @param player        - игрок, распределивший юнитов в клетку
     * @param protectedCell - клетка, в которую распределили юнитов
     */
    public static void printCellAfterDefendingLog(final Player player, final Cell protectedCell) {
        LOGGER.debug("Cell after defending: ");
        LOGGER.debug("--- Own: {} ", protectedCell.getOwn().getNickname());
        LOGGER.debug("--- Units: {} ", protectedCell.getUnits());
        LOGGER.debug("Player {} after defending: ", player.getNickname());
        LOGGER.debug("--- Available units: {} ", player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE));
        LOGGER.debug("--- Not available units: {} ", player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE));
    }

    /**
     * Вывод лога после фазы распределения войск
     *
     * @param player - игрок, завершивший фазу распределения войск
     */
    public static void printAfterDistributedUnitsLog(final Player player) {
        LOGGER.info("Player {} distributed units ", player.getNickname());
    }

    /**
     * Вывод лога об обновлении числа монет у игрока по клетке
     *
     * @param player       - игрок, у которого обновилось число монет
     * @param cellPosition - позиция клетки, по которой обновилось число монет у игрока
     */
    public static void printPlayerCoinsCountByCellUpdatingLog(final Player player, final Position cellPosition) {
        LOGGER.debug("Player {} update coins by cell in position {} ", player.getNickname(), cellPosition);
    }

    /**
     * Вывод лога об обновлении числа монет у игрока по типу клетки
     *
     * @param player   - игрок, у которого обновилось число монет
     * @param cellType - тип клетки, по которому у игрока обновилось число монет
     */
    public static void printPlayerCoinsCountByCellTypeUpdatingLog(final Player player, final CellType cellType) {
        LOGGER.debug("Player {} update coins by cellType {} ", player.getNickname(), cellType.getTitle());
    }

    /**
     * Вывод лога об обновлении числа монет у игрока по отдельной группе типа клетки
     *
     * @param player   - игрок, у которого обновилось число монет
     * @param cellType - тип клетки, по которому у игрока обновилось число монет
     */
    public static void printPlayerCoinsCountByCellTypeGroupUpdatingLog(final Player player, final CellType cellType) {
        LOGGER.debug("Player {} update coins by group cellType {} ", player.getNickname(), cellType.getTitle());
    }

    /**
     * Вывод лога об обновлении общего числа монет у игрока
     *
     * @param player - игрок, у которого обновилось число монет
     */
    public static void printPlayerCoinsCountUpdatingLog(final Player player) {
        LOGGER.debug("Player {} updated coins count. Now he has {} ", player.getNickname(), player.getCoins());
    }


    /**
     * Вывод лога в конце раунда
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
     * Вывод лога об игроках
     *
     * @param playerList    - список всех игроков (без нейтрального)
     * @param ownToCells    - списки клеток, которыми владеет каждый игрок
     * @param feudalToCells - множества клеток, приносящих каждому игроку монеты
     */
    public static void printPlayersInformation(final List<Player> playerList,
                                               final Map<Player, List<Cell>> ownToCells,
                                               final Map<Player, Set<Cell>> feudalToCells) {

        playerList.forEach(player ->
                LOGGER.debug("Player {}: [ coins {}, feudal for: {} cells, controled: {} cells ] ",
                        player.getNickname(), player.getCoins(),
                        ownToCells.get(player).size(), feudalToCells.get(player).size()));
    }

    /**
     * Выводит лога о результатах игры
     *
     * @param winners    - победители
     * @param playerList - список игроков
     */
    public static void printResultsInGameEnd(final List<Player> winners, final List<Player> playerList) {
        LOGGER.debug("* Finalize *");
        LOGGER.info("---------------------------------------");
        LOGGER.info("Game OVER !!!");
        LOGGER.info("Winners: ");
        winners.forEach(winner ->
                LOGGER.info("Player {} - coins {} ", winner.getNickname(), winner.getCoins()));
        LOGGER.info("***************************************");
        LOGGER.info("Results of other players: ");
        playerList.forEach(player -> {
            if (!winners.contains(player)) {
                LOGGER.info("Player {} - coins {} ", player.getNickname(), player.getCoins());
            }
        });
    }

    /**
     * Вывод лога об ошибке
     *
     * @param exception - сопутствующее исключение
     */
    public static void printErrorLog(final Exception exception) {
        printErrorLog("ERROR!!!", exception);
    }

    /**
     * Вывод лога об ошибке
     *
     * @param message   - сообщение об ошибке
     * @param exception - сопутствующее исключение
     */
    public static void printErrorLog(final String message, final Exception exception) {
        LOGGER.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LOGGER.error(message, exception);
    }
}