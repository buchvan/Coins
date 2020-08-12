package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.client.bot.ai.bim.model.Edge;
import io.neolab.internship.coins.client.bot.ai.bim.model.FunctionType;
import io.neolab.internship.coins.client.bot.ai.bim.model.NodeTree;
import io.neolab.internship.coins.client.bot.ai.bim.model.action.*;
import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.service.GameLoopProcessor;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.utils.RandomGenerator;
import io.neolab.internship.coins.utils.Triplet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.neolab.internship.coins.client.bot.ai.bim.SimulationTreeCreatingProcessor.*;

public class SimulationTreeCreator {
    private int maxDepth;
    private final @NotNull FunctionType functionType;

    @Contract(pure = true)
    public SimulationTreeCreator(final @NotNull FunctionType functionType) {
        this.functionType = functionType;
    }

    /**
     * Выйти на новую глубину
     *
     * @param currentDepth  - текущая глубина
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
                return reachNewDepthsInThisRound(currentDepth, game, player);
            }
        }
        if (wasCurrentPlayer) {
            return reachNewDepthsInNewRound(currentDepth, game, currentPlayer);
        }
        throw new CoinsException(CoinsErrorCode.PLAYER_NOT_FOUND);
    }

    /**
     * Выйти на новую глубину в пределах того же раунда
     *
     * @param currentDepth  - текущая глубина
     * @param game          - игра
     * @param nextPlayer    - следующий игрок
     * @return пару (новая глубина, следующий в очереди игрок)
     */
    @Contract("_, _, _ -> new")
    private @NotNull Pair<@NotNull Integer, @Nullable Player> reachNewDepthsInThisRound(final int currentDepth,
                                                                                        final @NotNull IGame game,
                                                                                        final @NotNull
                                                                                                Player nextPlayer) {
        final int newDepth = currentDepth + 1;
        if (newDepth > maxDepth) {
            updateCoins(game);
            return new Pair<>(newDepth, null);
        }
        return new Pair<>(newDepth, nextPlayer);
    }

    /**
     * Выйти на новую глубину в пределах уже следующего раунда
     *
     * @param currentDepth  - текущая глубина
     * @param game          - игра
     * @param currentPlayer - текущий игрок
     * @return пару (новая глубина, следующий в очереди игрок)
     */
    @Contract("_, _, _ -> new")
    private @NotNull Pair<@NotNull Integer, @Nullable Player> reachNewDepthsInNewRound(final int currentDepth,
                                                                                       final @NotNull IGame game,
                                                                                       final @NotNull
                                                                                               Player currentPlayer) {
        final int newDepth = currentDepth + 1;
        final Player nextPlayer = getNextPlayerFromBeginList(game);
        if (newDepth > maxDepth) {
            updateCoins(game);
            return new Pair<>(newDepth, null);
        }
        updateGameBeforeNewDepth(newDepth, game, currentPlayer, nextPlayer);
        return new Pair<>(newDepth, nextPlayer);
    }

    /**
     * Создать ветви с уходом в упадок игрока
     *
     * @param currentDepth - текущая глубина
     * @param game         - игра
     * @param player       - игрок
     * @param edges        - дуги от родителя
     */
    private void createDeclineRaceBranches(final int currentDepth,
                                           final @NotNull IGame game, final @NotNull Player player,
                                           final @NotNull List<Edge> edges) {
        final boolean isPossible = game.getRacesPool().size() > 0;
        final boolean isFirstChoice = game.getCurrentRound() == 1;
        if (isPossible && !isFirstChoice) {
            final ExecutorService executorService = Executors.newFixedThreadPool(2);
            executorService.execute(() -> createDeclineRaceNode(currentDepth, game, player, edges, true));
            executorService.execute(() -> createDeclineRaceNode(currentDepth, game, player, edges, false));
            ExecutorServiceProcessor.executeExecutorService(executorService);
            return;
        }
        createDeclineRaceNode(currentDepth, game, player, edges, false);
    }

