package io.neolab.internship.coins.server.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

public enum Race implements Serializable {
    MUSHROOM("MUSHROOM", 6),
    AMPHIBIAN("AMPHIBIAN", 6),
    ELF("ELF", 6),
    ORC("ORC", 5),
    GNOME("GNOME", 5),
    UNDEAD("UNDEAD", 11),
    ;

    @JsonProperty
    private final String title;

    @JsonProperty
    private final int unitsAmount;

    @JsonCreator
    Race(@JsonProperty("title") final String title,
         @JsonProperty("unitsAmount") final int unitsAmount) {
        this.title = title;
        this.unitsAmount = unitsAmount;
    }

    public static Race getRaceByTitle(final String title) {
        for (final Race race : Race.values()) {
            if (race.title.equals(title)) {
                return race;
            }
        }
        return null;
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
