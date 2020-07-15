package io.neolab.internship.coins.client;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Board;

public class Client implements IClient {
    private final ISimpleBot simpleBot;

    public Client() {
        simpleBot = new SimpleBot();
    }

    @Override
    public Answer getAnswer(final Question question, final int id, final IGame game) {
        return null;
    }
}
