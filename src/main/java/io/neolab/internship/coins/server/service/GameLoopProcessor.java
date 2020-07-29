package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.feature.CoefficientlyFeature;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.feature.FeatureType;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.ListProcessor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Процессор, содержащий все основные функции, обновляющие игровые сущности (с выводами логов)
 */
public class GameLoopProcessor {

    /**
     * Обновление данных игрока в начале раунда очередного игрового цикла игроком. К этому относится:
     * статус каждого юнита игрока - доступен,
     *
     * @param player - игрок, чьи данные нужно обновить
     */
    public static void playerRoundBeginUpdate(final @NotNull Player player) {
        makeAllUnitsSomeState(player, AvailabilityType.AVAILABLE);
        GameLogger.printRoundBeginUpdateLog(player);
    }

    /**
     * Конец раунда очередного игрового цикла игроком:
     * всех юнитов игрока сделать недоступными
     *
     * @param player - игрок, чьи данные нужно обновить согласно методу
     */
    public static void playerRoundEndUpdate(final @NotNull Player player) {
        makeAllUnitsSomeState(player, AvailabilityType.NOT_AVAILABLE);
        GameLogger.printRoundEndUpdateLog(player);
    }

    /**
     * Метод для получения достижимых в один ход игроком клеток, не подконтрольных ему
     *
     * @param player          - игрок
     * @param board           - борда
     * @param achievableCells - множество достижимых клеток
     * @param controlledCells - принадлежащие игроку клетки
     */
    public static void updateAchievableCells(final @NotNull Player player, final @NotNull IBoard board,
                                             final @NotNull Set<Cell> achievableCells,
                                             final @NotNull List<Cell> controlledCells) {
        achievableCells.clear();
        if (controlledCells.isEmpty()) {
            achievableCells.addAll(board.getEdgeCells());
            board.getEdgeCells().forEach(edgeCell -> updateNeighboringCellsIfNecessary(board, edgeCell));
        } else {
            controlledCells.forEach(controlledCell -> {
                achievableCells.add(controlledCell);
                final List<Cell> neighboringCells = getAllNeighboringCells(board, controlledCell);
                achievableCells.addAll(neighboringCells); // добавляем всех соседей каждой клетки, занятой игроком
                neighboringCells.forEach(neighboringCell -> updateNeighboringCellsIfNecessary(board, neighboringCell));
            });
        }
        GameLogger.printUpdateAchievableCellsLog(player, achievableCells);
    }

    /**
     * Метод взятия всех соседей клетки на борде
     *
     * @param board - борда, в рамках которой мы ищем соседей клетки
     * @param cell  - клетка, чьих соседей мы ищем
     * @return список всех соседей клетки cell на борде board
     */
    static @NotNull List<Cell> getAllNeighboringCells(final @NotNull IBoard board, final @NotNull Cell cell) {
        updateNeighboringCellsIfNecessary(board, cell);
        return Objects.requireNonNull(board.getNeighboringCells(cell));
    }

    /**
     * Обновить список соседей клетки cell на борде board, если необходимо
     *
     * @param board - борда
     * @param cell  - клетка, чьих соседей нам нужно обновить
     */
    static void updateNeighboringCellsIfNecessary(final @NotNull IBoard board, final @NotNull Cell cell) {
        if (board.getNeighboringCells(cell) == null) {
            updateNeighboringCells(board, cell);
        }
    }

    /**
     * Обновить список соседей клетки cell на борде board
     *
     * @param board - борда
     * @param cell  - клетка, чьих соседей нам нужно обновить
     */
    private static void updateNeighboringCells(final @NotNull IBoard board, final @NotNull Cell cell) {
        final List<Cell> neighboringCells = new LinkedList<>();
        final List<Position> neighboringPositions = Position.getAllNeighboringPositions(board.getPositionByCell(cell));
        neighboringPositions.forEach(neighboringPosition -> {
            final Cell potentiallyNeighboringCell = board.getCellByPosition(neighboringPosition);
            if (potentiallyNeighboringCell != null) { // если не вышли за пределы борды
                neighboringCells.add(potentiallyNeighboringCell);
            }
        });
        board.putNeighboringCells(cell, neighboringCells);
    }

