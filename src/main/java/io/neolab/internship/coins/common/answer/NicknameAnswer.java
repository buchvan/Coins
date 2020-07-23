package io.neolab.internship.coins.common.answer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class NicknameAnswer extends Answer {
    @JsonProperty
    private final String nickname;

    @JsonCreator
    public NicknameAnswer(@JsonProperty("nickname")final String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

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
