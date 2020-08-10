package io.neolab.internship.coins.client.bot;

import io.neolab.internship.coins.client.bot.ai.bim.AIProcessor;
import io.neolab.internship.coins.client.bot.ai.bim.SimulationTreeCreatingProcessor;
import io.neolab.internship.coins.client.bot.ai.bim.model.FunctionType;
import io.neolab.internship.coins.client.bot.ai.bim.model.NodeTree;
import io.neolab.internship.coins.client.bot.ai.bim.model.action.*;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.utils.RandomGenerator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SmartBot implements IBot {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(SimpleBot.class);
    private @Nullable NodeTree tree;
    private final @NotNull SimulationTreeCreatingProcessor treeCreatingProcessor;
    private final @NotNull FunctionType functionType;

    @Contract(pure = true)
    public SmartBot(final int maxDepth, final @NotNull FunctionType functionType) {
        this.treeCreatingProcessor = new SimulationTreeCreatingProcessor(maxDepth);
        this.functionType = functionType;
    }

    @Override
    public boolean declineRaceChoose(final @NotNull Player player, final @NotNull IGame game) {
        tree = treeCreatingProcessor.createTree(game, player);
        final Action action = AIProcessor.getAction(tree, player, functionType);
        tree = SimulationTreeCreatingProcessor.updateTree(tree, action);
        final boolean choice = ((DeclineRaceAction) action).isDeclineRace();
        LOGGER.debug("Smart bot decline race choice: {} ", choice);
        return choice;
    }

    @Override
    public @NotNull Race chooseRace(final @NotNull Player player, final @NotNull IGame game) {
        final Race race;
        if (tree != null) {
            final Action action = AIProcessor.getAction(tree, player, functionType);
            tree = SimulationTreeCreatingProcessor.updateTree(tree, action);
            race = ((ChangeRaceAction) action).getNewRace();
        } else {
            race = RandomGenerator.chooseItemFromList(game.getRacesPool());
        }
        LOGGER.debug("Smart bot choice race: {} ", race);
        return race;
    }

    @Override
    public @Nullable Pair<Position, List<Unit>> chooseCatchingCell(final @NotNull Player player,
                                                                   final @NotNull IGame game) {
        final Action action = AIProcessor.getAction(Objects.requireNonNull(tree), player, functionType);
        tree = SimulationTreeCreatingProcessor.updateTree(tree, action);
        final Pair<Position, List<Unit>> resolution = ((CatchCellAction) action).getResolution();
        LOGGER.debug("Resolution of smart bot: {} ", resolution);
        return resolution;
    }

    @Override
    public @NotNull Map<Position, List<Unit>> distributionUnits(final @NotNull Player player,
                                                                final @NotNull IGame game) {
        final Action action = AIProcessor.getAction(Objects.requireNonNull(tree), player, functionType);
        tree = SimulationTreeCreatingProcessor.updateTree(tree, action);
        final Map<Position, List<Unit>> resolutions = ((DistributionUnitsAction) action).getResolutions();
        LOGGER.debug("Smart bot distributed units: {} ", resolutions);
        return resolutions;
    }
}
