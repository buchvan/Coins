package io.neolab.internship.coins.bim.bot.ai.model.action;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

abstract public class Action {
    private final @NotNull ActionType type;

    public Action(final @NotNull ActionType type) {
        this.type = type;
    }

    public @NotNull ActionType getType() {
        return type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Action action = (Action) o;
        return type == action.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "Action{" +
                "type=" + type +
                '}';
    }
}
