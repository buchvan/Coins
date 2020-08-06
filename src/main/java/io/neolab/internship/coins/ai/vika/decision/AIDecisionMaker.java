package io.neolab.internship.coins.ai.vika.decision;

import io.neolab.internship.coins.ai.vika.decision.model.CatchCellDecision;
import io.neolab.internship.coins.ai.vika.decision.model.ChangeRaceDecision;
import io.neolab.internship.coins.ai.vika.decision.model.Decision;
import io.neolab.internship.coins.ai.vika.decision.model.DeclineRaceDecision;
import io.neolab.internship.coins.ai.vika.decision.tree.DecisionTreeNode;
import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.neolab.internship.coins.server.service.GameLoopProcessor.getBonusAttackToCatchCell;
import static io.neolab.internship.coins.server.service.GameLoopProcessor.getUnitsCountNeededToCatchCell;

/**
 * Симуляция принятия решений в ходе игры
 */
public class AIDecisionMaker {

    /**
     * Создает новые решения - узлы потомки - об отправлении расы в упадок
     *
     * @param currentNode - текущий узел в симуляционном дереве
     * @param game        - текущее состояние игры
     * @param player      - игрок, относительно которого принимаются решение
     */
    public static void createDeclineRaceDecisions(@NotNull final DecisionTreeNode currentNode, @NotNull final Game game,
                                                  @NotNull final Player player) {
        final Decision declineRaceDecisionTrue = new DeclineRaceDecision(true);
        final Player playerCopy = player.getCopy();
        final IGame gameCopy = game.getCopy();
        final DecisionTreeNode trueChildNode = new DecisionTreeNode(
                currentNode, declineRaceDecisionTrue, playerCopy, gameCopy);
        currentNode.getChildDecisions().add(trueChildNode);
        //TODO: decide for game and player copy
        final Decision declineRaceDecisionFalse = new DeclineRaceDecision(true);
        final DecisionTreeNode falseChildNode = new DecisionTreeNode(
                currentNode, declineRaceDecisionFalse, player.getCopy(), game.getCopy());
        currentNode.getChildDecisions().add(falseChildNode);
        //TODO: decide for game and player copy
    }

    /**
     * Создает новые решения - узлы потомки - о смене расы
     *
     * @param currentNode - текущий узел в симуляционном дереве
     * @param game        - текущее состояние игры
     * @param player      - игрок, относительно которого принимаются решение
     */
    public static void createChangeRaceDecisions(@NotNull final DecisionTreeNode currentNode, @NotNull final Game game,
                                                 @NotNull final Player player) {
        final List<Race> availableRaces = game.getRacesPool();
        availableRaces.forEach(race -> {
            final Decision changeRaceDecision = new ChangeRaceDecision(race);
            final DecisionTreeNode childNode = new DecisionTreeNode(
                    currentNode, changeRaceDecision, player.getCopy(), game.getCopy());
            currentNode.getChildDecisions().add(childNode);
            //TODO: decide for game and player copy
        });
    }

    /**
     * Создает новые решения - узлы потомки - о захвате клеток
     *
     * @param currentNode - текущий узел в симуляционном дереве
     * @param game        - текущее состояние игры
     * @param player      - игрок, относительно которого принимаются решение
     */
    public static void createCatchCellDecisions(@NotNull final DecisionTreeNode currentNode, @NotNull final Game game,
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
                //TODO: 4
            }
        });

    }

    private static boolean checkCellCaptureOpportunity(final Cell cell, final Player player, final IGame game) {
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
                                                        @NotNull final Game game, @NotNull final Player player) {
        // 1. составить все возможные комбинации перераспределний
        // 2. построить узлы
        // 3. применить решение для копийй игры и игрока
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
