package io.neolab.internship.coins.common.deserialize;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.utils.Pair;

import java.io.IOException;

public class PairRaceCellTypeDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(final String s, final DeserializationContext deserializationContext)
            throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Pair<String, String> pair = mapper.readValue(s, Pair.class);
        return new Pair<>(Race.getRaceByTitle(pair.getFirst()), CellType.getCellTypeByTitle(pair.getSecond()));
    }
}
