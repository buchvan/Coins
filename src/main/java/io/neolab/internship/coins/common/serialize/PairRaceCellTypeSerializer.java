package io.neolab.internship.coins.common.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.utils.Pair;

import java.io.IOException;

public class PairRaceCellTypeSerializer extends JsonSerializer<Pair<Race, CellType>> {
    @Override
    public void serialize(final Pair<Race, CellType> raceCellTypePair, final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        jsonGenerator.writeFieldName(mapper.writeValueAsString(raceCellTypePair));
    }
}