    /**
     * Вход игрока в свою клетку
     *
     * @param player          - игрок
     * @param targetCell      - клетка, в которую игрок пытается войти
     * @param controlledCells - подконтрольные игроку клетки
     * @param feudalCells     - клетки, приносящие монеты игроку
     * @param units           - список юнитов, которых игрок послал в клетку
     * @param tiredUnitsCount - число уставших юнитов
     * @param board           - борда
     */
    static void enterToCell(final @NotNull Player player, final @NotNull Cell targetCell,
                            final @NotNull List<Cell> controlledCells, final @NotNull Set<Cell> feudalCells,
                            final @NotNull List<Unit> units, final int tiredUnitsCount, final @NotNull IBoard board) {
        final List<Cell> neighboringCells = GameLoopProcessor.getAllNeighboringCells(board, targetCell);
        neighboringCells.add(targetCell);
        final List<Unit> tiredUnits = getTiredUnits(units, tiredUnitsCount);
        final List<Unit> achievableUnits = getRemainingAvailableUnits(units, tiredUnitsCount);
        withdrawUnits(neighboringCells, controlledCells, feudalCells, tiredUnits, achievableUnits);
        targetCell.getUnits().addAll(achievableUnits); // Вводим в захватываемую клетку оставшиеся доступные юниты
        makeAvailableUnitsToNotAvailable(player, tiredUnits);
        GameLogger.printAfterCellEnteringLog(player, targetCell);
    }

    /**
     * Взять список уставших юнитов
     *
     * @param units           - список всех юнитов
     * @param tiredUnitsCount - число уставших юнитов
     * @return список уставших юнитов
     */
    static @NotNull List<Unit> getTiredUnits(final @NotNull List<Unit> units, final int tiredUnitsCount) {
        return units.subList(0, tiredUnitsCount);
    }

    /**
     * Взять список оставшихся доступных юнитов
     *
     * @param units           - список всех юнитов
     * @param tiredUnitsCount - число уставших юнитов
     * @return список оставшихся доступных юнитов
     */
    static @NotNull List<Unit> getRemainingAvailableUnits(final @NotNull List<Unit> units, final int tiredUnitsCount) {
        return units.subList(tiredUnitsCount, units.size());
    }

    /**
     * Метод получения числа юнитов, необходимых для захвата клетки
     *
     * @param gameFeatures - особенности игры
     * @param catchingCell - захватываемая клетка
     * @return число юнитов, необходимое для захвата клетки catchingCell
     */
    public static int getUnitsCountNeededToCatchCell(final @NotNull GameFeatures gameFeatures,
                                              final @NotNull Cell catchingCell) {
        int unitsCountNeededToCatch = catchingCell.getType().getCatchDifficulty();
        final Player defendingPlayer = catchingCell.getFeudal();
        for (final Feature feature : gameFeatures.getFeaturesByRaceAndCellType(
                catchingCell.getRace(),
                catchingCell.getType())) { // Смотрим все особенности владельца

            if (feature.getType() == FeatureType.DEFENSE_CELL_CHANGING_UNITS_NUMBER) {
                unitsCountNeededToCatch += ((CoefficientlyFeature) feature).getCoefficient();
                GameLogger.printCatchCellDefenseFeatureLog(defendingPlayer, catchingCell);
            }
        }
        if (!catchingCell.getUnits().isEmpty()) { // если в захватываемой клетке есть юниты
            unitsCountNeededToCatch += catchingCell.getUnits().size() + 1;
        }
        GameLogger.printCatchCellCountNeededLog(unitsCountNeededToCatch);
        return unitsCountNeededToCatch;
    }

    /**
     * Метод получения бонуса атаки при захвате клетки
     *
     * @param player       - игрок-агрессор
     * @param gameFeatures - особенности игры
     * @param catchingCell - захватываемая клетка
     * @return бонус атаки (в числе юнитов) игрока player при захвате клетки catchingCell
     */
    static int getBonusAttackToCatchCell(final @NotNull Player player,
                                         final @NotNull GameFeatures gameFeatures,
                                         final @NotNull Cell catchingCell) {
        int bonusAttack = 0;
        for (final Feature feature : gameFeatures.getFeaturesByRaceAndCellType(
                player.getRace(), catchingCell.getType())) { // Смотрим все особенности агрессора

            if (feature.getType() == FeatureType.CATCH_CELL_CHANGING_UNITS_NUMBER) {
                bonusAttack += ((CoefficientlyFeature) feature).getCoefficient();
                GameLogger.printCatchCellCatchingFeatureLog(player, catchingCell);
            }
        }
        GameLogger.printCatchCellBonusAttackLog(bonusAttack);
        return bonusAttack;
    }

