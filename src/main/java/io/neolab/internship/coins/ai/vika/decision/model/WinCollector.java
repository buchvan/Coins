package io.neolab.internship.coins.ai.vika.decision.model;

import java.util.Objects;

public class WinCollector {
    private int coinsAmount;

    public WinCollector(final int coinsAmount) {
        this.coinsAmount = coinsAmount;
    }

    public int getCoinsAmount() {
        return coinsAmount;
    }

    public void setCoinsAmount(final int coinsAmount) {
        this.coinsAmount = coinsAmount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof WinCollector)) return false;
        final WinCollector that = (WinCollector) o;
        return getCoinsAmount() == that.getCoinsAmount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCoinsAmount());
    }

    @Override
    public String toString() {
        return "WinCollector{" +
                "coinsAmount=" + coinsAmount +
                '}';
    }
}
