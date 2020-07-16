package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.Player;

public class GameAnswerProcessorTestsUtils {

    public static GameAnswerProcessor getGameAnswerProcessor() {
        return new GameAnswerProcessor();
    }

    public static Question getQuestionByQuestionType(QuestionType type) {
        return new Question(type, new Game(), new Player("test"));
    }
}
