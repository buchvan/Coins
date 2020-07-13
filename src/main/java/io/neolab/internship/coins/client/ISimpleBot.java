package io.neolab.internship.coins.client;

import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Race;

public interface ISimpleBot {
    Race chooseRace(final IGame game);

}
