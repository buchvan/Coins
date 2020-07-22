package io.neolab.internship.coins.common.question;

import java.util.Objects;

public class ServerMessage {
    private final ServerMessageType serverMessageType;

    public ServerMessage(final ServerMessageType serverMessageType) {
        this.serverMessageType = serverMessageType;
    }

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

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
