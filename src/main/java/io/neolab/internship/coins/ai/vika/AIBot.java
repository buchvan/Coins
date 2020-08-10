package io.neolab.internship.coins.ai.vika;

import io.neolab.internship.coins.client.bot.IBot;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.neolab.internship.coins.ai.vika.decision.AIDecisionMaker.*;


/**
 * Класс бота с ИИ
 */
public class AIBot implements IBot {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(AIBot.class);

    @Override
    public boolean declineRaceChoose(@NotNull final Player player, @NotNull final IGame game) {
        final boolean choice = getDeclineRaceDecision(player, game);
        LOGGER.debug("AI bot decline race choice: {} ", choice);
        return choice;
    }

    @Override
    public @NotNull Race chooseRace(@NotNull final Player player, @NotNull final IGame game) {
        final Race race = getChooseRaceDecision(player, game);
        LOGGER.debug("AI bot choice race: {} ", race);
        return Objects.requireNonNull(race);
    }

    @Override
    public @Nullable Pair<Position, List<Unit>> chooseCatchingCell(@NotNull final Player player,
                                                                   @NotNull final IGame game) {
        LOGGER.debug("AI bot will capture of cells");
        final Pair<Position, List<Unit>> captureCell = getChooseCaptureCellDecision(player, game);
        LOGGER.debug("Resolution of AI bot: ");
        return captureCell;
    }

    @Override
    public @NotNull Map<Position, List<Unit>> distributionUnits(@NotNull final Player player, @NotNull final IGame game) {
        LOGGER.debug("AI bot distributes units");
        final Map<Position, List<Unit>> resolution = getDistributionUnitsDecision(player, game);
        LOGGER.debug("AI bot distributed units: ");
        return Objects.requireNonNull(resolution);
    }
}
