package io.neolab.internship.coins.ai.vika;

import io.neolab.internship.coins.ai.vika.decision.model.CatchCellDecision;
import io.neolab.internship.coins.ai.vika.decision.model.ChangeRaceDecision;
import io.neolab.internship.coins.ai.vika.decision.model.DeclineRaceDecision;
import io.neolab.internship.coins.ai.vika.decision.model.DistributionUnitsDecision;
import io.neolab.internship.coins.ai.vika.exception.AIBotException;
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
        boolean choice = true;
        try {
            final DeclineRaceDecision decision = (DeclineRaceDecision) getDeclineRaceDecision(player, game);
            choice = Objects.requireNonNull(decision).isDeclineRace();
            LOGGER.debug("AI bot decline race choice: {} ", choice);
        } catch (final AIBotException e) {
            e.printStackTrace();
        }
        return choice;
    }

    @Override
    public @NotNull Race chooseRace(@NotNull final Player player, @NotNull final IGame game) {
        LOGGER.debug("AI bot will choose race");
        Race race = null;
        try {
            final ChangeRaceDecision decision = (ChangeRaceDecision) getChooseRaceDecision(player, game);
            race = Objects.requireNonNull(decision).getDecision();
            LOGGER.debug("AI bot choice race: {} ", race);
        } catch (final AIBotException e) {
            e.printStackTrace();
        }
        return Objects.requireNonNull(race);
    }

    @Override
    public @Nullable Pair<Position, List<Unit>> chooseCatchingCell(@NotNull final Player player,
                                                                   @NotNull final IGame game) {
        LOGGER.debug("AI bot will capture of cells");
        Pair<Position, List<Unit>> captureCell = null;
        try {
            final CatchCellDecision decision = (CatchCellDecision) getChooseCaptureCellDecision(player, game);
            captureCell = Objects.requireNonNull(decision).getDecision();
            LOGGER.debug("Resolution of AI bot: ");
        } catch (final AIBotException e) {
            e.printStackTrace();
        }
        return captureCell;
    }

    @Override
    public @NotNull Map<Position, List<Unit>> distributionUnits(@NotNull final Player player, @NotNull final IGame game) {
        LOGGER.debug("AI bot distributes units");
        Map<Position, List<Unit>> resolution = null;
        try {
            final DistributionUnitsDecision distributionUnitsDecision = (DistributionUnitsDecision)
                    getDistributionUnitsDecision(player, game);
            resolution = Objects.requireNonNull(distributionUnitsDecision).getResolutions();
            LOGGER.debug("AI bot distributed units: ");
            return Objects.requireNonNull(resolution);
        } catch (AIBotException e) {
            e.printStackTrace();
        }
        return Objects.requireNonNull(resolution);
    }
}
