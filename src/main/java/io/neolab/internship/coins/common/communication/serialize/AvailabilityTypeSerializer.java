package io.neolab.internship.coins.common.communication.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.neolab.internship.coins.utils.AvailabilityType;

import java.io.IOException;

public class AvailabilityTypeSerializer extends JsonSerializer<AvailabilityType> {
    @Override
    public void serialize(final AvailabilityType availabilityType,
                          final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider)
            throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        jsonGenerator.writeFieldName(mapper.writeValueAsString(availabilityType));
    }
}
