package io.neolab.internship.coins.server.game.player;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public enum Race implements Serializable {
    MUSHROOM("MUSHROOM", 6),
    AMPHIBIAN("AMPHIBIAN", 6),
    ELF("ELF", 6),
    ORC("ORC", 5),
    GNOME("GNOME", 5),
    UNDEAD("UNDEAD", 11),
    ;

    private final @NotNull String title;
    private final int unitsAmount;

    @Contract(pure = true)
    @JsonCreator
    Race(@NotNull @JsonProperty("title") final String title,
         @JsonProperty("unitsAmount") final int unitsAmount) {
        this.title = title;
        this.unitsAmount = unitsAmount;
    }

    public static @Nullable Race getRaceByTitle(final @NotNull String title) {
        for (final Race race : Race.values()) {
            if (race.title.equals(title)) {
                return race;
            }
        }
        return null;
    }

    @Contract(pure = true)
    public int getUnitsAmount() {
        return unitsAmount;
    }

    @Contract(pure = true)
    public @NotNull String getTitle() {
        return title;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "Race{" +
                "title='" + title + '\'' +
                ", unitsAmount=" + unitsAmount +
                '}';
    }
}
