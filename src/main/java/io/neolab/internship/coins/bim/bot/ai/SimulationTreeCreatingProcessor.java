package io.neolab.internship.coins.bim.bot.ai;

import io.neolab.internship.coins.bim.bot.FunctionType;
import io.neolab.internship.coins.bim.bot.ai.model.Edge;
import io.neolab.internship.coins.bim.bot.ai.model.NodeTree;
import io.neolab.internship.coins.bim.bot.ai.model.action.*;
import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.service.GameLoopProcessor;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.utils.Triplet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static io.neolab.internship.coins.server.service.GameAnswerProcessor.*;

public class SimulationTreeCreatingProcessor {
    static final boolean isGameLoggedOn = false; // логгирование в функциях игры

    /**
     * Создать узел дерева
     *
     * @param currentDepth - текущая глубина
     * @param game         - игра
     * @param edges        - дуги к потомкам
     * @param functionType - тип функции бота
     * @return узел дерева
     */
    @Contract("_, _, _, _ -> new")
    @SuppressWarnings("ConstantConditions")
    static @NotNull NodeTree createNodeTree(final int currentDepth, final @NotNull IGame game,
                                            final @NotNull List<Edge> edges, final @NotNull FunctionType functionType) {
        if (isValueDifferenceFunctionType(functionType)) {
            return createNodeTreeDifferenceValue(currentDepth, game, edges);
        }
        if (isPercentFunctionType(functionType)) {
            return createNodeTreePercent(currentDepth, game, edges);
        }
        if (isValueFunctionType(functionType)) {
            return createNodeTreeValue(currentDepth, game, edges);
        }
        return null;
    }

    /**
     * Создать узел дерева с информацией о кол-ве побед игрока
     *
     * @param currentDepth - текущая глубина
     * @param game         - игра
     * @param edges        - дуги к потомкам
     * @return узел дерева с информацией о кол-ве побед игрока
     */
    @Contract("_, _, _ -> new")
    private static @NotNull NodeTree createNodeTreePercent(final int currentDepth, final @NotNull IGame game,
                                                           final @NotNull List<Edge> edges) {
        int casesCount = 0;
        final Map<Player, Integer> winsCount = new HashMap<>(game.getPlayers().size());
        game.getPlayers().forEach(player1 -> winsCount.put(player1, 0));
        for (final Edge edge : edges) {
            Objects.requireNonNull(edge.getTo().getWinsCount()).forEach((key, value) ->
                    winsCount.replace(key, winsCount.get(key) + value));
            casesCount += edge.getTo().getCasesCount();
        }
        AILogger.printLogCreatedNewNode(currentDepth, edges);
        return new NodeTree(edges, winsCount, casesCount, null, null);
    }

    /**
     * Создать узел дерева с информацией о числе монет игрока
     *
     * @param currentDepth - текущая глубина
     * @param game         - игра
     * @param edges        - дуги к потомкам
     * @return узел дерева с информацией о числе монет игрока
     */
    @Contract("_, _, _ -> new")
    private static @NotNull NodeTree createNodeTreeValue(final int currentDepth, final @NotNull IGame game,
                                                         final @NotNull List<Edge> edges) {
        int casesCount = 0;
        final Map<Player, Pair<Integer, Integer>> playerToMaxAndMinCoinsCount =
                new HashMap<>(game.getPlayers().size());
        game.getPlayers().forEach(player1 -> playerToMaxAndMinCoinsCount.put(player1,
                new Pair<>(-1, Integer.MAX_VALUE)));
        for (final Edge edge : edges) {
            Objects.requireNonNull(edge.getTo().getPlayerToMaxAndMinCoinsCount()).forEach((key, value) ->
                    playerToMaxAndMinCoinsCount.replace(key,
                            new Pair<>(
                                    Math.max(playerToMaxAndMinCoinsCount.get(key).getFirst(), value.getFirst()),
                                    Math.min(playerToMaxAndMinCoinsCount.get(key).getSecond(), value.getSecond())
                            )
                    )
            );
            casesCount += edge.getTo().getCasesCount();
        }
        AILogger.printLogCreatedNewNode(currentDepth, edges);
        return new NodeTree(edges, null, casesCount, playerToMaxAndMinCoinsCount, null);
    }

