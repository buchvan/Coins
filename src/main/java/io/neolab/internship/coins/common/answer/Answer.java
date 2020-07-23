package io.neolab.internship.coins.common.answer;

import com.fasterxml.jackson.annotation.*;

/**
 * Абстрактный класс ответа. Класс-родитель для всех различных ответов клиента
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CatchCellAnswer.class, name = "CatchCellAnswer"),
        @JsonSubTypes.Type(value = ChangeRaceAnswer.class, name = "ChangeRaceAnswer"),
        @JsonSubTypes.Type(value = DeclineRaceAnswer.class, name = "DeclineRaceAnswer"),
        @JsonSubTypes.Type(value = DistributionUnitsAnswer.class, name = "DistributionUnitsAnswer"),
        @JsonSubTypes.Type(value = NicknameAnswer.class, name = "NicknameAnswer"),
})
public class Answer extends ClientMessage {
    @JsonCreator
    public Answer(@JsonProperty("type") final ClientMessageType type) {
        super(type);
    }
}
