package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.client.IBot;
import io.neolab.internship.coins.client.SimpleBot;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.board.*;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.game.service.*;
import io.neolab.internship.coins.server.service.GameAnswerProcessor;
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
        try (final GameLoggerFile ignored = new GameLoggerFile()) {
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
        playerList.forEach(player ->
                GameAnswerProcessor.chooseRace(player, game.getRacesPool(),
                        simpleBotToPlayer.getKey(player).chooseRace(player, game)));
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
                    GameLoopProcessor.updateCoinsCount(player, game.getFeudalToCells().get(player),
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
        GameAnswerProcessor.changeRace(player, simpleBot.chooseRace(player, game), game.getRacesPool());
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
        GameLoopProcessor.updateAchievableCells(player, board, achievableCells, controlledCells);
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
            }
        }
    }

    /**
     * Метод попытки захвата одной клетки игроком
     *
     * @param player        - игрок, захватывающий клетку
     * @param catchingCell  - захватываемая клетка
     * @param units         - список юнитов, направленных на захвать клетки
     * @param board         - борда
     * @param gameFeatures  - особенности игры
     * @param ownToCells    - список подконтрольных клеток для каждого игрока
     * @param feudalToCells - множества клеток для каждого феодала
     * @param transitCells  - транзитные клетки игрока
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
        final List<Cell> controlledCells = ownToCells.get(player);
        final boolean isControlled = controlledCells.contains(catchingCell);
        if (isControlled) {
            return tryEnterToCell(player, catchingCell, units, board);
        }
        GameLogger.printCellCatchAttemptLog(player, board.getPositionByCell(catchingCell));
        GameLogger.printCatchCellUnitsQuantityLog(player.getNickname(), units.size());
        final int unitsCountNeededToCatch = GameLoopProcessor.getUnitsCountNeededToCatchCell(gameFeatures, catchingCell);
        final int bonusAttack = GameLoopProcessor.getBonusAttackToCatchCell(player, gameFeatures, catchingCell);
        if (!cellIsCatching(units.size() + bonusAttack, unitsCountNeededToCatch)) {
            GameLogger.printCatchCellNotCapturedLog(player);
            return false;
        }
        final int tiredUnitsCount = unitsCountNeededToCatch - bonusAttack;
        final List<Cell> neighboringCells = GameLoopProcessor.getAllNeighboringCells(board, catchingCell);
        GameLoopProcessor.catchCell(player, catchingCell, neighboringCells,
                GameLoopProcessor.getTiredUnits(units, tiredUnitsCount),
                GameLoopProcessor.getRemainingAvailableUnits(units, tiredUnitsCount), gameFeatures,
                ownToCells, feudalToCells, transitCells);
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
        GameLogger.printCellTryEnterLog(player, board.getPositionByCell(targetCell));
        GameLogger.printCellTryEnterUnitsQuantityLog(player, units.size());
        final int tiredUnitsCount = targetCell.getType().getCatchDifficulty();
        if (!IsPossibleEnterToCell(units.size(), tiredUnitsCount)) {
            GameLogger.printCellNotEnteredLog(player);
            return false;
        }
        GameLoopProcessor.enterToCell(player, targetCell, units, tiredUnitsCount, board);
        return true;
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
        GameLoopProcessor.freeTransitCells(player, transitCells, controlledCells);
        controlledCells.forEach(controlledCell -> controlledCell.getUnits().clear());
        GameLoopProcessor.makeAllUnitsSomeState(player,
                AvailabilityType.AVAILABLE); // доступными юнитами становятся все имеющиеся у игрока юниты
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        if (controlledCells.size() > 0 && availableUnits.size() > 0) { // Если есть куда и какие распределять войска
            final Map<Position, List<Unit>> distributionUnits = simpleBot.distributionUnits(player, game);
            distributionUnits.forEach((position, units) -> {
                GameLogger.printCellDefendingLog(player, units.size(), position);
                GameLoopProcessor.protectCell(player, game.getBoard().getCellByPosition(position), units);
            });
        }
        GameLogger.printAfterDistributedUnitsLog(player);
    }


    public static void main(final String[] args) {
        selfPlay();
    }
}