    /**
     * Создать узел дерева с информацией о минимальной разности чисел монет игроков
     *
     * @param currentDepth - текущая глубина
     * @param game         - игра
     * @param edges        - дуги к потомкам
     * @return узел дерева с информацией о минимальной разности чисел монет игроков
     */
    @Contract("_, _, _ -> new")
    private static @NotNull NodeTree createNodeTreeDifferenceValue(final int currentDepth, final @NotNull IGame game,
                                                                   final @NotNull List<Edge> edges) {
        int casesCount = 0;
        final Map<Player, Integer> playerToValueDifference = new HashMap<>(game.getPlayers().size());
        for (final Edge edge : edges) {
            Objects.requireNonNull(edge.getTo().getPlayerToValueDifference()).forEach((key, value) -> {
                        if (playerToValueDifference.containsKey(key)) {
                            playerToValueDifference.replace(key,
                                    Math.min(playerToValueDifference.get(key),
                                            value));
                        } else {
                            playerToValueDifference.put(key,
                                    Math.min(playerToValueDifference.getOrDefault(key, value),
                                            value));
                        }
                    }
            );
            casesCount += edge.getTo().getCasesCount();
        }
        AILogger.printLogCreatedNewNode(currentDepth, edges);
        return new NodeTree(edges, null, casesCount, null, playerToValueDifference);
    }

    /**
     * @param functionType - тип функции бота
     * @return true, если бот ориентируется на проценты, false - иначе
     */
    @Contract(pure = true)
    private static boolean isPercentFunctionType(final @NotNull FunctionType functionType) {
        return functionType == FunctionType.MAX_PERCENT
                || functionType == FunctionType.MIN_PERCENT
                || functionType == FunctionType.MIN_MAX_PERCENT;
    }

    /**
     * @param functionType - тип функции бота
     * @return true, если бот ориентируется на число монет, false - иначе
     */
    @Contract(pure = true)
    private static boolean isValueFunctionType(final @NotNull FunctionType functionType) {
        return functionType == FunctionType.MAX_VALUE
                || functionType == FunctionType.MIN_VALUE
                || functionType == FunctionType.MIN_MAX_VALUE;
    }

    /**
     * @param functionType - тип функции бота
     * @return true, если бот ориентируется на разность чисел монет, false - иначе
     */
    @Contract(pure = true)
    private static boolean isValueDifferenceFunctionType(final @NotNull FunctionType functionType) {
        return functionType == FunctionType.MAX_VALUE_DIFFERENCE
                || functionType == FunctionType.MIN_MAX_VALUE_DIFFERENCE;
    }

    /**
     * Создать терминальный узел
     *
     * @param game         - игра в текущем состоянии
     * @param functionType - тип функции бота
     * @return терминальный узел с оценённым данным действием
     */
    @Contract("_, _ -> new")
    @SuppressWarnings("ConstantConditions")
    static @NotNull NodeTree createTerminalNode(final @NotNull IGame game, final @NotNull FunctionType functionType) {
        if (isValueDifferenceFunctionType(functionType)) {
            return createTerminalNodeValueDifference(game);
        }
        if (isPercentFunctionType(functionType)) {
            return createTerminalNodePercent(game);
        }
        if (isValueFunctionType(functionType)) {
            return createTerminalNodeValue(game);
        }
        return null;
    }

    /**
     * Создать терминальный узел с информацией о кол-ве побед каждого игрока
     *
     * @param game - игра
     * @return терминальный узел с информацией о кол-ве побед каждого игрока
     */
    @Contract("_ -> new")
    private static @NotNull NodeTree createTerminalNodePercent(final @NotNull IGame game) {
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
        final Map<Player, Integer> winsCount = new HashMap<>(game.getPlayers().size());
        game.getPlayers().forEach(player -> winsCount.put(player, winners.contains(player) ? 1 : 0));
        AILogger.printLogNewTerminalNodePercent(winsCount);
        return new NodeTree(new LinkedList<>(), winsCount, 1, null, null);
    }

    /**
     * Создать терминальный узел с информацией о числе монет каждого игрока
     *
     * @param game - игра
     * @return терминальный узел с информацией о числе монет каждого игрока
     */
    @Contract("_ -> new")
    private static @NotNull NodeTree createTerminalNodeValue(final @NotNull IGame game) {
        final Map<Player, Pair<Integer, Integer>> playerToMaxAndMinCoinsCount = new HashMap<>(game.getPlayers().size());
        game.getPlayers().forEach(player1 ->
                playerToMaxAndMinCoinsCount.put(player1, new Pair<>(player1.getCoins(), player1.getCoins())));
        AILogger.printLogNewTerminalNodeValue(playerToMaxAndMinCoinsCount);
        return new NodeTree(new LinkedList<>(), null, 1,
                playerToMaxAndMinCoinsCount, null);
    }

