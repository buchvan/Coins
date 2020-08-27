package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.utils.AvailabilityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Класс, содержащий основные функции вывода логов
 */
public class GameLogger {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(GameLogger.class);

    /**
     * Вывод лога о файле, куда ведётся логгирование
     *
     * @param logFileName - имя файла-лога
     */
    public static void printLogFileLog(final @NotNull String logFileName) {
        LOGGER.debug("* Logging in file {} *", logFileName);
    }

    /**
     * Вывод лога о создании игры
     *
     * @param game - созданная игра
     */
    public static void printGameCreatedLog(final @NotNull IGame game) {
        LOGGER.debug("---");
        LOGGER.debug("Game is created: ");
        LOGGER.debug("Board: {} ", game.getBoard());
        LOGGER.debug("Players: {} ", game.getPlayers());
        LOGGER.debug("Pool of races: {} ", game.getRacesPool());
        LOGGER.debug("---");
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
    public static void printChooseRaceLog(final @NotNull Player player, final @NotNull Race newRace) {
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
     * Вывод лога об обновлении игрока в начале его раунда
     *
     * @param player - игрок, который обновляется
     */
    public static void printRoundBeginUpdateLog(final @NotNull Player player) {
        LOGGER.info("Player {} is updated in round begin ", player.getNickname());
    }

    /**
     * Вывод лога об обновлении игрока в конце его раунда
     *
     * @param player - игрок, который обновляется
     */
    public static void printRoundEndUpdateLog(final @NotNull Player player) {
        LOGGER.info("Player {} is updated in round end ", player.getNickname());
    }

    /**
     * Вывод лога о следующем игроке
     *
     * @param player - следующий игрок
     */
    public static void printNextPlayerLog(final @NotNull Player player) {
        LOGGER.info("Next player: {} ", player.getNickname());
    }

    /**
     * Вывод лога об упадке
     *
     * @param player - игрок в упадке
     */
    public static void printDeclineRaceLog(final @NotNull Player player) {
        LOGGER.info("* Player {} in decline of race! *", player.getNickname());
    }

    /**
     * Вывод лога об обновлении множества достижимых игроком клеток
     *
     * @param player          - игрок
     * @param achievableCells - множество достижимых клеток
     */
    public static void printUpdateAchievableCellsLog(final @NotNull Player player,
                                                     final @NotNull Set<Cell> achievableCells) {
        LOGGER.info("Player {} updated his achievable cells: {} ", player.getNickname(), achievableCells);
    }

    /**
     * Вывод лога о начале захвата клеток
     *
     * @param player - игрок, начинающий захватывать клетки
     */
    public static void printBeginCatchCellsLog(final @NotNull Player player) {
        LOGGER.debug("=================================");
        LOGGER.debug("* Player {} captures cells! *", player.getNickname());
    }

    /**
     * Вывод лога о попытке входа в свою клетку
     *
     * @param player       - игрок, пытающийся войти юнитами в свою клетку
     * @param cellPosition - позиция клетки на борде
     */
    public static void printCellTryEnterLog(final @NotNull Player player, final @NotNull Position cellPosition) {
        LOGGER.debug("Player {} try enter to his cell {} ", player.getNickname(), cellPosition);
    }

    /**
     * Вывод лога о выборе игроком числа юнитов для захвата клетки
     *
     * @param player     - игрок, выбравший число юнитов для входа в свою клетку
     * @param unitsCount - выбранное число юнитов
     */
    public static void printCellTryEnterUnitsQuantityLog(final @NotNull Player player, final int unitsCount) {
        LOGGER.debug("Player {} try enter to his cell units in quantity {} ", player.getNickname(), unitsCount);
    }

    /**
     * Вывод лога о неудачном входе в свою клетку
     *
     * @param player - игрок, который не смог войти в свою клетку
     */
    public static void printCellNotEnteredLog(final @NotNull Player player) {
        LOGGER.debug("The cell is not entered. The player {} retreated ", player.getNickname());
    }

    /**
     * Вывод лога после входа в свою клетку
     *
     * @param player - игрок, вошедший в свою клетку
     * @param cell   - клетка игрока
     */
    public static void printAfterCellEnteringLog(final @NotNull Player player, final @NotNull Cell cell) {
        LOGGER.debug("+++++++++++++++++++++++++++++++");
        LOGGER.debug("Cell after entering: ");
        printCellInformationLog(cell);
        LOGGER.debug("Player after entering: ");
        printPlayerUnitsInformationLog(player);
        LOGGER.debug("+++++++++++++++++++++++++++++++");
    }

    /**
     * Вывод лога после освобождения юнитами клеток
     *
     * @param cells - список клеток
     */
    public static void printAfterWithdrawCellsLog(final @NotNull List<Cell> cells) {
        LOGGER.debug("Cells after withdraw of units: ");
        cells.forEach(GameLogger::printCellInformationLog);
    }

    /**
     * Вывод лога с информацией о клетке
     *
     * @param cell - клетка
     */
    public static void printCellInformationLog(final @NotNull Cell cell) {
        LOGGER.debug("--- CellType: {} ", cell.getType().getTitle());
        LOGGER.debug("--- Race: {} ", cell.getRace() != null ? cell.getRace().getTitle() : "NULL");
        LOGGER.debug("--- Feudal: {} ", cell.getFeudal() != null ? cell.getFeudal().getNickname() : "NULL");
        LOGGER.debug("--- Units: {} ", cell.getUnits());
    }

    /**
     * Вывод лога с информацией о юнитов игрока
     *
     * @param player - игрок
     */
    public static void printPlayerUnitsInformationLog(final @NotNull Player player) {
        LOGGER.debug("--- Available units: {} ", player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE));
        LOGGER.debug("--- Not available units: {} ", player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE));
    }

