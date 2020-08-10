package io.neolab.internship.coins.ai.vika.decision.tree;

import io.neolab.internship.coins.ai.vika.decision.model.Decision;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import org.jetbrains.annotations.NotNull;

public class DecisionTreeNodeProcessor {
    /**
     * Создает узел по новому решению
     *
     * @param currentNode - текущий узел
     * @param newDecision - новое решение
     * @param player      - текущий игрок
     * @param game        - игра
     */
    public static void createDecisionNode(@NotNull final DecisionTreeNode currentNode,
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
