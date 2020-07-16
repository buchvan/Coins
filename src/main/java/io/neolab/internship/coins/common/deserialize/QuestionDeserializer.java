package io.neolab.internship.coins.common.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;

import java.io.IOException;

public class QuestionDeserializer extends JsonDeserializer<Question> {
    @Override
    public Question deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        final QuestionType questionType = mapper.readerFor(QuestionType.class).readValue(jsonNode.get("questionType"));
        final IGame game = mapper.readerFor(Game.class).readValue(jsonNode.get("game"));
        final Player player = mapper.readerFor(Player.class).readValue(jsonNode.get("player"));
        return new Question(questionType, game, player);
    }
}
