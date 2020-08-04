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

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.neolab.internship.coins.server.service.GameAnswerProcessor.*;

public class AIProcessor {

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
                        game.getOwnToCells().get(player));
                return;
            case CHANGE_RACE:
                changeRace(player, ((ChangeRaceAction) action).getNewRace(), game.getRacesPool());
                GameLoopProcessor.updateAchievableCells(player, game.getBoard(),
                        game.getPlayerToAchievableCells().get(player), game.getOwnToCells().get(player));
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
                        game.getPlayerToAchievableCells().get(player));
                return;
            case DISTRIBUTION_UNITS:
                final DistributionUnitsAction distributionUnitsAction = (DistributionUnitsAction) action;
                distributionUnits(player, game.getOwnToCells().get(player),
                        game.getFeudalToCells().get(player),
                        distributionUnitsAction.getResolutions(),
                        game.getBoard());
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
    private static @Nullable Player getNextPlayer(final @NotNull IGame game, final @NotNull Player currentPlayer)
            throws CoinsException {
        GameLoopProcessor.playerRoundEndUpdate(currentPlayer);
        boolean wasCurrentPlayer = false;
        for (final Player player : game.getPlayers()) {
            if (player.equals(currentPlayer)) {
                wasCurrentPlayer = true;
                continue;
            }
            if (wasCurrentPlayer) {
                GameLoopProcessor.playerRoundBeginUpdate(player);
                return player;
            }
        }
        if (wasCurrentPlayer) {
            game.incrementCurrentRound();
            final Player nextPlayer = game.getCurrentRound() <= Game.ROUNDS_COUNT ? game.getPlayers().get(0) : null;
            if (nextPlayer != null) {
                GameLoopProcessor.playerRoundBeginUpdate(nextPlayer);
            }
            return nextPlayer;
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
    private static void createDeclineRaceBranches(final @NotNull IGame game, final @NotNull Player player,
                                                  final @NotNull List<Edge> edges) {
        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> {
            final Action newAction = new DeclineRaceAction(true);
            try {
                edges.add(new Edge(player, newAction, createSubtree(game, player, newAction)));
            } catch (final CoinsException exception) {
                exception.printStackTrace();
            }
        });
        executorService.execute(() -> {
            final Action newAction = new DeclineRaceAction(false);
            try {
                edges.add(new Edge(player, newAction, createSubtree(game, player, newAction)));
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
    private static void createChangeRaceBranches(final @NotNull IGame game, final @NotNull Player player,
                                                 final @NotNull List<Edge> edges) {
        final ExecutorService executorService = Executors.newFixedThreadPool(game.getRacesPool().size());
        game.getRacesPool().forEach(race -> executorService.execute(() -> {
            final Action newAction = new ChangeRaceAction(race);
            try {
                edges.add(new Edge(player, newAction, createSubtree(game, player, newAction)));
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
        createDeclineRaceBranches(game, player, edges);
        return createNodeTree(game, edges);
    }

    /**
     * Создать узел дерева
     *
     * @param game  - игра
     * @param edges - дуги к потомкам
     * @return узел дерева
     */
    @Contract("_, _ -> new")
    private static @NotNull NodeTree createNodeTree(final @NotNull IGame game, final @NotNull List<Edge> edges) {
        final Map<Player, Integer> winsCount = new HashMap<>();
        game.getPlayers().forEach(player1 -> winsCount.put(player1, 0));
        int casesCount = 0;
        for (final Edge edge : edges) {
            edge.getTo().getWinsCount().forEach((key, value) -> winsCount.replace(key, winsCount.get(key) + value));
            casesCount += edge.getTo().getCasesCount();
        }
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
    private static @NotNull NodeTree createSubtree(final @NotNull IGame game, final @NotNull Player player,
                                                   final @NotNull Action action) throws CoinsException {
        final List<Edge> edges = Collections.synchronizedList(new LinkedList<>());
        final IGame gameCopy;
        final Player playerCopy;
        switch (action.getType()) {
            case DECLINE_RACE:
                if (((DeclineRaceAction) action).isDeclineRace()) {
                    createChangeRaceBranches(game, player, edges);
                    break;
                }
                gameCopy = game.getCopy();
                playerCopy = getPlayerCopy(gameCopy, player);
                updateGame(gameCopy, playerCopy, action);
                return createCatchCellSubtree(gameCopy, playerCopy, edges,
                        Collections.synchronizedSet(new HashSet<>()));
            case CHANGE_RACE:
                gameCopy = game.getCopy();
                playerCopy = getPlayerCopy(gameCopy, player);
                updateGame(gameCopy, playerCopy, action);
                return createCatchCellSubtree(gameCopy, playerCopy, edges,
                        Collections.synchronizedSet(new HashSet<>()));
            case CATCH_CELL:
                if (((CatchCellAction) action).getResolution() != null) {
                    throw new CoinsException(CoinsErrorCode.LOGIC_ERROR);
                }
                createDistributionUnitsNodes(game, player, edges);
                break;
            case DISTRIBUTION_UNITS:
                final Player nextPlayer = getNextPlayer(game, player);
                if (nextPlayer == null) {
                    edges.add(new Edge(null, null, createTerminalNode(game)));
                    break;
                }
                createDeclineRaceBranches(game, nextPlayer, edges);
                break;
            default:
                throw new CoinsException(CoinsErrorCode.ACTION_TYPE_NOT_FOUND);
        }
        return createNodeTree(game, edges);
    }

    /**
     * Создать поддерево с захватом клеток
     *
     * @param game           - игра
     * @param player         - игрок
     * @param edges          - список дуг от общего родителя
     * @param prevCatchCells - предыдущие захваченные клетки
     * @return ссылку на корень поддерева
     */
    private static NodeTree createCatchCellSubtree(final @NotNull IGame game, final Player player,
                                                   final @NotNull List<Edge> edges,
                                                   final @NotNull Set<Cell> prevCatchCells) {
        createCatchCellsNodes(game, player, edges, prevCatchCells);
        return createNodeTree(game, edges);
    }

    /**
     * Создать всевозможные узлы с захватом клеток
     *
     * @param game           - игра
     * @param player         - игрок
     * @param edges          - список дуг от общего родителя
     * @param prevCatchCells - предыдущие захваченные клетки
     */
    private static void createCatchCellsNodes(final @NotNull IGame game, final @NotNull Player player,
                                              final @NotNull List<Edge> edges,
                                              final @NotNull Set<Cell> prevCatchCells) {
        final Set<Cell> achievableCells = new HashSet<>(game.getPlayerToAchievableCells().get(player));
        final List<ExecutorService> executorServices = new LinkedList<>();
        final ExecutorService executorServiceEnd = Executors.newFixedThreadPool(1);
        executorServiceEnd.execute(() -> createCatchCellEndNode(game, player, edges));
        executorServices.add(executorServiceEnd);
        achievableCells.removeAll(prevCatchCells);
        final List<Pair<List<Unit>, Pair<Integer, Cell>>> unitsToTiredUnitsCount = new LinkedList<>();
        achievableCells.forEach(cell -> addPairUnitsToTiredUnits(game, player, cell, prevCatchCells,
                unitsToTiredUnitsCount));
        unitsToTiredUnitsCount.forEach(pair ->
                addExecutorService(game, player, edges, prevCatchCells, pair, executorServices));
        final ExecutorService executorService = Executors.newFixedThreadPool(executorServices.size());
        executorServices.forEach(item -> executorService.execute(() -> executeExecutorService(item)));
        executeExecutorService(executorService);
    }

    /**
     * Создание заключительного узла с захватом клетки (с resolution = null)
     *
     * @param game   - игра
     * @param player - игрок
     * @param edges  - дуги от родителя
     */
    private static void createCatchCellEndNode(final @NotNull IGame game, final @NotNull Player player,
                                               final @NotNull List<Edge> edges) {
        final Action newAction = new CatchCellAction(null);
        final IGame gameCopy = game.getCopy();
        final Player playerCopy = getPlayerCopy(gameCopy, player);
        GameLoopProcessor.makeAllUnitsSomeState(playerCopy, AvailabilityType.AVAILABLE);
        try {
            edges.add(new Edge(player, newAction, createSubtree(gameCopy, playerCopy, newAction)));
        } catch (final CoinsException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Добавить пару (список юнитов, пара(число уставших юнитов, клетка))
     *
     * @param game                   - игра
     * @param player                 - игрок
     * @param cell                   - клетка
     * @param prevCatchCells         - предыдущие захваченные клетки
     * @param unitsToTiredUnitsCount - список, в который нужно добавить пару
     */
    private static void addPairUnitsToTiredUnits(final @NotNull IGame game, final @NotNull Player player,
                                                 final @NotNull Cell cell, final @NotNull Set<Cell> prevCatchCells,
                                                 final @NotNull List<Pair<List<Unit>, Pair<Integer, Cell>>>
                                                         unitsToTiredUnitsCount) {
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<Cell> catchingCellNeighboringCells =
                new LinkedList<>(
                        Objects.requireNonNull(game.getBoard().getNeighboringCells(
                                Objects.requireNonNull(cell))));
        catchingCellNeighboringCells.removeIf(neighboringCell -> !controlledCells.contains(neighboringCell));
        final List<Unit> units = new LinkedList<>(player.getUnitsByState(AvailabilityType.AVAILABLE));
        removeNotAvailableForCaptureUnits(game.getBoard(), units, catchingCellNeighboringCells,
                cell, controlledCells);
        units.removeIf(unit -> cell.getUnits().contains(unit));
        final int unitsCountNeededToCatchCell =
                GameLoopProcessor.getUnitsCountNeededToCatchCell(game.getGameFeatures(), cell);
        final int bonusAttack = GameLoopProcessor.getBonusAttackToCatchCell(player, game.getGameFeatures(), cell);
        final int tiredUnitsCount =
                controlledCells.contains(cell)
                        ? cell.getType().getCatchDifficulty()
                        : unitsCountNeededToCatchCell - bonusAttack;
        if (units.size() <= tiredUnitsCount - 1) {
            prevCatchCells.add(cell);
            return;
        }
        unitsToTiredUnitsCount.add(new Pair<>(units, new Pair<>(tiredUnitsCount, cell)));
    }

    /**
     * Добавить ExecutorService
     *
     * @param game             - игра
     * @param player           - игрок
     * @param edges            - дуги от родителя
     * @param prevCatchCells   - предыдущие захваченные клетки
     * @param pair             - очередная пара (список юнитов, пара(число уставших юнитов, клетка))
     * @param executorServices - список, в который нужно добавить новый ExecutorService
     */
    private static void addExecutorService(final @NotNull IGame game, final @NotNull Player player,
                                           final @NotNull List<Edge> edges, final @NotNull Set<Cell> prevCatchCells,
                                           final @NotNull Pair<List<Unit>, Pair<Integer, Cell>> pair,
                                           final @NotNull List<ExecutorService> executorServices) {
        final List<Unit> units = pair.getFirst();
        final int tiredUnitsCount = pair.getSecond().getFirst();
        final Cell cell = pair.getSecond().getSecond();
        final ExecutorService executorService1 =
                Executors.newFixedThreadPool(units.size() - tiredUnitsCount + 1);
        for (int i = tiredUnitsCount; i <= units.size(); i++) {
            final int index = i;
            executorService1.execute(() ->
                    createCatchCellNode(index, game, player, cell, units, edges,
                            Collections.synchronizedSet(new HashSet<>(prevCatchCells))));
        }
        executorServices.add(executorService1);
    }

    /**
     * Выполнить ExecutorService
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
    private static void createCatchCellNode(final int index,
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
            prevCatchCells.add(cell);
            edges.add(new Edge(player, newAction, createCatchCellSubtree(gameCopy, playerCopy, edges, prevCatchCells)));
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
     * @throws CoinsException       при ошибке обновления игры
     */
    private static void createDistributionUnitsNodes(final @NotNull IGame game, final @NotNull Player player,
                                                     final @NotNull List<Edge> edges) throws CoinsException {
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<List<Pair<Cell, Integer>>> distributions = getDistributions(controlledCells,
                player.getUnitsByState(AvailabilityType.AVAILABLE).size());
        if (distributions.size() == 0) {
            final Action action = new DistributionUnitsAction(new HashMap<>());
            final IGame gameCopy = game.getCopy();
            final Player playerCopy = getPlayerCopy(gameCopy, player);
            updateGame(gameCopy, playerCopy, action);
            edges.add(new Edge(player, action, createSubtree(gameCopy, playerCopy, action)));
            return;
        }
        final ExecutorService executorService = Executors.newFixedThreadPool(distributions.size());
        for (final List<Pair<Cell, Integer>> distribution : distributions) {
            executorService.execute(() -> createDistributionUnitsNode(game, player, edges, distribution));
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
    private static void createDistributionUnitsNode(final @NotNull IGame game, final @NotNull Player player,
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
            edges.add(new Edge(player, action, createSubtree(gameCopy, playerCopy, action)));
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
