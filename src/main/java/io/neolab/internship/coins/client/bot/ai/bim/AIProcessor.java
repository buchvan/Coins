package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.client.bot.ai.bim.model.action.*;
import io.neolab.internship.coins.client.bot.ai.bim.model.Edge;
import io.neolab.internship.coins.client.bot.ai.bim.model.FunctionType;
import io.neolab.internship.coins.client.bot.ai.bim.model.NodeTree;
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
import io.neolab.internship.coins.utils.RandomGenerator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.neolab.internship.coins.server.service.GameAnswerProcessor.*;

public class AIProcessor {
    private static final double EPS = 1E-7;
    private static final int MAX_DEPTH = 1;
    private static final boolean isGameLoggedOn = false;

    /**
     * Обновить дерево согласно выполненному действию
     *
     * @param tree   - дерево (ссылка на корень)
     * @param action - совершённое действие
     * @return ссылка на новый корень
     */
    public static NodeTree updateTree(final @NotNull NodeTree tree, final @NotNull Action action) {
        NodeTree newTree = null;
        for (final Edge edge : tree.getEdges()) {
            if (Objects.requireNonNull(edge.getAction()).equals(action)) {
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
                        game.getOwnToCells().get(player), isGameLoggedOn);
                GameLoopProcessor.makeAllUnitsSomeState(player, AvailabilityType.AVAILABLE);
                return;
            case CHANGE_RACE:
                game.getOwnToCells().get(player).clear(); // Освобождаем все занятые игроком клетки (юниты остаются там же)
                changeRace(player, ((ChangeRaceAction) action).getNewRace(), game.getRacesPool(), isGameLoggedOn);
                GameLoopProcessor.updateAchievableCells(player, game.getBoard(),
                        game.getPlayerToAchievableCells().get(player), game.getOwnToCells().get(player),
                        isGameLoggedOn);
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
                        game.getPlayerToAchievableCells().get(player), isGameLoggedOn);
                return;
            case DISTRIBUTION_UNITS:
                final DistributionUnitsAction distributionUnitsAction = (DistributionUnitsAction) action;
                distributionUnits(player, game.getOwnToCells().get(player),
                        game.getFeudalToCells().get(player),
                        distributionUnitsAction.getResolutions(),
                        game.getBoard(), isGameLoggedOn);
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
    @Contract("_, _, _ -> new")
    private static @NotNull Pair<@NotNull Integer, @Nullable Player> getNextPlayer(final int currentDepth,
                                                                                   final @NotNull IGame game, final @NotNull Player currentPlayer)
            throws CoinsException {
        GameLoopProcessor.playerRoundEndUpdate(currentPlayer, isGameLoggedOn);
        boolean wasCurrentPlayer = false;
        for (final Player player : game.getPlayers()) {
            if (player.equals(currentPlayer)) {
                wasCurrentPlayer = true;
                continue;
            }
            if (wasCurrentPlayer) {
                AILogger.printLogNextPlayer(player);
                GameLoopProcessor.playerRoundBeginUpdate(player, isGameLoggedOn);
                return new Pair<>(currentDepth, player);
            }
        }
        if (wasCurrentPlayer) {
            final int newDepth = currentDepth + 1;
            if (newDepth > MAX_DEPTH) {
                return new Pair<>(newDepth, null);
            }
            updateGameEndRound(game);
            AILogger.printLogNewDepth(newDepth, game.getCurrentRound());
            final Player nextPlayer =
                    game.getCurrentRound() <= Game.ROUNDS_COUNT
                            ? game.getPlayers().get(0)
                            : null;
            if (nextPlayer != null) {
                AILogger.printLogNextPlayer(nextPlayer);
                GameLoopProcessor.playerRoundBeginUpdate(nextPlayer, isGameLoggedOn);
            }
            return new Pair<>(newDepth, nextPlayer);
        }
        throw new CoinsException(CoinsErrorCode.PLAYER_NOT_FOUND);
    }

    /**
     * Обновление игры в конце раунда
     *
     * @param game - игра
     */
    private static void updateGameEndRound(final @NotNull IGame game) {
        game.getPlayers().forEach(item -> // обновление числа монет у каждого игрока
                GameLoopProcessor.updateCoinsCount(
                        item, game.getFeudalToCells().get(item),
                        game.getGameFeatures(), game.getBoard(), isGameLoggedOn));
        game.incrementCurrentRound();
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
        final boolean isPossible = game.getRacesPool().size() > 0;
        final ExecutorService executorService = Executors.newFixedThreadPool(isPossible ? 2 : 1);
        if (isPossible) {
            executorService.execute(() -> {
                final boolean isDeclineRace = true;
                final Action newAction = new DeclineRaceAction(isDeclineRace);
                try {
                    AILogger.printLogDeclineRace(currentDepth, player, isDeclineRace);
                    edges.add(new Edge(player, newAction, createSubtree(currentDepth, game, player, newAction)));
                } catch (final CoinsException exception) {
                    exception.printStackTrace();
                }
            });
        }
        executorService.execute(() -> {
            final boolean isDeclineRace = false;
            final Action newAction = new DeclineRaceAction(isDeclineRace);
            try {
                AILogger.printLogDeclineRace(currentDepth, player, isDeclineRace);
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
                AILogger.printLogChangeRace(currentDepth, race, player);
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
        createDeclineRaceBranches(1, gameCopy, playerCopy, edges);
        return createNodeTree(0, gameCopy, edges);
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
        AILogger.printLogCreatedNewNode(currentDepth, edges);
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
        switch (action.getType()) {
            case DECLINE_RACE:
                createDeclineRaceSubtree(currentDepth, game, player, (DeclineRaceAction) action, edges);
                break;
            case CHANGE_RACE:
                createChangeRaceSubtree(currentDepth, game, player, action, edges);
                break;
            case CATCH_CELL:
                createCatchCellSubtree(currentDepth, game, player, action, edges);
                break;
            case DISTRIBUTION_UNITS:
                createDistributionUnitsSubtree(currentDepth, game, player, edges);
                break;
            default:
                throw new CoinsException(CoinsErrorCode.ACTION_TYPE_NOT_FOUND);
        }
        return createNodeTree(currentDepth, game, edges);
    }

    /**
     * Создать поддерево узла типа ухода в упадок
     *
     * @param currentDepth - текущая глубина
     * @param game         - игра
     * @param player       - игрок
     * @param action       - предыдущее действие
     * @param edges        - дуги от общего родителя
     * @throws CoinsException при ошибке обновления игры
     */
    private static void createDeclineRaceSubtree(final int currentDepth,
                                                 final @NotNull IGame game, final @NotNull Player player,
                                                 final @NotNull DeclineRaceAction action,
                                                 final @NotNull List<Edge> edges) throws CoinsException {
        if (action.isDeclineRace()) {
            createChangeRaceBranches(currentDepth, game, player, edges);
            return;
        }
        final IGame gameCopy = game.getCopy();
        final Player playerCopy = getPlayerCopy(gameCopy, player);
        updateGame(gameCopy, playerCopy, action);
        createCatchCellsNodes(currentDepth, gameCopy, playerCopy, edges,
                Collections.synchronizedSet(new HashSet<>()));
    }

    /**
     * Создать поддерево узла типа смены расы
     *
     * @param currentDepth - текущая глубина
     * @param game         - игра
     * @param player       - игрок
     * @param action       - предыдущее действие
     * @param edges        - дуги от общего родителя
     * @throws CoinsException при ошибке обновления игры
     */
    private static void createChangeRaceSubtree(final int currentDepth,
                                                final @NotNull IGame game, final @NotNull Player player,
                                                final @NotNull Action action, final @NotNull List<Edge> edges)
            throws CoinsException {

        final IGame gameCopy = game.getCopy();
        final Player playerCopy = getPlayerCopy(gameCopy, player);
        updateGame(gameCopy, playerCopy, action);
        createCatchCellsNodes(currentDepth, gameCopy, playerCopy, edges,
                Collections.synchronizedSet(new HashSet<>()));
    }

    /**
     * Создать поддерево узла типа захват клетки
     *
     * @param currentDepth - текущая глубина
     * @param game         - игра в текущем состоянии
     * @param player       - игрок
     * @param action       - действие, привёдшее к данному узлу
     * @param edges        - дуги от общего родителя
     * @throws CoinsException при ошибке обновления игры
     */
    private static void createCatchCellSubtree(final int currentDepth,
                                               final @NotNull IGame game, final @NotNull Player player,
                                               final @NotNull Action action,
                                               final @NotNull List<Edge> edges)
            throws CoinsException {

        if (((CatchCellAction) action).getResolution() != null) {
            throw new CoinsException(CoinsErrorCode.LOGIC_ERROR);
        }
        final List<Cell> transitCells = game.getPlayerToTransitCells().get(player);
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        GameLoopProcessor.freeTransitCells(player, transitCells, controlledCells, isGameLoggedOn);
        controlledCells.forEach(controlledCell -> controlledCell.getUnits().clear());
        GameLoopProcessor.makeAllUnitsSomeState(player,
                AvailabilityType.AVAILABLE); // доступными юнитами становятся все имеющиеся у игрока юниты
        createDistributionUnitsNodes(currentDepth, game, player, edges);
    }

    /**
     * Создать поддерево узла типа распределение юнитов
     *
     * @param currentDepth - текущая глубина
     * @param game         - игра в текущем состоянии
     * @param player       - игрок
     * @param edges        - дуги от общего родителя
     * @throws CoinsException при ошибке обновления игры
     */
    private static void createDistributionUnitsSubtree(final int currentDepth,
                                                       final @NotNull IGame game, final @NotNull Player player,
                                                       final @NotNull List<Edge> edges) throws CoinsException {

        final Pair<Integer, Player> pair = getNextPlayer(currentDepth, game, player);
        final int newDepth = pair.getFirst();
        final Player nextPlayer = pair.getSecond();
        if (nextPlayer == null) {
            edges.add(new Edge(null, null, createTerminalNode(game)));
            return;
        }
        GameLoopProcessor.playerRoundEndUpdate(player, isGameLoggedOn);
        createDeclineRaceBranches(newDepth, game, nextPlayer, edges);
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
        final List<Pair<List<Unit>, Pair<Integer, Cell>>> unitsToPairTiredUnitsToCellList = new LinkedList<>();
        if (!player.getUnitsByState(AvailabilityType.AVAILABLE).isEmpty()) {
            final Set<Cell> achievableCells = new HashSet<>(game.getPlayerToAchievableCells().get(player));
            achievableCells.removeAll(prevCatchCells);
            for (final Cell achievableCell : achievableCells) {
                if (RandomGenerator.isYes()) {
                    final Pair<List<Unit>, Pair<Integer, Cell>> pair =
                            getUnitsToPairTiredUnitsToCell(game, player, achievableCell, prevCatchCells);
                    if (pair != null) {
                        unitsToPairTiredUnitsToCellList.add(pair);
                    }
                }
            }
            unitsToPairTiredUnitsToCellList.forEach(unitsToPairTiredUnitsToCell -> {
                final List<Unit> units = unitsToPairTiredUnitsToCell.getFirst();
                final int tiredUnitsCount = unitsToPairTiredUnitsToCell.getSecond().getFirst();
                final Cell cell = unitsToPairTiredUnitsToCell.getSecond().getSecond();
                for (int i = tiredUnitsCount; i <= units.size(); i++) {
                    createCatchCellNode(currentDepth, i, game, player, cell,
                            Collections.synchronizedList(new LinkedList<>(units)), edges, prevCatchCells);
                }
            });
        }
        if (unitsToPairTiredUnitsToCellList.isEmpty()) {
            createCatchCellEndNode(currentDepth, game, player, edges);
        }
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
     * Узнать список юнитов, доступных для захвата клетки, и сложность захвата клетки
     *
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
                            achievableCell, isGameLoggedOn);
            final int bonusAttack =
                    GameLoopProcessor.getBonusAttackToCatchCell(player, game.getGameFeatures(),
                            achievableCell, isGameLoggedOn);
            tiredUnitsCount = unitsCountNeededToCatchCell - bonusAttack;
        }
        prevCatchCells.add(achievableCell);
        return units.size() >= tiredUnitsCount
                ? new Pair<>(units, new Pair<>(tiredUnitsCount, achievableCell))
                : null;
    }

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
        AILogger.printLogCatchCellResolution(currentDepth, index, player, resolution);
        final IGame gameCopy = game.getCopy();
        final Player playerCopy = getPlayerCopy(gameCopy, player);
        try {
            updateGame(gameCopy, playerCopy, newAction);
            edges.add(new Edge(player, newAction,
                    continueCreatingCatchCellSubtree(currentDepth, gameCopy, playerCopy, newAction, prevCatchCells)));
        } catch (final CoinsException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Продолжить создание поддерева узла типа захват клетки
     *
     * @param currentDepth   - текущая глубина
     * @param game           - игра в текущем состоянии
     * @param player         - игрок
     * @param action         - действие, привёдшее к данному узлу
     * @param prevCatchCells - предыдущие захваченные клетки (в данном поддереве)
     * @return узел с оценённым данным действием
     */
    private static @NotNull NodeTree continueCreatingCatchCellSubtree(final int currentDepth,
                                                                      final @NotNull IGame game,
                                                                      final @NotNull Player player,
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
        for (final List<Pair<Cell, Integer>> distribution : distributions) {
            createDistributionUnitsNode(currentDepth, game, player, edges, distribution);
        }
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
     * Создать терминальный узел
     *
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
        AILogger.printLogNewTerminalNode(map);
        return new NodeTree(new LinkedList<>(), map, 1);
    }

    /**
     * @param nodeTree - узел дерева, в котором мы в данный момент находимся
     * @return самое выгодное на данном этапе действие (если таковых несколько, то берём случайное из их числа)
     */
    public static @NotNull Action getAction(final @NotNull NodeTree nodeTree,
                                            final @NotNull FunctionType functionType) {
        final double value;
        final Player player;
        switch (functionType) {
            case MAX:
                player = nodeTree.getEdges().get(0).getPlayer();
                value = nodeTree.getEdges().stream()
                        .map(edge ->
                                (double) edge.getTo().getWinsCount().get(player)
                                        / edge.getTo().getCasesCount())
                        .max(Double::compareTo)
                        .orElseThrow();
                break;
            case MIN:
                player =
                        nodeTree.getEdges().get(0).getTo()
                                .getWinsCount()
                                .keySet()
                                .stream()
                                .filter(item ->
                                        !item.equals(nodeTree.getEdges().get(0).getPlayer()))
                                .findFirst()
                                .orElseThrow();
                value = nodeTree.getEdges().stream()
                        .map(edge ->
                                (double) edge.getTo().getWinsCount().get(player)
                                        / edge.getTo().getCasesCount())
                        .min(Double::compareTo)
                        .orElseThrow();
                break;
            default:
                return null;
        }
        return Objects.requireNonNull(
                RandomGenerator.chooseItemFromList(nodeTree.getEdges().stream()
                        .filter(edge ->
                                Double.compare(Math.abs(value -
                                        (double) edge.getTo().getWinsCount().get(player)
                                                / edge.getTo().getCasesCount()), EPS) < 0)
                        .collect(Collectors.toList()))
                        .getAction());
    }
}
