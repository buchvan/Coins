package io.neolab.internship.coins.common.answer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
public abstract class Answer {
}
