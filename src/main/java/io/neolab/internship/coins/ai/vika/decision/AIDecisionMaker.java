package io.neolab.internship.coins.ai.vika.decision;

import io.neolab.internship.coins.ai.vika.decision.model.ChangeRaceDecision;
import io.neolab.internship.coins.ai.vika.decision.model.Decision;
import io.neolab.internship.coins.ai.vika.decision.model.DeclineRaceDecision;
import io.neolab.internship.coins.ai.vika.decision.tree.DecisionTreeNode;
import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.service.GameAnswerProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    }
}
