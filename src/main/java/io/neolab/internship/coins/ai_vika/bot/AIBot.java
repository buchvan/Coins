package io.neolab.internship.coins.ai_vika.bot;

import io.neolab.internship.coins.ai_vika.bot.decision.model.CatchCellDecision;
import io.neolab.internship.coins.ai_vika.bot.decision.model.ChangeRaceDecision;
import io.neolab.internship.coins.ai_vika.bot.decision.model.DeclineRaceDecision;
import io.neolab.internship.coins.ai_vika.bot.decision.model.DistributionUnitsDecision;
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

import static io.neolab.internship.coins.ai_vika.bot.decision.AIDecisionMaker.*;

/**
 * Класс бота с ИИ
 */
public class AIBot implements IBot {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(AIBot.class);


    /**
     * Возвращает решение бота об упадке расы в игру для совершение хода
     *
     * @param player - игровая сущность симплбота
     * @param game   - объект, хранящий всю метаинформацию об игре
     * @return - упадок/не упадок
     */
    @Override
    public boolean declineRaceChoose(@NotNull final Player player, @NotNull final IGame game) {
        LOGGER.debug("AI bot will decline race");
        final boolean choice;
        final DeclineRaceDecision decision = (DeclineRaceDecision) getDeclineRaceDecision(player, game);
        choice = Objects.requireNonNull(decision).isDeclineRace();
        LOGGER.debug("AI bot decline race choice: {} ", choice);
        return choice;
    }

    /**
     * Возвращает решение бота о выборе новой расы в игру для совершение хода
     *
     * @param player - игровая сущность симплбота
     * @param game   - объект, хранящий всю метаинформацию об игре
     * @return - новая раса
     */
    @Override
    public @NotNull Race chooseRace(@NotNull final Player player, @NotNull final IGame game) {
        LOGGER.debug("AI bot will choose race");
        final Race race;
        final ChangeRaceDecision decision = (ChangeRaceDecision) getChooseRaceDecision(player, game);
        race = Objects.requireNonNull(decision).getDecision();
        LOGGER.debug("AI bot choice race: {} ", race);
        return Objects.requireNonNull(race);
    }

    /**
     * Возвращает решение бота о захвате клетки в игру для совершение хода
     *
     * @param player - игровая сущность симплбота
     * @param game   - объект, хранящий всю метаинформацию об игре
     * @return - (позиция; юниты для захвата)
     */
    @Override
    public @Nullable Pair<Position, List<Unit>> chooseCatchingCell(@NotNull final Player player,
                                                                   @NotNull final IGame game) {
        LOGGER.debug("AI bot will capture of cells");
        final Pair<Position, List<Unit>> captureCell;
        final CatchCellDecision decision = (CatchCellDecision) getChooseCaptureCellDecision(player, game);
        captureCell = Objects.requireNonNull(decision).getDecision();
        LOGGER.debug("Resolution of AI bot: {}", captureCell);
        return captureCell;
    }

    /**
     * Возвращает решение бота о перераспределении юнитов в игру для совершение хода
     *
     * @param player - игровая сущность симплбота
     * @param game   - объект, хранящий всю метаинформацию об игре
     * @return - отображение клетка -> список юнитов
     */
    @Override
    public @NotNull Map<Position, List<Unit>> distributionUnits(@NotNull final Player player,
                                                                @NotNull final IGame game) {
        LOGGER.debug("AI bot will distribute units");
        final Map<Position, List<Unit>> resolution;
        final DistributionUnitsDecision distributionUnitsDecision = (DistributionUnitsDecision)
                getDistributionUnitsDecision(player, game);
        resolution = Objects.requireNonNull(distributionUnitsDecision).getResolutions();
        LOGGER.debug("AI bot distributed units: {}", resolution);
        return Objects.requireNonNull(resolution);
    }
}
