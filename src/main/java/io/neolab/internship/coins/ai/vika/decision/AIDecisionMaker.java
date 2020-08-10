package io.neolab.internship.coins.ai.vika.decision;

import io.neolab.internship.coins.ai.vika.decision.model.*;
import io.neolab.internship.coins.ai.vika.decision.tree.DecisionTreeNode;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.neolab.internship.coins.ai.vika.decision.DecisionSimulationProcessor.*;
import static io.neolab.internship.coins.server.service.GameLoopProcessor.*;

/**
 * Симуляция принятия решений в ходе игры
 */
public class AIDecisionMaker {


    public static boolean getDeclineRaceDecision(final Player player, final IGame game) {
        return true;
    }

    /**
     * Создает новые решения - узлы потомки - об отправлении расы в упадок
     *
     * @param currentNode - текущий узел в симуляционном дереве
     * @param game        - текущее состояние игры
     * @param player      - игрок, относительно которого принимаются решение
     */
    public static void createDeclineRaceDecisions(@NotNull final DecisionTreeNode currentNode, @NotNull final IGame game,
                                                  @NotNull final Player player) {
        final Decision declineRaceDecisionTrue = new DeclineRaceDecision(true);
        final Player playerCopy = player.getCopy();
        final IGame gameCopy = game.getCopy();
        final DecisionTreeNode trueChildNode = new DecisionTreeNode(
                currentNode, declineRaceDecisionTrue, playerCopy, gameCopy);
        currentNode.getChildDecisions().add(trueChildNode);

        simulateDeclineRaceDecision(playerCopy, gameCopy, (DeclineRaceDecision) declineRaceDecisionTrue);

        createChangeRaceDecisions(trueChildNode, gameCopy, playerCopy);

        final Decision declineRaceDecisionFalse = new DeclineRaceDecision(true);
        final DecisionTreeNode falseChildNode = new DecisionTreeNode(
                currentNode, declineRaceDecisionFalse, player.getCopy(), game.getCopy());
        currentNode.getChildDecisions().add(falseChildNode);
        simulateDeclineRaceDecision(playerCopy, gameCopy, (DeclineRaceDecision) declineRaceDecisionFalse);
        createCatchCellDecisions(falseChildNode, gameCopy, playerCopy);
    }

    /**
     * Создает новые решения - узлы потомки - о смене расы
     *
     * @param currentNode - текущий узел в симуляционном дереве
     * @param game        - текущее состояние игры
     * @param player      - игрок, относительно которого принимаются решение
     */
    public static void createChangeRaceDecisions(@NotNull final DecisionTreeNode currentNode, @NotNull final IGame game,
                                                 @NotNull final Player player) {
        final List<Race> availableRaces = game.getRacesPool();
        availableRaces.forEach(race -> {
            final Player playerCopy = player.getCopy();
            final IGame gameCopy = game.getCopy();
            final Decision changeRaceDecision = new ChangeRaceDecision(race);
            final DecisionTreeNode childNode = new DecisionTreeNode(
                    currentNode, changeRaceDecision, player.getCopy(), game.getCopy());
            currentNode.getChildDecisions().add(childNode);

            simulateChangeRaceDecision(playerCopy, gameCopy, (ChangeRaceDecision) changeRaceDecision);

            createCatchCellDecisions(childNode, gameCopy, playerCopy);
        });
    }



    /**
     * Создает новые решения - узлы потомки - о захвате клеток
     *
     * @param currentNode - текущий узел в симуляционном дереве
     * @param game        - текущее состояние игры
     * @param player      - игрок, относительно которого принимаются решение
     */
    public static void createCatchCellDecisions(@NotNull final DecisionTreeNode currentNode, @NotNull final IGame game,
                                                @NotNull final Player player) {
        // 1. получить все доступные для захвата клетки +
        // 2. отфильтровать по принципу достаточно ли юнитов для захвата +
        // 3. построить узлы по оставшимся +
        // 4. применить решение для копий игры и игрока

        final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
        achievableCells.forEach(cell -> {
            if (checkCellCaptureOpportunity(cell, player, game)) {
                final Position position = game.getBoard().getPositionByCell(cell);
                final List<Unit> unitsForCapture = new ArrayList<>(); //TODO:
                final Decision decision = new CatchCellDecision(new Pair<>(position, unitsForCapture));
                final IGame gameCopy = game.getCopy();
                final Player playerCopy = player.getCopy();
                createDecisionNode(currentNode, decision, playerCopy, gameCopy);

                simulateCatchCellDecision(playerCopy, gameCopy, (CatchCellDecision) decision);

                createDistributionUnitsDecisions(currentNode, gameCopy, playerCopy);
            }
        });
        final IGame gameCopy = game.getCopy();
        final Player playerCopy = player.getCopy();
        createCatchCellNullDecision(currentNode, gameCopy, playerCopy);
        createDistributionUnitsDecisions(currentNode, gameCopy, playerCopy);
    }

