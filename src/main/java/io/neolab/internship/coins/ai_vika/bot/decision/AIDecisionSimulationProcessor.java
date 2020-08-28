package io.neolab.internship.coins.ai_vika.bot.decision;

import io.neolab.internship.coins.ai_vika.bot.decision.model.CatchCellDecision;
import io.neolab.internship.coins.ai_vika.bot.decision.model.ChangeRaceDecision;
import io.neolab.internship.coins.ai_vika.bot.decision.model.DeclineRaceDecision;
import io.neolab.internship.coins.ai_vika.bot.decision.model.DistributionUnitsDecision;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.service.GameLoopProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;

import static io.neolab.internship.coins.server.service.GameAnswerProcessor.*;

public class AIDecisionSimulationProcessor {

    private static final boolean isLoggingTurnOn = false;

    /**
     * Симулирует принятое решение об упадке расы для копий игровых сущностей
     *
     * @param player   - текущий игрок
     * @param game     - текуще состояние игры
     * @param decision - принятое решение
     */
    static void simulateDeclineRaceDecision(final @NotNull Player player, final @NotNull IGame game,
                                            @NotNull final DeclineRaceDecision decision) {
        GameLoopProcessor.playerRoundBeginUpdate(player, isLoggingTurnOn);
        if (decision.isDeclineRace()) {
            game.getOwnToCells().get(player).clear();
            GameLoopProcessor.updateAchievableCells(player, game.getBoard(),
                    game.getPlayerToAchievableCells().get(player),
                    game.getOwnToCells().get(player), isLoggingTurnOn);
        }
    }

    /**
     * Симулирует принятое решение о смене расы для копий игровых сущностей
     *
     * @param player   - текущий игрок
     * @param game     - текуще состояние игры
     * @param decision - принятое решение
     */
    static void simulateChangeRaceDecision(final @NotNull Player player, final @NotNull IGame game,
                                           @NotNull final ChangeRaceDecision decision) {
        game.getOwnToCells().get(player).clear();
        changeRace(player, decision.getDecision(), game.getRacesPool(), isLoggingTurnOn);
        GameLoopProcessor.updateAchievableCells(player, game.getBoard(),
                new HashSet<>(game.getPlayerToAchievableCells().get(player)),
                game.getOwnToCells().get(player), false);
    }

    /**
     * Симулирует принятое решение о захвате для копий игровых сущностей
     *
     * @param player   - текущий игрок
     * @param game     - текуще состояние игры
     * @param decision - принятое решение
     */
    static void simulateCatchCellDecision(final @NotNull Player player, final @NotNull IGame game,
                                          @NotNull final CatchCellDecision decision) {
        final IBoard board = game.getBoard();
        final Cell captureCell = board.getCellByPosition(Objects.requireNonNull(decision.getDecision()).getFirst());
        pretendToCell(player, Objects.requireNonNull(captureCell), decision.getDecision().getSecond(),
                board, game.getGameFeatures(), game.getOwnToCells(), game.getFeudalToCells(),
                game.getPlayerToTransitCells().get(player),
                game.getPlayerToAchievableCells().get(player), isLoggingTurnOn);
    }

    /**
     * Симулирует принятое решение о перераспределении юнитов для копий игровых сущностей
     *
     * @param player   - текущий игрок
     * @param game     - текуще состояние игры
     * @param decision - принятое решение
     */
    static void simulateDistributionUnitsDecision(final DistributionUnitsDecision decision, final Player player,
                                                  final IGame game) {
        distributionUnits(player, game.getOwnToCells().get(player),
                game.getFeudalToCells().get(player),
                decision.getResolutions(),
                game.getBoard(), isLoggingTurnOn);
        GameLoopProcessor.playerRoundEndUpdate(player, isLoggingTurnOn);
    }

    /**
     * Обновляет монеты игрока
     *
     * @param game   - игра
     * @param player - игрок
     */
    static void updateDecisionNodeCoinsAmount(@NotNull final IGame game, final Player player) {
        GameLoopProcessor.updateCoinsCount(player, game.getFeudalToCells().get(player),
                game.getGameFeatures(), game.getBoard(), false);
    }
}
