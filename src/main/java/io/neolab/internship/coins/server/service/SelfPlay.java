package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.ai.vika.AIBot;
import io.neolab.internship.coins.ai.vika.exception.AIBotException;
import io.neolab.internship.coins.client.bot.IBot;
import io.neolab.internship.coins.client.bot.SimpleBot;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.LogCleaner;
import io.neolab.internship.coins.utils.LoggerFile;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static io.neolab.internship.coins.server.service.GameLoopProcessor.loseCells;

public class SelfPlay {
    private static final int ROUNDS_COUNT = 10;

    private static final int BOARD_SIZE_X = 3;
    private static final int BOARD_SIZE_Y = 4;
    private static final int PLAYERS_COUNT = 2;

    private static @NotNull List<Pair<IBot, Player>> simpleBotToPlayer = new LinkedList<>(); // каждому симплботу
    // соответствует только один игрок, и наоборот


    /**
     * Игра сама с собой (self play)
     * - Создание борды
     * - Добавление метаинформации о игре(борда, игроки, юниты)
     * - Игровой цикл
     * - Финализатор (результат игры)
     */
    private static void selfPlay() {
        try (final LoggerFile ignored = new LoggerFile("self-play")) {
            LogCleaner.clean();
            final IGame game = GameInitializer.gameInit(BOARD_SIZE_X, BOARD_SIZE_Y, PLAYERS_COUNT);
            GameLogger.printGameCreatedLog(game);
            game.getPlayers().forEach(player -> simpleBotToPlayer.add(new Pair<>(new SimpleBot(), player)));
            gameLoop(game);
            GameFinalizer.finalization(game.getPlayers());
        } catch (final CoinsException | IOException exception) {
            GameLogger.printErrorLog(exception);
        }
    }

    private static void AIBotAndSimpleBotToPlayers(final IGame game) {
        final List<Player> players = game.getPlayers();
        simpleBotToPlayer.add(new Pair<>(new AIBot(), players.get(0)));
        simpleBotToPlayer.add(new Pair<>(new SimpleBot(), players.get(1)));
    }

    /**
     * Игра сама с собой (self play)
     * - Создание борды
     * - Добавление метаинформации о игре(борда, игроки, юниты)
     * - Игровой цикл
     * - Финализатор (результат игры)
     */
    public static @NotNull List<Player> selfPlayByBotToPlayers(final @NotNull List<Pair<IBot, Player>> botPlayerPairs) {
        try (final LoggerFile ignored = new LoggerFile("self-play")) {
            LogCleaner.clean();
            final List<Player> players = new LinkedList<>();
            botPlayerPairs.forEach(botPlayerPair -> {
                        simpleBotToPlayer.add(botPlayerPair);
                        players.add(botPlayerPair.getSecond());
                    }
            );

            final IGame game = GameInitializer.gameInit(BOARD_SIZE_X, BOARD_SIZE_Y, players);
            GameLogger.printGameCreatedLog(game);
            gameLoop(game);
            simpleBotToPlayer.clear();
            return GameFinalizer.finalization(game.getPlayers());
        } catch (final CoinsException | IOException exception) {
            GameLogger.printErrorLog(exception);
        }
        return Collections.emptyList();
    }