    /**
     * Создает узел с null захватом клетки(прекратить захват)
     *
     * @param currentNode - текущий узел-родитель
     * @param game        - текущее состояние игры
     * @param player      - текущий игрок
     */
    private static void createCatchCellNullDecision(@NotNull final DecisionTreeNode currentNode,
                                                    @NotNull final IGame game, @NotNull final Player player) {
        final Decision decision = new CatchCellDecision(null);
        createDecisionNode(currentNode, decision, player.getCopy(), game.getCopy());
        simulateCatchCellDecision(player, game, (CatchCellDecision) decision);
    }

    /**
     * Проверка возможности захвата клетки
     *
     * @param cell   -клетка
     * @param player - текущий игрок
     * @param game   - текущее состояние игры
     * @return - возможность захвата клетки
     */
    private static boolean checkCellCaptureOpportunity(@NotNull final Cell cell, @NotNull final Player player,
                                                       @NotNull final IGame game) {
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<Unit> playerAvailableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        if (controlledCells.contains(cell)) {
            return playerAvailableUnits.size() >= cell.getType().getCatchDifficulty();
        }
        final GameFeatures features = game.getGameFeatures();
        return playerAvailableUnits.size() >=
                getUnitsCountNeededToCatchCell(features, cell)
                        + getBonusAttackToCatchCell(player, features, cell);
    }

    /**
     * Создает новые решения - узлы потомки - о перераспределении юнитов
     *
     * @param currentNode - текущий узел в симуляционном дереве
     * @param game        - текущее состояние игры
     * @param player      - игрок, относительно которого принимаются решение
     */
    public static void createDistributionUnitsDecisions(@NotNull final DecisionTreeNode currentNode,
                                                        @NotNull final IGame game, @NotNull final Player player) {
        // 1. составить все возможные комбинации перераспределний
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<Unit> playerUnits = new LinkedList<>();
        playerUnits.addAll(player.getUnitsByState(AvailabilityType.AVAILABLE));
        playerUnits.addAll(player.getUnitsByState(AvailabilityType.NOT_AVAILABLE));
        final List<List<Pair<Cell, Integer>>> combinations = getDistributionUnitsCombination(
                new LinkedList<>(controlledCells), playerUnits.size());
        // 2. построить узлы
        // 3. применить решение для копий игры и игрока
        final IBoard board = game.getBoard();
        for (final List<Pair<Cell, Integer>> combination : combinations) {
            final Map<Position, List<Unit>> resolutions = new HashMap<>();
            combination
                    .forEach(cellUnitsAmountsPair
                            -> resolutions.put(board.getPositionByCell(cellUnitsAmountsPair.getFirst()),
                            playerUnits.subList(0, cellUnitsAmountsPair.getSecond())));
            final IGame gameCopy = game.getCopy();
            final Player playerCopy = player.getCopy();
            final Decision decision = new DistributionUnitsDecision(resolutions);
            createDecisionNode(currentNode, decision, playerCopy, gameCopy);
            simulateDistributionUnitsDecision(decision, playerCopy, game);
        }
    }

    /**
     * Рекурсивная функция, которая возвращает всевозможные комбинации для клеток и количества юнитов,
     * которые туда отправляются
     *
     * @param cellForDistribution  - клетки, для которых нужно решить сколько юнитов туда отправлять
     * @param remainingUnitsAmount - оставшееся количество юнитов
     * @return - всевозможные комбинации клетка->количество юнитов для нее
     */
    private static List<List<Pair<Cell, Integer>>> getDistributionUnitsCombination(final List<Cell> cellForDistribution,
                                                                                   final int remainingUnitsAmount) {
        final List<List<Pair<Cell, Integer>>> combinations = new LinkedList<>();
        if (remainingUnitsAmount <= 0) {
            return combinations;
        }
        for (int i = 0; i < remainingUnitsAmount; i++) {
            for (final Cell cell : cellForDistribution) {
                final List<Pair<Cell, Integer>> currentCombination = new LinkedList<>();
                currentCombination.add(new Pair<>(cell, i));
                cellForDistribution.remove(cell);
                final List<List<Pair<Cell, Integer>>> remainingCellCombinations =
                        getDistributionUnitsCombination(cellForDistribution, remainingUnitsAmount - i);
                for (final List<Pair<Cell, Integer>> remainingCellCombination : remainingCellCombinations) {
                    currentCombination.addAll(remainingCellCombination);
                }
                combinations.add(currentCombination);
            }
        }
        return combinations;
    }


    /**
     * Создает узел по новому решению
     *
     * @param currentNode - текущий узел
     * @param newDecision - новое решение
     * @param player      - текущий игрок
     * @param game        - игра
     */
    private static void createDecisionNode(@NotNull final DecisionTreeNode currentNode,
                                           @NotNull final Decision newDecision,
                                           @NotNull final Player player,
                                           @NotNull final IGame game) {
        final DecisionTreeNode childNode = new DecisionTreeNode(currentNode, newDecision, player, game);
        addChildNodeToParentNode(currentNode, childNode);
    }

    /**
     * Добавляет созданный узел к списку потомков текущего узла
     *
     * @param currentNode - текущий узел
     * @param childNode   - новый узел-потомок
     */
    private static void addChildNodeToParentNode(@NotNull final DecisionTreeNode currentNode,
                                                 @NotNull final DecisionTreeNode childNode) {
        currentNode.getChildDecisions().add(childNode);
    }
}
