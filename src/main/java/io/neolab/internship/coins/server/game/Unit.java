package io.neolab.internship.coins.server.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neolab.internship.coins.utils.IdGenerator;

import java.io.Serializable;
import java.util.Objects;

public class Unit implements Serializable {
    private final int id;

    public Unit() {
        this.id = IdGenerator.getCurrentId();
    }

    @JsonCreator
    public Unit(@JsonProperty("id") final Unit unit) {
        this.id = unit.id;
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
