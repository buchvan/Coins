package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.client.bot.ai.bim.model.Edge;
import io.neolab.internship.coins.client.bot.ai.bim.model.NodeTree;
import io.neolab.internship.coins.client.bot.ai.bim.model.action.*;
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
     * @return узел дерева
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    @Contract("_, _, _ -> new")
    static @NotNull NodeTree createNodeTree(final int currentDepth, final @NotNull IGame game,
                                            final @NotNull List<Edge> edges) {
        final Map<Player, Integer> winsCount = new HashMap<>();
        game.getPlayers().forEach(player1 -> winsCount.put(player1, 0));
        int casesCount = 0;
        synchronized (edges) {
            for (final Edge edge : edges) {
                edge.getTo().getWinsCount().forEach((key, value) -> winsCount.replace(key, winsCount.get(key) + value));
                casesCount += edge.getTo().getCasesCount();
            }
        }
        AILogger.printLogCreatedNewNode(currentDepth, edges);
        return new NodeTree(edges, winsCount, casesCount);
    }

    /**
     * Создать терминальный узел
     *
     * @param game - игра в текущем состоянии
     * @return терминальный узел с оценённым данным действием
     */
    @Contract("_ -> new")
    static @NotNull NodeTree createTerminalNode(final @NotNull IGame game) {
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
     * Игрок имеет право первого хода?
     *
     * @param game   - игра
     * @param player - игрок
     * @return true, если игрок ходит первым, false - иначе
     */
    static boolean isFirstPlayer(final @NotNull IGame game, final @NotNull Player player) {
        return game.getPlayers().get(0).getId() == player.getId();
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
     * @return пару (список юнитов, пара(число уставших юнитов, захватываемая клетка)
     */
    static @Nullable Pair<List<Unit>, Pair<Integer, Cell>> getUnitsToPairTiredUnitsToCell(
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
        AIProcessor.removeNotAvailableForCaptureUnits(game.getBoard(), units, catchingCellNeighboringCells,
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
}
