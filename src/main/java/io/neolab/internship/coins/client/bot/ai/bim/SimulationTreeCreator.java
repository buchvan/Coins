package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.client.bot.ai.bim.model.Edge;
import io.neolab.internship.coins.client.bot.ai.bim.model.NodeTree;
import io.neolab.internship.coins.client.bot.ai.bim.model.action.*;
import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
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

import static io.neolab.internship.coins.client.bot.ai.bim.SimulationTreeCreatingProcessor.*;

public class SimulationTreeCreator {
    private final int maxDepth;
    private static final long TIMEOUT_MILLIS = 5000;

    @Contract(pure = true)
    public SimulationTreeCreator(final int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * Выйти на новую глубину
     *
     * @param game          - игра
     * @param currentPlayer - текущий игрок
     * @return пару (новая глубина, следующий в очереди игрок)
     * @throws CoinsException в случае, если currentPlayer отсутствует в игре game
     */
    @Contract("_, _, _ -> new")
    private @NotNull Pair<@NotNull Integer, @Nullable Player> reachNewDepths(final int currentDepth,
                                                                             final @NotNull IGame game,
                                                                             final @NotNull Player currentPlayer)
            throws CoinsException {
        boolean wasCurrentPlayer = false;
        for (final Player player : game.getPlayers()) {
            if (player.equals(currentPlayer)) {
                wasCurrentPlayer = true;
                continue;
            }
            if (wasCurrentPlayer) {
                final int newDepth = currentDepth + 1;
                if (!isFirstPlayer(game, currentPlayer) && maxDepth % 2 != 1) {
                    updateGameBeforeNewDepth(newDepth, game, currentPlayer, player);
                } else if (isFirstPlayer(game, currentPlayer) && maxDepth % 2 != 0) {
                    updateGameBeforeNewDepth(newDepth, game, currentPlayer, player);
                }
                if (newDepth > maxDepth) {
                    return new Pair<>(newDepth, null);
                }
                return new Pair<>(newDepth, player);
            }
        }
        if (wasCurrentPlayer) {
            final int newDepth = currentDepth + 1;
            final Player nextPlayer = getNextPlayerFromBeginList(game);
            updateGameBeforeNewDepth(newDepth, game, currentPlayer, nextPlayer);
            if (newDepth > maxDepth) {
                return new Pair<>(newDepth, null);
            }
            return new Pair<>(newDepth, nextPlayer);
        }
        throw new CoinsException(CoinsErrorCode.PLAYER_NOT_FOUND);
    }

    /**
     * Создать ветви с уходом в упадок игрока
     *
     * @param game   - игра
     * @param player - игрок
     * @param edges  - дуги от родителя
     */
    private void createDeclineRaceBranches(final int currentDepth,
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
        ExecutorServiceProcessor.executeExecutorService(executorService, TIMEOUT_MILLIS);
    }

    /**
     * Создать ветви с изменением расы игрока
     *
     * @param game   - игра
     * @param player - игрок
     * @param edges  - дуги от родителя
     */
    private void createChangeRaceBranches(final int currentDepth,
                                          final @NotNull IGame game, final @NotNull Player player,
                                          final @NotNull List<Edge> edges) {
//        int capacity = game.getRacesPool().size() / 2;
//        capacity = capacity == 0 ? 1 : capacity;
//        final Set<Race> races = new HashSet<>(capacity);
//        for (final Race race : game.getRacesPool()) {
//            if (RandomGenerator.isYes()) {
//                races.add(race);
//            }
//            if (races.size() >= capacity) {
//                break;
//            }
//        }
//        if (races.size() < capacity) {
//            for (final Race race : game.getRacesPool()) {
//                races.add(race);
//                if (races.size() >= capacity) {
//                    break;
//                }
//            }
//        }
//        final ExecutorService executorService = Executors.newFixedThreadPool(capacity);
//        races.forEach(race -> executorService.execute(() -> {
//            final Action newAction = new ChangeRaceAction(race);
//            try {
//                AILogger.printLogChangeRace(currentDepth, race, player);
//                edges.add(new Edge(player, newAction, createSubtree(currentDepth, game, player, newAction)));
//            } catch (final CoinsException exception) {
//                exception.printStackTrace();
//            }
//        }));
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
        ExecutorServiceProcessor.executeExecutorService(executorService, TIMEOUT_MILLIS);
    }

    /**
     * Создать симуляционное дерево игры
     *
     * @param game   - игра
     * @param player - игрок 1
     * @return корень на созданное дерево
     */
    public @NotNull NodeTree createTree(final @NotNull IGame game, final @NotNull Player player) {
        final List<Edge> edges = Collections.synchronizedList(new LinkedList<>());
        final IGame gameCopy = game.getCopy();
        final Player playerCopy = getPlayerCopy(gameCopy, player);
        createDeclineRaceBranches(1, gameCopy, playerCopy, edges);
        return SimulationTreeCreatingProcessor.createNodeTree(0, gameCopy, edges);
    }

    /**
     * Создать поддерево симуляционного дерева игры
     *
     * @param game   - игра в текущем состоянии
     * @param player - игрок
     * @param action - действие, привёдшее к данному узлу
     * @return узел с оценённым данным действием
     */
    private @NotNull NodeTree createSubtree(final int currentDepth,
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
        return SimulationTreeCreatingProcessor.createNodeTree(currentDepth, game, edges);
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
    private void createDeclineRaceSubtree(final int currentDepth,
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
    private void createChangeRaceSubtree(final int currentDepth,
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
    private void createCatchCellSubtree(final int currentDepth,
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
    private void createDistributionUnitsSubtree(final int currentDepth,
                                                final @NotNull IGame game, final @NotNull Player player,
                                                final @NotNull List<Edge> edges) throws CoinsException {

        final Pair<Integer, Player> pair = reachNewDepths(currentDepth, game, player);
        final int newDepth = pair.getFirst();
        final Player nextPlayer = pair.getSecond();
        if (nextPlayer == null) {
            edges.add(new Edge(null, null, SimulationTreeCreatingProcessor.createTerminalNode(game)));
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
    private void createCatchCellsNodes(final int currentDepth,
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
                int capacity = (units.size() - tiredUnitsCount + 1) / 2;
                capacity = capacity == 0 ? 1 : capacity;
                final Set<Integer> indexes = new HashSet<>(capacity);
                for (int i = tiredUnitsCount; i <= units.size(); i++) {
                    if (RandomGenerator.isYes()) {
                        indexes.add(i);
                    }
                    if (indexes.size() >= capacity) {
                        i = units.size();
                    }
                }
                if (indexes.size() < capacity) {
                    for (int i = tiredUnitsCount; i <= units.size(); i++) {
                        indexes.add(i);
                        if (indexes.size() >= capacity) {
                            i = units.size();
                        }
                    }
                }
                indexes.forEach(i ->
                        createCatchCellNode(currentDepth, i, game, player, cell,
                                Collections.synchronizedList(new LinkedList<>(units)), edges, prevCatchCells));
//                for (int i = tiredUnitsCount; i <= units.size(); i++) {
//                    createCatchCellNode(currentDepth, i, game, player, cell,
//                            Collections.synchronizedList(new LinkedList<>(units)), edges, prevCatchCells);
//                }
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
    private void createCatchCellEndNode(final int currentDepth,
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
    private void createCatchCellNode(final int currentDepth, final int index,
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
    private @NotNull NodeTree continueCreatingCatchCellSubtree(final int currentDepth,
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
        return SimulationTreeCreatingProcessor.createNodeTree(currentDepth, game, edges);
    }

    /**
     * Создать всевозможные узлы с распределением юнитов
     *
     * @param game   - игра
     * @param player - игрок
     * @param edges  - дуги от родителя
     */
    private void createDistributionUnitsNodes(final int currentDepth,
                                              final @NotNull IGame game, final @NotNull Player player,
                                              final @NotNull List<Edge> edges) {
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<List<Pair<Cell, Integer>>> distributions = AIDistributionProcessor.getDistributions(controlledCells,
                player.getUnitsByState(AvailabilityType.AVAILABLE).size());
        AIDistributionProcessor.distributionsNumberReduce(distributions, player);
        if (distributions.size() == 0) {
            createDistributionUnitsNode(currentDepth, game, player, edges, new LinkedList<>());
            return;
        }
        final ExecutorService executorService = Executors.newFixedThreadPool(distributions.size());
        distributions.forEach(distribution ->
                executorService.execute(() ->
                        createDistributionUnitsNode(currentDepth, game, player, edges, distribution)));
        ExecutorServiceProcessor.executeExecutorService(executorService, TIMEOUT_MILLIS);
    }

    /**
     * Создать узел с распределением юнитов
     *
     * @param game         - игра
     * @param player       - игрок
     * @param edges        - дуги от родителя
     * @param distribution - распределение
     */
    private void createDistributionUnitsNode(final int currentDepth,
                                             final @NotNull IGame game, final @NotNull Player player,
                                             final @NotNull List<Edge> edges,
                                             final @NotNull List<Pair<Cell, Integer>> distribution) {
        final Map<Position, List<Unit>> resolution = new HashMap<>();
        distribution.forEach(pair -> {
            final List<Unit> availableUnits = new LinkedList<>(player.getUnitsByState(AvailabilityType.AVAILABLE));
            final List<Unit> units = new LinkedList<>(availableUnits.subList(
                    0, pair.getSecond())); // список юнитов, которое игрок хочет распределить в эту клетку
            resolution.put(game.getBoard().getPositionByCell(pair.getFirst()), units);
            availableUnits.removeAll(units);
        });
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
}
