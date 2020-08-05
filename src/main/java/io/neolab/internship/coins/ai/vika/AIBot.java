package io.neolab.internship.coins.ai.vika;

import io.neolab.internship.coins.client.bot.IBot;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.utils.RandomGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс бота с ИИ
 */
public class AIBot implements IBot {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(AIBot.class);

    @Override
    public boolean declineRaceChoose(@NotNull final Player player, @NotNull final IGame game) {
        final boolean choice = true; //make decision here
        LOGGER.debug("AI bot decline race choice: {} ", choice);
        return choice;
    }

    @Override
    public @NotNull Race chooseRace(@NotNull final Player player, @NotNull final IGame game) {
        final Race race = RandomGenerator.chooseItemFromList(game.getRacesPool()); //make decision here, now random
        LOGGER.debug("AI bot choice race: {} ", race);
        return race;
    }

    @Override
    public @Nullable Pair<Position, List<Unit>> chooseCatchingCell(@NotNull final Player player,
                                                                   @NotNull final IGame game) {
        LOGGER.debug("AI bot will capture of cells"); //only logs
        LOGGER.debug("Resolution of AI bot: ");
        return null;
    }

    @Override
    public @NotNull Map<Position, List<Unit>> distributionUnits(@NotNull final Player player, @NotNull final IGame game) {
        LOGGER.debug("AI bot distributes units"); //only logs
        LOGGER.debug("AI bot distributed units: ");
        return new HashMap<>();
    }
}
