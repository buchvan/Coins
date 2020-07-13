package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.*;
import io.neolab.internship.coins.server.game.feature.CoefficientlyFeature;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.feature.FeatureType;
import io.neolab.internship.coins.server.game.service.GameFinalizer;
import io.neolab.internship.coins.server.game.service.GameInitializer;
import io.neolab.internship.coins.server.game.service.GameLogger;
import io.neolab.internship.coins.server.game.service.GameLoggerFile;
import io.neolab.internship.coins.utils.*;

import java.util.*;

public class SelfPlay {
    private static final int ROUNDS_COUNT = 10;

    private static final int BOARD_SIZE_X = 3;
    private static final int BOARD_SIZE_Y = 4;

    private static final Random random = new Random(); // объект для "бросания монетки" (взятия рандомного числа)


    /**
     * Игра сама с собой (self play)
     * - Создание борды
     * - Добавление метаинформации о игре(борда, игроки, юниты)
     * - Игровой цикл
     * - Финализатор (результат работы)
     */
    private static void selfPlay() {

        try (final GameLoggerFile loggerFile = new GameLoggerFile()) {
            final Game game = GameInitializer.gameInit(BOARD_SIZE_X, BOARD_SIZE_Y);
            GameLogger.printGameCreatedLog(game);
            gameLoop(game);
            GameFinalizer.finalize(game.getPlayers());
        } catch (final Exception exception) { // TODO: своё исключение
            GameLogger.printErrorLog(exception);
        }
    }

    /**
     * Игровой цикл, вся игровая логика начинается отсюда
     *
     * @param game - объект, хранящий всю метаинформацию об игровых сущностях
     */
    private static void gameLoop(final Game game) {
        GameLogger.printStartGameChoiceLog();
        for (final Player player : game.getPlayers()) {
            chooseRace(player, game.getRacesPool());
        }
        GameLogger.printStartGame();
        while (game.getCurrentRound() < ROUNDS_COUNT) { // Непосредственно игровой цикл
            game.incrementCurrentRound();
            GameLogger.printRoundBeginLog(game.getCurrentRound());
            game.getPlayers()
                    .forEach(player -> {
                        GameLogger.printNextPlayerLog(player);
                        playerRound(player, game.getNeutralPlayer(), game.getBoard(), game.getRacesPool(),
                                game.getGameFeatures(), game.getOwnToCells(), game.getFeudalToCells(),
                                game.getPlayerToTransitCells().get(player)
                        ); // раунд игрока. Все свои решения он принимает здесь
                    });
            game.getPlayers()
                    .forEach(player ->
                            updateCoinsCount(player, game.getFeudalToCells(),
                                    game.getGameFeatures(),
                                    game.getBoard()));  // обновление числа монет у каждого игрока
            GameLogger.printRoundEndLog(game.getCurrentRound(), game.getPlayers(),
                    game.getOwnToCells(), game.getFeudalToCells());
        }
    }

    /**
     * Раунд в исполнении игрока
     *
     * @param player        - игрок, который исполняет раунд
     * @param neutralPlayer - нейтральный игрок
     * @param board         - борда
     * @param racesPool     - пул всех доступных рас
     * @param gameFeatures  - особенности игры
     * @param ownToCells    - список подконтрольных клеток для каждого игрока
     * @param feudalToCells - множества клеток для каждого феодала
     * @param transitCells  - транзитные клетки игрока
     */
    private static void playerRound(final Player player, final Player neutralPlayer, final IBoard board,
                                    final List<Race> racesPool,
                                    final GameFeatures gameFeatures,
                                    final Map<Player, List<Cell>> ownToCells,
                                    final Map<Player, Set<Cell>> feudalToCells,
                                    final List<Cell> transitCells) {

        playerRoundBeginUpdate(player, ownToCells.get(player));  // активация данных игрока в начале раунда
        if (isSayYes()) { // В случае ответа "ДА" от игрока на вопрос: "Идти в упадок?"
            declineRace(player, neutralPlayer, racesPool,
                    ownToCells.get(player), feudalToCells.get(player)); // Уход в упадок
        }
        catchCells(player, neutralPlayer, board, gameFeatures,
                ownToCells, feudalToCells, transitCells); // Завоёвывание клеток

        distributionUnits(player, transitCells, ownToCells.get(player), board); // Распределение войск
        playerRoundEndUpdate(player); // "затухание" (дезактивация) данных игрока в конце раунда
    }

