package io.neolab.internship.coins.common.communication.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;

import java.io.IOException;


public class GameSerializer extends JsonSerializer<Game> {
    @Override
    public void serialize(final Game game, final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("board", game.getBoard());
        jsonGenerator.writeNumberField("currentRound", game.getCurrentRound());
        jsonGenerator.writeObjectField("feudalToCells", game.getFeudalToCells());
        jsonGenerator.writeObjectField("ownToCells", game.getOwnToCells());
        jsonGenerator.writeObjectField("playerToTransitCells", game.getPlayerToTransitCells());
        jsonGenerator.writeObjectField("gameFeatures", game.getGameFeatures());
        jsonGenerator.writeArrayFieldStart("racesPool");
        for(final Race race: game.getRacesPool()) {
            jsonGenerator.writeObject(race);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeArrayFieldStart("players");
        for(final Player player: game.getPlayers()) {
            jsonGenerator.writeObject(player);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
