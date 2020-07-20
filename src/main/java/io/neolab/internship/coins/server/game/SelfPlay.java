package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.client.IBot;
import io.neolab.internship.coins.client.SimpleBot;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.board.*;
import io.neolab.internship.coins.server.game.feature.CoefficientlyFeature;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.feature.FeatureType;
import io.neolab.internship.coins.server.game.service.*;
import io.neolab.internship.coins.utils.*;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.IOException;
import java.util.*;

public class SelfPlay {
    private static final int ROUNDS_COUNT = 10;

    private static final int BOARD_SIZE_X = 3;
    private static final int BOARD_SIZE_Y = 4;
    private static final int PLAYERS_COUNT = 2;

    private static final BidiMap<SimpleBot, Player> simpleBotToPlayer = new DualHashBidiMap<>(); // каждому симплботу
    // соответствует только один игрок, и наоборот


    /**
     * Игра сама с собой (self play)
     * - Создание борды
     * - Добавление метаинформации о игре(борда, игроки, юниты)
     * - Игровой цикл
     * - Финализатор (результат работы)
     */
    private static void selfPlay() {
        try (final GameLoggerFile loggerFile = new GameLoggerFile()) {
            LogCleaner.clean();
            final IGame game = GameInitializer.gameInit(BOARD_SIZE_X, BOARD_SIZE_Y, PLAYERS_COUNT);
            GameLogger.printGameCreatedLog(game);
            game.getPlayers().forEach(player -> simpleBotToPlayer.put(new SimpleBot(), player));
            gameLoop(game);
            GameFinalizer.finalize(game.getPlayers());
        } catch (final CoinsException | IOException exception) {
            GameLogger.printErrorLog(exception);
        }
    }

    /**
     * Игровой цикл, вся игровая логика начинается отсюда
     *
     * @param game - объект, хранящий всю метаинформацию об игровых сущностях
     */
    private static void gameLoop(final IGame game) {
        GameLogger.printStartGameChoiceLog();
        final List<Player> playerList = game.getPlayers();
        playerList.forEach(player -> chooseRace(player, simpleBotToPlayer.getKey(player), game));
        GameLogger.printStartGame();
        while (game.getCurrentRound() < ROUNDS_COUNT) { // Непосредственно игровой цикл
            game.incrementCurrentRound();
            GameLogger.printRoundBeginLog(game.getCurrentRound());
            playerList.forEach(player -> {
                GameLogger.printNextPlayerLog(player);

                // Раунд игрока. Все свои решения он принимает здесь
                playerRound(player, simpleBotToPlayer.getKey(player), game);
            });

            // обновление числа монет у каждого игрока
            playerList.forEach(player ->
                    updateCoinsCount(player, game.getFeudalToCells().get(player),
                            game.getGameFeatures(),
                            game.getBoard()));

            GameLogger.printRoundEndLog(game.getCurrentRound(), game.getPlayers(),
                    game.getOwnToCells(), game.getFeudalToCells());
        }
    }

    /**
     * Раунд в исполнении игрока
     *
     * @param player    - игрок, который исполняет раунд
     * @param simpleBot - симплбот игрока
     * @param game      - объект, хранящий всю метаинформацию об игре
     */
    private static void playerRound(final Player player, final IBot simpleBot, final IGame game) {
        GameLoopProcessor.playerRoundBeginUpdate(player);  // активация данных игрока в начале раунда
        if (simpleBot.declineRaceChoose(player, game)) { // В случае ответа "ДА" от симплбота на вопрос: "Идти в упадок?"
            declineRace(player, simpleBot, game); // Уход в упадок
        }
        catchCells(player, simpleBot, game); // Завоёвывание клеток
        distributionUnits(player, simpleBot, game); // Распределение войск
        GameLoopProcessor.playerRoundEndUpdate(player); // "затухание" (дезактивация) данных игрока в конце раунда
    }

    /**
     * Процесс упадка: потеря контроля над всеми клетками с сохранением от них дохода, выбор новой расы
     *
     * @param player    - игрок, который решил идти в упадок
     * @param simpleBot - симплбот игрока
     * @param game      - объект, хранящий всю метаинформацию об игре
     */
    private static void declineRace(final Player player, final IBot simpleBot, final IGame game) {
        GameLogger.printDeclineRaceLog(player);
        game.getOwnToCells().get(player).clear(); // Освобождаем все занятые игроком клетки (юниты остаются там же)
        changeRace(player, simpleBot, game);
    }