    /**
     * Обновление данных игрока в начале раунда очередного игрового цикла игроком. К этому относится (пока что):
     * статус каждого юнита игрока - доступен,
     * снятие юнитов игрока с клеток, в которые они были распределены
     *
     * @param player          - игрок, чьи данные нужно обновить
     * @param controlledCells - принадлежащие игроку клетки
     */
    private static void playerRoundBeginUpdate(final Player player, final List<Cell> controlledCells) {
        makeAllUnitsSomeState(player, AvailabilityType.AVAILABLE);
        controlledCells.forEach(cell -> cell.getUnits().clear());
    }

    /**
     * Конец раунда очередного игрового цикла игроком:
     * всех юнитов игрока сделать недоступными
     *
     * @param player - игрок, чьи данные нужно обновить согласно методу
     */
    private static void playerRoundEndUpdate(final Player player) {
        makeAllUnitsSomeState(player, AvailabilityType.NOT_AVAILABLE);
    }

    /**
     * Метод для определения сказанного слова игроком
     *
     * @return true - если игрок сказал да, false - нет
     */
    private static boolean isSayYes() {
        return random.nextInt(2) == 1;
    }

    /**
     * Процесс упадка: потеря контроля над всеми клетками с сохранением от них дохода, выбор новой расы
     *
     * @param player          - игрок, который решил идти в упадок
     * @param neutralPlayer   - нейтральный игрок
     * @param racesPool       - пул всех доступных рас
     * @param controlledCells - принадлежащие игроку клетки
     * @param feudalCells     - клетки, приносящие монеты игроку
     */
    private static void declineRace(final Player player, final Player neutralPlayer,
                                    final List<Race> racesPool, final List<Cell> controlledCells,
                                    final Set<Cell> feudalCells) {
        GameLogger.printDeclineRaceLog(player);
        feudalCells
                .forEach(cell ->
                        cell.setOwn(neutralPlayer)); // Освобождаем все занятые игроком клетки (юниты остаются там же)
        controlledCells.clear();
        changeRace(player, racesPool);
    }

    /**
     * Процесс смены расы у игрока
     *
     * @param player    - игрок, который решил идти в упадок
     * @param racesPool - пул всех доступных рас
     */
    private static void changeRace(final Player player, final List<Race> racesPool) {
        final Race oldRace = player.getRace();
        Arrays.stream(AvailabilityType.values())
                .forEach(availabilityType ->
                        player.getUnitStateToUnits().get(availabilityType).clear()); // Чистим у игрока юниты
        chooseRace(player, racesPool);
        racesPool.add(oldRace); // Возвращаем бывшую расу игрока в пул рас
    }

    /**
     * Метод для выбора новой расы игроком
     *
     * @param player    - игрок, выбирающий новую расу
     * @param racesPool - пул всех доступных рас
     */
    private static void chooseRace(final Player player, final List<Race> racesPool) {
        final Race newRace = RandomGenerator.chooseItemFromList(racesPool);
        racesPool.remove(newRace); // Удаляем выбранную игроком расу из пула
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
     * Сделать первые N доступных юнитов игрока недоступными
     *
     * @param player - игрок, первые N доступных юнитов которого нужно сделать недоступными
     * @param N      - то число доступных юнитов, которых необходимо сделать недоступными
     */
    private static void makeNAvailableUnitsToNotAvailable(final Player player, final int N) {
        final Iterator<Unit> iterator = player.getUnitsByState(AvailabilityType.AVAILABLE).iterator();
        int i = 0;
        while (iterator.hasNext() && i < N) {
            player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).add(iterator.next());
            iterator.remove();
            i++;
        }
//        int i = 0;
//        for (final Unit unit : unitStateToUnits.get(UnitState.AVAILABLE)) {
//            if (i >= N) {
//                break;
//            }
//            unitStateToUnits.get(UnitState.NOT_AVAILABLE).add(unit);
//            i++;
//        }
//        unitStateToUnits.get(UnitState.AVAILABLE)
//                .removeIf(unit -> unitStateToUnits.get(UnitState.NOT_AVAILABLE).contains(unit));
    }

