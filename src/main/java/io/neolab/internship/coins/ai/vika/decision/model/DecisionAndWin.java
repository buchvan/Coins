package io.neolab.internship.coins.ai.vika.decision.model;

import java.util.Objects;

public class DecisionAndWin {
    private final Decision decision;
    private final WinCollector winCollector;

    public DecisionAndWin(final Decision decision, final WinCollector winCollector) {
        this.decision = decision;
        this.winCollector = winCollector;
    }

    public Decision getDecision() {
        return decision;
    }

    public WinCollector getWinCollector() {
        return winCollector;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DecisionAndWin)) return false;
        final DecisionAndWin that = (DecisionAndWin) o;
        return Objects.equals(getDecision(), that.getDecision()) &&
                Objects.equals(getWinCollector(), that.getWinCollector());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDecision(), getWinCollector());
    }

    @Override
    public String toString() {
        return "DecisionAndWin{" +
                "decision=" + decision +
                ", winCollector=" + winCollector +
                '}';
    }
}
