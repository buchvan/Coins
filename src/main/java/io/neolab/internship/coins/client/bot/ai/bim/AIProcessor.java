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
     * @throws InterruptedException при ошибке потока
     */
    private static void createDeclineRaceBranches(final @NotNull IGame game, final @NotNull Player player,
                                                  final @NotNull List<Edge> edges) throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> {
            final Action newAction = new DeclineRaceAction(true);
            try {
                edges.add(new Edge(player, newAction, createSubtree(game, player, newAction, null)));
            } catch (final CoinsException | InterruptedException exception) {
                exception.printStackTrace();
            }
        });
        executorService.execute(() -> {
            final Action newAction = new DeclineRaceAction(false);
            try {
                edges.add(new Edge(player, newAction, createSubtree(game, player, newAction, null)));
            } catch (final CoinsException | InterruptedException exception) {
                exception.printStackTrace();
            }
        });
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    /**
     * Создать ветви с изменением расы игрока
     *
     * @param game   - игра
     * @param player - игрок
     * @param edges  - дуги от родителя
     * @throws InterruptedException при ошибке потока
     */
    private static void createChangeRaceBranches(final @NotNull IGame game, final @NotNull Player player,
                                                 final @NotNull List<Edge> edges) throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(game.getRacesPool().size());
        game.getRacesPool().forEach(race -> executorService.execute(() -> {
            final Action newAction = new ChangeRaceAction(race);
            try {
                edges.add(new Edge(player, newAction, createSubtree(game, player,
                        newAction, null)));
            } catch (final CoinsException | InterruptedException exception) {
                exception.printStackTrace();
            }
        }));
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    /**
     * Создать симуляционное дерево игры
     *
     * @param game   - игра
     * @param player - игрок 1
     * @return корень на созданное дерево
     * @throws InterruptedException при ошибке потока
     */
    public static @NotNull NodeTree createTree(final @NotNull IGame game, final @NotNull Player player)
            throws InterruptedException {
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
                                                   final @NotNull Action action,
                                                   final @Nullable Set<Action> prevActions)
            throws CoinsException, InterruptedException {

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
                createCatchCellsNodes(gameCopy, playerCopy, edges, Collections.synchronizedSet(new HashSet<>()));
                break;
            case CHANGE_RACE:
                gameCopy = game.getCopy();
                playerCopy = getPlayerCopy(gameCopy, player);
                updateGame(gameCopy, playerCopy, action);
                createCatchCellsNodes(gameCopy, playerCopy, edges, Collections.synchronizedSet(new HashSet<>()));
                break;
            case CATCH_CELL:
                if (((CatchCellAction) action).getResolution() == null) {
                    createDistributionUnitsNodes(game, player, edges);
                    break;
                }
                createCatchCellsNodes(game, player, edges, Objects.requireNonNull(prevActions));
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
     * Создать всевозможные узлы с захватом клеток
     *
     * @param game   - игра
     * @param player - игрок
     * @param edges  - список дуг от общего родителя
     */
    private static void createCatchCellsNodes(final @NotNull IGame game, final @NotNull Player player,
                                              final @NotNull List<Edge> edges, final @NotNull Set<Action> prevActions)
            throws InterruptedException {

        final ExecutorService executorService =
                Executors.newFixedThreadPool(game.getPlayerToAchievableCells().get(player).size() + 1);
        executorService.execute(() -> createCatchCellEndNode(game, player, edges));
        for (final Cell cell : game.getPlayerToAchievableCells().get(player)) {
            executorService.execute(() -> createCatchCellNodes(game, player, cell, edges, prevActions));
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
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
            edges.add(new Edge(player, newAction, createSubtree(gameCopy, playerCopy, newAction, null)));
        } catch (final CoinsException | InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Создание всевозможных узлов с захватом клетки (вариативность по числу юнитов)
     *
     * @param game        - игра
     * @param player      - игрок
     * @param cell        - захватываемая клетка
     * @param edges       - дуги от родителя
     * @param prevActions - предыдущие действия
     */
    private static void createCatchCellNodes(final @NotNull IGame game, final @NotNull Player player,
                                             final @NotNull Cell cell, final @NotNull List<Edge> edges,
                                             final @NotNull Set<Action> prevActions) {
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
            return;
        }
        final ExecutorService executorService1 =
                Executors.newFixedThreadPool(units.size() - tiredUnitsCount + 1);
        for (int i = tiredUnitsCount; i <= units.size(); i++) {
            final int index = i;
            executorService1.execute(() ->
                    createCatchCellNode(index, game, player, cell, units, edges, prevActions));
        }
        executorService1.shutdown();
        try {
            executorService1.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создать узел с захватом клетки
     *
     * @param index       - индекс потока
     * @param game        - игра
     * @param player      - игрок
     * @param cell        - захватываемая клетка
     * @param units       - список юнитов для захвата
     * @param edges       - дуги от родителя
     * @param prevActions - предыдущие действия
     */
    private static void createCatchCellNode(final int index,
                                            final @NotNull IGame game, final @NotNull Player player,
                                            final @NotNull Cell cell, final @NotNull List<Unit> units,
                                            final @NotNull List<Edge> edges, final @NotNull Set<Action> prevActions) {
        final Pair<Position, List<Unit>> resolution =
                new Pair<>(game.getBoard().getPositionByCell(cell), units.subList(0, index));
        final Action newAction = new CatchCellAction(resolution);
        final IGame gameCopy = game.getCopy();
        final Player playerCopy = getPlayerCopy(gameCopy, player);
        try {
            updateGame(gameCopy, playerCopy, newAction);
            if (!prevActions.add(newAction)) {
                return;
            }
            final Edge newEdge =
                    new Edge(player, newAction, createSubtree(gameCopy, playerCopy, newAction, prevActions));
            edges.add(newEdge);
        } catch (final CoinsException | InterruptedException exception) {
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
     * @throws InterruptedException при ошибке работы потоков
     */
    private static void createDistributionUnitsNodes(final @NotNull IGame game, final @NotNull Player player,
                                                     final @NotNull List<Edge> edges)
            throws CoinsException, InterruptedException {

        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<List<Pair<Cell, Integer>>> distributions = getDistributions(controlledCells,
                player.getUnitsByState(AvailabilityType.AVAILABLE).size());
        if (distributions.size() == 0) {
            final Action action = new DistributionUnitsAction(new HashMap<>());
            final IGame gameCopy = game.getCopy();
            final Player playerCopy = getPlayerCopy(gameCopy, player);
            updateGame(gameCopy, playerCopy, action);
            edges.add(new Edge(player, action, createSubtree(gameCopy, playerCopy, action, null)));
            return;
        }
        final ExecutorService executorService = Executors.newFixedThreadPool(distributions.size());
        for (final List<Pair<Cell, Integer>> distribution : distributions) {
            executorService.execute(() -> createDistributionUnitsNode(game, player, edges, distribution));
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
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
            edges.add(new Edge(player, action, createSubtree(gameCopy, playerCopy, action, null)));
        } catch (final CoinsException | InterruptedException exception) {
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
