package io.neolab.internship.coins.ai.vika.decision.model;

import java.util.Objects;

public class Decision {
    private final DecisionType decisionType;

    public Decision(final DecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public DecisionType getDecisionType() {
        return decisionType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Decision)) return false;
        final Decision decision = (Decision) o;
        return getDecisionType() == decision.getDecisionType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDecisionType());
    }

    @Override
    public String toString() {
        return "Decision{" +
                "decisionType=" + decisionType +
                '}';
    }
}