    /**
     * Игровой цикл, вся игровая логика начинается отсюда
     *
     * @param game - объект, хранящий всю метаинформацию об игровых сущностях
     */
    private static void gameLoop(final @NotNull IGame game) {
        GameLogger.printStartGameChoiceLog();
        simpleBotToPlayer.forEach(pair ->
        {
            try {
                GameAnswerProcessor.changeRace(pair.getSecond(),
                        pair.getFirst().chooseRace(pair.getSecond(), game),
                        game.getRacesPool(), false);
            } catch (final AIBotException e) {
                e.printStackTrace();
            }
        });
        simpleBotToPlayer.clear();
        AIBotAndSimpleBotToPlayers(game);
        GameLogger.printStartGame();
        while (game.getCurrentRound() < ROUNDS_COUNT) {
            // Непосредственно игровой цикл
            game.incrementCurrentRound();
            GameLogger.printRoundBeginLog(game.getCurrentRound());
            simpleBotToPlayer.forEach(pair -> {
                GameLogger.printNextPlayerLog(pair.getSecond());
                try {
                    playerRoundProcess(pair.getSecond(), pair.getFirst(),
                            game); // Раунд игрока. Все свои решения он принимает здесь
                } catch (final AIBotException e) {
                    e.printStackTrace();
                }
            });
            simpleBotToPlayer.forEach(pair -> // обновление числа монет у каждого игрока
                    GameLoopProcessor.updateCoinsCount(
                            pair.getSecond(), game.getFeudalToCells().get(pair.getSecond()),
                            game.getGameFeatures(), game.getBoard()));
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
    private static void playerRoundProcess(final @NotNull Player player, final @NotNull IBot simpleBot,
                                           final @NotNull IGame game) throws AIBotException {
        GameLoopProcessor.playerRoundBeginUpdate(player, false);  // активация данных игрока в начале раунда
        if (game.getRacesPool().size() > 0 && simpleBot.declineRaceChoose(player, game)) {
            // В случае ответа "ДА" от симплбота на вопрос: "Идти в упадок?"
            declineRaceProcess(player, simpleBot, game); // Уход в упадок
        }
        cellCaptureProcess(player, simpleBot, game); // Завоёвывание клеток
        distributionUnits(player, simpleBot, game); // Распределение войск
        GameLoopProcessor.playerRoundEndUpdate(player, false); // "затухание" (дезактивация) данных игрока в конце раунда
    }

    /**
     * Процесс упадка: потеря контроля над всеми клетками с сохранением от них дохода, выбор новой расы
     *
     * @param player    - игрок, который решил идти в упадок
     * @param simpleBot - симплбот игрока
     * @param game      - объект, хранящий всю метаинформацию об игре
     */
    private static void declineRaceProcess(final @NotNull Player player, final @NotNull IBot simpleBot,
                                           final @NotNull IGame game) throws AIBotException {
        GameLogger.printDeclineRaceLog(player);
        game.getOwnToCells().get(player).clear(); // Освобождаем все занятые игроком клетки (юниты остаются там же)
        GameAnswerProcessor.changeRace(player, simpleBot.chooseRace(player, game), game.getRacesPool(), false);
    }

    /**
     * Метод для завоёвывания клеток игроком
     *
     * @param player    - игрок, проводящий завоёвывание
     * @param simpleBot - симплбот игрока
     * @param game      - объект, хранящий всю метаинформацию об игре
     */
    private static void cellCaptureProcess(final @NotNull Player player, final @NotNull IBot simpleBot,
                                           final @NotNull IGame game) throws AIBotException {
        GameLogger.printBeginCatchCellsLog(player);
        final IBoard board = game.getBoard();
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<Cell> transitCells = game.getPlayerToTransitCells().get(player);
        final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
        GameLoopProcessor.updateAchievableCells(player, board, achievableCells, controlledCells, false);
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
            if (isCatchCellAttemptSucceed(player, Objects.requireNonNull(catchingCell), units, board,
                    game.getGameFeatures(), game.getOwnToCells(), game.getFeudalToCells(),
                    transitCells)) { // если попытка захвата увеначалась успехом
                if (controlledCells.size() == 1) { // если до этого у игрока не было клеток
                    achievableCells.clear();
                    achievableCells.add(catchingCell);
                }
                final List<Cell> neighboringCells =
                        GameLoopProcessor.getAllNeighboringCells(board, Objects.requireNonNull(catchingCell));
                achievableCells.addAll(neighboringCells);
                neighboringCells.forEach(neighboringCell ->
                        GameLoopProcessor.updateNeighboringCellsIfNecessary(board, neighboringCell));
            }
        }
    }

    /**
     * Попытка захвата одной клетки игроком
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
    private static boolean isCatchCellAttemptSucceed(final @NotNull Player player,
                                                     final @NotNull Cell catchingCell,
                                                     final @NotNull List<Unit> units,
                                                     final @NotNull IBoard board,
                                                     final @NotNull GameFeatures gameFeatures,
                                                     final @NotNull Map<Player, List<Cell>> ownToCells,
                                                     final @NotNull Map<Player, Set<Cell>> feudalToCells,
                                                     final @NotNull List<Cell> transitCells) {
        final List<Cell> controlledCells = ownToCells.get(player);
        final boolean isControlled = controlledCells.contains(catchingCell);
        if (isControlled) {
            return isTryEnterToCellSucceed(player, catchingCell, ownToCells.get(player), feudalToCells.get(player),
                    units, board);
        }
        GameLogger.printCellCatchAttemptLog(player, board.getPositionByCell(catchingCell));
        GameLogger.printCatchCellUnitsQuantityLog(player, units.size());
        final int unitsCountNeededToCatch = GameLoopProcessor.getUnitsCountNeededToCatchCell(gameFeatures, catchingCell, false);
        final int bonusAttack = GameLoopProcessor.getBonusAttackToCatchCell(player, gameFeatures, catchingCell);
        if (!isCellCatching(units.size() + bonusAttack, unitsCountNeededToCatch)) {
            GameLogger.printCatchCellNotCapturedLog(player);
            return false;
        }
        final int tiredUnitsCount = unitsCountNeededToCatch - bonusAttack;
        final List<Cell> neighboringCells = new LinkedList<>(
                GameLoopProcessor.getAllNeighboringCells(board, catchingCell));
        neighboringCells.removeIf(neighboringCell -> !controlledCells.contains(neighboringCell));
        GameLoopProcessor.catchCell(player, catchingCell, neighboringCells,
                GameLoopProcessor.getTiredUnits(units, tiredUnitsCount),
                GameLoopProcessor.getRemainingAvailableUnits(units, tiredUnitsCount), gameFeatures,
                ownToCells, feudalToCells, transitCells, false);
        GameLogger.printAfterCellCatchingLog(player, catchingCell);
        return true;
    }

    /**
     * Попытка входа игрока в свою клетку
     *
     * @param player          - игрок
     * @param targetCell      - клетка, в которую игрок пытается войти
     * @param controlledCells - подконтрольные игроку клетки
     * @param feudalCells     - клетки, приносящие игроку монетки
     * @param units           - список юнитов, которых игрок послал в клетку
     * @param board           - борда
     * @return true - если попытка удачная, false - иначе
     */
    private static boolean isTryEnterToCellSucceed(final @NotNull Player player, final @NotNull Cell targetCell,
                                                   final @NotNull List<Cell> controlledCells,
                                                   final @NotNull Set<Cell> feudalCells,
                                                   final @NotNull List<Unit> units, final @NotNull IBoard board) {
        GameLogger.printCellTryEnterLog(player, board.getPositionByCell(targetCell));
        GameLogger.printCellTryEnterUnitsQuantityLog(player, units.size());
        final int tiredUnitsCount = targetCell.getType().getCatchDifficulty();
        if (!isPossibleEnterToCell(units.size(), tiredUnitsCount)) {
            GameLogger.printCellNotEnteredLog(player);
            return false;
        }
        GameLoopProcessor.enterToCell(player, targetCell, controlledCells, feudalCells, units, tiredUnitsCount, board, false);
        return true;
    }

    /**
     * Может ли игрок войти в свою клетку?
     *
     * @param unitsSize       - число юнитов, которых игрок послал в клетку
     * @param tiredUnitsCount - число уставших юнитов
     * @return true - если, игрок может войти в клетку, false - иначе
     */
    private static boolean isPossibleEnterToCell(final int unitsSize, final int tiredUnitsCount) {
        return unitsSize >= tiredUnitsCount;
    }

    /**
     * Проверка на возможность захвата клетки
     *
     * @param attackPower          - сила атаки на клетку
     * @param necessaryAttackPower - необходимая сила атаки на эту клетку для её захвата
     * @return true - если клетку можно захватить, имея attackPower, false - иначе
     */
    private static boolean isCellCatching(final int attackPower, final int necessaryAttackPower) {
        return attackPower >= necessaryAttackPower;
    }

    /**
     * Метод для распределения юнитов игроком
     *
     * @param player    - игрок, делающий выбор
     * @param simpleBot - симплбот игрока
     * @param game      - объект, хранящий всю метаинформацию об игре
     */
    private static void distributionUnits(final @NotNull Player player, final @NotNull IBot simpleBot,
                                          final @NotNull IGame game) throws AIBotException {
        GameLogger.printBeginUnitsDistributionLog(player);
        final List<Cell> transitCells = game.getPlayerToTransitCells().get(player);
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        GameLoopProcessor.freeTransitCells(player, transitCells, controlledCells);
        controlledCells.forEach(controlledCell -> controlledCell.getUnits().clear());
        GameLoopProcessor.makeAllUnitsSomeState(player,
                AvailabilityType.AVAILABLE); // доступными юнитами становятся все имеющиеся у игрока юниты
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        final Map<Position, List<Unit>> distributionUnits = simpleBot.distributionUnits(player, game);
        distributionUnits.forEach((position, units) -> {
            GameLogger.printCellDefendingLog(player, units.size(), position);
            GameLoopProcessor.protectCell(player,
                    Objects.requireNonNull(game.getBoard().getCellByPosition(position)), units);
        });
        loseCells(controlledCells, controlledCells, game.getFeudalToCells().get(player));
        GameLogger.printAfterDistributedUnitsLog(player);
    }

    public static void main(final String[] args) {
        selfPlay();
    }
}
