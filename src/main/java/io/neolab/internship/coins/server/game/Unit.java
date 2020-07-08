package io.neolab.internship.coins.server.game;

import java.util.Objects;

public class Unit {
    private final int id;

    public Unit(final int id) {
        this.id = id;
    }

    public Unit() {
        this(0);
    }

    public int getId() {
        return id;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Unit unit = (Unit) o;
        return id == unit.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Unit{" +
                "id=" + id +
                '}';
    }
}