    /**
     * Вывод лога о попытке захвата клетки
     *
     * @param player       - игрок, пытающийся захватить клетку
     * @param cellPosition - позиция клетки на борде
     */
    public static void printCellCatchAttemptLog(final @NotNull Player player, final @NotNull Position cellPosition) {
        LOGGER.debug("Player {} catch attempt the cell {} ", player.getNickname(), cellPosition);
    }

    /**
     * Вывод лога о выборе игроком числа юнитов для захвата клетки
     *
     * @param aggressor  - игрок, выбравший число юнитов для захвата
     * @param unitsCount - выбранное число юнитов
     */
    public static void printCatchCellUnitsQuantityLog(final @NotNull Player aggressor, final int unitsCount) {
        LOGGER.debug("Player {} capture units in quantity {} ", aggressor.getNickname(), unitsCount);
    }

    /**
     * Вывод лога о неудачном захвате клетки
     *
     * @param player - игрок, который не смог захватить клетку
     */
    public static void printCatchCellNotCapturedLog(final @NotNull Player player) {
        LOGGER.debug("The cell is not captured. The aggressor {} retreated ", player.getNickname());
    }

    /**
     * Вывод лога о применении особенности при обороне клетки
     *
     * @param defendingPlayer - игрок, защищающий клетку
     * @param catchingCell    - захватываемая клетка
     */
    public static void printCatchCellDefenseFeatureLog(final @Nullable Player defendingPlayer,
                                                       final @NotNull Cell catchingCell) {
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
    public static void printCatchCellCatchingFeatureLog(final @NotNull Player player,
                                                        final @NotNull Cell catchingCell) {
        LOGGER.debug("Player {} took advantage of the feature race {} and cellType of catchCell {}",
                player.getNickname(), Objects.requireNonNull(player.getRace()).getTitle(),
                catchingCell.getType().getTitle());
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
    public static void printCapturedCellLog(final @NotNull Player player) {
        LOGGER.info("Cell is captured of player {} ", player.getNickname());
    }

    /**
     * Вывод лога о смерти юнитов игрока
     *
     * @param player         - игрок, чьи юниты погибли
     * @param deadUnitsCount - число погибших юнитов игрока
     */
    public static void printCatchCellUnitsDiedLog(final @NotNull Player player, final int deadUnitsCount) {
        LOGGER.debug("{} units of player {} died ", deadUnitsCount, player.getNickname());
    }

    /**
     * Вывод лога после захвата клетки
     *
     * @param player       - игрок, захвативший клетку
     * @param catchingCell - захваченная клетка
     */
    public static void printAfterCellCatchingLog(final @NotNull Player player, final @NotNull Cell catchingCell) {
        LOGGER.debug("+++++++++++++++++++++++++++++++");
        LOGGER.debug("Cell after catching: ");
        printCellInformationLog(catchingCell);
        LOGGER.debug("Player after catching: ");
        printPlayerUnitsInformationLog(player);
        LOGGER.debug("+++++++++++++++++++++++++++++++");
    }

    /**
     * Вывод лога о транизитных клетках игрока
     *
     * @param player       - игрок, чьи транзитные клетки мы логгируем
     * @param transitCells - транзитные клетки игрока
     *                     (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     */
    public static void printTransitCellsLog(final @NotNull Player player, final @NotNull List<Cell> transitCells) {
        LOGGER.debug("* Transit cells of player {}: ", player.getNickname());
        transitCells.forEach(GameLogger::printCellInformationLog);
    }

    /**
     * Вывод лога о начале фазы распределения войск
     *
     * @param player - игрок, чьи транзитные клетки мы логгируем
     */
    public static void printBeginUnitsDistributionLog(final @NotNull Player player) {
        LOGGER.debug("=======================================");
        LOGGER.debug("* Player {} is distributes units! *", player.getNickname());
    }

    /**
     * Вывод лога об освобождении транизитных клеток игроком
     *
     * @param player - игрок, который освободил свои транзитные клетки
     */
    public static void printFreedTransitCellsLog(final @NotNull Player player) {
        LOGGER.debug("Player {} freed his transit cells ", player.getNickname());
    }

    /**
     * Вывод лога о намерении игрока распределить юниты в клетку
     *
     * @param player     - игрок, распределяющий юнитов в клетку
     * @param unitsCount - число юнитов, направленных в эту клетку
     * @param position   - позиция клетки, в которую распределяют юнитов
     */
    public static void printCellDefendingLog(final @NotNull Player player, final int unitsCount,
                                             final @NotNull Position position) {
        LOGGER.debug("Player {} protects by {} units the cell in position {}",
                player.getNickname(), unitsCount, position);
    }

    /**
     * Вывод лога о последствиях в данных после распределения войск в клетку
     *
     * @param player        - игрок, распределивший юнитов в клетку
     * @param protectedCell - клетка, в которую распределили юнитов
     */
    public static void printCellAfterDefendingLog(final @NotNull Player player, final @NotNull Cell protectedCell) {
        LOGGER.debug("Cell after defending: ");
        printCellInformationLog(protectedCell);
        LOGGER.debug("Player {} after defending: ", player.getNickname());
        printPlayerUnitsInformationLog(player);
    }

    /**
     * Вывод лога после фазы распределения войск
     *
     * @param player - игрок, завершивший фазу распределения войск
     */
    public static void printAfterDistributedUnitsLog(final @NotNull Player player) {
        LOGGER.info("Player {} distributed units ", player.getNickname());
    }

    /**
     * Вывод лога об обновлении числа монет у игрока по клетке
     *
     * @param player       - игрок, у которого обновилось число монет
     * @param cellPosition - позиция клетки, по которой обновилось число монет у игрока
     */
    public static void printPlayerCoinsCountByCellUpdatingLog(final @NotNull Player player,
                                                              final @NotNull Position cellPosition) {
        LOGGER.debug("Player {} update coins by cell in position {} ", player.getNickname(), cellPosition);
    }

    /**
     * Вывод лога об обновлении числа монет у игрока по типу клетки
     *
     * @param player   - игрок, у которого обновилось число монет
     * @param cellType - тип клетки, по которому у игрока обновилось число монет
     */
    public static void printPlayerCoinsCountByCellTypeUpdatingLog(final @NotNull Player player,
                                                                  final @NotNull CellType cellType) {
        LOGGER.debug("Player {} update coins by cellType {} ", player.getNickname(), cellType.getTitle());
    }

    /**
     * Вывод лога об обновлении числа монет у игрока по отдельной группе типа клетки
     *
     * @param player   - игрок, у которого обновилось число монет
     * @param cellType - тип клетки, по которому у игрока обновилось число монет
     */
    public static void printPlayerCoinsCountByCellTypeGroupUpdatingLog(final @NotNull Player player,
                                                                       final @NotNull CellType cellType) {
        LOGGER.debug("Player {} update coins by group cellType {} ", player.getNickname(), cellType.getTitle());
    }

    /**
     * Вывод лога об обновлении общего числа монет у игрока
     *
     * @param player - игрок, у которого обновилось число монет
     */
    public static void printPlayerCoinsCountUpdatingLog(final @NotNull Player player) {
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
    public static void printRoundEndLog(final int currentRound, final @NotNull List<Player> playerList,
                                        final @NotNull Map<Player, List<Cell>> ownToCells,
                                        final @NotNull Map<Player, Set<Cell>> feudalToCells) {
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
    public static void printPlayersInformation(final @NotNull List<Player> playerList,
                                               final @NotNull Map<Player, List<Cell>> ownToCells,
                                               final @NotNull Map<Player, Set<Cell>> feudalToCells) {

        playerList.forEach(player ->
                LOGGER.debug("Player {}: [ coins {}, feudal for: {} cells, controlled: {} cells ] ",
                        player.getNickname(), player.getCoins(),
                        feudalToCells.get(player).size(), ownToCells.get(player).size()));
    }

    /**
     * Выводит лога о результатах игры
     *
     * @param winners    - победители
     * @param playerList - список игроков
     */
    public static void printResultsInGameEnd(final @NotNull List<Player> winners,
                                             final @NotNull List<Player> playerList) {
        LOGGER.debug("* Finalize *");
        LOGGER.info("---------------------------------------");
        LOGGER.info("Game OVER !!!");
        LOGGER.info("Winners: ");
        winners.forEach(winner ->
                LOGGER.info("--- Player {} - coins {} ", winner.getNickname(), winner.getCoins()));
        LOGGER.info("***************************************");
        LOGGER.info("Results of other players: ");
        playerList.forEach(player -> {
            if (!winners.contains(player)) {
                LOGGER.info("--- Player {} - coins {} ", player.getNickname(), player.getCoins());
            }
        });
    }

    /**
     * Вывод лога об ошибке
     *
     * @param exception - сопутствующее исключение
     */
    public static void printErrorLog(final @NotNull Exception exception) {
        printErrorLog("ERROR!!!", exception);
    }

    /**
     * Вывод лога об ошибке
     *
     * @param message   - сообщение об ошибке
     * @param exception - сопутствующее исключение
     */
    public static void printErrorLog(final @NotNull String message, final @NotNull Exception exception) {
        LOGGER.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LOGGER.error(message, exception);
    }
}