    /**
     * Захватить клетку
     *
     * @param player           - игрок-агрессор
     * @param catchingCell     - захватываемая клетка
     * @param neighboringCells - соседние с захватываемой клеткой клетки
     * @param tiredUnits       - список "уставших юнитов" (юнитов, которые перестанут быть доступными в этом раунде)
     * @param units            - юниты, вошедшие в клетку
     * @param gameFeatures     - особенности игры
     * @param ownToCells       - список подконтрольных клеток для каждого игрока
     * @param feudalToCells    - множества клеток для каждого феодала
     * @param transitCells     - транзитные клетки игрока
     *                         (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     */
    static void catchCell(final @NotNull Player player,
                          final @NotNull Cell catchingCell,
                          final @NotNull List<Cell> neighboringCells,
                          final @NotNull List<Unit> tiredUnits,
                          final @NotNull List<Unit> units,
                          final @NotNull GameFeatures gameFeatures,
                          final @NotNull Map<Player, List<Cell>> ownToCells,
                          final @NotNull Map<Player, Set<Cell>> feudalToCells,
                          final @NotNull List<Cell> transitCells) {
        withdrawUnits(neighboringCells, ownToCells.get(player), feudalToCells.get(player), tiredUnits, units);
        final Player defendingPlayer = catchingCell.getFeudal();
        depriveCellFeudalAndOwner(catchingCell, defendingPlayer, ownToCells.get(defendingPlayer),
                feudalToCells.get(defendingPlayer));
        catchingCell.getUnits().addAll(units); // Вводим в захватываемую клетку оставшиеся доступные юниты
        makeAvailableUnitsToNotAvailable(player, tiredUnits);
        boolean catchingCellIsFeudalizable = true;
        for (final Feature feature : gameFeatures.getFeaturesByRaceAndCellType(
                player.getRace(), catchingCell.getType())) { // Смотрим все особенности агрессора

            catchingCellIsFeudalizable =
                    catchingCellIsFeudalizable &&
                            catchCellCheckFeature(defendingPlayer, feature);
        }
        giveCellFeudalAndOwner(player, catchingCell, catchingCellIsFeudalizable,
                transitCells, ownToCells.get(player), feudalToCells.get(player));
        GameLogger.printCapturedCellLog(player);
    }

    /**
     * Вывести юнитов с клеток
     *
     * @param cells           - клетки, с которых необходимо вывести юнитов
     * @param controlledCells - подконтрольные клетки игрока
     * @param feudalCells     - клетки, приносящие монеты игроку
     * @param units           - юниты, которых необходимо вывести с клеток
     */
    @SafeVarargs
    private static void withdrawUnits(final @NotNull List<Cell> cells, final @NotNull List<Cell> controlledCells,
                                      final @NotNull Set<Cell> feudalCells, final @NotNull List<Unit>... units) {
        cells.forEach(cell ->
                cell.getUnits().removeIf(unit ->
                        Arrays.stream(units).anyMatch(unitsList -> unitsList.contains(unit))));
        loseCells(cells, controlledCells, feudalCells);
        GameLogger.printAfterWithdrawCellsLog(cells);
    }

    /**
     * Потерять клетки, на которых нет юнитов игрока
     *
     * @param cells       - клетки, которые необходимо проверить на то, потеряны ли они
     * @param feudalCells - клетки, приносящие монеты игроку
     */
    public static void loseCells(final @NotNull List<Cell> cells,
                                 final @NotNull List<Cell> controlledCells, final @NotNull Set<Cell> feudalCells) {
        final Iterator<Cell> iterator = cells.iterator();
        final List<Cell> lostCells = new LinkedList<>();
        while (iterator.hasNext()) {
            final Cell cell = iterator.next();
            if (cell.getUnits().isEmpty()) {
                lostCells.add(cell);
                cell.setFeudal(null);
                iterator.remove();
            }
        }
        feudalCells.removeAll(lostCells);
        controlledCells.removeAll(lostCells);
    }

