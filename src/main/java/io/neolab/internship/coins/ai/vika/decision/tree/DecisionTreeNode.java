package io.neolab.internship.coins.ai.vika.decision.tree;

import io.neolab.internship.coins.ai.vika.decision.model.Decision;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Узел дерева принятия решений
 */
public class DecisionTreeNode {
    //Ссылка на узел-родитель
    private final DecisionTreeNode parentDecision;
    //Принятое решение
    private final Decision decision;
    //Игрок, который принимет решения
    private final Player player;
    //Состояние игры после принятия решения в этом узле
    private final IGame game;
    //Ссылки на потомков
    private final List<DecisionTreeNode> childDecisions = new LinkedList<>();

    private int coinsAmountAfterDecision = 0;

    public DecisionTreeNode(final DecisionTreeNode parentDecision, final Decision decision,
                            final Player player, final IGame game) {
        this.parentDecision = parentDecision;
        this.decision = decision;
        this.player = player;
        this.game = game;
        this.coinsAmountAfterDecision = 0;
    }

    public Decision getDecision() {
        return decision;
    }

    public Player getPlayer() {
        return player;
    }

    public IGame getGame() {
        return game;
    }

    public DecisionTreeNode getParentDecision() {
        return parentDecision;
    }

    public List<DecisionTreeNode> getChildDecisions() {
        return childDecisions;
    }

    public int getCoinsAmountAfterDecision() {
        return coinsAmountAfterDecision;
    }

    public void setCoinsAmountAfterDecision(final int coinsAmountAfterDecision) {
        this.coinsAmountAfterDecision = coinsAmountAfterDecision;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DecisionTreeNode)) return false;
        final DecisionTreeNode that = (DecisionTreeNode) o;
        return getCoinsAmountAfterDecision() == that.getCoinsAmountAfterDecision() &&
                Objects.equals(getParentDecision(), that.getParentDecision()) &&
                Objects.equals(getDecision(), that.getDecision()) &&
                Objects.equals(getPlayer(), that.getPlayer()) &&
                Objects.equals(getGame(), that.getGame()) &&
                Objects.equals(getChildDecisions(), that.getChildDecisions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParentDecision(), getDecision(), getPlayer(), getGame(), getChildDecisions(), getCoinsAmountAfterDecision());
    }

    @Override
    public String toString() {
        return "DecisionTreeNode{" +
                "parentDecision=" + parentDecision +
                ", decision=" + decision +
                ", player=" + player +
                ", game=" + game +
                ", childDecisions=" + childDecisions +
                ", coinsAmountAfterDecision=" + coinsAmountAfterDecision +
                '}';
    }
}