    /**
     * Создать терминальный узел с информацией о минимальной разности чисел монет игроков
     *
     * @param game - игра
     * @return терминальный узел с информацией о минимальной разности чисел монет игроков
     */
    @Contract("_ -> new")
    private static @NotNull NodeTree createTerminalNodeValueDifference(final @NotNull IGame game) {
        final Map<Player, Integer> playerToValueDifference = new HashMap<>(game.getPlayers().size());
        game.getPlayers().forEach(player -> {
            int minValueDifference = Integer.MAX_VALUE;
            for (final Player player1 : game.getPlayers()) {
                if (!player1.equals(player)) {
                    final int valueDifference = player.getCoins() - player1.getCoins();
                    if (valueDifference < minValueDifference) {
                        minValueDifference = valueDifference;
                    }
                }
            }
            playerToValueDifference.put(player, minValueDifference);
        });
        AILogger.printLogNewTerminalNodeValueDifference(playerToValueDifference);
        return new NodeTree(new LinkedList<>(), null, 1,
                null, playerToValueDifference);
    }

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
     * Обновить состояние игры исходя из совершённого действия
     *
     * @param game   - игра (желательно копия)
     * @param player - игрок
     * @param action - совершённое действие
     * @throws CoinsException при неизвестном типе действия
     */
    static void updateGame(final @NotNull IGame game, final @NotNull Player player,
                           final @NotNull Action action) throws CoinsException {
        switch (action.getType()) {
            case DECLINE_RACE:
                updateGameAfterDeclineRace(game, player);
                return;
            case CHANGE_RACE:
                updateGameAfterChangeRace(game, player, action);
                return;
            case CATCH_CELL:
                updateGameAfterCatchCell(game, player, action);
                return;
            case DISTRIBUTION_UNITS:
                updateGameAfterDistributionUnits(game, player, action);
                return;
            default:
                throw new CoinsException(CoinsErrorCode.ACTION_TYPE_NOT_FOUND);
        }
    }

    /**
     * Обновить состояние игры исходя после создания узла с уходом в упадок (или нет)
     *
     * @param game   - игра
     * @param player - игрок
     */
    private static void updateGameAfterDeclineRace(final @NotNull IGame game, final @NotNull Player player) {
        GameLoopProcessor.updateAchievableCells(player, game.getBoard(),
                game.getPlayerToAchievableCells().get(player),
                game.getOwnToCells().get(player), isGameLoggedOn);
        GameLoopProcessor.makeAllUnitsSomeState(player, AvailabilityType.AVAILABLE);
    }

    /**
     * Обновить состояние игры исходя из совершённого действия смены расы
     *
     * @param game   - игра
     * @param player - игрок
     * @param action - совершённое действие
     */
    private static void updateGameAfterChangeRace(final @NotNull IGame game, final @NotNull Player player,
                                                  final @NotNull Action action) {
        game.getOwnToCells().get(player).clear(); // Освобождаем все занятые игроком клетки (юниты остаются там же)
        changeRace(player, ((ChangeRaceAction) action).getNewRace(), game.getRacesPool(), isGameLoggedOn);
        GameLoopProcessor.updateAchievableCells(player, game.getBoard(),
                game.getPlayerToAchievableCells().get(player), game.getOwnToCells().get(player),
                isGameLoggedOn);
    }

    /**
     * Обновить состояние игры исходя из совершённого действия захвата клетки
     *
     * @param game   - игра
     * @param player - игрок
     * @param action - совершённое действие
     */
    private static void updateGameAfterCatchCell(final @NotNull IGame game, final @NotNull Player player,
                                                 final @NotNull Action action) {
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
    }

    /**
     * Обновить состояние игры исходя из совершённого действия распределения юнитов
     *
     * @param game   - игра
     * @param player - игрок
     * @param action - совершённое действие
     */
    private static void updateGameAfterDistributionUnits(final @NotNull IGame game, final @NotNull Player player,
                                                         final @NotNull Action action) {
        final DistributionUnitsAction distributionUnitsAction = (DistributionUnitsAction) action;
        distributionUnits(player, game.getOwnToCells().get(player),
                game.getFeudalToCells().get(player),
                distributionUnitsAction.getResolutions(),
                game.getBoard(), isGameLoggedOn);
    }

    /**
     * Взять следующего игрока из начала списка игроков
     *
     * @param game - игра
     * @return следующего игрока из начала списка
     */
    static @Nullable Player getNextPlayerFromBeginList(final @NotNull IGame game) {
        return game.getCurrentRound() <= Game.ROUNDS_COUNT
                ? game.getPlayers().get(0)
                : null;
    }

