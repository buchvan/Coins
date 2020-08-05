package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.client.bot.ai.bim.action.*;
import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.service.GameLoopProcessor;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.neolab.internship.coins.server.service.GameAnswerProcessor.*;

public class AIProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AIProcessor.class);

    private static final int MAX_DEPTH = 1;
    private static final boolean isLoggedOn = false;

    /**
     * Обновить дерево согласно выполненному действию
     *
     * @param tree   - дерево (ссылка на корень)
     * @param action - совершённое действие
     * @return ссылка на новый корень
     */
    public static NodeTree updateTree(final @NotNull NodeTree tree, final @NotNull Action action) {
        NodeTree newTree = null;
        for (final Edge edge : Objects.requireNonNull(tree).getEdges()) {
            if (edge.getAction() == action) {
                newTree = edge.getTo();
                break;
            }
        }
        return newTree;
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
     * Обновить состояние игры исходя из совершённого действия
     *
     * @param game   - игра (желательно копия)
     * @param player - игрок
     * @param action - совершённое действие
     * @throws CoinsException при неизвестном типе действия
     */
    private static void updateGame(final @NotNull IGame game, final @NotNull Player player,
                                   final @NotNull Action action) throws CoinsException {
        switch (action.getType()) {
            case DECLINE_RACE:
                GameLoopProcessor.updateAchievableCells(player, game.getBoard(),
                        game.getPlayerToAchievableCells().get(player),
                        game.getOwnToCells().get(player), isLoggedOn);
                GameLoopProcessor.makeAllUnitsSomeState(player, AvailabilityType.AVAILABLE);
                return;
            case CHANGE_RACE:
                game.getOwnToCells().get(player).clear(); // Освобождаем все занятые игроком клетки (юниты остаются там же)
                changeRace(player, ((ChangeRaceAction) action).getNewRace(), game.getRacesPool(), isLoggedOn);
                GameLoopProcessor.updateAchievableCells(player, game.getBoard(),
                        game.getPlayerToAchievableCells().get(player), game.getOwnToCells().get(player), isLoggedOn);
                return;
            case CATCH_CELL:
                final CatchCellAction catchCellAction = (CatchCellAction) action;
                final IBoard board = game.getBoard();
                final Cell captureCell =
                        board.getCellByPosition(
                                Objects.requireNonNull(
                                        catchCellAction.getResolution()).getFirst());
                final List<Unit> units = catchCellAction.getResolution().getSecond();
                pretendToCell(player, Objects.requireNonNull(captureCell), units, board, game.getGameFeatures(),
                        game.getOwnToCells(), game.getFeudalToCells(),
                        game.getPlayerToTransitCells().get(player),
                        game.getPlayerToAchievableCells().get(player), isLoggedOn);
                return;
            case DISTRIBUTION_UNITS:
                final DistributionUnitsAction distributionUnitsAction = (DistributionUnitsAction) action;
                distributionUnits(player, game.getOwnToCells().get(player),
                        game.getFeudalToCells().get(player),
                        distributionUnitsAction.getResolutions(),
                        game.getBoard(), isLoggedOn);
                return;
            default:
                throw new CoinsException(CoinsErrorCode.ACTION_TYPE_NOT_FOUND);
        }
    }

    /**
     * Взять следующего в очереди игрока
     *
     * @param game          - игра
     * @param currentPlayer - текущий игрок
     * @return следуюшего в очереди игрока
     * @throws CoinsException в случае, если currentPlayer отсутствует в игре game
     */
    private static @NotNull Pair<@Nullable Player, @NotNull Integer> getNextPlayer(final int currentDepth,
                                                                                   final @NotNull IGame game,
                                                                                   final @NotNull Player currentPlayer)
            throws CoinsException {
        GameLoopProcessor.playerRoundEndUpdate(currentPlayer, isLoggedOn);
        boolean wasCurrentPlayer = false;
        for (final Player player : game.getPlayers()) {
            if (player.equals(currentPlayer)) {
                wasCurrentPlayer = true;
                continue;
            }
            if (wasCurrentPlayer) {
                LOGGER.debug("Next player: {}", player.getNickname());
                GameLoopProcessor.playerRoundBeginUpdate(player, isLoggedOn);
                return new Pair<>(player, currentDepth);
            }
        }
        if (wasCurrentPlayer) {
            final int newDepth = currentDepth + 1;
            LOGGER.debug("New depth: {}", newDepth);
            game.getPlayers().forEach(player -> // обновление числа монет у каждого игрока
                    GameLoopProcessor.updateCoinsCount(
                            player, game.getFeudalToCells().get(player),
                            game.getGameFeatures(), game.getBoard(), isLoggedOn));
            game.incrementCurrentRound();
            LOGGER.debug("New round: {}", game.getCurrentRound());
            final Player nextPlayer =
                    newDepth < MAX_DEPTH && game.getCurrentRound() <= Game.ROUNDS_COUNT
                            ? game.getPlayers().get(0)
                            : null;
            if (nextPlayer != null) {
                LOGGER.debug("Next player: {}", nextPlayer.getNickname());
                GameLoopProcessor.playerRoundBeginUpdate(nextPlayer, isLoggedOn);
            }
            return new Pair<>(nextPlayer, newDepth);
        }
        throw new CoinsException(CoinsErrorCode.PLAYER_NOT_FOUND);
    }

    /**
     * Взять ссылку на игрока из скопированной игры
     *
     * @param gameCopy - копия игры
     * @param player   - игрок, которого мы ищем в скопированной игре
     * @return ссылку на копию игрока
     */
    private static @NotNull Player getPlayerCopy(final @NotNull IGame gameCopy, final @NotNull Player player) {
        return gameCopy.getPlayers().stream()
                .filter(player1 -> player1.getId() == player.getId())
                .findFirst()
                .orElseThrow();
    }

    /**
     * Создать ветви с уходом в упадок игрока
     *
     * @param game   - игра
     * @param player - игрок
     * @param edges  - дуги от родителя
     */
    private static void createDeclineRaceBranches(final int currentDepth,
                                                  final @NotNull IGame game, final @NotNull Player player,
                                                  final @NotNull List<Edge> edges) {
        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> {
            final Action newAction = new DeclineRaceAction(true);
            try {
                edges.add(new Edge(player, newAction, createSubtree(currentDepth, game, player, newAction)));
            } catch (final CoinsException exception) {
                exception.printStackTrace();
            }
        });
        executorService.execute(() -> {
            final Action newAction = new DeclineRaceAction(false);
            try {
                edges.add(new Edge(player, newAction, createSubtree(currentDepth, game, player, newAction)));
            } catch (final CoinsException exception) {
                exception.printStackTrace();
            }
        });
        executeExecutorService(executorService);
    }

    /**
     * Создать ветви с изменением расы игрока
     *
     * @param game   - игра
     * @param player - игрок
     * @param edges  - дуги от родителя
     */
    private static void createChangeRaceBranches(final int currentDepth,
                                                 final @NotNull IGame game, final @NotNull Player player,
                                                 final @NotNull List<Edge> edges) {
        final ExecutorService executorService = Executors.newFixedThreadPool(game.getRacesPool().size());
        game.getRacesPool().forEach(race -> executorService.execute(() -> {
            final Action newAction = new ChangeRaceAction(race);
            try {
                edges.add(new Edge(player, newAction, createSubtree(currentDepth, game, player, newAction)));
            } catch (final CoinsException exception) {
                exception.printStackTrace();
            }
        }));
        executeExecutorService(executorService);
    }

    /**
     * Создать симуляционное дерево игры
     *
     * @param game   - игра
     * @param player - игрок 1
     * @return корень на созданное дерево
     */
    public static @NotNull NodeTree createTree(final @NotNull IGame game, final @NotNull Player player) {
        final List<Edge> edges = Collections.synchronizedList(new LinkedList<>());
        final IGame gameCopy = game.getCopy();
        final Player playerCopy = getPlayerCopy(gameCopy, player);
        createDeclineRaceBranches(0, gameCopy, playerCopy, edges);
        return createNodeTree(-1, gameCopy, edges);
    }

    /**
     * Создать узел дерева
     *
     * @param currentDepth - текущая глубина
     * @param game         - игра
     * @param edges        - дуги к потомкам
     * @return узел дерева
     */
    @Contract("_, _, _ -> new")
    private static @NotNull NodeTree createNodeTree(final int currentDepth, final @NotNull IGame game,
                                                    final @NotNull List<Edge> edges) {
        final Map<Player, Integer> winsCount = new HashMap<>();
        game.getPlayers().forEach(player1 -> winsCount.put(player1, 0));
        int casesCount = 0;
        for (final Edge edge : edges) {
            edge.getTo().getWinsCount().forEach((key, value) -> winsCount.replace(key, winsCount.get(key) + value));
            casesCount += edge.getTo().getCasesCount();
        }
        LOGGER.debug("Created new node in depth {} with edges: {}", currentDepth, edges);
        return new NodeTree(edges, winsCount, casesCount);
    }

    /**
     * Создать поддерево симуляционного дерева игры
     *
     * @param game   - игра в текущем состоянии
     * @param player - игрок
     * @param action - действие, привёдшее к данному узлу
     * @return узел с оценённым данным действием
     */
    private static @NotNull NodeTree createSubtree(final int currentDepth,
                                                   final @NotNull IGame game, final @NotNull Player player,
                                                   final @NotNull Action action) throws CoinsException {
        final List<Edge> edges = Collections.synchronizedList(new LinkedList<>());
        final IGame gameCopy;
        final Player playerCopy;
        switch (action.getType()) {
            case DECLINE_RACE:
                if (((DeclineRaceAction) action).isDeclineRace()) {
                    createChangeRaceBranches(currentDepth, game, player, edges);
                    break;
                }
                gameCopy = game.getCopy();
                playerCopy = getPlayerCopy(gameCopy, player);
                updateGame(gameCopy, playerCopy, action);
                createCatchCellsNodes(currentDepth, gameCopy, playerCopy, edges,
                        Collections.synchronizedSet(new HashSet<>()));
                break;
            case CHANGE_RACE:
                gameCopy = game.getCopy();
                playerCopy = getPlayerCopy(gameCopy, player);
                updateGame(gameCopy, playerCopy, action);
                createCatchCellsNodes(currentDepth, gameCopy, playerCopy, edges,
                        Collections.synchronizedSet(new HashSet<>()));
                break;
            case CATCH_CELL:
                if (((CatchCellAction) action).getResolution() != null) {
                    throw new CoinsException(CoinsErrorCode.LOGIC_ERROR);
                }
                final List<Cell> transitCells = game.getPlayerToTransitCells().get(player);
                final List<Cell> controlledCells = game.getOwnToCells().get(player);
                GameLoopProcessor.freeTransitCells(player, transitCells, controlledCells, isLoggedOn);
                controlledCells.forEach(controlledCell -> controlledCell.getUnits().clear());
                GameLoopProcessor.makeAllUnitsSomeState(player,
                        AvailabilityType.AVAILABLE); // доступными юнитами становятся все имеющиеся у игрока юниты
                createDistributionUnitsNodes(currentDepth, game, player, edges);
                break;
            case DISTRIBUTION_UNITS:
                final Pair<Player, Integer> pair = getNextPlayer(currentDepth, game, player);
                final Player nextPlayer = pair.getFirst();
                final int nextDepth = pair.getSecond();
                if (nextPlayer == null) {
                    edges.add(new Edge(null, null, createTerminalNode(game)));
                    break;
                }
                GameLoopProcessor.playerRoundEndUpdate(player, isLoggedOn);
                createDeclineRaceBranches(nextDepth, game, nextPlayer, edges);
                break;
            default:
                throw new CoinsException(CoinsErrorCode.ACTION_TYPE_NOT_FOUND);
        }
        return createNodeTree(currentDepth, game, edges);
    }

    /**
     * Создать поддерево захватов клетки симуляционного дерева игры
     *
     * @param game   - игра в текущем состоянии
     * @param player - игрок
     * @param action - действие, привёдшее к данному узлу
     * @return узел с оценённым данным действием
     */
    private static @NotNull NodeTree createCatchCellSubtree(final int currentDepth,
                                                            final @NotNull IGame game, final @NotNull Player player,
                                                            final @NotNull Action action,
                                                            final @NotNull Set<Cell> prevCatchCells)
            throws CoinsException {

        if (((CatchCellAction) action).getResolution() == null) {
            throw new CoinsException(CoinsErrorCode.LOGIC_ERROR);
        }
        final List<Edge> edges = Collections.synchronizedList(new LinkedList<>());
        createCatchCellsNodes(currentDepth, game, player, edges, prevCatchCells);
        return createNodeTree(currentDepth, game, edges);
    }

    /**
     * Создать всевозможные узлы с захватом клеток
     *
     * @param game           - игра
     * @param player         - игрок
     * @param edges          - список дуг от общего родителя
     * @param prevCatchCells - предыдущие захваченные клетки
     */
    private static void createCatchCellsNodes(final int currentDepth,
                                              final @NotNull IGame game, final @NotNull Player player,
                                              final @NotNull List<Edge> edges,
                                              final @NotNull Set<Cell> prevCatchCells) {
//        final List<ExecutorService> executorServices = new LinkedList<>();
        boolean wasCreated = false;
        if (!player.getUnitsByState(AvailabilityType.AVAILABLE).isEmpty()) {
            final Set<Cell> achievableCells = new HashSet<>(game.getPlayerToAchievableCells().get(player));
            achievableCells.removeAll(prevCatchCells);
            for (final Cell achievableCell : achievableCells) {
                final Pair<List<Unit>, Pair<Integer, Cell>> unitsToPairTiredUnitsToCell =
                        getUnitsToPairTiredUnitsToCell(game, player, achievableCell, prevCatchCells);
                if (unitsToPairTiredUnitsToCell == null) {
                    continue;
                } // else

                final List<Unit> units = unitsToPairTiredUnitsToCell.getFirst();
                final int tiredUnitsCount = unitsToPairTiredUnitsToCell.getSecond().getFirst();
                final Cell cell = unitsToPairTiredUnitsToCell.getSecond().getSecond();

//        final ExecutorService executorService1 =
//                Executors.newFixedThreadPool(1);
//        executorService1.execute(() ->
//                createCatchCellNode(currentDepth, tiredUnitsCount, game, player, cell,
//                        Collections.synchronizedList(new LinkedList<>(units)), edges, prevCatchCells));

                for (int i = tiredUnitsCount; i <= units.size(); i++) {
                    final Set<Cell> copyPrevCatchCells = Collections.synchronizedSet(new HashSet<>(prevCatchCells));
                    createCatchCellNode(currentDepth, i, game, player, cell,
                            Collections.synchronizedList(new LinkedList<>(units)), edges, copyPrevCatchCells);
                }

//                addExecuteService(currentDepth, game, player, edges, prevCatchCells,
//                        unitsToPairTiredUnitsToCell, executorServices);
                wasCreated = true;
                break;
            }
        }
        if (!wasCreated) {
            createCatchCellEndNode(currentDepth, game, player, edges);

//            final ExecutorService executorServiceEnd = Executors.newFixedThreadPool(1);
//            executorServiceEnd.execute(() -> createCatchCellEndNode(currentDepth, game, player, edges));
//            executorServices.add(executorServiceEnd);
        }
//        final ExecutorService executorService = Executors.newFixedThreadPool(executorServices.size());
//        executorServices.forEach(item -> executorService.execute(() -> executeExecutorService(item)));
//        executeExecutorService(executorService);
    }

    /**
     * Создание заключительного узла с захватом клетки (с resolution = null)
     *
     * @param game   - игра
     * @param player - игрок
     * @param edges  - дуги от родителя
     */
    private static void createCatchCellEndNode(final int currentDepth,
                                               final @NotNull IGame game, final @NotNull Player player,
                                               final @NotNull List<Edge> edges) {
        final Action newAction = new CatchCellAction(null);
        final IGame gameCopy = game.getCopy();
        final Player playerCopy = getPlayerCopy(gameCopy, player);
        GameLoopProcessor.makeAllUnitsSomeState(playerCopy, AvailabilityType.AVAILABLE);
        try {
            edges.add(new Edge(player, newAction, createSubtree(currentDepth, gameCopy, playerCopy, newAction)));
        } catch (final CoinsException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * @param game           - игра
     * @param player         - игрок
     * @param achievableCell - достижимая клетка
     * @param prevCatchCells - предыдущие захваченные клетки в этой ветке
     * @return пару (список юнитов, пара(число уставших юнитов, захватываемая клетка)
     */
    private static @Nullable Pair<List<Unit>, Pair<Integer, Cell>> getUnitsToPairTiredUnitsToCell(
            final @NotNull IGame game, final @NotNull Player player, final @NotNull Cell achievableCell,
            final @NotNull Set<Cell> prevCatchCells) {

        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<Cell> catchingCellNeighboringCells =
                new LinkedList<>(
                        Objects.requireNonNull(game.getBoard().getNeighboringCells(
                                Objects.requireNonNull(achievableCell))));
        catchingCellNeighboringCells.removeIf(neighboringCell -> !controlledCells.contains(neighboringCell));
        final List<Unit> units =
                Collections.synchronizedList(new LinkedList<>(player.getUnitsByState(AvailabilityType.AVAILABLE)));
        removeNotAvailableForCaptureUnits(game.getBoard(), units, catchingCellNeighboringCells,
                achievableCell, controlledCells);
        units.removeIf(unit -> achievableCell.getUnits().contains(unit));
        final int tiredUnitsCount;
        if (controlledCells.contains(achievableCell)) {
            tiredUnitsCount = achievableCell.getType().getCatchDifficulty();
        } else {
            final int unitsCountNeededToCatchCell =
                    GameLoopProcessor.getUnitsCountNeededToCatchCell(game.getGameFeatures(),
                            achievableCell, isLoggedOn);
            final int bonusAttack =
                    GameLoopProcessor.getBonusAttackToCatchCell(player, game.getGameFeatures(),
                            achievableCell, isLoggedOn);
            tiredUnitsCount = unitsCountNeededToCatchCell - bonusAttack;
        }
        prevCatchCells.add(achievableCell);
        return units.size() >= tiredUnitsCount
                ? new Pair<>(units, new Pair<>(tiredUnitsCount, achievableCell))
                : null;
    }

//    /**
//     * Добавить ExecuteService (вариация по числу юнитов для захвата)
//     *
//     * @param game                        - игра
//     * @param player                      - игрок
//     * @param edges                       - дуги от общего родителя
//     * @param prevCatchCells              - предыдущие захваченные клетки в этой ветке
//     * @param unitsToPairTiredUnitsToCell - пара (список юнитов, пара(число уставших юнитов, захватываемая клетка)
//     * @param executorServices            - список сервисов, в который нужно добавить новый
//     */
//    private static void addExecuteService(final int currentDepth,
//                                          final @NotNull IGame game, final @NotNull Player player,
//                                          final @NotNull List<Edge> edges, final @NotNull Set<Cell> prevCatchCells,
//                                          final @NotNull Pair<List<Unit>, Pair<Integer, Cell>>
//                                                  unitsToPairTiredUnitsToCell,
//                                          final @NotNull List<ExecutorService> executorServices) {
//        final List<Unit> units = unitsToPairTiredUnitsToCell.getFirst();
//        final int tiredUnitsCount = unitsToPairTiredUnitsToCell.getSecond().getFirst();
//        final Cell cell = unitsToPairTiredUnitsToCell.getSecond().getSecond();
//
////        final ExecutorService executorService1 =
////                Executors.newFixedThreadPool(1);
////        executorService1.execute(() ->
////                createCatchCellNode(currentDepth, tiredUnitsCount, game, player, cell,
////                        Collections.synchronizedList(new LinkedList<>(units)), edges, prevCatchCells));
//
//        final ExecutorService executorService1 =
//                Executors.newFixedThreadPool(units.size() - tiredUnitsCount + 1);
//        for (int i = tiredUnitsCount; i <= units.size(); i++) {
//            final int index = i;
//            final Set<Cell> copyPrevCatchCells = Collections.synchronizedSet(new HashSet<>(prevCatchCells));
//            executorService1.execute(() ->
//                    createCatchCellNode(currentDepth, index, game, player, cell,
//                            Collections.synchronizedList(new LinkedList<>(units)), edges, copyPrevCatchCells));
//        }
//        executorServices.add(executorService1);
//    }

    /**
     * Выполнить ExecutorService
     *
     * @param executorService - очевидно, ExecutorService
     */
    private static void executeExecutorService(final @NotNull ExecutorService executorService) {
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создать узел с захватом клетки
     *
     * @param index          - индекс потока
     * @param game           - игра
     * @param player         - игрок
     * @param cell           - захватываемая клетка
     * @param units          - список юнитов для захвата
     * @param edges          - дуги от родителя
     * @param prevCatchCells - предыдущие захваченные клетки
     */
    private static void createCatchCellNode(final int currentDepth, final int index,
                                            final @NotNull IGame game, final @NotNull Player player,
                                            final @NotNull Cell cell, final @NotNull List<Unit> units,
                                            final @NotNull List<Edge> edges, final @NotNull Set<Cell> prevCatchCells) {
        final Pair<Position, List<Unit>> resolution =
                new Pair<>(game.getBoard().getPositionByCell(cell), units.subList(0, index));
        final Action newAction = new CatchCellAction(resolution);
        final IGame gameCopy = game.getCopy();
        final Player playerCopy = getPlayerCopy(gameCopy, player);
        try {
            updateGame(gameCopy, playerCopy, newAction);
            edges.add(new Edge(player, newAction,
                    createCatchCellSubtree(currentDepth, gameCopy, playerCopy, newAction, prevCatchCells)));
        } catch (final CoinsException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Создать всевозможные узлы с распределением юнитов
     *
     * @param game   - игра
     * @param player - игрок
     * @param edges  - дуги от родителя
     * @throws CoinsException при ошибке обновления игры
     */
    private static void createDistributionUnitsNodes(final int currentDepth,
                                                     final @NotNull IGame game, final @NotNull Player player,
                                                     final @NotNull List<Edge> edges) throws CoinsException {
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<List<Pair<Cell, Integer>>> distributions = getDistributions(controlledCells,
                player.getUnitsByState(AvailabilityType.AVAILABLE).size());
        if (distributions.size() == 0) {
            final Action action = new DistributionUnitsAction(new HashMap<>());
            final IGame gameCopy = game.getCopy();
            final Player playerCopy = getPlayerCopy(gameCopy, player);
            updateGame(gameCopy, playerCopy, action);
            edges.add(new Edge(player, action, createSubtree(currentDepth, gameCopy, playerCopy, action)));
            return;
        }
        final ExecutorService executorService = Executors.newFixedThreadPool(distributions.size());
        for (final List<Pair<Cell, Integer>> distribution : distributions) {
            executorService.execute(() -> createDistributionUnitsNode(currentDepth, game, player, edges, distribution));
        }
        executeExecutorService(executorService);
    }

    /**
     * Создать узел с распределением юнитов
     *
     * @param game         - игра
     * @param player       - игрок
     * @param edges        - дуги от родителя
     * @param distribution - распределение
     */
    private static void createDistributionUnitsNode(final int currentDepth,
                                                    final @NotNull IGame game, final @NotNull Player player,
                                                    final @NotNull List<Edge> edges,
                                                    final @NotNull List<Pair<Cell, Integer>> distribution) {
        final Map<Position, List<Unit>> resolution = new HashMap<>();
        for (final Pair<Cell, Integer> pair : distribution) {
            final List<Unit> availableUnits = new LinkedList<>(player.getUnitsByState(AvailabilityType.AVAILABLE));
            final List<Unit> units = new LinkedList<>(availableUnits.subList(
                    0, pair.getSecond())); // список юнитов, которое игрок хочет распределить в эту клетку
            if (!units.isEmpty()) {
                resolution.put(game.getBoard().getPositionByCell(pair.getFirst()), units);
                availableUnits.removeAll(units);
            }
        }
        final Action action = new DistributionUnitsAction(resolution);
        final IGame gameCopy = game.getCopy();
        final Player playerCopy = getPlayerCopy(gameCopy, player);
        try {
            updateGame(gameCopy, playerCopy, action);
            edges.add(new Edge(player, action, createSubtree(currentDepth, gameCopy, playerCopy, action)));
        } catch (final CoinsException exception) {
            exception.printStackTrace();
        }
    }

    //FIXME: придумать перебор всех случаев распределения юнитов
    @Contract(pure = true)
    private static @NotNull List<List<Pair<Cell, Integer>>> getDistributions(final @NotNull List<Cell> cells,
                                                                             final int n) {
        final List<List<Pair<Cell, Integer>>> distributions = new LinkedList<>();
        for (int i = 0; i < cells.size(); i++) {
            final List<Pair<Cell, Integer>> distribution = new LinkedList<>();
            int j = 0;
            for (final Cell cell : cells) {
                distribution.add(new Pair<>(cell, j == i ? n : 0));
                j++;
            }
            distributions.add(distribution);
        }
        return distributions;
    }

    /**
     * @param game - игра в текущем состоянии
     * @return терминальный узел с оценённым данным действием
     */
    @Contract("_ -> new")
    private static @NotNull NodeTree createTerminalNode(final @NotNull IGame game) {
        final List<Player> winners = new LinkedList<>();
        int maxCoinsCount = 0;
        for (final Player item : game.getPlayers()) {
            if (item.getCoins() > maxCoinsCount) {
                maxCoinsCount = item.getCoins();
                winners.clear();
                winners.add(item);
                continue;
            }
            if (item.getCoins() == maxCoinsCount) {
                winners.add(item);
            }
        }
        final Map<Player, Integer> map = new HashMap<>();
        game.getPlayers().forEach(player -> map.put(player, winners.contains(player) ? 1 : 0));
        return new NodeTree(new LinkedList<>(), map, 1);
    }

    /**
     * @param nodeTree - узел дерева, в котором мы в данный момент находимся
     * @return самое выгодное на данном этапе действие
     */
    public static @NotNull Action getAction(final @NotNull NodeTree nodeTree) {
        return Objects.requireNonNull(nodeTree.getEdges().stream()
                .max(Comparator.comparingDouble(edge ->
                        (double) edge.getTo().getWinsCount().get(edge.getPlayer()) / edge.getTo().getCasesCount()))
                .orElseThrow()
                .getAction());
    }
}
