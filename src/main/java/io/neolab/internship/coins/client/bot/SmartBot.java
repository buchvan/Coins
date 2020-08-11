package io.neolab.internship.coins.client.bot;

import io.neolab.internship.coins.client.bot.ai.bim.AIProcessor;
import io.neolab.internship.coins.client.bot.ai.bim.SimulationTreeCreatingProcessor;
import io.neolab.internship.coins.client.bot.ai.bim.SimulationTreeCreator;
import io.neolab.internship.coins.client.bot.ai.bim.model.Edge;
import io.neolab.internship.coins.client.bot.ai.bim.model.FunctionType;
import io.neolab.internship.coins.client.bot.ai.bim.model.NodeTree;
import io.neolab.internship.coins.client.bot.ai.bim.model.action.*;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SmartBot implements IBot {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(SimpleBot.class);
    private static final long TIMEOUT_MILLIS = 500;
    private @Nullable NodeTree tree;
    private final @NotNull SimulationTreeCreator treeCreator;
    private final @NotNull FunctionType functionType;
    private final @NotNull SimpleBot simpleBot = new SimpleBot();

    @Contract(pure = true)
    public SmartBot(final int maxDepth, final @NotNull FunctionType functionType) {
        this.treeCreator = new SimulationTreeCreator(maxDepth);
        this.functionType = functionType;
    }

    @Contract(mutates = "this")
    private void clearTree() {
        tree = null;
    }

    /**
     * Обновление симуляционного дерева игры после выбора до начала игры
     *
     * @param tree - дерево
     * @param game - игра
     * @return ссылку на новый корень
     */
    private static @NotNull NodeTree updateTreeAfterChoiceBeforeGame(final @NotNull NodeTree tree,
                                                                     final @NotNull IGame game) {
        while (!tree.getEdges().isEmpty() && tree.getEdges().get(0).getAction() instanceof ChangeRaceAction) {
            final Player currentPlayer = game.getPlayers().stream()
                    .filter(player1 ->
                            player1.getId() == Objects.requireNonNull(tree.getEdges().get(0).getPlayer()).getId())
                    .findAny()
                    .orElseThrow();
            for (final Edge edge : tree.getEdges()) {
                final ChangeRaceAction changeRaceAction = (ChangeRaceAction) edge.getAction();
                if (currentPlayer.getRace() == Objects.requireNonNull(changeRaceAction).getNewRace()) {
                    return SimulationTreeCreatingProcessor.updateTree(tree, changeRaceAction);
                }
            }
        }
        return new NodeTree(new LinkedList<>(), new HashMap<>(), 0);
    }

    @Override
    public boolean declineRaceChoose(final @NotNull Player player, final @NotNull IGame game) {
        if (tree != null) {
            tree = updateTreeAfterChoiceBeforeGame(tree, game);
        } else {
            tree = treeCreator.createTree(game, player);
            try {
                Thread.sleep(TIMEOUT_MILLIS);
            } catch (final InterruptedException e) {
                LOGGER.error("Error!", e);
                clearTree();
                return simpleBot.declineRaceChoose(player, game);
            }
        }
        if (tree.getEdges().isEmpty()) {
            return simpleBot.declineRaceChoose(player, game);
        }
        final Action action = AIProcessor.getAction(tree, player, functionType);
        tree = SimulationTreeCreatingProcessor.updateTree(tree, action);
        final boolean choice = ((DeclineRaceAction) action).isDeclineRace();
        LOGGER.debug("Smart bot decline race choice: {} ", choice);
        return choice;
    }

    @Override
    public @NotNull Race chooseRace(final @NotNull Player player, final @NotNull IGame game) {
        final boolean isChoiceBeforeGame = game.getCurrentRound() == 0;
        if (isChoiceBeforeGame) {
            tree = treeCreator.createTree(game, player);
            try {
                Thread.sleep(TIMEOUT_MILLIS);
            } catch (final InterruptedException e) {
                LOGGER.error("Error!", e);
                clearTree();
                return simpleBot.chooseRace(player, game);
            }
        }
        if (Objects.requireNonNull(tree).getEdges().isEmpty()) {
            return simpleBot.chooseRace(player, game);
        }
        final Action action = AIProcessor.getAction(tree, player, functionType);
        if (isChoiceBeforeGame && !SimulationTreeCreatingProcessor.isFirstPlayer(game, player)) {
            clearTree();
        } else {
            tree = SimulationTreeCreatingProcessor.updateTree(tree, action);
        }
        final Race race = ((ChangeRaceAction) action).getNewRace();
        LOGGER.debug("Smart bot choice race: {} ", race);
        return race;
    }

    @Override
    public @Nullable Pair<Position, List<Unit>> chooseCatchingCell(final @NotNull Player player,
                                                                   final @NotNull IGame game) {
        if (Objects.requireNonNull(tree).getEdges().isEmpty()) {
            return simpleBot.chooseCatchingCell(player, game);
        }
        final Action action = AIProcessor.getAction(Objects.requireNonNull(tree), player, functionType);
        tree = SimulationTreeCreatingProcessor.updateTree(tree, action);
        final Pair<Position, List<Unit>> resolution = ((CatchCellAction) action).getResolution();
        LOGGER.debug("Resolution of smart bot: {} ", resolution);
        return resolution;
    }

    @Override
    public @NotNull Map<Position, List<Unit>> distributionUnits(final @NotNull Player player,
                                                                final @NotNull IGame game) {
        if (Objects.requireNonNull(tree).getEdges().isEmpty()) {
            return simpleBot.distributionUnits(player, game);
        }
        final Action action = AIProcessor.getAction(Objects.requireNonNull(tree), player, functionType);
        clearTree();
        final Map<Position, List<Unit>> resolutions = ((DistributionUnitsAction) action).getResolutions();
        LOGGER.debug("Smart bot distributed units: {} ", resolutions);
        return resolutions;
    }
}