    /**
     * Процесс смены расы у игрока
     *
     * @param player    - игрок, который решил идти в упадок
     * @param simpleBot - симплбот игрока
     * @param game      - объект, хранящий всю метаинформацию об игре
     */
    private static void changeRace(final Player player, final IBot simpleBot, final IGame game) {
        Arrays.stream(AvailabilityType.values())
                .forEach(availabilityType ->
                        player.getUnitStateToUnits().get(availabilityType).clear()); // Чистим у игрока юниты
        final Race oldRace = player.getRace();
        chooseRace(player, simpleBot, game);
        game.getRacesPool().add(oldRace); // Возвращаем бывшую расу игрока в пул рас
    }

    /**
     * Метод для выбора новой расы игроком
     *
     * @param player    - игрок, выбирающий новую расу
     * @param simpleBot - симплбот игрока
     * @param game      - объект, хранящий всю метаинформацию об игре
     */
    private static void chooseRace(final Player player, final IBot simpleBot, final IGame game) {
        final Race newRace = simpleBot.chooseRace(player, game);
        game.getRacesPool().remove(newRace); // Удаляем выбранную игроком расу из пула
        player.setRace(newRace);

        /* Добавляем юнитов выбранной расы */
        int i = 0;
        while (i < newRace.getUnitsAmount()) {
            player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).add(new Unit());
            i++;
        }
        GameLogger.printChooseRaceLog(player, newRace);
    }

    /**
     * Перевести всех юнитов игрока в одно состояние
     *
     * @param player           - игрок, чьих юнитов нужно перевести в одно состояние
     * @param availabilityType - состояние, в которое нужно перевести всех юнитов игрока
     */
    private static void makeAllUnitsSomeState(final Player player, final AvailabilityType availabilityType) {
        for (final AvailabilityType item : AvailabilityType.values()) {
            if (item != availabilityType) {
                player.getUnitStateToUnits().get(availabilityType).addAll(player.getUnitStateToUnits().get(item));
                player.getUnitStateToUnits().get(item).clear();
            }
        }
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
     * Метод для завоёвывания клеток игроком
     *
     * @param player    - игрок, проводящий завоёвывание
     * @param simpleBot - симплбот игрока
     * @param game      - объект, хранящий всю метаинформацию об игре
     */
    private static void catchCells(final Player player, final IBot simpleBot, final IGame game) {
        GameLogger.printBeginCatchCellsLog(player);
        final IBoard board = game.getBoard();
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<Cell> transitCells = game.getPlayerToTransitCells().get(player);
        final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
        GameLoopProcessor.updateAchievableCells(board, achievableCells, controlledCells);
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        while (achievableCells.size() > 0 && availableUnits.size() > 0) {
            /* Пока есть что захватывать и какими войсками захватывать */
            final Pair<Position, List<Unit>> catchingCellToUnitsList = simpleBot.chooseCatchingCell(player, game);
            if (catchingCellToUnitsList == null) { // если игрок не захотел больше захватывать
                break;
            }
            final Cell catchingCell = board
                    .getCellByPosition(catchingCellToUnitsList.getFirst()); // клетка, которую игрок хочет захватить
            final List<Unit> units = catchingCellToUnitsList.getSecond(); // юниты для захвата этой клетки
            if (isCatchCellAttemptSucceed(player, catchingCell, units, board, game.getGameFeatures(),
                    game.getOwnToCells(), game.getFeudalToCells(),
                    transitCells)) { // если попытка захвата увеначалась успехом
                if (controlledCells.size() == 1) { // если до этого у игрока не было клеток
                    achievableCells.clear();
                    achievableCells.add(catchingCell);
                }
                achievableCells.addAll(GameLoopProcessor.getAllNeighboringCells(board, catchingCell));
                achievableCells.removeIf(controlledCells::contains); // удаляем те клетки, которые уже заняты игроком
            }
        }
    }

    /**
     * Метод попытки захвата одной клетки игроком
     *
     * @param player                - игрок, захватывающий клетку
     * @param catchingCell          - захватываемая клетка
     * @param units                 - список юнитов, направленных на захвать клетки
     * @param board                 - борда
     * @param gameFeatures          - особенности игры
     * @param ownToCells            - список подконтрольных клеток для каждого игрока
     * @param feudalToCells         - множества клеток для каждого феодала
     * @param transitCells          - транзитные клетки игрока
     * @return true - если попытка увенчалась успехом, false - иначе
     */
    private static boolean isCatchCellAttemptSucceed(final Player player,
                                                     final Cell catchingCell,
                                                     final List<Unit> units,
                                                     final IBoard board,
                                                     final GameFeatures gameFeatures,
                                                     final Map<Player, List<Cell>> ownToCells,
                                                     final Map<Player, Set<Cell>> feudalToCells,
                                                     final List<Cell> transitCells) {
        GameLogger.printCellCatchAttemptLog(player, board.getPositionByCell(catchingCell));
        GameLogger.printCatchCellUnitsQuantityLog(player.getNickname(), units.size());
        final boolean isControlled = ownToCells.get(player).contains(catchingCell);
        if (isControlled) {
            return tryEnterToCell(player, catchingCell, units, board);
        }
        final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(gameFeatures, catchingCell);
        final int bonusAttack = getBonusAttackToCatchCell(player, gameFeatures, catchingCell);
        if (!cellIsCatching(units.size() + bonusAttack, unitsCountNeededToCatch)) {
            GameLogger.printCatchCellNotCapturedLog(player.getNickname());
            return false;
        }
        final int tiredUnitsCount = unitsCountNeededToCatch - bonusAttack;
        final List<Cell> neighboringCells = GameLoopProcessor.getAllNeighboringCells(board, catchingCell);
        catchCell(player, catchingCell, neighboringCells, units.subList(0, tiredUnitsCount),
                units.subList(tiredUnitsCount, units.size()), gameFeatures, ownToCells, feudalToCells, transitCells);
        GameLogger.printAfterCellCatchingLog(player, catchingCell);
        return true;
    }

    /**
     * Попытка входа игрока в свою клетку
     *
     * @param player     - игрок
     * @param targetCell - клетка, в которую игрок пытается войти
     * @param units      - список юнитов, которых игрок послал в клетку
     * @param board      - борда
     * @return true - если попытка удачная, false - иначе
     */
    private static boolean tryEnterToCell(final Player player, final Cell targetCell, final List<Unit> units,
                                          final IBoard board) {
        final int tiredUnitsCount = targetCell.getType().getCatchDifficulty();
        if (IsPossibleEnterToCell(units.size(), tiredUnitsCount)) {
            final List<Cell> neighboringCells = GameLoopProcessor.getAllNeighboringCells(board, targetCell);
            neighboringCells.add(targetCell);
            final List<Unit> tiredUnits = units.subList(0, tiredUnitsCount);
            final List<Unit> achievableUnits = units.subList(tiredUnitsCount, units.size());
            withdrawUnits(neighboringCells, tiredUnits, achievableUnits);
            targetCell.getUnits().addAll(achievableUnits); // Вводим в захватываемую клетку оставшиеся доступные юниты
            makeAvailableUnitsToNotAvailable(player, tiredUnits);
            return true;
        }
        return false;
    }

    /**
     * Может ли игрок войти в свою клетку?
     *
     * @param unitsSize       - число юнитов, которых игрок послал в клетку
     * @param tiredUnitsCount - число уставших юнитов
     * @return true - если, игрок может войти в клетку, false - иначе
     */
    private static boolean IsPossibleEnterToCell(final int unitsSize, final int tiredUnitsCount) {
        return unitsSize >= tiredUnitsCount;
    }

    /**
     * Метод получения числа юнитов, необходимых для захвата клетки
     *
     * @param gameFeatures - особенности игры
     * @param catchingCell - захватываемая клетка
     * @return число юнитов, необходимое для захвата клетки catchingCell
     */
    private static int getUnitsCountNeededToCatchCell(final GameFeatures gameFeatures,
                                                      final Cell catchingCell) {
        int unitsCountNeededToCatch = catchingCell.getType().getCatchDifficulty();
        final Player defendingPlayer = catchingCell.getFeudal();
        for (final Feature feature : gameFeatures.getFeaturesByRaceAndCellType(
                catchingCell.getRace(),
                catchingCell.getType())) { // Смотрим все особенности владельца

            if (feature.getType() == FeatureType.DEFENSE_CELL_CHANGING_UNITS_NUMBER) {
                unitsCountNeededToCatch += ((CoefficientlyFeature) feature).getCoefficient();
                GameLogger.printCatchCellDefenseFeatureLog(
                        isAlivePlayer(defendingPlayer) ? defendingPlayer.getNickname() : "NULL", catchingCell);
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
    private static int getBonusAttackToCatchCell(final Player player,
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
     * Проверка на возможность захвата клетки
     *
     * @param attackPower          - сила атаки на клетку
     * @param necessaryAttackPower - необходимая сила атаки на эту клетку для её захвата
     * @return true - если клетку можно захватить, имея attackPower, false - иначе
     */
    private static boolean cellIsCatching(final int attackPower, final int necessaryAttackPower) {
        return attackPower >= necessaryAttackPower;
    }

    /**
     * Захватить клетку
     *
     * @param player                - игрок-агрессор
     * @param catchingCell          - захватываемая клетка
     * @param neighboringCells      - соседние с захватываемой клеткой клетки
     * @param tiredUnits            - список "уставших юнитов" (юнитов, которые перестанут быть доступными в этом раунде)
     * @param units                 - юниты, вошедшие в клетку
     * @param gameFeatures          - особенности игры
     * @param ownToCells            - список подконтрольных клеток для каждого игрока
     * @param feudalToCells         - множества клеток для каждого феодала
     * @param transitCells          - транзитные клетки игрока
     *                              (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     */
    private static void catchCell(final Player player,
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
        cells.forEach(cell ->
                cell.getUnits().removeIf(unit ->
                        Arrays.stream(units).anyMatch(unitsList -> unitsList.contains(unit))));
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
     * Убить какое-то количество юнитов игрока
     *
     * @param deadUnitsCount - кол-во, которое необходимо убить
     * @param player         - игрок, чьих юнитов необходимо убить
     */
    private static void killUnits(final int deadUnitsCount, final Player player) {
        ListProcessor.removeFirstN(deadUnitsCount, player.getUnitsByState(AvailabilityType.NOT_AVAILABLE));
        GameLogger.printCatchCellUnitsDiedLog(player, deadUnitsCount);
    }

    /**
     * Лишить клетку владельца и феодала
     *
     * @param cell            - клетка, которую нужно лишить владельца и феодала
     * @param isFeudalAlive   - является ли владелец "живым" игроком? Т.е. не ссылкой null
     * @param controlledCells - принадлежащие игроку клетки
     * @param feudalCells     - клетки, приносящие монеты игроку
     */
    private static void depriveCellFeudalAndOwner(final Cell cell,
                                                  final boolean isFeudalAlive,
                                                  final List<Cell> controlledCells,
                                                  final Set<Cell> feudalCells) {
        cell.getUnits().clear(); // Юниты бывшего владельца с этой клетки убираются
        if (isFeudalAlive) {
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
     * Метод для распределения юнитов игроком
     *
     * @param player    - игрок, делающий выбор
     * @param simpleBot - симплбот игрока
     * @param game      - объект, хранящий всю метаинформацию об игре
     */
    private static void distributionUnits(final Player player, final IBot simpleBot, final IGame game) {
        GameLogger.printBeginUnitsDistributionLog(player);
        final List<Cell> transitCells = game.getPlayerToTransitCells().get(player);
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        freeTransitCells(player, transitCells, controlledCells);
        controlledCells.forEach(controlledCell -> controlledCell.getUnits().clear());
        makeAllUnitsSomeState(player,
                AvailabilityType.AVAILABLE); // доступными юнитами становятся все имеющиеся у игрока юниты
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        if (controlledCells.size() > 0 && availableUnits.size() > 0) { // Если есть куда и какие распределять войска
            final Map<Position, List<Unit>> distributionUnits = simpleBot.distributionUnits(player, game);
            distributionUnits.forEach((position, units) -> {
                GameLogger.printCellDefendingLog(player, units.size(), position);
                protectCell(player, game.getBoard().getCellByPosition(position), units);
            });
        }
        GameLogger.printAfterDistributedUnitsLog(player);
    }

    /**
     * Освобождение игроком всех его транзитных клеток
     *
     * @param player          - игрок, который должен освободить все свои транзитные клетки
     * @param transitCells    - транзитные клетки игрока
     *                        (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     * @param controlledCells - принадлежащие игроку клетки
     */
    private static void freeTransitCells(final Player player, final List<Cell> transitCells,
                                         final List<Cell> controlledCells) {
        GameLogger.printTransitCellsLog(player, transitCells);

        /* Игрок покидает каждую транзитную клетку */
        controlledCells.removeIf(transitCells::contains);
        transitCells.forEach(transitCell -> transitCell.getUnits().clear());
        transitCells.clear();

        GameLogger.printFreedTransitCellsLog(player);
    }

    /**
     * Защитить клетку: владелец помещает в ней своих юнитов
     *
     * @param player        - владелец (в этой ситуации он же - феодал)
     * @param protectedCell - защищаемая клетка
     * @param units         - список юнитов, которых игрок хочет направить в клетку
     */
    private static void protectCell(final Player player, final Cell protectedCell, final List<Unit> units) {
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
    private static void updateCoinsCount(final Player player,
                                         final Set<Cell> feudalCells,
                                         final GameFeatures gameFeatures,
                                         final IBoard board) {
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


    public static void main(final String[] args) {
        selfPlay();
    }
}
