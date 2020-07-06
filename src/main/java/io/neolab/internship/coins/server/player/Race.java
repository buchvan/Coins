package io.neolab.internship.coins.server.player;

public enum Race {
    MUSHROOM("MUSHROOM", 6),
    AMPHIBIAN("AMPHIBIAN", 6),
    ELF("ELF", 6),
    ORC("ORC", 5),
    GNOME("GNOME", 5),
    UNDEAD("UNDEAD", 11),
    NEUTRAL("NEUTRAL", 0);

    private String title;

    private int unitsAmount;

    Race(String title, int unitsAmount) {
        this.title = title;
        this.unitsAmount = unitsAmount;
    }

    Race(String title) {
        this.title = title;
    }

    public int getUnitsAmount() {
        return unitsAmount;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Race{" +
                "title='" + title + '\'' +
                ", unitsAmount=" + unitsAmount +
                '}';
    }
}
