package io.neolab.internship.coins.common.serialization.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.utils.AvailabilityType;

import java.io.IOException;
import java.util.*;

public class PlayerDeserializer extends JsonDeserializer<Player> {
    @Override
    public Player deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext)
            throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        final int id = jsonNode.get("id").asInt();
        final String nickname = jsonNode.get("nickname").asText();
        final Race race = Race.getRaceByTitle(jsonNode.get("race").asText());
        final Map<AvailabilityType, List<Unit>> unitStateToUnits = new HashMap<>();
        final Iterator<Map.Entry<String, JsonNode>> iterator = jsonNode.get("unitStateToUnits").fields();
        while (iterator.hasNext()) {
            final Map.Entry<String, JsonNode> entry = iterator.next();
            final List<Unit> units = new LinkedList<>();
            for (final JsonNode node : entry.getValue()) {
                units.add(mapper.readerFor(Unit.class).readValue(node));
            }
            unitStateToUnits.put(mapper.readValue(entry.getKey(), AvailabilityType.class), units);
        }
        final int coins = jsonNode.get("coins").asInt();
        return new Player(id, nickname, race, unitStateToUnits, coins);
    }
}
