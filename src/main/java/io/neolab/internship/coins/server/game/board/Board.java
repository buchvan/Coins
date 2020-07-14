package io.neolab.internship.coins.server.game.board;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.neolab.internship.coins.common.communication.deserialize.PositionDeserializer;
import io.neolab.internship.coins.common.communication.deserialize.PositionToCellBidiMapDeserializer;
import io.neolab.internship.coins.common.communication.serialize.PositionSerializer;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.Serializable;
import java.util.Objects;

@JsonDeserialize
public class Board implements IBoard, Serializable {

    @JsonSerialize(keyUsing = PositionSerializer.class)
    @JsonDeserialize(keyUsing = PositionDeserializer.class, using = PositionToCellBidiMapDeserializer.class)
    private final BidiMap<Position, Cell> positionToCellMap;

    @JsonCreator
    public Board(@JsonProperty("positionToCellMap") final BidiMap<Position, Cell> positionToCellMap) {
        this.positionToCellMap = new DualHashBidiMap<>();
        positionToCellMap.forEach(this.positionToCellMap::put);
    }

    public Board() {
        this(new DualHashBidiMap<>());
    }

    @Override
    public BidiMap<Position, Cell> getPositionToCellMap() {
        return positionToCellMap;
    }

    @Override
    public Cell getCellByPosition(final Position position) {
        return getPositionToCellMap().get(position);
    }

    @Override
    public Cell getCellByPosition(final int x, final int y) {
        return getCellByPosition(new Position(x, y));
    }

    @Override
    public Position getPositionByCell(final Cell cell) {
        return getPositionToCellMap().getKey(cell);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Board)) return false;
        final Board board = (Board) o;
        return Objects.equals(getPositionToCellMap(), board.getPositionToCellMap());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPositionToCellMap());
    }

    @Override
    public String toString() {
        return "Board{" +
                "positionToCellMap=" + positionToCellMap +
                '}';
    }
}
