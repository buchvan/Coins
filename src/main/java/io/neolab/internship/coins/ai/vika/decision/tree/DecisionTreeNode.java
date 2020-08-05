package io.neolab.internship.coins.ai.vika.decision.tree;

import io.neolab.internship.coins.ai.vika.decision.model.Decision;
import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.player.Player;

import java.util.Objects;

/**
 * Узел дерева принятия решений
 */
public class DecisionTreeNode {
    private final Decision decision;
    private final Player player;
    private final Game game;

    public DecisionTreeNode(final Decision decision, final Player player, final Game game) {
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
