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
     * @param player     - игрок, чьи данные нужно обновить
     * @param isLoggingTurnOn - включено логгирование?
     */
    public static void playerRoundBeginUpdate(final @NotNull Player player, final boolean isLoggingTurnOn) {
        makeAllUnitsSomeState(player, AvailabilityType.AVAILABLE);
        if (isLoggingTurnOn) {
            GameLogger.printRoundBeginUpdateLog(player);
        }
    }

    /**
     * Конец раунда очередного игрового цикла игроком:
     * всех юнитов игрока сделать недоступными
     *
     * @param player     - игрок, чьи данные нужно обновить согласно методу
     * @param isLoggingTurnOn - включено логгирование?
     */
    public static void playerRoundEndUpdate(final @NotNull Player player, final boolean isLoggingTurnOn) {
        makeAllUnitsSomeState(player, AvailabilityType.NOT_AVAILABLE);
        if (isLoggingTurnOn) {
            GameLogger.printRoundEndUpdateLog(player);
        }
    }

    public static void updateAchievableCellsAfterCatchCell(final @NotNull IBoard board, final @NotNull Cell catchingCell,
                                                           final @NotNull List<Cell> controlledCells,
                                                           final @NotNull Set<Cell> achievableCells) {
        if (controlledCells.size() == 1) { // если до этого у игрока не было клеток
            achievableCells.clear();
        }
        achievableCells.add(catchingCell);
        final List<Cell> neighboringCells =
                GameLoopProcessor.getAllNeighboringCells(board, Objects.requireNonNull(catchingCell));
        achievableCells.addAll(neighboringCells);
        neighboringCells.forEach(neighboringCell ->
                GameLoopProcessor.updateNeighboringCellsIfNecessary(board, neighboringCell));
    }

    /**
     * Метод для получения достижимых в один ход игроком клеток, не подконтрольных ему
     *
     * @param player          - игрок
     * @param board           - борда
     * @param achievableCells - множество достижимых клеток
     * @param controlledCells - принадлежащие игроку клетки
     * @param isLoggingTurnOn      - включено логгирование?
     */
    public static void updateAchievableCells(final @NotNull Player player, final @NotNull IBoard board,
                                             final @NotNull Set<Cell> achievableCells,
                                             final @NotNull List<Cell> controlledCells,
                                             final boolean isLoggingTurnOn) {
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
        if (isLoggingTurnOn) {
            GameLogger.printUpdateAchievableCellsLog(player, achievableCells);
        }
    }

    /**
     * Метод взятия всех соседей клетки на борде
     *
     * @param board - борда, в рамках которой мы ищем соседей клетки
     * @param cell  - клетка, чьих соседей мы ищем
     * @return список всех соседей клетки cell на борде board
     */
    public static @NotNull List<Cell> getAllNeighboringCells(final @NotNull IBoard board, final @NotNull Cell cell) {
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
     * @param isLoggingTurOn      - включено логгирование?
     */
    public static void enterToCell(final @NotNull Player player, final @NotNull Cell targetCell,
                                   final @NotNull List<Cell> controlledCells, final @NotNull Set<Cell> feudalCells,
                                   final @NotNull List<Unit> units, final int tiredUnitsCount, final @NotNull IBoard board,
                                   final boolean isLoggingTurOn) {
        final List<Cell> achievableCells = new LinkedList<>(GameLoopProcessor.getAllNeighboringCells(board, targetCell));
        achievableCells.add(targetCell);
        final List<Unit> tiredUnits = getTiredUnits(units, tiredUnitsCount);
        final List<Unit> achievableUnits = getRemainingAvailableUnits(units, tiredUnitsCount);
        withdrawUnits(achievableCells, controlledCells, feudalCells, isLoggingTurOn, tiredUnits, achievableUnits);
        targetCell.getUnits().addAll(achievableUnits); // Вводим в захватываемую клетку оставшиеся доступные юниты
        makeAvailableUnitsToNotAvailable(player, tiredUnits);
        if (isLoggingTurOn) {
            GameLogger.printAfterCellEnteringLog(player, targetCell);
        }
    }

    /**
     * Взять список уставших юнитов
     *
     * @param units           - список всех юнитов
     * @param tiredUnitsCount - число уставших юнитов
     * @return список уставших юнитов
     */
    public static @NotNull List<Unit> getTiredUnits(final @NotNull List<Unit> units, final int tiredUnitsCount) {
        return units.subList(0, tiredUnitsCount);
    }

    /**
     * Взять список оставшихся доступных юнитов
     *
     * @param units           - список всех юнитов
     * @param tiredUnitsCount - число уставших юнитов
     * @return список оставшихся доступных юнитов
     */
    public static @NotNull List<Unit> getRemainingAvailableUnits(final @NotNull List<Unit> units, final int tiredUnitsCount) {
        return units.subList(tiredUnitsCount, units.size());
    }

    /**
     * Метод получения числа юнитов, необходимых для захвата клетки
     *
     * @param gameFeatures - особенности игры
     * @param catchingCell - захватываемая клетка
     * @param isLoggingTurnOn   - включено логгирование?
     * @return число юнитов, необходимое для захвата клетки catchingCell
     */
    public static int getUnitsCountNeededToCatchCell(final @NotNull GameFeatures gameFeatures,
                                                     final @NotNull Cell catchingCell,
                                                     final boolean isLoggingTurnOn) {
        int unitsCountNeededToCatch = catchingCell.getType().getCatchDifficulty();
        final Player defendingPlayer = catchingCell.getFeudal();
        for (final Feature feature : gameFeatures.getFeaturesByRaceAndCellType(
                catchingCell.getRace(),
                catchingCell.getType())) { // Смотрим все особенности владельца

            if (feature.getType() == FeatureType.DEFENSE_CELL_CHANGING_UNITS_NUMBER) {
                unitsCountNeededToCatch += ((CoefficientlyFeature) feature).getCoefficient();
                if (isLoggingTurnOn) {
                    GameLogger.printCatchCellDefenseFeatureLog(defendingPlayer, catchingCell);
                }
            }
        }
        if (!catchingCell.getUnits().isEmpty()) { // если в захватываемой клетке есть юниты
            unitsCountNeededToCatch += catchingCell.getUnits().size() + 1;
        }
        if (isLoggingTurnOn) {
            GameLogger.printCatchCellCountNeededLog(unitsCountNeededToCatch);
        }
        return unitsCountNeededToCatch;
    }

    /**
     * Метод получения бонуса атаки при захвате клетки
     *
     * @param player       - игрок-агрессор
     * @param gameFeatures - особенности игры
     * @param catchingCell - захватываемая клетка
     * @param isLoggedOn   - включено логгирование?
     * @return бонус атаки (в числе юнитов) игрока player при захвате клетки catchingCell
     */
    public static int getBonusAttackToCatchCell(final @NotNull Player player,
                                                final @NotNull GameFeatures gameFeatures,
                                                final @NotNull Cell catchingCell, final boolean isLoggedOn) {
        int bonusAttack = 0;
        for (final Feature feature : gameFeatures.getFeaturesByRaceAndCellType(
                player.getRace(), catchingCell.getType())) { // Смотрим все особенности агрессора

            if (feature.getType() == FeatureType.CATCH_CELL_CHANGING_UNITS_NUMBER) {
                bonusAttack += ((CoefficientlyFeature) feature).getCoefficient();
                if (isLoggedOn) {
                    GameLogger.printCatchCellCatchingFeatureLog(player, catchingCell);
                }
            }
        }
        if (isLoggedOn) {
            GameLogger.printCatchCellBonusAttackLog(bonusAttack);
        }
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
     * @param isLoggingTurOn       - включено логгирование?
     */
    public static void catchCell(final @NotNull Player player,
                                 final @NotNull Cell catchingCell,
                                 final @NotNull List<Cell> neighboringCells,
                                 final @NotNull List<Unit> tiredUnits,
                                 final @NotNull List<Unit> units,
                                 final @NotNull GameFeatures gameFeatures,
                                 final @NotNull Map<Player, List<Cell>> ownToCells,
                                 final @NotNull Map<Player, Set<Cell>> feudalToCells,
                                 final @NotNull List<Cell> transitCells,
                                 final boolean isLoggingTurOn) {
        withdrawUnits(neighboringCells, ownToCells.get(player), feudalToCells.get(player), isLoggingTurOn,
                tiredUnits, units);
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
                            catchCellCheckFeature(defendingPlayer, feature, isLoggingTurOn);
        }
        giveCellFeudalAndOwner(player, catchingCell, catchingCellIsFeudalizable,
                transitCells, ownToCells.get(player), feudalToCells.get(player));
        if (isLoggingTurOn) {
            GameLogger.printCapturedCellLog(player);
        }
    }

    /**
     * Найти и удалить недоступные для захвата клетки юнитов
     *
     * @param board                        - борда
     * @param units                        - список юнитов
     * @param catchingCellNeighboringCells - клетки, соседние с захватываемой клеткой
     * @param catchingCell                 - захватываемая клетка
     * @param controlledCells              - контролируемые игроком клетки
     */
    public static void removeNotAvailableForCaptureUnits(final @NotNull IBoard board, final @NotNull List<Unit> units,
                                                         final @NotNull List<Cell> catchingCellNeighboringCells,
                                                         final @NotNull Cell catchingCell,
                                                         final @NotNull List<Cell> controlledCells) {
        final List<Cell> boardEdgeCells = board.getEdgeCells();
        final Iterator<Unit> iterator = units.iterator();
        while (iterator.hasNext()) {
            boolean unitAvailableForCapture = false;
            final Unit unit = iterator.next();
            for (final Cell neighboringCell : catchingCellNeighboringCells) {
                if (neighboringCell.getUnits().contains(unit)) {
                    unitAvailableForCapture = true;
                    break;
                }
            }
            if (boardEdgeCells.contains(catchingCell) && !unitAvailableForCapture) {
                unitAvailableForCapture = true;
                for (final Cell controlledCell : controlledCells) {
                    if (controlledCell.getUnits().contains(unit)) {
                        if (!catchingCellNeighboringCells.contains(controlledCell)) {
                            unitAvailableForCapture = false;
                        }
                        break;
                    }
                }
            }
            if (!unitAvailableForCapture) {
                iterator.remove();
            }
        }
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
                                      final @NotNull Set<Cell> feudalCells,
                                      final boolean isLoggingTurOn, final @NotNull List<Unit>... units) {
        cells.forEach(cell ->
                cell.getUnits().removeIf(unit ->
                        Arrays.stream(units).anyMatch(unitsList -> unitsList.contains(unit))));
        loseCells(cells, controlledCells, feudalCells);
        if (isLoggingTurOn) {
            GameLogger.printAfterWithdrawCellsLog(cells);
        }
    }

    /**
     * Потерять клетки, на которых нет юнитов игрока
     *
     * @param cells           - клетки, которые необходимо проверить на то, потеряны ли они
     * @param controlledCells - подконтрольные клетки игрока
     * @param feudalCells     - клетки, приносящие монеты игроку
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
     * @param cellOwner  - владелец захватываемой клетки
     * @param feature    - особенность пары (раса агрессора, тип захватываемой клетки), которая рассматривается
     * @param isLoggedOn - включено логгирование?
     * @return true - если feature не CATCH_CELL_IMPOSSIBLE, false - иначе
     */
    private static boolean catchCellCheckFeature(final @Nullable Player cellOwner,
                                                 final @NotNull Feature feature, final boolean isLoggedOn) {
        final boolean isHasOpponent = isAlivePlayer(cellOwner);
        if (isHasOpponent && feature.getType() == FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL) {
            int deadUnitsCount = ((CoefficientlyFeature) feature).getCoefficient();
            deadUnitsCount = Math.min(
                    deadUnitsCount,
                    cellOwner.getUnitsByState(AvailabilityType.NOT_AVAILABLE).size());
            killUnits(deadUnitsCount, cellOwner, isLoggedOn);
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
     * @param isLoggedOn     - включено логгирование?
     */
    private static void killUnits(final int deadUnitsCount, final @NotNull Player player, final boolean isLoggedOn) {
        ListProcessor.removeFirstN(deadUnitsCount, player.getUnitsByState(AvailabilityType.NOT_AVAILABLE));
        if (isLoggedOn) {
            GameLogger.printCatchCellUnitsDiedLog(player, deadUnitsCount);
        }
    }

    /**
     * Освобождение игроком всех его транзитных клеток
     *
     * @param player          - игрок, который должен освободить все свои транзитные клетки
     * @param transitCells    - транзитные клетки игрока
     *                        (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     * @param controlledCells - принадлежащие игроку клетки
     * @param isLoggedOn      - включено логгирование?
     */
    public static void freeTransitCells(final @NotNull Player player, final @NotNull List<Cell> transitCells,
                                        final @NotNull List<Cell> controlledCells, final boolean isLoggedOn) {
        if (isLoggedOn) {
            GameLogger.printTransitCellsLog(player, transitCells);
        }

        /* Игрок покидает каждую транзитную клетку */
        controlledCells.removeIf(transitCells::contains);
        transitCells.forEach(transitCell -> transitCell.getUnits().clear());
        transitCells.clear();

        if (isLoggedOn) {
            GameLogger.printFreedTransitCellsLog(player);
        }
    }

    /**
     * Перевести всех юнитов игрока в одно состояние
     *
     * @param player           - игрок, чьих юнитов нужно перевести в одно состояние
     * @param availabilityType - состояние, в которое нужно перевести всех юнитов игрока
     */
    public static void makeAllUnitsSomeState(final @NotNull Player player,
                                             final @NotNull AvailabilityType availabilityType) {
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
     * @param isLoggedOn    - включено логгирование?
     */
    public static void protectCell(final @NotNull Player player, final @NotNull Cell protectedCell,
                                   final @NotNull List<Unit> units, final boolean isLoggedOn) {
        protectedCell.getUnits().addAll(units); // отправить первые unitsCount доступных юнитов
        makeAvailableUnitsToNotAvailable(player, units);
        if (isLoggedOn) {
            GameLogger.printCellAfterDefendingLog(player, protectedCell);
        }
    }

    /**
     * Обновить число монет у игрока
     *
     * @param player       - игрок, чьё число монет необходимо обновить
     * @param feudalCells  - множество клеток, приносящих монеты
     * @param gameFeatures - особенности игры
     * @param board        - как ни странно, борда :)
     * @param isLoggedOn   - включено логгирование?
     */
    public static void updateCoinsCount(final @NotNull Player player,
                                        final @NotNull Set<Cell> feudalCells,
                                        final @NotNull GameFeatures gameFeatures,
                                        final @NotNull IBoard board, final boolean isLoggedOn) {
        final Map<CellType, Boolean> cellTypeMet = new HashMap<>(CellType.values().length);
        Arrays.stream(CellType.values()).forEach(cellType -> cellTypeMet.put(cellType, false));
        feudalCells.forEach(cell -> {
            updateCoinsCountByCellWithFeatures(player, gameFeatures, cell, cellTypeMet, isLoggedOn);
            player.increaseCoins(cell.getType().getCoinYield());
            if (isLoggedOn) {
                GameLogger.printPlayerCoinsCountByCellUpdatingLog(player, board.getPositionByCell(cell));
            }
        });
        if (isLoggedOn) {
            GameLogger.printPlayerCoinsCountUpdatingLog(player);
        }
    }

    /**
     * Обновить число монет у игрока, учитывая только особенности одной клетки
     *
     * @param player       - игрок, чьё число монет необходимо обновить
     * @param gameFeatures - особенности игры
     * @param cell         - клетка, чьи особенности мы рассматриваем
     * @param isLoggedOn   - включено логгирование?
     */
    private static void updateCoinsCountByCellWithFeatures(final @NotNull Player player,
                                                           final @NotNull GameFeatures gameFeatures,
                                                           final @NotNull Cell cell,
                                                           final @NotNull Map<CellType, Boolean> cellTypeMet,
                                                           final boolean isLoggedOn) {
        gameFeatures.getFeaturesByRaceAndCellType(player.getRace(), cell.getType())
                .forEach(feature -> {
                    if (feature.getType() == FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL) {
                        final int coefficient = ((CoefficientlyFeature) feature).getCoefficient();
                        player.increaseCoins(coefficient);
                        if (isLoggedOn) {
                            GameLogger.printPlayerCoinsCountByCellTypeUpdatingLog(player, cell.getType());
                        }
                    } else if (feature.getType() == FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL_GROUP
                            && !cellTypeMet.get(cell.getType())) {
                        cellTypeMet.put(cell.getType(), true);
                        final int coefficient = ((CoefficientlyFeature) feature).getCoefficient();
                        player.increaseCoins(coefficient);
                        if (isLoggedOn) {
                            GameLogger.printPlayerCoinsCountByCellTypeGroupUpdatingLog(player, cell.getType());
                        }
                    }
                });
    }
}
