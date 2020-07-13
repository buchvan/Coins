package io.neolab.internship.coins.client;

import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.utils.RandomGenerator;

public class SimpleBot implements ISimpleBot {

    @Override
    public Race chooseRace(final IGame game) {
        return RandomGenerator.chooseItemFromList(game.getRacesPool());
    }
}
