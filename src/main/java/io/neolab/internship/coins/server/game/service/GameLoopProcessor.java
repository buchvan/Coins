package io.neolab.internship.coins.server.game.service;

import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.feature.CoefficientlyFeature;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.feature.FeatureType;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.ListProcessor;
import io.neolab.internship.coins.utils.Pair;

import java.util.*;

public class GameLoopProcessor {

    /**
     * Обновление данных игрока в начале раунда очередного игрового цикла игроком. К этому относится:
     * статус каждого юнита игрока - доступен,
     *
     * @param player          - игрок, чьи данные нужно обновить
     */
    public static void playerRoundBeginUpdate(final Player player) {
        makeAllUnitsSomeState(player, AvailabilityType.AVAILABLE);
        GameLogger.printRoundBeginUpdateLog(player);
    }

    /**
     * Конец раунда очередного игрового цикла игроком:
     * всех юнитов игрока сделать недоступными
     *
     * @param player - игрок, чьи данные нужно обновить согласно методу
     */
    public static void playerRoundEndUpdate(final Player player) {
        makeAllUnitsSomeState(player, AvailabilityType.NOT_AVAILABLE);
        GameLogger.printRoundEndUpdateLog(player);
    }

    /**
     * Метод для получения достижимых в один ход игроком клеток, не подконтрольных ему
     *
     * @param board           - борда
     * @param achievableCells - множество достижимых клеток
     * @param controlledCells - принадлежащие игроку клетки
     */
    public static void updateAchievableCells(final IBoard board, final Set<Cell> achievableCells,
                                             final List<Cell> controlledCells) {
        achievableCells.clear();
        if (controlledCells.isEmpty()) {
            achievableCells.addAll(board.getEdgeCells());
            return;
        }
        controlledCells.forEach(controlledCell -> {
            achievableCells.add(controlledCell);
            achievableCells.addAll(
                    getAllNeighboringCells(
                            board, controlledCell)); // добавляем всех соседей каждой клетки, занятой игроком
        });
    }

    /**
     * Метод взятия всех соседей клетки на борде
     *
     * @param board - борда, в рамках которой мы ищем соседей клетки
     * @param cell  - клетка, чьих соседей мы ищем
     * @return список всех соседей клетки cell на борде board
     */
    public static List<Cell> getAllNeighboringCells(final IBoard board, final Cell cell) {
        final List<Cell> neighboringCells = new LinkedList<>();
        final List<Position> neighboringPositions = Position.getAllNeighboringPositions(board.getPositionByCell(cell));
        neighboringPositions.forEach(neighboringPosition -> {
            final Cell potentiallyAchievableCell = board.getCellByPosition(neighboringPosition);
            if (potentiallyAchievableCell != null) { // если не вышли за пределы борды
                neighboringCells.add(potentiallyAchievableCell);
            }
        });
        return neighboringCells;
    }

    /**
     * Метод взятия всех крайних клеток борды
     *
     * @param board - борда, крайние клетки которой мы хотим взять
     * @return список всех крайних клеток борды board
     */
    public static List<Cell> getBoardEdgeCells(final IBoard board) {
        final List<Cell> boardEdgeCells = new LinkedList<>();
        int strIndex;
        int colIndex = 0;
        while (colIndex < board.getSizeY()) { // обход по верхней границе борды
            boardEdgeCells.add(board.getCellByPosition(0, colIndex));
            colIndex++;
        }
        strIndex = 1;
        colIndex--; // colIndex = BOARD_SIZE_Y;
        while (strIndex < board.getSizeX()) { // обход по правой границе борды
            boardEdgeCells.add(board.getCellByPosition(strIndex, colIndex));
            strIndex++;
        }
        strIndex--; // strIndex = BOARD_SIZE_X;
        colIndex--; // colIndex = BOARD_SIZE_Y - 1;
        while (colIndex >= 0) { // обход по нижней границе борды
            boardEdgeCells.add(board.getCellByPosition(strIndex, colIndex));
            colIndex--;
        }
        strIndex--; // strIndex = BOARD_SIZE_X - 1;
        colIndex++; // strIndex = 0;
        while (strIndex > 0) { // обход по левой границе борды
            boardEdgeCells.add(board.getCellByPosition(strIndex, colIndex));
            strIndex--;
        }
        return boardEdgeCells;
    }

