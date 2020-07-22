package io.neolab.internship.coins.server.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.neolab.internship.coins.common.serialization.deserialize.PairRaceCellTypeKeyDeserializer;
import io.neolab.internship.coins.common.serialization.serialize.PairRaceCellTypeSerializer;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.utils.Pair;

import java.io.Serializable;
import java.util.*;

public class GameFeatures implements Serializable {

    @JsonProperty
    @JsonSerialize(keyUsing = PairRaceCellTypeSerializer.class)
    @JsonDeserialize(keyUsing = PairRaceCellTypeKeyDeserializer.class)
    /* (раса, тип клетки) -> список соответствующих им особенностей */
    private final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures;

    public GameFeatures() {
        this(new HashMap<>());
    }

    @JsonCreator
    public GameFeatures(@JsonProperty("raceCellTypeFeatures") final Map<Pair<Race, CellType>, List<Feature>>
                                raceCellTypeFeatures) {
        this.raceCellTypeFeatures = raceCellTypeFeatures;
    }

    public Map<Pair<Race, CellType>, List<Feature>> getRaceCellTypeFeatures() {
        return raceCellTypeFeatures;
    }

    public List<Feature> getFeaturesByRaceAndCellType(final Race race, final CellType cellType) {
        return getRaceCellTypeFeatures().getOrDefault(new Pair<>(race, cellType), Collections.emptyList());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final GameFeatures that = (GameFeatures) o;
        return Objects.equals(raceCellTypeFeatures, that.raceCellTypeFeatures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceCellTypeFeatures);
    }

    @Override
    public String toString() {
        return "GameFeatures{" +
                "raceCellTypeFeatures=" + raceCellTypeFeatures +
                '}';
    }
}