    /**
     * Метод для завоёвывания клеток игроком
     *
     * @param player        - игрок, проводящий завоёвывание
     * @param neutralPlayer - нейтральный игрок
     * @param board         - борда
     * @param gameFeatures  - особенности игры
     * @param ownToCells    - список подконтрольных клеток для каждого игрока
     * @param feudalToCells - множества клеток для каждого феодала
     * @param transitCells  - транзитные клетки игрока
     */
    private static void catchCells(final Player player, final Player neutralPlayer,
                                   final IBoard board,
                                   final GameFeatures gameFeatures,
                                   final Map<Player, List<Cell>> ownToCells,
                                   final Map<Player, Set<Cell>> feudalToCells,
                                   final List<Cell> transitCells) {

        GameLogger.printBeginCatchCellsLog(player);
        final List<Cell> controlledCells = ownToCells.get(player);
        final List<Cell> achievableCells = getAchievableCells(board, controlledCells);
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        while (achievableCells.size() > 0 && availableUnits.size() > 0 && isSayYes()) {
            /* Пока есть что захватывать и
                какими войсками захватывать и
                 ответ "ДА" от игрока на вопрос: "Продолжить захват клеток?" */

            final Cell catchingCell = RandomGenerator
                    .chooseItemFromList(achievableCells); // клетка, которую игрок хочет захватить
            if (catchCellAttempt(player, catchingCell, neutralPlayer, board, gameFeatures,
                    ownToCells, feudalToCells, transitCells)) { // если попытка захвата увеначалась успехом

                achievableCells.remove(catchingCell);
                achievableCells.addAll(getAllNeighboringCells(board, catchingCell));
                achievableCells.removeIf(controlledCells::contains); // удаляем те клетки, которые уже заняты игроком
            }
        }
    }

    /**
     * Метод для получения достижимых в один ход игроком клеток, не подконтрольных ему
     *
     * @param controlledCells - принадлежащие игроку клетки
     * @return список достижимых в один ход игроком клеток, не подконтрольных ему
     */
    private static List<Cell> getAchievableCells(final IBoard board,
                                                 final List<Cell> controlledCells) {
        if (controlledCells.isEmpty()) {
            return boardEdgeGetCells(board);
        } // else
        final Set<Cell> achievableCells = new HashSet<>();
        for (final Cell cell : controlledCells) {
            achievableCells.addAll(
                    getAllNeighboringCells(board, cell)); // добавляем всех соседей каждой клетки, занятой игроком
        }
        achievableCells.removeIf(controlledCells::contains); // удаляем те клетки, которые уже заняты игроком
        return new LinkedList<>(achievableCells);
    }

