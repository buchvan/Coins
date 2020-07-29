package io.neolab.internship.coins.common.message.client.answer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neolab.internship.coins.common.message.client.ClientMessageType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NicknameAnswer extends Answer {
    @JsonProperty
    private final @NotNull String nickname;

    @JsonCreator
    public NicknameAnswer(@NotNull @JsonProperty("nickname") final String nickname) {
        super(ClientMessageType.GAME_ANSWER);
        this.nickname = nickname;
    }

    public @NotNull String getNickname() {
        return nickname;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof NicknameAnswer)) return false;
        final NicknameAnswer that = (NicknameAnswer) o;
        return Objects.equals(getNickname(), that.getNickname());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNickname());
    }

    @Override
    public String toString() {
        return "NicknameAnswer{" +
                "nickname='" + nickname + '\'' +
                '}';
    }
}
