package io.neolab.internship.coins.client.bot;

import io.neolab.internship.coins.ai_vika.bot.exception.AIBotException;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface IBot {
    /**
     * Выбрать, идти ли в упадок
     *
     * @param player - игровая сущность симплбота
     * @param game   - объект, хранящий всю метаинформацию об игре
     * @return true - если идти в упадок, false - иначе
     */
    boolean declineRaceChoose(final @NotNull Player player, final @NotNull IGame game) throws AIBotException;

    /**
     * @param game - объект, хранящий всю метаинформацию об игре
     * @return выбранную расу
     */
    @NotNull Race chooseRace(final @NotNull Player player, final @NotNull IGame game) throws AIBotException;

    /**
     * Выбрать клетку для захвата
     *
     * @param player          - игровая сущность симплбота
     * @param game            - объект, хранящий всю метаинформацию об игре
     * @return пару: выбранная для захвата клетка, список юнитов для её захвата
     */
    @Nullable Pair<Position, List<Unit>> chooseCatchingCell(final @NotNull Player player, final @NotNull IGame game) throws AIBotException;

    /**
     * Выбрать клетки для защиты
     *
     * @param player - игровая сущность симплбота
     * @param game   - объект, хранящий всю метаинформацию об игре
     * @return отображение выбранных для защиты клеток в списки юнитов для их защиты
     */
    @NotNull Map<Position, List<Unit>> distributionUnits(final @NotNull Player player, final @NotNull IGame game) throws AIBotException;
}
