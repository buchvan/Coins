package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.DeclineRaceAnswer;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.Player;
import org.junit.Test;

import static io.neolab.internship.coins.server.service.GameAnswerProcessorTestsUtils.getGameAnswerProcessor;
import static io.neolab.internship.coins.server.service.GameAnswerProcessorTestsUtils.getQuestionByQuestionType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class GameDeclineRaceAnswerProcessorTests {

    @Test
    public void emptyAnswerTest(){
        GameAnswerProcessor gameAnswerProcessor = getGameAnswerProcessor();
        Question question = getQuestionByQuestionType(QuestionType.DECLINE_RACE);
        CoinsException exception = assertThrows(CoinsException.class,
                () -> gameAnswerProcessor.process(question, null));
        assertEquals(ErrorCode.EMPTY_ANSWER, exception.getErrorCode());
    }
}
