package io.neolab.internship.coins.common.message.client;

import com.fasterxml.jackson.annotation.*;
import io.neolab.internship.coins.common.message.client.answer.Answer;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Answer.class, name = "Answer"),
})
public class ClientMessage {
    @JsonProperty
    private final ClientMessageType messageType;

    @JsonCreator
    public ClientMessage(@JsonProperty("messageType") final ClientMessageType messageType) {
        this.messageType = messageType;
    }

    public ClientMessageType getMessageType() {
        return messageType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ClientMessage that = (ClientMessage) o;
        return messageType == that.messageType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType);
    }

    @Override
    public String toString() {
        return "ClientMessage{" +
                "messageType=" + messageType +
                '}';
    }
}
