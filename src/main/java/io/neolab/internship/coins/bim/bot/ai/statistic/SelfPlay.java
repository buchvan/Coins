package io.neolab.internship.coins.bim.bot.ai.statistic;

import io.neolab.internship.coins.ai_vika.bot.exception.AIBotException;
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
import io.neolab.internship.coins.server.service.*;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.LogCleaner;
import io.neolab.internship.coins.utils.LoggerFile;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

import static io.neolab.internship.coins.server.service.GameLoopProcessor.loseCells;
import static io.neolab.internship.coins.server.service.GameLoopProcessor.updateAchievableCellsAfterCatchCell;

class SelfPlay {
    private static final int ROUNDS_COUNT = 10;

    private static final int BOARD_SIZE_X = 3;
    private static final int BOARD_SIZE_Y = 4;
    private static final int PLAYERS_COUNT = 2;

    private static Map<Player, GameStatistic.Statistic> playerStatistic;


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
            final List<Pair<IBot, Player>> botPlayerPairs = new LinkedList<>();
            SelfPlay.playerStatistic = new HashMap<>(game.getPlayers().size());
            game.getPlayers().forEach(player -> {
                botPlayerPairs.add(new Pair<>(new SimpleBot(), player));
                SelfPlay.playerStatistic.put(player, new GameStatistic.Statistic());
            });
            gameLoop(game, botPlayerPairs);
            GameFinalizer.finalization(game.getPlayers());
        } catch (final CoinsException | IOException exception) {
            GameLogger.printErrorLog(exception);
        }
    }

    /**
     * Игра сама с собой (self play)
     * - Создание борды
     * - Добавление метаинформации о игре(борда, игроки, юниты)
     * - Игровой цикл
     * - Финализатор (результат игры)
     */
    @SuppressWarnings("SameParameterValue")
    static @NotNull List<Player> selfPlayByBotToPlayersWithStatistic(final int index,
                                                                     final @NotNull List<Pair<IBot, Player>>
                                                                             botPlayerPairs,
                                                                     final @NotNull Map<Player, GameStatistic.Statistic>
                                                                             playerStatistic) {
        try (final LoggerFile ignored = new LoggerFile("self-play-" + index)) {
            LogCleaner.clean();
            SelfPlay.playerStatistic = playerStatistic;
            final List<Player> players = new LinkedList<>();
            botPlayerPairs.forEach(pair -> players.add(pair.getSecond()));
            final IGame game = GameInitializer.gameInit(BOARD_SIZE_X, BOARD_SIZE_Y, players);
            GameLogger.printGameCreatedLog(game);
            gameLoop(game, botPlayerPairs);
            botPlayerPairs.clear();
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
    private static void gameLoop(final @NotNull IGame game, final @NotNull List<Pair<IBot, Player>> botPlayerPairs) {
        GameLogger.printStartGameChoiceLog();
        botPlayerPairs.forEach(pair -> {
            final long firstTime = System.currentTimeMillis();
            final Race race;
            try {
                race = pair.getFirst().chooseRace(pair.getSecond(), game);
                playerStatistic.get(pair.getSecond()).updateMaxTime(System.currentTimeMillis() - firstTime);
                GameAnswerProcessor.changeRace(pair.getSecond(), race, game.getRacesPool(), true);
            } catch (final AIBotException e) {
                e.printStackTrace();
            }
        });
        botPlayerPairs.forEach(pair ->
                SelfPlay.playerStatistic.get(pair.getSecond())
                        .addFirstRace(Objects.requireNonNull(pair.getSecond().getRace())));
        GameLogger.printStartGame();
        while (game.getCurrentRound() < ROUNDS_COUNT) { // Непосредственно игровой цикл
            game.incrementCurrentRound();
            GameLogger.printRoundBeginLog(game.getCurrentRound());
            botPlayerPairs.forEach(pair -> {
                GameLogger.printNextPlayerLog(pair.getSecond());
                try {
                    playerRoundProcess(pair.getSecond(), pair.getFirst(),
                            game); // Раунд игрока. Все свои решения он принимает здесь
                } catch (final AIBotException e) {
                    e.printStackTrace();
                }
            });
            botPlayerPairs.forEach(pair -> // обновление числа монет у каждого игрока
                    GameLoopProcessor.updateCoinsCount(
                            pair.getSecond(), game.getFeudalToCells().get(pair.getSecond()),
                            game.getGameFeatures(), game.getBoard(), true));
            GameLogger.printRoundEndLog(game.getCurrentRound(), game.getPlayers(),
                    game.getOwnToCells(), game.getFeudalToCells());
        }
    }

    /**
     * Раунд в исполнении игрока
     *
     * @param player - игрок, который исполняет раунд
     * @param bot    - бот игрока
     * @param game   - объект, хранящий всю метаинформацию об игре
     */
    private static void playerRoundProcess(final @NotNull Player player, final @NotNull IBot bot,
                                           final @NotNull IGame game) throws AIBotException {
        GameLoopProcessor.playerRoundBeginUpdate(player, true);  // активация данных игрока в начале раунда
        if (game.getRacesPool().size() > 0) {
            final long firstTime = System.currentTimeMillis();
            final boolean isDeclineRace = bot.declineRaceChoose(player, game);
            playerStatistic.get(player).updateMaxTime(System.currentTimeMillis() - firstTime);
            if (isDeclineRace) {
                // В случае ответа "ДА" от бота на вопрос: "Идти в упадок?"
                declineRaceProcess(player, bot, game); // Уход в упадок
            }
        }
        cellCaptureProcess(player, bot, game); // Завоёвывание клеток
        distributionUnits(player, bot, game); // Распределение войск
        GameLoopProcessor.playerRoundEndUpdate(player, true); // "затухание" (дезактивация) данных игрока в конце раунда
    }

    /**
     * Процесс упадка: потеря контроля над всеми клетками с сохранением от них дохода, выбор новой расы
     *
     * @param player - игрок, который решил идти в упадок
     * @param bot    - бот игрока
     * @param game   - объект, хранящий всю метаинформацию об игре
     */
    private static void declineRaceProcess(final @NotNull Player player, final @NotNull IBot bot,
                                           final @NotNull IGame game) throws AIBotException {
        GameLogger.printDeclineRaceLog(player);
        game.getOwnToCells().get(player).clear(); // Освобождаем все занятые игроком клетки (юниты остаются там же)
        final long firstTime = System.currentTimeMillis();
        final Race newRace = bot.chooseRace(player, game);
        playerStatistic.get(player).updateMaxTime(System.currentTimeMillis() - firstTime);
        GameAnswerProcessor.changeRace(player, newRace, game.getRacesPool(), true);
    }

    /**
     * Метод для завоёвывания клеток игроком
     *
     * @param player - игрок, проводящий завоёвывание
     * @param bot    - бот игрока
     * @param game   - объект, хранящий всю метаинформацию об игре
     */
    private static void cellCaptureProcess(final @NotNull Player player, final @NotNull IBot bot,
                                           final @NotNull IGame game) throws AIBotException {
        GameLogger.printBeginCatchCellsLog(player);
        final IBoard board = game.getBoard();
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<Cell> transitCells = game.getPlayerToTransitCells().get(player);
        final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
        GameLoopProcessor.updateAchievableCells(player, board, achievableCells, controlledCells, true);
        while (true) {
            /* Пока есть что захватывать и какими войсками захватывать */
            final long firstTime = System.currentTimeMillis();
            final Pair<Position, List<Unit>> catchingCellToUnitsList = bot.chooseCatchingCell(player, game);
            playerStatistic.get(player).updateMaxTime(System.currentTimeMillis() - firstTime);
            if (catchingCellToUnitsList == null) { // если игрок не захотел больше захватывать
                break;
            }
            final Cell catchingCell = board
                    .getCellByPosition(catchingCellToUnitsList.getFirst()); // клетка, которую игрок хочет захватить
            final List<Unit> units = catchingCellToUnitsList.getSecond(); // юниты для захвата этой клетки
            if (isCatchCellAttemptSucceed(player, Objects.requireNonNull(catchingCell), units, board,
                    game.getGameFeatures(), game.getOwnToCells(), game.getFeudalToCells(),
                    transitCells)) { // если попытка захвата увеначалась успехом
                updateAchievableCellsAfterCatchCell(board, catchingCell, controlledCells, achievableCells);
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
        final int unitsCountNeededToCatch = GameLoopProcessor.getUnitsCountNeededToCatchCell(gameFeatures,
                catchingCell, true);
        final int bonusAttack = GameLoopProcessor.getBonusAttackToCatchCell(player, gameFeatures, catchingCell, true);
        if (!isCellCatching(units.size() + bonusAttack, unitsCountNeededToCatch)) {
            GameLogger.printCatchCellNotCapturedLog(player);
            return false;
        }
        SelfPlay.playerStatistic.get(player)
                .incrementCapturesNumber(Objects.requireNonNull(player.getRace()), catchingCell.getType());
        final int tiredUnitsCount = unitsCountNeededToCatch - bonusAttack;
        final List<Cell> neighboringCells = new LinkedList<>(
                GameLoopProcessor.getAllNeighboringCells(board, catchingCell));
        neighboringCells.removeIf(neighboringCell -> !controlledCells.contains(neighboringCell));
        GameLoopProcessor.catchCell(player, catchingCell, neighboringCells,
                GameLoopProcessor.getTiredUnits(units, tiredUnitsCount),
                GameLoopProcessor.getRemainingAvailableUnits(units, tiredUnitsCount), gameFeatures,
                ownToCells, feudalToCells, transitCells, true);
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
        GameLoopProcessor.enterToCell(player, targetCell, controlledCells, feudalCells, units, tiredUnitsCount,
                board, true);
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
     * @param player - игрок, делающий выбор
     * @param bot    - бот игрока
     * @param game   - объект, хранящий всю метаинформацию об игре
     */
    private static void distributionUnits(final @NotNull Player player, final @NotNull IBot bot,
                                          final @NotNull IGame game) throws AIBotException {
        GameLogger.printBeginUnitsDistributionLog(player);
        final List<Cell> transitCells = game.getPlayerToTransitCells().get(player);
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        GameLoopProcessor.freeTransitCells(player, transitCells, controlledCells, true);
        controlledCells.forEach(controlledCell -> controlledCell.getUnits().clear());
        GameLoopProcessor.makeAllUnitsSomeState(player,
                AvailabilityType.AVAILABLE); // доступными юнитами становятся все имеющиеся у игрока юниты
        final long firstTime = System.currentTimeMillis();
        final Map<Position, List<Unit>> distributionUnits = bot.distributionUnits(player, game);
        playerStatistic.get(player).updateMaxTime(System.currentTimeMillis() - firstTime);
        distributionUnits.forEach((position, units) -> {
            GameLogger.printCellDefendingLog(player, units.size(), position);
            GameLoopProcessor.protectCell(player,
                    Objects.requireNonNull(game.getBoard().getCellByPosition(position)), units, true);
        });
        loseCells(controlledCells, controlledCells, game.getFeudalToCells().get(player));
        GameLogger.printAfterDistributedUnitsLog(player);
    }

    public static void main(final String[] args) {
        selfPlay();
    }
}