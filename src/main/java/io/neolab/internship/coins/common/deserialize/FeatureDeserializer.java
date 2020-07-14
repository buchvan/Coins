package io.neolab.internship.coins.common.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.server.game.feature.CoefficientlyFeature;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.feature.FeatureType;

import java.io.IOException;

public class FeatureDeserializer extends JsonDeserializer<Feature> {
    @Override
    public Feature deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext)
            throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        final FeatureType type = mapper.readerFor(FeatureType.class).readValue(jsonNode.get("type"));
        final JsonNode coefficientNode = jsonNode.get("coefficient");
        if (coefficientNode != null) {
            final int coefficient = coefficientNode.asInt();
            return new CoefficientlyFeature(type, coefficient);
        } // else
        return new Feature(type);
    }
}
