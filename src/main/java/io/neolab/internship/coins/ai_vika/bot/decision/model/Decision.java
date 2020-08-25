package io.neolab.internship.coins.ai_vika.bot.decision.model;

import java.util.Objects;

public abstract class Decision {
    private final DecisionType decisionType;
    private WinCollector winCollector;

    public Decision(final DecisionType decisionType) {
        this.decisionType = decisionType;
        winCollector = null;
    }

    public DecisionType getDecisionType() {
        return decisionType;
    }

    public WinCollector getWinCollector() {
        return winCollector;
    }

    public void setWinCollector(final WinCollector winCollector) {
        this.winCollector = winCollector;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Decision)) return false;
        final Decision decision = (Decision) o;
        return getDecisionType() == decision.getDecisionType() &&
                Objects.equals(getWinCollector(), decision.getWinCollector());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDecisionType(), getWinCollector());
    }

    @Override
    public String toString() {
        return "Decision{" +
                "decisionType=" + decisionType +
                ", winCollector=" + winCollector +
                '}';
    }
}
