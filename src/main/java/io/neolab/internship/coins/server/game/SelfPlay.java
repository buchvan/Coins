package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.*;
import io.neolab.internship.coins.server.game.feature.CoefficientlyFeature;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.feature.FeatureType;
import io.neolab.internship.coins.utils.*;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

public class SelfPlay {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfPlay.class);

    private static final int ROUNDS_COUNT = 10; // > 0

    private static final int BOARD_SIZE_X = 3;
    private static final int BOARD_SIZE_Y = 4;

    private static final Random random = new Random(); // объект для "бросания монетки" (взятия рандомного числа)


    /**
     * Игра сама с собой (self play)
     * - Создание борды
     * - Добавление метаинформации о борде (игроки, количество юнитов и т.п.)
     * - игровой цикл
     * - финализатор (результат работы)
     */
    private static void selfPlay() {

        /* генерируем имя файла-лога (self-play__HH-mm-ss) */
        final String logFileName = "self-play__" +
                new Date().toString().split(" ")[3].replaceAll(":", "-");

        try {
            MDC.put("logFileName", logFileName);
            LoggerProcessor.printDebug(LOGGER, "* Logging in file {} *", logFileName);

            final Game game = GameInitializer.gameInit(BOARD_SIZE_X, BOARD_SIZE_Y);

            LoggerProcessor.printDebug(LOGGER, "Game is created: {} ", game);

            gameLoop(game);

            finalize(game.getPlayers());
        } catch (final Exception exception) { // TODO: своё исключение
            LoggerProcessor.printError(LOGGER, "ERROR!!! ", exception);
        } finally {
            LoggerProcessor.printDebug(LOGGER, "* Logs in file {} *", logFileName);
            MDC.remove("logFileName");
        }
    }

    /**
     * Игровой цикл, вся игровая логика начинается отсюда
     *
     * @param game - объект, хранящий всю метаинформацию об игровых сущностях
     */
    private static void gameLoop(final Game game) {

        /* Выбор перед стартом игры */
        LoggerProcessor.printInfo(LOGGER, "--------------------------------------------------");
        LoggerProcessor.printInfo(LOGGER, "Choice at the beginning of the game");
        for (final Player player : game.getPlayers()) {
            chooseRace(player, game.getRacesPool());
        }

        LoggerProcessor.printInfo(LOGGER, "---------------------------------------------------");
        LoggerProcessor.printInfo(LOGGER, "* Game is started *");

        while (game.getCurrentRound() < ROUNDS_COUNT) { // Непосредственно игровой цикл
            game.incrementCurrentRound();

            printRoundBeginLog(game.getCurrentRound());
            for (final Player player : game.getPlayers()) {
                LoggerProcessor.printInfo(LOGGER, "Next player: {} ", player.getNickname());
                playerRound(player, game.getNeutralPlayer(), game.getBoard(), game.getRacesPool(),
                        game.getRaceCellTypeFeatures(), game.getOwnToCells(), game.getFeudalToCells(),
                        game.getPlayerToTransitCells().get(player)); // раунд игрока. Все свои решения он принимает здесь
            }
            for (final Player player : game.getPlayers()) {
                updateCoinsCount(player, game.getFeudalToCells(),
                        game.getRaceCellTypeFeatures(), game.getBoard());  // обновление числа монет у каждого игрока
            }

            printRoundEndLog(game.getCurrentRound(), game.getPlayers(), game.getOwnToCells(), game.getFeudalToCells());
        }
    }

    /**
     * Вывод информации в начале раунда
     *
     * @param currentRound - номер текущего раунда
     */
    private static void printRoundBeginLog(final int currentRound) {
        LoggerProcessor.printInfo(LOGGER, "---------------------------------------------------");
        LoggerProcessor.printInfo(LOGGER, "Round {} ", currentRound);
    }

    /**
     * Вывод информации в конце раунда
     *
     * @param currentRound  - номер текущего раунда
     * @param playerList    - список всех игроков (без нейтрального)
     * @param ownToCells    - списки клеток, которыми владеет каждый игрок
     * @param feudalToCells - множества клеток, приносящих каждому игроку монеты
     */
    private static void printRoundEndLog(final int currentRound, final List<Player> playerList,
                                         final Map<Player, List<Cell>> ownToCells,
                                         final Map<Player, Set<Cell>> feudalToCells) {
        LoggerProcessor.printDebug(LOGGER, "* Game after {} rounds: {} *", currentRound);
        LoggerProcessor.printDebug(LOGGER, "* Players after {} rounds:", currentRound);
        printPlayersInformation(playerList, ownToCells, feudalToCells);
    }

    /**
     * Вывод информации об игроках
     *
     * @param playerList    - список всех игроков (без нейтрального)
     * @param ownToCells    - списки клеток, которыми владеет каждый игрок
     * @param feudalToCells - множества клеток, приносящих каждому игроку монеты
     */
    private static void printPlayersInformation(final List<Player> playerList,
                                                final Map<Player, List<Cell>> ownToCells,
                                                final Map<Player, Set<Cell>> feudalToCells) {

        for (final Player player : playerList) {
            LoggerProcessor.printDebug(LOGGER,
                    "Player {}: [ coins {}, feudal for: {} cells, controled: {} cells ] ",
                    player.getNickname(), player.getCoins(),
                    ownToCells.get(player).size(), feudalToCells.get(player).size());
        }
    }

    /**
     * Раунд в исполнении игрока
     *
     * @param player               - игрок, который исполняет раунд
     * @param neutralPlayer        - нейтральный игрок
     * @param board                - борда
     * @param racesPool            - пул всех доступных рас
     * @param raceCellTypeFeatures - особенности для каждой пары (раса, тип клетки)
     * @param ownToCells           - список подконтрольных клеток для каждого игрока
     * @param feudalToCells        - множества клеток для каждого феодала
     * @param transitCells         - транзитные клетки игрока
     */
    private static void playerRound(final Player player, final Player neutralPlayer, final IBoard board,
                                    final List<Race> racesPool,
                                    final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures,
                                    final Map<Player, List<Cell>> ownToCells,
                                    final Map<Player, Set<Cell>> feudalToCells,
                                    final List<Cell> transitCells) {

        playerRoundBeginUpdate(player, ownToCells.get(player));  // активация данных игрока в начале раунда
        if (isSayYes()) { // В случае ответа "ДА" от игрока на вопрос: "Идти в упадок?"
            declineRace(player, neutralPlayer, racesPool, ownToCells.get(player), feudalToCells.get(player)); // Уход в упадок
        }
        catchCells(player, neutralPlayer, board, raceCellTypeFeatures,
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

        player.makeAllUnitsSomeState(AvailabilityType.AVAILABLE);
        controlledCells.forEach(cell -> cell.getUnits().clear());
//        for (final Cell cell : game.getOwnToCells().get(player)) {
//            cell.getUnits()
//                    .removeIf(unit ->
//                            player.getUnitsByState(AvailabilityType.AVAILABLE).contains(unit)
//                    );
//        }
    }

    /**
     * Конец раунда очередного игрового цикла игроком:
     * всех юнитов игрока сделать недоступными
     *
     * @param player - игрок, чьи данные нужно обновить согласно методу
     */
    private static void playerRoundEndUpdate(final Player player) {
        player.makeAllUnitsSomeState(AvailabilityType.NOT_AVAILABLE);
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

        LoggerProcessor.printInfo(LOGGER, "* Player {} in decline of race! *", player.getNickname());

        /* Освобождаем все занятые игроком клетки (юниты остаются там же) */
        for (final Cell cell : feudalCells) {
            cell.setOwn(neutralPlayer);
        }
        controlledCells.clear();

        changeRace(player, racesPool);
    }

    /**
     * Процесс замены расы у игрока
     *
     * @param player    - игрок, который решил идти в упадок
     * @param racesPool - пул всех доступных рас
     */
    private static void changeRace(final Player player, final List<Race> racesPool) {
        final Race oldRace = player.getRace();

        /* Чистим у игрока юниты */
        for (final AvailabilityType availabilityType : AvailabilityType.values()) {
            player.getUnitStateToUnits().get(availabilityType).clear();
        }

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
        final Race newRace = racesPool.get(random.nextInt(racesPool.size()));
        racesPool.remove(newRace); // Удаляем выбранную игроком расу из пула
        player.setRace(newRace);

        /* Добавляем юнитов выбранной расы */
        int i = 0;
        while (i < newRace.getUnitsAmount()) {
            player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).add(new Unit(IdGenerator.getCurrentId()));
            i++;
        }
        LoggerProcessor.printInfo(LOGGER,
                "* Player {} choose race {} *", player.getNickname(), newRace);
    }

    /**
     * Метод для завоёвывания клеток игроком
     *
     * @param player               - игрок, проводящий завоёвывание
     * @param neutralPlayer        - нейтральный игрок
     * @param board                - борда
     * @param raceCellTypeFeatures - особенности для каждой пары (раса, тип клетки)
     * @param ownToCells           - список подконтрольных клеток для каждого игрока
     * @param feudalToCells        - множества клеток для каждого феодала
     * @param transitCells         - транзитные клетки игрока
     */
    private static void catchCells(final Player player, final Player neutralPlayer,
                                   final IBoard board,
                                   final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures,
                                   final Map<Player, List<Cell>> ownToCells,
                                   final Map<Player, Set<Cell>> feudalToCells,
                                   final List<Cell> transitCells) {

        LoggerProcessor.printDebug(LOGGER, "* Player {} catch cells! *", player.getNickname());
        final List<Cell> controlledCells = ownToCells.get(player);
        final List<Cell> achievableCells = getAchievableCells(player, board, controlledCells);
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        while (achievableCells.size() > 0 && availableUnits.size() > 0 && isSayYes()) {
            // Пока есть что захватывать и
            // какими войсками захватывать и
            // ответ "ДА" от игрока на вопрос: "Продолжить захват клеток?"

            final Cell catchCell = chooseCell(achievableCells); // клетка, которую игрок хочет захватить

            if (catchCellAttempt(player, catchCell, neutralPlayer, board, raceCellTypeFeatures,
                    ownToCells, feudalToCells, transitCells)) { // если попытка захвата увеначалась успехом

                achievableCells.remove(catchCell);
                achievableCells.addAll(getAllNeighboringCells(board, catchCell));
                achievableCells.removeIf(controlledCells::contains); // удаляем те клетки, которые уже заняты игроком
            }
        }
    }

    /**
     * Метод для получения достижимых в один ход игроком клеток, не подконтрольных ему
     *
     * @param player          - игрок, чьи достижимые клетки мы хотим получить
     * @param controlledCells - принадлежащие игроку клетки
     * @return список достижимых в один ход игроком клеток, не подконтрольных ему
     */
    private static List<Cell> getAchievableCells(final Player player, final IBoard board,
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
        for (final Position neighboringPosition : neighboringPositions) {
            final Cell potentiallyAchievableCell = board.getCellByPosition(neighboringPosition);
            if (potentiallyAchievableCell != null) { // если не вышли за пределы борды
                neighboringCells.add(potentiallyAchievableCell);
            }
        }
        return neighboringCells;
    }

    /**
     * Метод попытки захвата одной клетки игроком
     *
     * @param player               - игрок, захватывающий клетку
     * @param catchingCell         - захватываемая клетка
     * @param neutralPlayer        - нейтральный игрок
     * @param board                - борда
     * @param raceCellTypeFeatures - особенности для каждой пары (раса, тип клетки)
     * @param ownToCells           - список подконтрольных клеток для каждого игрока
     * @param feudalToCells        - множества клеток для каждого феодала
     * @param transitCells         - транзитные клетки игрока
     * @return true - если попытка увенчалась успехом, false - иначе
     */
    private static boolean catchCellAttempt(final Player player, final Cell catchingCell, final Player neutralPlayer,
                                            final IBoard board,
                                            final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures,
                                            final Map<Player, List<Cell>> ownToCells,
                                            final Map<Player, Set<Cell>> feudalToCells,
                                            final List<Cell> transitCells) {
        LoggerProcessor.printDebug(LOGGER,
                "Player {} catch attempt the cell {} ", player.getNickname(),
                board.getPositionByCell(catchingCell));

        final int unitsCount = chooseNumber(player.getUnitsByState(
                AvailabilityType.AVAILABLE).size()); // число юнитов, которое игрок хочет направить в эту клетку

        LoggerProcessor.printDebug(LOGGER,
                "Player {} capture units in quantity {} ", player.getNickname(), unitsCount);

        final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(raceCellTypeFeatures, catchingCell);
        final int bonusAttack = getBonusAttackToCatchCell(player, raceCellTypeFeatures, catchingCell);
        if (!cellIsCatching(unitsCount + bonusAttack, unitsCountNeededToCatch)) {
            LoggerProcessor.printDebug(LOGGER,
                    "The cell is not captured. The aggressor {} retreated ", player.getNickname());

            return false;
        } // else
        catchCell(player, catchingCell, unitsCountNeededToCatch - bonusAttack, neutralPlayer,
                raceCellTypeFeatures, ownToCells, feudalToCells, transitCells);
        LoggerProcessor.printDebug(LOGGER, "Cell after catching: {} ", catchingCell);
        return true;
    }

    /**
     * Метод получения числа юнитов, необходимых для захвата клетки
     *
     * @param raceCellTypeFeatures - особенности для каждой пары (раса, тип клетки)
     * @param catchingCell         - захватываемая клетка
     * @return число юнитов, необходимое для захвата клетки catchCell
     */
    private static int getUnitsCountNeededToCatchCell(
            final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures, final Cell catchingCell) {

        final Player defendingPlayer = catchingCell.getOwn();
        int unitsCountNeededToCatch = catchingCell.getType().getCatchDifficulty();
        for (final Feature feature : raceCellTypeFeatures.getOrDefault(
                new Pair<>(catchingCell.getRace(),
                        catchingCell.getType()), new LinkedList<>())) { // Смотрим все особенности владельца

            if (feature.getType() == FeatureType.DEFENSE_CELL_CHANGING_UNITS_NUMBER) {
                unitsCountNeededToCatch += ((CoefficientlyFeature) feature).getCoefficient();

                LoggerProcessor.printDebug(LOGGER,
                        "Player stumbled upon a defense of {} in cellType {} of defending player {}",
                        catchingCell.getRace(), catchingCell.getType(),
                        defendingPlayer != null ? defendingPlayer.getNickname() : null);
            }
        }

        if (catchingCell.getUnits().size() > 0) { // если в захватываемой клетке есть юниты
            unitsCountNeededToCatch += catchingCell.getUnits().size() + 1;
        }
        LoggerProcessor.printDebug(LOGGER, "Units count needed to catch: {} ", unitsCountNeededToCatch);
        return unitsCountNeededToCatch;
    }

    /**
     * Метод получения бонуса атаки при захвате клетки
     *
     * @param player               - игрок-агрессор
     * @param raceCellTypeFeatures - особенности для каждой пары (раса, тип клетки)
     * @param catchingCell         - захватываемая клетка
     * @return бонус атаки (в числе юнитов) игрока player при захвате клетки catchCell
     */
    private static int getBonusAttackToCatchCell(final Player player,
                                                 final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures,
                                                 final Cell catchingCell) {
        int bonusAttack = 0;
        for (final Feature feature : raceCellTypeFeatures.get(
                new Pair<>(player.getRace(), catchingCell.getType()))) { // Смотрим все особенности агрессора

            if (feature.getType() == FeatureType.CATCH_CELL_CHANGING_UNITS_NUMBER) {
                bonusAttack += ((CoefficientlyFeature) feature).getCoefficient();
                LoggerProcessor.printDebug(LOGGER,
                        "Player {} took advantage of the feature race {} and cellType of catchCell {}",
                        player.getNickname(), player.getRace(), catchingCell.getType());
            }
        }
        LoggerProcessor.printDebug(LOGGER, "Bonus attack: {} ", bonusAttack);
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
     * @param player               - игрок-агрессор
     * @param catchingCell         - захватываемая клетка
     * @param tiredUnitsCount      - количество "уставших юнитов" (юнитов, которые перестанут быть доступными в этом раунде)
     * @param neutralPlayer        - нейтральный игрок
     * @param raceCellTypeFeatures - особенности для каждой пары (раса, тип клетки)
     * @param ownToCells           - список подконтрольных клеток для каждого игрока
     * @param feudalToCells        - множества клеток для каждого феодала
     * @param transitCells         - транзитные клетки игрока
     *                             (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     */
    private static void catchCell(final Player player, final Cell catchingCell,
                                  final int tiredUnitsCount, final Player neutralPlayer,
                                  final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures,
                                  final Map<Player, List<Cell>> ownToCells,
                                  final Map<Player, Set<Cell>> feudalToCells,
                                  final List<Cell> transitCells) {

        player.makeNAvailableUnitsToNotAvailable(tiredUnitsCount); // все юниты, задействованные в захвате клетки,
        // становятся недоступными

        final Player defendingPlayer = catchingCell.getOwn();
        boolean catchingCellIsFeudalizable = true;
        final boolean haveARival = isAlivePlayer(defendingPlayer, neutralPlayer);

        for (final Feature feature : raceCellTypeFeatures.getOrDefault(
                new Pair<>(player.getRace(), catchingCell.getType()), new LinkedList<>())) { // Смотрим все особенности агрессора

            catchingCellIsFeudalizable =
                    catchingCellIsFeudalizable &&
                            catchCellCheckFeature(player, catchingCell, haveARival, feature, transitCells);
        }

        if (defendingPlayer != null) {
            catchingCell.getUnits().clear(); // Юниты бывшего владельца с этой клетки убираются
            feudalToCells.get(catchingCell.getFeudal()).remove(catchingCell);
            catchingCell.setFeudal(null); // Клетка лишается своего бывшего феодала
            if (isNotNeutralPlayer(defendingPlayer, neutralPlayer)) {
                ownToCells.get(defendingPlayer).remove(catchingCell);
            }
            catchingCell.setOwn(null);
        }
        giveCellFeudalAndOwner(player, catchingCell, catchingCellIsFeudalizable,
                transitCells, ownToCells.get(player), feudalToCells.get(player));
        LoggerProcessor.printInfo(LOGGER, "Cell is catched of player {} ", player.getNickname());
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
     * @param player       - игрок-агрессор
     * @param catchingCell - захватываемая клетка
     * @param haveARival   - true - если владелец захватываемой клетки "живой", т. е. не ссылка null и не нейтральный
     * @param feature      - особенность пары (раса, тип клетки), которая рассматривается
     * @param transitCells - транзитные клетки игрока
     *                     (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     * @return true - если feature не CATCH_CELL_IMPOSSIBLE, false - иначе
     */
    private static boolean catchCellCheckFeature(final Player player, final Cell catchingCell, final boolean haveARival,
                                                 final Feature feature, final List<Cell> transitCells) {

        if (haveARival) {
            if (feature.getType() == FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL) {
                final Player defendingPlayer = catchingCell.getOwn();
                int deadUnitsCount = ((CoefficientlyFeature) feature).getCoefficient();
                deadUnitsCount = Math.min(
                        deadUnitsCount,
                        defendingPlayer.getUnitsByState(AvailabilityType.NOT_AVAILABLE).size());
                killUnits(deadUnitsCount, defendingPlayer);
                return true;
            }
        }
        if (feature.getType() == FeatureType.CATCH_CELL_IMPOSSIBLE) { // то тогда клетка не будет давать монет
            transitCells.add(catchingCell);
            return false;
        }
        return true;
    }

    /**
     * Убить какое-то количество юнитов игрока
     *
     * @param deadUnitsCount - кол-во, которое необходимо убить
     * @param player         - игрок, чьих юнитов необходимо убить
     */
    private static void killUnits(final int deadUnitsCount, final Player player) {
        ListProcessor.removeFirstN(deadUnitsCount, player.getUnitsByState(AvailabilityType.NOT_AVAILABLE));
        LoggerProcessor.printDebug(LOGGER, "{} units of player {} died ",
                deadUnitsCount, player.getNickname());
    }

    /**
     * Лишить клетку владельца и феодала
     *
     * @param player          - владелец и феодал
     * @param cell            - клетка, которую нужно лишить владельца и феодала
     * @param controlledCells - принадлежащие игроку клетки
     * @param feudalCells     - клетки, приносящие монеты игроку
     */
    private static void depriveCellFeudalAndOwner(final Player player, final Cell cell,
                                                  final List<Cell> controlledCells, final List<Cell> feudalCells) {
        cell.getUnits().clear(); // Юниты бывшего владельца с этой клетки убираются
        feudalCells.remove(cell);
        controlledCells.remove(cell);
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
    private static void giveCellFeudalAndOwner(final Player player, final Cell cell,
                                               final boolean cellIsFeudalizable, final List<Cell> transitCells,
                                               final List<Cell> controlledCells, final Set<Cell> feudalCells) {
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

        LoggerProcessor.printDebug(LOGGER, "* Player {} is distributes units! *", player.getNickname());
        freeTransitCells(player, transitCells, controlledCells);
        player.makeAllUnitsSomeState(
                AvailabilityType.AVAILABLE); // доступными юнитами становятся все имеющиеся у игрока юниты

        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        if (controlledCells.size() > 0) { // Если есть куда распределять войска
            while (availableUnits.size() > 0 && isSayYes()) {
                /* Пока есть какие войска распределять и
                ответ "ДА" от игрока на вопрос: "Продолжить распределять войска?" */

                final Cell protectedCell =
                        chooseCell(controlledCells); // клетка, в которую игрок хочет распределить войска

                final int unitsCount =
                        chooseNumber(
                                availableUnits.size()
                        ); // число юнитов, которое игрок хочет распределить в эту клетку

                LoggerProcessor.printDebug(LOGGER, "Player {} protects by {} units the cell {}",
                        player.getNickname(), unitsCount, board.getPositionByCell(protectedCell));
                protectCell(player, availableUnits, protectedCell, unitsCount);
            }
        }
        LoggerProcessor.printInfo(LOGGER, "Player {} distributed units ", player.getNickname());
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

        /* Так находятся транзитные клетки: */
//        final List<Cell> transitCells = new LinkedList<>(game.getOwnToCells().get(player));
//        transitCells.removeIf(game.getFeudalToCells().get(player)::contains);

        controlledCells.removeIf(transitCells::contains);
        for (final Cell transitCell : transitCells) {
            transitCell.getUnits().removeIf(
                    unit -> player.getUnitsByState(AvailabilityType.AVAILABLE).contains(unit) ||
                            player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).contains(unit)
            ); // Убираем юниты игрока с каждой транзитной клетки
            transitCell.setOwn(null);
        }
        transitCells.clear();
        LoggerProcessor.printDebug(LOGGER, "Player {} freed his transit cells ", player.getNickname());
    }

    /**
     * Выбрать клетку (подбросить монетку)
     *
     * @param cells - список доступных на выбор клеток
     * @return выбранную клетку
     */
    private static Cell chooseCell(final List<Cell> cells) {
        final int numberOfCell = chooseNumber(cells.size()); // номер выбранной клетки из списка
        return cells.get(numberOfCell);
    }

    /**
     * Выбрать число (подбросить монетку)
     *
     * @param bound - граница подходящего числа
     * @return выбранное число
     */
    private static int chooseNumber(final int bound) {
        return random.nextInt(bound);
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
        player.makeNAvailableUnitsToNotAvailable(unitsCount);
        LoggerProcessor.printDebug(LOGGER, "Cell after defending: {} ", protectedCell);
    }

    /**
     * Обновить число монет у игрока
     *
     * @param player               - игрок, чьё число монет необходимо обновить
     * @param feudalToCells        - множества клеток для каждого феодала
     * @param raceCellTypeFeatures - особенности для каждой пары (раса, тип клетки)
     * @param board                - как ни странно, борда :)
     */
    private static void updateCoinsCount(final Player player, final Map<Player, Set<Cell>> feudalToCells,
                                         final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures,
                                         final IBoard board) {

        LoggerProcessor.printDebug(LOGGER, "* Count of coins of player {} updated! *", player.getNickname());
        for (final Cell cell : feudalToCells.get(player)) {
            updateCoinsCountByCellWithFeatures(player, raceCellTypeFeatures, cell);
            player.setCoins(player.getCoins() + cell.getType().getCoinYield());
            LoggerProcessor.printDebug(LOGGER,
                    "Player {} update coins by cell in position {} ",
                    player.getNickname(), board.getPositionByCell(cell));
        }
        LoggerProcessor.printDebug(LOGGER,
                "Player {} updated coins count. Now he has {} ", player.getNickname(), player.getCoins());
    }

    /**
     * Обновить число монет у игрока, учитывая только особенности одной клетки
     *
     * @param player               - игрок, чьё число монет необходимо обновить
     * @param raceCellTypeFeatures - особенности для каждой пары (раса, тип клетки)
     * @param cell                 - клетка, чьи особенности мы рассматриваем
     */
    private static void updateCoinsCountByCellWithFeatures(final Player player,
                                                           final Map<Pair<Race, CellType>, List<Feature>>
                                                                   raceCellTypeFeatures, final Cell cell) {
        final Map<CellType, Boolean> cellTypeMet = new HashMap<>();
        for (final CellType cellType : CellType.values()) {
            cellTypeMet.put(cellType, false);
        }
        for (final Feature feature : raceCellTypeFeatures.getOrDefault(new Pair<>(player.getRace(), cell.getType()),
                new LinkedList<>())) {

            if (feature.getType() == FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL) {
                final int coefficient = ((CoefficientlyFeature) feature).getCoefficient();
                player.increaseCoins(coefficient);
                LoggerProcessor.printDebug(LOGGER,
                        "Player {} update coins by cellType {} ", player.getNickname(), cell.getType());
                continue;
            }
            if (feature.getType() == FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL_GROUP
                    && !cellTypeMet.get(cell.getType())) {
                cellTypeMet.put(cell.getType(), true);
                final int coefficient = ((CoefficientlyFeature) feature).getCoefficient();
                player.increaseCoins(coefficient);
                LoggerProcessor.printDebug(LOGGER,
                        "Player {} update coins by group cellType {} ", player.getNickname(), cell.getType());
            }
        }
    }

    /**
     * Финализатор selfPlay'я. Выводит победителей в лог.
     *
     * @param playerList - список игроков.
     */
    private static void finalize(final List<Player> playerList) {
        LoggerProcessor.printDebug(LOGGER, "* Finalize *");
        final int maxCoinsCount = getMaxCoinsCount(playerList);
        if (maxCoinsCount == -1) {
            LoggerProcessor.printError(LOGGER, "max count of coins < 0 !!!");
            return;
        }
        final List<Player> winners = getWinners(maxCoinsCount, playerList);
        LoggerProcessor.printInfo(LOGGER, "---------------------------------------");
        LoggerProcessor.printInfo(LOGGER, "Game OVER !!!");
        LoggerProcessor.printInfo(LOGGER, "Winners: ");
        for (final Player winner : winners) {
            LoggerProcessor.printInfo(LOGGER, "Player {} - coins {} ", winner.getNickname(), winner.getCoins());
        }
        LoggerProcessor.printInfo(LOGGER, "***************************************");
        LoggerProcessor.printInfo(LOGGER, "Results of other players: ");
        for (final Player player : playerList) {
            if (winners.contains(player)) {
                continue;
            }
            LoggerProcessor.printInfo(LOGGER, "Player {} - coins {} ", player.getNickname(), player.getCoins());
        }
    }

    /**
     * @param playerList - список игроков
     * @return максимальное кол-во монет, имеющихся у одного игрока
     */
    private static int getMaxCoinsCount(final List<Player> playerList) {
        return playerList.stream()
                .map(Player::getCoins)
                .max(Integer::compareTo)
                .orElse(-1);
    }

    /**
     * @param maxCoinsCount - максимальное кол-во монет, имеющихся у одного игрока
     * @param playerList    - - список игроков
     * @return список победителей (игроков, имеющих монет в кол-ве Давай наверное сделаем как в ТЗ
     */
    private static List<Player> getWinners(final int maxCoinsCount, final List<Player> playerList) {
        final List<Player> winners = new LinkedList<>();
        for (final Player player : playerList) {
            if (player.getCoins() == maxCoinsCount) {
                winners.add(player);
            }
        }
        return winners;
    }


    public static void main(final String[] args) {
        selfPlay();
    }
}
