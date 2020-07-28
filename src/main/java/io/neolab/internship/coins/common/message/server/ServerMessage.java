package io.neolab.internship.coins.common.message.server;

import com.fasterxml.jackson.annotation.*;
import io.neolab.internship.coins.common.message.server.question.PlayerQuestion;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayerQuestion.class, name = "PlayerQuestion"),
        @JsonSubTypes.Type(value = GameOverMessage.class, name = "GameOverMessage"),
})
public class ServerMessage {
    @JsonProperty
    private final @NotNull ServerMessageType serverMessageType;

    @Contract(pure = true)
    @JsonCreator
    public ServerMessage(@NotNull @JsonProperty("serverMessageType") final ServerMessageType serverMessageType) {
        this.serverMessageType = serverMessageType;
    }

    public @NotNull ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ServerMessage serverMessage = (ServerMessage) o;
        return serverMessageType == serverMessage.serverMessageType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverMessageType);
    }

    @Override
    public String toString() {
        return "Question{" +
                "questionType=" + serverMessageType +
                '}';
    }
}