    /**
     * Обновить игру перед спуском на новую глубину
     *
     * @param newDepth   - новая глубина
     * @param game       - игра
     * @param prevPlayer - предыдущий игрока
     * @param nextPlayer - следующий игрок
     */
    static void updateGameBeforeNewDepth(final int newDepth,
                                         final @NotNull IGame game,
                                         final @NotNull Player prevPlayer,
                                         final @Nullable Player nextPlayer) {
        GameLoopProcessor.playerRoundEndUpdate(prevPlayer, isGameLoggedOn);
        updateGameEndRound(game);
        AILogger.printLogNewDepth(newDepth, game.getCurrentRound());
        if (nextPlayer != null) {
            AILogger.printLogNextPlayer(nextPlayer);
            GameLoopProcessor.playerRoundBeginUpdate(nextPlayer, isGameLoggedOn);
        }
    }

    /**
     * Обновление игры в конце раунда
     *
     * @param game - игра
     */
    static void updateGameEndRound(final @NotNull IGame game) {
        updateCoins(game);
        game.incrementCurrentRound();
    }

    /**
     * Обновить число монет
     *
     * @param game - игра
     */
    static void updateCoins(final @NotNull IGame game) {
        game.getPlayers().forEach(item -> // обновление числа монет у каждого игрока
                GameLoopProcessor.updateCoinsCount(
                        item, game.getFeudalToCells().get(item),
                        game.getGameFeatures(), game.getBoard(), isGameLoggedOn));
    }

    /**
     * Взять ссылку на игрока из скопированной игры
     *
     * @param gameCopy - копия игры
     * @param player   - игрок, которого мы ищем в скопированной игре
     * @return ссылку на копию игрока
     */
    static @NotNull Player getPlayerCopy(final @NotNull IGame gameCopy, final @NotNull Player player) {
        return gameCopy.getPlayers().stream()
                .filter(player1 -> player1.getId() == player.getId())
                .findFirst()
                .orElseThrow();
    }

    /**
     * Узнать список юнитов, доступных для захвата клетки, и сложность захвата клетки
     *
     * @param game           - игра
     * @param player         - игрок
     * @param achievableCell - достижимая клетка
     * @param prevCatchCells - предыдущие захваченные клетки в этой ветке
     * @return тройку (список юнитов, число уставших юнитов, захватываемая клетка)
     */
    static @Nullable Triplet<List<Unit>, Integer, Cell> getUnitsToPairTiredUnitsToCell(
            final @NotNull IGame game, final @NotNull Player player, final @NotNull Cell achievableCell,
            final @NotNull Set<Cell> prevCatchCells) {
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<Unit> units =
                getAvailableForCaptureCellUnits(game, player, achievableCell, controlledCells,
                        getCatchingCellNeighboringCells(game, achievableCell, controlledCells));
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
                ? new Triplet<>(units, tiredUnitsCount, achievableCell)
                : null;
    }

    /**
     * Взять соседние с клеткой и подконтрольные игроку клетки
     *
     * @param game            - игра
     * @param cell            - клетка
     * @param controlledCells - список подконтрольных клеток
     * @return список соседних с cell и подконтрольных игроку клеток
     */
    private static @NotNull List<Cell> getCatchingCellNeighboringCells(final @NotNull IGame game,
                                                                       final @NotNull Cell cell,
                                                                       final @NotNull List<Cell> controlledCells) {
        final List<Cell> catchingCellNeighboringCells =
                new LinkedList<>(
                        Objects.requireNonNull(game.getBoard().getNeighboringCells(
                                Objects.requireNonNull(cell))));
        catchingCellNeighboringCells.removeIf(neighboringCell -> !controlledCells.contains(neighboringCell));
        return catchingCellNeighboringCells;
    }

    /**
     * Взять доступные для захвата клетки юниты
     *
     * @param game                         - игра
     * @param player                       - игрок
     * @param cell                         - захватываемая клетка
     * @param controlledCells              - список подконтрольных клеток игрока
     * @param catchingCellNeighboringCells - список соседних с cell и подконтрольных игроку клеток
     * @return список юнитов, доступных для захвата клетки cell
     */
    private static @NotNull List<Unit> getAvailableForCaptureCellUnits(final @NotNull IGame game,
                                                                       final @NotNull Player player,
                                                                       final @NotNull Cell cell,
                                                                       final @NotNull List<Cell> controlledCells,
                                                                       final @NotNull List<Cell>
                                                                               catchingCellNeighboringCells) {
        final List<Unit> units =
                Collections.synchronizedList(new LinkedList<>(player.getUnitsByState(AvailabilityType.AVAILABLE)));
        AIProcessor.removeNotAvailableForCaptureUnits(game.getBoard(), units, catchingCellNeighboringCells,
                cell, controlledCells);
        units.removeIf(unit -> cell.getUnits().contains(unit));
        return units;
    }
}
