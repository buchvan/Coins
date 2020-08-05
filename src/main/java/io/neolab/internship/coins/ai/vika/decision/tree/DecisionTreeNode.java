package io.neolab.internship.coins.ai.vika.decision.tree;

import io.neolab.internship.coins.ai.vika.decision.model.Decision;
import io.neolab.internship.coins.server.game.Game;
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
    private final Game game;
    //Ссылки на потомков
    private final List<DecisionTreeNode> childDecisions = new LinkedList<>();

    public DecisionTreeNode(final DecisionTreeNode parentDecision, final Decision decision,
                            final Player player, final Game game) {
        this.parentDecision = parentDecision;
        this.decision = decision;
        this.player = player;
        this.game = game;
    }

    public Decision getDecision() {
        return decision;
    }

    public Player getPlayer() {
        return player;
    }

    public Game getGame() {
        return game;
    }

    public DecisionTreeNode getParentDecision() {
        return parentDecision;
    }

    public List<DecisionTreeNode> getChildDecisions() {
        return childDecisions;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DecisionTreeNode)) return false;
        final DecisionTreeNode that = (DecisionTreeNode) o;
        return Objects.equals(getDecision(), that.getDecision()) &&
                Objects.equals(getPlayer(), that.getPlayer()) &&
                Objects.equals(getGame(), that.getGame());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDecision(), getPlayer(), getGame());
    }

    @Override
    public String toString() {
        return "DecisionTreeNode{" +
                "decision=" + decision +
                ", player=" + player +
                ", game=" + game +
                '}';
    }
}