    /**
     * Сделать подсписок доступных юнитов игрока недоступными
     *
     * @param player - игрок, подсписок доступных юнитов которого нужно сделать недоступными
     * @param units  - список доступных юнитов, которых необходимо сделать недоступными
     */
    private static void makeAvailableUnitsToNotAvailable(final @NotNull Player player,
                                                         final @NotNull List<Unit> units) {
        player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).addAll(units);
        player.getUnitsByState(AvailabilityType.AVAILABLE).removeAll(units);
    }

    /**
     * Является ли игрок "живым", т. е. не ссылкой null?
     *
     * @param player - игрок, про которого необходимо выяснить, является ли он нейтральным
     * @return true - если игрок player не нейтрален в игре game, false - иначе
     */
    @Contract(value = "null -> false; !null -> true", pure = true)
    private static boolean isAlivePlayer(final @Nullable Player player) {
        return player != null;
    }

    /**
     * Проверка особенности на CATCH_CELL_IMPOSSIBLE при захвате клетки и
     * попутная обработка всех остальных типов особенностей
     *
     * @param cellOwner - владелец захватываемой клетки
     * @param feature   - особенность пары (раса агрессора, тип захватываемой клетки), которая рассматривается
     * @return true - если feature не CATCH_CELL_IMPOSSIBLE, false - иначе
     */
    private static boolean catchCellCheckFeature(final @Nullable Player cellOwner,
                                                 final @NotNull Feature feature) {
        final boolean isHasOpponent = isAlivePlayer(cellOwner);
        if (isHasOpponent && feature.getType() == FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL) {
            int deadUnitsCount = ((CoefficientlyFeature) feature).getCoefficient();
            deadUnitsCount = Math.min(
                    deadUnitsCount,
                    cellOwner.getUnitsByState(AvailabilityType.NOT_AVAILABLE).size());
            killUnits(deadUnitsCount, cellOwner);
            return true;
        } //else если клетка не будет давать монет
        return feature.getType() != FeatureType.CATCH_CELL_IMPOSSIBLE;
    }

    /**
     * Лишить клетку владельца и феодала
     *
     * @param cell            - клетка, которую нужно лишить владельца и феодала
     * @param cellFeudal      - владелец клетки cell
     * @param controlledCells - принадлежащие игроку клетки
     * @param feudalCells     - клетки, приносящие монеты игроку
     */
    private static void depriveCellFeudalAndOwner(final @NotNull Cell cell,
                                                  final @Nullable Player cellFeudal,
                                                  final @Nullable List<Cell> controlledCells,
                                                  final @Nullable Set<Cell> feudalCells) {
        cell.getUnits().clear(); // Юниты бывшего владельца с этой клетки убираются
        final boolean isFeudalAlive = isAlivePlayer(cellFeudal);
        if (isFeudalAlive) {
            Objects.requireNonNull(controlledCells).remove(cell);
            cell.setFeudal(null);
            Objects.requireNonNull(feudalCells).remove(cell);
        }
    }

    /**
     * Дать клетке владельца и, возможно, феодала
     *
     * @param player             - новый владелец и, возможно, феодал клетки
     * @param cell               - клетка, нуждающаяся в новом владельце и феодале
     * @param isCellFeudalizable - true - если клетка может приносить монеты, false - иначе
     * @param transitCells       - транзитные клетки игрока
     *                           (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     * @param controlledCells    - принадлежащие игроку клетки
     * @param feudalCells        - клетки, приносящие монеты игроку
     */
    private static void giveCellFeudalAndOwner(final @NotNull Player player,
                                               final @NotNull Cell cell,
                                               final boolean isCellFeudalizable,
                                               final @NotNull List<Cell> transitCells,
                                               final @NotNull List<Cell> controlledCells,
                                               final @NotNull Set<Cell> feudalCells) {
        cell.setRace(player.getRace());
        controlledCells.add(cell);
        if (isCellFeudalizable) {
            feudalCells.add(cell);
            cell.setFeudal(player);
            return;
        }
        transitCells.add(cell);
    }

    /**
     * Убить какое-то количество юнитов игрока
     *
     * @param deadUnitsCount - кол-во, которое необходимо убить
     * @param player         - игрок, чьих юнитов необходимо убить
     */
    private static void killUnits(final int deadUnitsCount, final @NotNull Player player) {
        ListProcessor.removeFirstN(deadUnitsCount, player.getUnitsByState(AvailabilityType.NOT_AVAILABLE));
        GameLogger.printCatchCellUnitsDiedLog(player, deadUnitsCount);
    }

    /* Освобождение игроком всех его транзитных клеток
     *
     * @param player          - игрок, который должен освободить все свои транзитные клетки
     * @param transitCells    - транзитные клетки игрока
     *                        (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     * @param controlledCells - принадлежащие игроку клетки
     */
    public static void freeTransitCells(final @NotNull Player player, final @NotNull List<Cell> transitCells,
                                        final @NotNull List<Cell> controlledCells) {
        GameLogger.printTransitCellsLog(player, transitCells);

        /* Игрок покидает каждую транзитную клетку */
        controlledCells.removeIf(transitCells::contains);
        transitCells.forEach(transitCell -> transitCell.getUnits().clear());
        transitCells.clear();

        GameLogger.printFreedTransitCellsLog(player);
    }

    /**
     * Перевести всех юнитов игрока в одно состояние
     *
     * @param player           - игрок, чьих юнитов нужно перевести в одно состояние
     * @param availabilityType - состояние, в которое нужно перевести всех юнитов игрока
     */
    static void makeAllUnitsSomeState(final @NotNull Player player, final @NotNull AvailabilityType availabilityType) {
        for (final AvailabilityType item : AvailabilityType.values()) {
            if (item != availabilityType) {
                player.getUnitStateToUnits().get(availabilityType).addAll(player.getUnitStateToUnits().get(item));
                player.getUnitStateToUnits().get(item).clear();
            }
        }
    }

    /**
     * Защитить клетку: владелец помещает в ней своих юнитов
     *
     * @param player        - владелец (в этой ситуации он же - феодал)
     * @param protectedCell - защищаемая клетка
     * @param units         - список юнитов, которых игрок хочет направить в клетку
     */
    static void protectCell(final @NotNull Player player, final @NotNull Cell protectedCell,
                            final @NotNull List<Unit> units) {
        protectedCell.getUnits().addAll(units); // отправить первые unitsCount доступных юнитов
        makeAvailableUnitsToNotAvailable(player, units);
        GameLogger.printCellAfterDefendingLog(player, protectedCell);
    }

    /**
     * Обновить число монет у игрока
     *
     * @param player       - игрок, чьё число монет необходимо обновить
     * @param feudalCells  - множество клеток, приносящих монеты
     * @param gameFeatures - особенности игры
     * @param board        - как ни странно, борда :)
     */
    public static void updateCoinsCount(final @NotNull Player player,
                                        final @NotNull Set<Cell> feudalCells,
                                        final @NotNull GameFeatures gameFeatures,
                                        final @NotNull IBoard board) {
        feudalCells.forEach(cell -> {
            updateCoinsCountByCellWithFeatures(player, gameFeatures, cell);
            player.increaseCoins(cell.getType().getCoinYield());
            GameLogger.printPlayerCoinsCountByCellUpdatingLog(player, board.getPositionByCell(cell));
        });
        GameLogger.printPlayerCoinsCountUpdatingLog(player);
    }

    /**
     * Обновить число монет у игрока, учитывая только особенности одной клетки
     *
     * @param player       - игрок, чьё число монет необходимо обновить
     * @param gameFeatures - особенности игры
     * @param cell         - клетка, чьи особенности мы рассматриваем
     */
    private static void updateCoinsCountByCellWithFeatures(final @NotNull Player player,
                                                           final @NotNull GameFeatures gameFeatures,
                                                           final @NotNull Cell cell) {

        final Map<CellType, Boolean> cellTypeMet = new HashMap<>(CellType.values().length);
        Arrays.stream(CellType.values()).forEach(cellType -> cellTypeMet.put(cellType, false));
        gameFeatures.getFeaturesByRaceAndCellType(player.getRace(), cell.getType())
                .forEach(feature -> {
                    if (feature.getType() == FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL) {
                        final int coefficient = ((CoefficientlyFeature) feature).getCoefficient();
                        player.increaseCoins(coefficient);
                        GameLogger.printPlayerCoinsCountByCellTypeUpdatingLog(player, cell.getType());
                    } else if (feature.getType() == FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL_GROUP
                            && !cellTypeMet.get(cell.getType())) {

                        cellTypeMet.put(cell.getType(), true);
                        final int coefficient = ((CoefficientlyFeature) feature).getCoefficient();
                        player.increaseCoins(coefficient);
                        GameLogger.printPlayerCoinsCountByCellTypeGroupUpdatingLog(player, cell.getType());
                    }
                });
    }
}