    /**
     * Вход игрока в свою клетку
     *
     * @param player           - игрок
     * @param targetCell       - клетка, в которую игрок пытается войти
     * @param neighboringCells - соседние клетки
     * @param units            - список юнитов, которых игрок послал в клетку
     * @param board            - борда
     */
    public static void enterToCell(final Player player, final Cell targetCell, final List<Cell> neighboringCells,
                                   final List<Unit> units, final IBoard board) {
        neighboringCells.add(targetCell);
        final int tiredUnitsCount = targetCell.getType().getCatchDifficulty();
        final List<Unit> tiredUnits = units.subList(0, tiredUnitsCount);
        final List<Unit> achievableUnits = units.subList(tiredUnitsCount, units.size());
        withdrawUnits(neighboringCells, tiredUnits, achievableUnits);
        targetCell.getUnits().addAll(achievableUnits); // Вводим в захватываемую клетку оставшиеся доступные юниты
        makeAvailableUnitsToNotAvailable(player, tiredUnits);
    }

    /**
     * Метод получения числа юнитов, необходимых для захвата клетки
     *
     * @param gameFeatures - особенности игры
     * @param catchingCell - захватываемая клетка
     * @return число юнитов, необходимое для захвата клетки catchingCell
     */
    public static int getUnitsCountNeededToCatchCell(final GameFeatures gameFeatures,
                                                     final Cell catchingCell) {
        final Player defendingPlayer = catchingCell.getFeudal();
        int unitsCountNeededToCatch = catchingCell.getType().getCatchDifficulty();
        for (final Feature feature : gameFeatures.getFeaturesByRaceAndCellType(
                catchingCell.getRace(),
                catchingCell.getType())) { // Смотрим все особенности владельца

            if (feature.getType() == FeatureType.DEFENSE_CELL_CHANGING_UNITS_NUMBER) {
                unitsCountNeededToCatch += ((CoefficientlyFeature) feature).getCoefficient();
                GameLogger.printCatchCellDefenseFeatureLog(defendingPlayer.getNickname(), catchingCell);
            }
        }
        if (catchingCell.getUnits().size() > 0) { // если в захватываемой клетке есть юниты
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
    public static int getBonusAttackToCatchCell(final Player player,
                                                final GameFeatures gameFeatures,
                                                final Cell catchingCell) {
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
    public static void catchCell(final Player player,
                                 final Cell catchingCell,
                                 final List<Cell> neighboringCells,
                                 final List<Unit> tiredUnits,
                                 final List<Unit> units,
                                 final GameFeatures gameFeatures,
                                 final Map<Player, List<Cell>> ownToCells,
                                 final Map<Player, Set<Cell>> feudalToCells,
                                 final List<Cell> transitCells) {
        withdrawUnits(neighboringCells, tiredUnits, units);
        final Player defendingPlayer = catchingCell.getFeudal();
        final boolean isHasOpponent = isAlivePlayer(defendingPlayer);
        depriveCellFeudalAndOwner(catchingCell, isHasOpponent, ownToCells.get(defendingPlayer),
                feudalToCells.get(defendingPlayer));
        catchingCell.getUnits().addAll(units); // Вводим в захватываемую клетку оставшиеся доступные юниты
        makeAvailableUnitsToNotAvailable(player, tiredUnits);
        boolean catchingCellIsFeudalizable = true;
        for (final Feature feature : gameFeatures.getFeaturesByRaceAndCellType(
                player.getRace(), catchingCell.getType())) { // Смотрим все особенности агрессора

            catchingCellIsFeudalizable =
                    catchingCellIsFeudalizable &&
                            catchCellCheckFeature(isHasOpponent, defendingPlayer, feature);
        }
        giveCellFeudalAndOwner(player, catchingCell, catchingCellIsFeudalizable,
                transitCells, ownToCells.get(player), feudalToCells.get(player));
        GameLogger.printCatchCellBonusAttackLog(player);
    }

    /**
     * Вывести юнитов с клеток
     *
     * @param cells - клетки, с которых необходимо вывести юнитов
     * @param units - юниты, которых необходимо вывести с клеток
     */
    @SafeVarargs
    private static void withdrawUnits(final List<Cell> cells, final List<Unit>... units) {
        cells.forEach(neighboringCell ->
                neighboringCell.getUnits().removeIf(unit ->
                        Arrays.stream(units).anyMatch(unitsList -> unitsList.contains(unit))));
    }

    /**
     * Сделать подсписок доступных юнитов игрока недоступными
     *
     * @param player - игрок, подсписок доступных юнитов которого нужно сделать недоступными
     * @param units  - список доступных юнитов, которых необходимо сделать недоступными
     */
    private static void makeAvailableUnitsToNotAvailable(final Player player, final List<Unit> units) {
        player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).addAll(units);
        player.getUnitsByState(AvailabilityType.AVAILABLE).removeAll(units);
    }

    /**
     * Является ли игрок "живым", т. е. не ссылкой null?
     *
     * @param player - игрок, про которого необходимо выяснить, является ли он нейтральным
     * @return true - если игрок player не нейтрален в игре game, false - иначе
     */
    private static boolean isAlivePlayer(final Player player) {
        return player != null;
    }

    /**
     * Проверка особенности на CATCH_CELL_IMPOSSIBLE при захвате клетки и
     * попутная обработка всех остальных типов особенностей
     *
     * @param isHasOpponent - true - если владелец захватываемой клетки "живой", т. е. не ссылка null
     * @param cellOwner     - владелец захватываемой клетки
     * @param feature       - особенность пары (раса агрессора, тип захватываемой клетки), которая рассматривается
     * @return true - если feature не CATCH_CELL_IMPOSSIBLE, false - иначе
     */
    private static boolean catchCellCheckFeature(final boolean isHasOpponent,
                                                 final Player cellOwner,
                                                 final Feature feature) {
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
     * @param feudalIsAlive   - является ли владелец "живым" игроком? Т.е. не ссылкой null
     * @param controlledCells - принадлежащие игроку клетки
     * @param feudalCells     - клетки, приносящие монеты игроку
     */
    private static void depriveCellFeudalAndOwner(final Cell cell,
                                                  final boolean feudalIsAlive,
                                                  final List<Cell> controlledCells,
                                                  final Set<Cell> feudalCells) {
        cell.getUnits().clear(); // Юниты бывшего владельца с этой клетки убираются
        if (feudalIsAlive) {
            controlledCells.remove(cell);
            cell.setFeudal(null);
            feudalCells.remove(cell);
        }
    }

    /**
     * Дать клетке владельца и, возможно, феодала
     *
     * @param player             - новый владелец и, возможно, феодал клетки
     * @param cell               - клетка, нуждающаяся в новом владельце и феодале
     * @param cellIsFeudalizable - true - если клетка может приносить монеты, false - иначе
     * @param transitCells       - транзитные клетки игрока
     *                           (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     * @param controlledCells    - принадлежащие игроку клетки
     * @param feudalCells        - клетки, приносящие монеты игроку
     */
    private static void giveCellFeudalAndOwner(final Player player,
                                               final Cell cell,
                                               final boolean cellIsFeudalizable,
                                               final List<Cell> transitCells,
                                               final List<Cell> controlledCells,
                                               final Set<Cell> feudalCells) {
        cell.setRace(player.getRace());
        controlledCells.add(cell);
        if (cellIsFeudalizable) {
            feudalCells.add(cell);
            cell.setFeudal(player);
            return;
        } // else
        transitCells.add(cell);
    }

    /**
     * Убить какое-то количество юнитов игрока
     *
     * @param deadUnitsCount - кол-во, которое необходимо убить
     * @param player         - игрок, чьих юнитов необходимо убить
     */
    private static void killUnits(final int deadUnitsCount, final Player player) {
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
    public static void freeTransitCells(final Player player, final List<Cell> transitCells,
                                        final List<Cell> controlledCells) {
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
    public static void makeAllUnitsSomeState(final Player player, final AvailabilityType availabilityType) {
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
    public static void protectCell(final Player player, final Cell protectedCell, final List<Unit> units) {
        protectedCell.getUnits().addAll(units); // отправить первые unitsCount доступных юнитов
        makeAvailableUnitsToNotAvailable(player, units);
        GameLogger.printCellAfterDefendingLog(player, protectedCell);
    }

    /**
     * Обновить число монет у игрока
     *
     * @param player        - игрок, чьё число монет необходимо обновить
     * @param feudalToCells - множества клеток для каждого феодала
     * @param gameFeatures  - особенности игры
     * @param board         - как ни странно, борда :)
     */
    public static void updateCoinsCount(final Player player,
                                        final Map<Player, Set<Cell>> feudalToCells,
                                        final GameFeatures gameFeatures,
                                        final IBoard board) {

        feudalToCells.get(player).forEach(cell -> {
            updateCoinsCountByCellWithFeatures(player, gameFeatures, cell);
            player.setCoins(player.getCoins() + cell.getType().getCoinYield());
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
    private static void updateCoinsCountByCellWithFeatures(final Player player,
                                                           final GameFeatures gameFeatures,
                                                           final Cell cell) {

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
