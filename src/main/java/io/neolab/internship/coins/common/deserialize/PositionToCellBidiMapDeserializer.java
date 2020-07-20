package io.neolab.internship.coins.common.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.Position;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class PositionToCellBidiMapDeserializer extends JsonDeserializer<BidiMap<Position, Cell>> {
    @Override
    public BidiMap<Position, Cell> deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext)
            throws IOException {

        final JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        final ObjectMapper mapper = new ObjectMapper();
        final BidiMap<Position, Cell> positionToCell = new DualHashBidiMap<>();
        final Iterator<Map.Entry<String, JsonNode>> iterator = jsonNode.fields();
        while (iterator.hasNext()) {
            final Map.Entry<String, JsonNode> entry = iterator.next();
            positionToCell.put(mapper.readValue(entry.getKey(), Position.class),
                    mapper.readerFor(Cell.class).readValue(entry.getValue()));
        }
        return positionToCell;
    }
}
