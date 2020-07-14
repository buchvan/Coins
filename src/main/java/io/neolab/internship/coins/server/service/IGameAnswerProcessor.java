package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.Player;

public interface IGameAnswerProcessor {
    void process(Player player, final Question question, final Answer answer) throws CoinsException;
}
