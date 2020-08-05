package io.neolab.internship.coins.client.bot;

import io.neolab.internship.coins.client.bot.ai.bim.AIProcessor;
import io.neolab.internship.coins.client.bot.ai.bim.NodeTree;
import io.neolab.internship.coins.client.bot.ai.bim.action.*;
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

import java.util.*;

public class SmartBot implements IBot {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(SimpleBot.class);
    static @Nullable NodeTree tree;
    private final @NotNull IBot simpleBot = new SimpleBot();

    public void clearTree() {
        tree = null;
    }

    @Override
    public boolean declineRaceChoose(final @NotNull Player player, final @NotNull IGame game) {
        if (tree != null && tree.getEdges().iterator().next().getAction() == null) {
            tree = null;
        }
        if (tree == null) {
            tree = AIProcessor.createTree(game, player);
        }
        final Action action = AIProcessor.getAction(tree);
        tree = AIProcessor.updateTree(tree, action);
        final boolean choice = ((DeclineRaceAction) action).isDeclineRace();
        LOGGER.debug("Smart bot decline race choice: {} ", choice);
        return choice;
    }

    @Override
    public @NotNull Race chooseRace(final @NotNull Player player, final @NotNull IGame game) {
        final Race race;
        if (tree != null) {
            final Action action = AIProcessor.getAction(tree);
            tree = AIProcessor.updateTree(tree, action);
            race = ((ChangeRaceAction) action).getNewRace();
        } else {
            race = simpleBot.chooseRace(player, game);
        }
        LOGGER.debug("Smart bot choice race: {} ", race);
        return race;
    }

    @Override
    public @Nullable Pair<Position, List<Unit>> chooseCatchingCell(final @NotNull Player player,
                                                                   final @NotNull IGame game) {
        final Action action = AIProcessor.getAction(Objects.requireNonNull(tree));
        tree = AIProcessor.updateTree(tree, action);
        final Pair<Position, List<Unit>> resolution = ((CatchCellAction) action).getResolution();
        LOGGER.debug("Resolution of smart bot: {} ", resolution);
        return resolution;
    }

    @Override
    public @NotNull Map<Position, List<Unit>> distributionUnits(final @NotNull Player player,
                                                                final @NotNull IGame game) {
        final Action action = AIProcessor.getAction(Objects.requireNonNull(tree));
        tree = AIProcessor.updateTree(tree, action);
        final Map<Position, List<Unit>> resolutions = ((DistributionUnitsAction) action).getResolutions();
        LOGGER.debug("Smart bot distributed units: {} ", resolutions);
        return resolutions;
    }
}