    /**
     * Метод взятия всех крайних клеток борды
     *
     * @param board - борда, крайние клетки которой мы хотим взять
     * @return список всех крайних клеток борды board
     */
    private static List<Cell> boardEdgeGetCells(final IBoard board) {
        final List<Cell> boardEdgeCells = new LinkedList<>();
        int strIndex;
        int colIndex = 0;
        while (colIndex < BOARD_SIZE_Y) { // обход по верхней границе борды
            boardEdgeCells.add(board.getCellByPosition(0, colIndex));
            colIndex++;
        }
        strIndex = 1;
        colIndex--; // colIndex = BOARD_SIZE_Y;
        while (strIndex < BOARD_SIZE_X) { // обход по правой границе борды
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
     * Метод взятия всех соседей клетки на борде
     *
     * @param board - борда, в рамках которой мы ищем соседей клетки
     * @param cell  - клетка, чьих соседей мы ищем
     * @return список всех соседей клетки cell на борде board
     */
    private static List<Cell> getAllNeighboringCells(final IBoard board, final Cell cell) {
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
     * Метод попытки захвата одной клетки игроком
     *
     * @param player        - игрок, захватывающий клетку
     * @param catchingCell  - захватываемая клетка
     * @param neutralPlayer - нейтральный игрок
     * @param board         - борда
     * @param gameFeatures  - особенности игры
     * @param ownToCells    - список подконтрольных клеток для каждого игрока
     * @param feudalToCells - множества клеток для каждого феодала
     * @param transitCells  - транзитные клетки игрока
     * @return true - если попытка увенчалась успехом, false - иначе
     */
    private static boolean catchCellAttempt(final Player player, final Cell catchingCell, final Player neutralPlayer,
                                            final IBoard board,
                                            final GameFeatures gameFeatures,
                                            final Map<Player, List<Cell>> ownToCells,
                                            final Map<Player, Set<Cell>> feudalToCells,
                                            final List<Cell> transitCells) {
        GameLogger.printCellCatchAttemptLog(player, board.getPositionByCell(catchingCell));

        final int unitsCount = RandomGenerator.chooseNumber(player.getUnitsByState(
                AvailabilityType.AVAILABLE).size()); // число юнитов, которое игрок хочет направить в эту клетку

        GameLogger.printCatchCellUnitsQuantityLog(player, unitsCount);
        final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(gameFeatures, catchingCell);
        final int bonusAttack = getBonusAttackToCatchCell(player, gameFeatures, catchingCell);
        if (!cellIsCatching(unitsCount + bonusAttack, unitsCountNeededToCatch)) {
            GameLogger.printCatchCellNotCapturedLog(player);
            return false;
        } // else
        catchCell(player, catchingCell, unitsCountNeededToCatch - bonusAttack, neutralPlayer,
                gameFeatures, ownToCells, feudalToCells, transitCells);
        GameLogger.printAfterCellCatchingLog(player, catchingCell);
        return true;
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
        final Player defendingPlayer = catchingCell.getOwn();
        int unitsCountNeededToCatch = catchingCell.getType().getCatchDifficulty();
        for (final Feature feature : gameFeatures.getFeaturesByRaceAndCellType(
                catchingCell.getRace(),
                catchingCell.getType())) { // Смотрим все особенности владельца

            if (feature.getType() == FeatureType.DEFENSE_CELL_CHANGING_UNITS_NUMBER) {
                unitsCountNeededToCatch += ((CoefficientlyFeature) feature).getCoefficient();
                GameLogger.printCatchCellDefenseFeatureLog(defendingPlayer, catchingCell);
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
     * @param player          - игрок-агрессор
     * @param catchingCell    - захватываемая клетка
     * @param tiredUnitsCount - количество "уставших юнитов" (юнитов, которые перестанут быть доступными в этом раунде)
     * @param neutralPlayer   - нейтральный игрок
     * @param gameFeatures    - особенности игры
     * @param ownToCells      - список подконтрольных клеток для каждого игрока
     * @param feudalToCells   - множества клеток для каждого феодала
     * @param transitCells    - транзитные клетки игрока
     *                        (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     */
    private static void catchCell(final Player player, final Cell catchingCell,
                                  final int tiredUnitsCount, final Player neutralPlayer,
                                  final GameFeatures gameFeatures,
                                  final Map<Player, List<Cell>> ownToCells,
                                  final Map<Player, Set<Cell>> feudalToCells,
                                  final List<Cell> transitCells) {

        makeNAvailableUnitsToNotAvailable(player, tiredUnitsCount); // все юниты, задействованные в захвате клетки,
        // становятся недоступными

        final Player defendingPlayer = catchingCell.getOwn();
        boolean catchingCellIsFeudalizable = true;
        final boolean haveARival = isAlivePlayer(defendingPlayer, neutralPlayer);

        for (final Feature feature : gameFeatures.getFeaturesByRaceAndCellType(
                player.getRace(), catchingCell.getType())) { // Смотрим все особенности агрессора

            catchingCellIsFeudalizable =
                    catchingCellIsFeudalizable &&
                            catchCellCheckFeature(catchingCell, haveARival, feature);
        }
        if (defendingPlayer != null) {
            depriveCellFeudalAndOwner(catchingCell, haveARival, ownToCells.get(player), feudalToCells.get(player));
        }
        giveCellFeudalAndOwner(player, catchingCell, catchingCellIsFeudalizable,
                transitCells, ownToCells.get(player), feudalToCells.get(player));
        GameLogger.printCatchCellBonusAttackLog(player);
    }

    /**
     * Является ли игрок "живым", т. е. не ссылкой null и не нейтральным игроком?
     *
     * @param player        - игрок, про которого необходимо выяснить, является ли он нейтральным
     * @param neutralPlayer - нейтральный игрок
     * @return true - если игрок player не нейтрален в игре game, false - иначе
     */
    private static boolean isAlivePlayer(final Player player, final Player neutralPlayer) {
        return player != null && isNotNeutralPlayer(player, neutralPlayer);
    }

    /**
     * Является ли игрок нейтральным?
     *
     * @param player        - игрок, про которого необходимо выяснить, является ли он нейтральным
     * @param neutralPlayer - нейтральный игрок
     * @return true - если игрок player не нейтрален в игре game, false - иначе
     */
    private static boolean isNotNeutralPlayer(final Player player, final Player neutralPlayer) {
        return player != neutralPlayer; // можно сравнивать ссылки,
        // так как нейтральный игрок в игре имеется в единственном экземпляре
    }

    /**
     * Проверка особенности на CATCH_CELL_IMPOSSIBLE при захвате клетки и
     * попутная обработка всех остальных типов особенностей
     *
     * @param catchingCell - захватываемая клетка
     * @param haveARival   - true - если владелец захватываемой клетки "живой", т. е. не ссылка null и не нейтральный
     * @param feature      - особенность пары (раса агрессора, тип захватываемой клетки), которая рассматривается
     * @return true - если feature не CATCH_CELL_IMPOSSIBLE, false - иначе
     */
    private static boolean catchCellCheckFeature(final Cell catchingCell, final boolean haveARival,
                                                 final Feature feature) {

        if (haveARival && feature.getType() == FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL) {
            final Player defendingPlayer = catchingCell.getOwn();
            int deadUnitsCount = ((CoefficientlyFeature) feature).getCoefficient();
            deadUnitsCount = Math.min(
                    deadUnitsCount,
                    defendingPlayer.getUnitsByState(AvailabilityType.NOT_AVAILABLE).size());
            killUnits(deadUnitsCount, defendingPlayer);
            return true;
        } //else клетка не будет давать монет
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
     * @param ownIsAlive      - является ли владелец "живым" игроком? Т.е. не ссылкой null и не нейтральным игроком
     * @param controlledCells - принадлежащие игроку клетки
     * @param feudalCells     - клетки, приносящие монеты игроку
     */
    private static void depriveCellFeudalAndOwner(final Cell cell,
                                                  final boolean ownIsAlive,
                                                  final List<Cell> controlledCells,
                                                  final Set<Cell> feudalCells) {
        cell.getUnits().clear(); // Юниты бывшего владельца с этой клетки убираются
        feudalCells.remove(cell);
        cell.setFeudal(null);
        if (ownIsAlive) {
            controlledCells.remove(cell);
        }
        cell.setOwn(null);
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
        cell.setOwn(player);
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
     * @param player          - игрок, делающий выбор
     * @param transitCells    - транзитные клетки игрока
     *                        (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     * @param controlledCells - принадлежащие игроку клетки
     * @param board           - борда
     */
    private static void distributionUnits(final Player player, final List<Cell> transitCells,
                                          final List<Cell> controlledCells, final IBoard board) {
        GameLogger.printBeginUnitsDistributionLog(player);
        freeTransitCells(player, transitCells, controlledCells);
        makeAllUnitsSomeState(player,
                AvailabilityType.AVAILABLE); // доступными юнитами становятся все имеющиеся у игрока юниты
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        if (controlledCells.size() > 0) { // Если есть куда распределять войска
            while (availableUnits.size() > 0 && isSayYes()) {
                /* Пока есть какие войска распределять и
                ответ "ДА" от игрока на вопрос: "Продолжить распределять войска?" */

                final Cell protectedCell = RandomGenerator.chooseItemFromList(
                        controlledCells); // клетка, в которую игрок хочет распределить войска

                final int unitsCount = RandomGenerator.chooseNumber(
                        availableUnits.size()); // число юнитов, которое игрок хочет распределить в эту клетку

                GameLogger.printCellDefendingLog(player, unitsCount, board.getPositionByCell(protectedCell));
                protectCell(player, availableUnits, protectedCell, unitsCount);
            }
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
        transitCells.forEach(transitCell -> transitCell.setOwn(null));
        transitCells.clear();

        GameLogger.printFreedTransitCellsLog(player);
    }

    /**
     * Защитить клетку: владелец помещает в ней своих юнитов
     *
     * @param player         - владелец (в этой ситуации он же - феодал)
     * @param availableUnits - список доступных юнитов
     * @param protectedCell  - защищаемая клетка
     * @param unitsCount     - число юнитов, которое игрок хочет направить в клетку
     */
    private static void protectCell(final Player player, final List<Unit> availableUnits,
                                    final Cell protectedCell, final int unitsCount) {
        protectedCell.getUnits()
                .addAll(availableUnits.subList(0, unitsCount)); // отправить первые unitsCount доступных юнитов
        makeNAvailableUnitsToNotAvailable(player, unitsCount);
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
    private static void updateCoinsCount(final Player player,
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


    public static void main(final String[] args) {
        selfPlay();
    }
}