    /**
     * Создать узел с упадком расы
     *
     * @param currentDepth  - текущая глубина
     * @param game          - игра
     * @param player        - игрок
     * @param edges         - список рёбер
     * @param isDeclineRace - это упадок?
     */
    private void createDeclineRaceNode(final int currentDepth,
                                       final @NotNull IGame game, final @NotNull Player player,
                                       final @NotNull List<Edge> edges, final boolean isDeclineRace) {
        final Action newAction = new DeclineRaceAction(isDeclineRace);
        try {
            AILogger.printLogDeclineRace(currentDepth, player, isDeclineRace);
            edges.add(new Edge(player.getId(), newAction, createSubtree(currentDepth, game, player, newAction)));
        } catch (final CoinsException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Создать ветви с изменением расы игрока
     *
     * @param currentDepth - текущая глубина
     * @param game         - игра
     * @param player       - игрок
     * @param edges        - дуги от родителя
     */
    private void createChangeRaceBranches(final int currentDepth,
                                          final @NotNull IGame game, final @NotNull Player player,
                                          final @NotNull List<Edge> edges) {
//        final int capacity = Math.min(game.getRacesPool().size(), 3);
//        final Set<Race> races = Collections.synchronizedSet(new HashSet<>(capacity));
//        if (game.getRacesPool().contains(Race.ELF)) {
//            races.add(Race.ELF);
//        }
//        if (game.getRacesPool().contains(Race.UNDEAD)) {
//            races.add(Race.UNDEAD);
//        }
//        if (game.getRacesPool().contains(Race.AMPHIBIAN)) {
//            races.add(Race.AMPHIBIAN);
//        }
//        for (final Race race : game.getRacesPool()) {
//            if (races.size() >= capacity) {
//                break;
//            }
//            races.add(race);
//        }
        final ExecutorService executorService = Executors.newFixedThreadPool(game.getRacesPool().size());
        game.getRacesPool().forEach(race -> executorService.execute(() -> {
            final Action newAction = new ChangeRaceAction(race);
            try {
                AILogger.printLogChangeRace(currentDepth, race, player);
                edges.add(new Edge(player.getId(), newAction, createSubtree(currentDepth, game, player, newAction)));
            } catch (final CoinsException exception) {
                exception.printStackTrace();
            }
        }));
        ExecutorServiceProcessor.executeExecutorService(executorService);
    }

    /**
     * Создать симуляционное дерево игры
     *
     * @param game   - игра
     * @param player - игрок 1
     * @return корень на созданное дерево
     */
    public @NotNull NodeTree createTree(final @NotNull IGame game, final @NotNull Player player, final int tempDepth) {
        this.maxDepth = tempDepth;
        final List<Edge> edges = Collections.synchronizedList(new LinkedList<>());
        if (isBeforeGame(game)) {
            createChangeRaceBranches(1, game, player, edges);
        } else {
            createDeclineRaceBranches(1, game, player, edges);
        }
        return SimulationTreeCreatingProcessor.createNodeTree(0, game, edges, functionType);
    }

    /**
     * Создать поддерево симуляционного дерева игры
     *
     * @param currentDepth - текущая глубина
     * @param game         - игра в текущем состоянии
     * @param player       - игрок
     * @param action       - действие, привёдшее к данному узлу
     * @return узел с оценённым данным действием
     */
    private @NotNull NodeTree createSubtree(final int currentDepth,
                                            final @NotNull IGame game, final @NotNull Player player,
                                            final @NotNull Action action)
            throws CoinsException {
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
        return SimulationTreeCreatingProcessor.createNodeTree(currentDepth, game, edges, functionType);
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
        if (!isBeforeGame(game)) {
            createCatchCellsNodes(
                    currentDepth, gameCopy, playerCopy, edges, Collections.synchronizedSet(new HashSet<>()));
            return;
        } // else
        boolean wasCurrentPlayer = false;
        boolean isChangeRaceBranchesCreated = false;
        for (final Player item : gameCopy.getPlayers()) {
            if (item.equals(playerCopy)) {
                wasCurrentPlayer = true;
                continue;
            }
            if (wasCurrentPlayer) {
                createChangeRaceBranches(currentDepth, gameCopy, item, edges);
                isChangeRaceBranchesCreated = true;
                break;
            }
        }
        if (wasCurrentPlayer && !isChangeRaceBranchesCreated) {
            gameCopy.incrementCurrentRound();
            createDeclineRaceBranches(currentDepth, gameCopy, gameCopy.getPlayers().get(0), edges);
        }
    }

    /**
     * Игра началась?
     *
     * @param game - игра
     * @return true, если да, началась, false - иначе
     */
    private boolean isBeforeGame(final @NotNull IGame game) {
        return game.getCurrentRound() == 0;
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
            edges.add(new Edge(-1, null,
                    SimulationTreeCreatingProcessor.createTerminalNode(game, functionType)));
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
        final List<Triplet<List<Unit>, Integer, Cell>> unitsToPairTiredUnitsToCellList = new LinkedList<>();
        if (!player.getUnitsByState(AvailabilityType.AVAILABLE).isEmpty()) {
            final Set<Cell> achievableCells = new HashSet<>(game.getPlayerToAchievableCells().get(player));
            achievableCells.removeAll(prevCatchCells);
            for (final Cell achievableCell : achievableCells) {
                if (isCellBeneficial(game, player, achievableCell) || RandomGenerator.isYes()) {
                    final Triplet<List<Unit>, Integer, Cell> triplet =
                            getUnitsToPairTiredUnitsToCell(game, player, achievableCell, prevCatchCells);
                    if (triplet != null) {
                        unitsToPairTiredUnitsToCellList.add(triplet);
                    }
                }
            }
            unitsToPairTiredUnitsToCellList.forEach(unitsToPairTiredUnitsToCell -> {
                final List<Unit> units = unitsToPairTiredUnitsToCell.getFirst();
                final int tiredUnitsCount = unitsToPairTiredUnitsToCell.getSecond();
                final Cell cell = unitsToPairTiredUnitsToCell.getThird();
                AIDistributionProcessor.getIndexes(units, tiredUnitsCount, maxDepth).forEach(i ->
                        createCatchCellNode(currentDepth, i, game, player, cell,
                                Collections.synchronizedList(new LinkedList<>(units)), edges, prevCatchCells));
            });
        }
        if (unitsToPairTiredUnitsToCellList.isEmpty()) {
            createCatchCellEndNode(currentDepth, game, player, edges);
        }
    }

    /**
     * Клетка выгодна игроку?
     *
     * @param game   - игра
     * @param player - игрок
     * @param cell   - клетка
     * @return true, если выгодна, false - иначе
     */
    private boolean isCellBeneficial(final @NotNull IGame game, final @NotNull Player player,
                                     final @NotNull Cell cell) {
        return player.getRace() == Race.ELF
                && game.getOwnToCells().get(player)
                .stream()
                .noneMatch(controlledCell ->
                        controlledCell.getType() == cell.getType())
                || player.getRace() == Race.AMPHIBIAN && cell.getType() == CellType.WATER
                || player.getRace() == Race.MUSHROOM && cell.getType() == CellType.MUSHROOM;
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
            edges.add(new Edge(
                    playerCopy.getId(), newAction, createSubtree(currentDepth, gameCopy,
                    playerCopy, newAction)));
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
            edges.add(new Edge(playerCopy.getId(), newAction,
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
        return SimulationTreeCreatingProcessor.createNodeTree(currentDepth, game, edges, functionType);
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
        final List<List<Pair<Cell, Integer>>> distributions =
                AIDistributionProcessor.getDistributions(controlledCells,
                        player.getUnitsByState(AvailabilityType.AVAILABLE).size());
        final Set<List<Pair<Cell, Integer>>> actualDistributions =
                AIDistributionProcessor
                        .distributionsNumberReduce(distributions, game.getOwnToCells().get(player).size());
        if (actualDistributions.size() == 0) {
            createDistributionUnitsNode(currentDepth, game, player, edges, new LinkedList<>());
            return;
        }
        final ExecutorService executorService = Executors.newFixedThreadPool(actualDistributions.size());
        actualDistributions.forEach(distribution ->
                executorService.execute(() ->
                        createDistributionUnitsNode(currentDepth, game, player, edges, distribution)));
        ExecutorServiceProcessor.executeExecutorService(executorService);
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
            edges.add(new Edge(
                    playerCopy.getId(), action, createSubtree(currentDepth, gameCopy, playerCopy, action)));
        } catch (final CoinsException exception) {
            exception.printStackTrace();
        }
    }
}
