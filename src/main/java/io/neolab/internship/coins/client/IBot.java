package io.neolab.internship.coins.client;

import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.utils.Pair;

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
    boolean declineRaceChoose(final Player player, final IGame game);

    /**
     * @param game - объект, хранящий всю метаинформацию об игре
     * @return выбранную расу
     */
    Race chooseRace(final Player player, final IGame game);

    /**
     * Выбрать клетку для захвата
     *
     * @param player          - игровая сущность симплбота
     * @param game            - объект, хранящий всю метаинформацию об игре
     * @return пару: выбранная для захвата клетка, список юнитов для её захвата
     */
    Pair<Position, List<Unit>> catchCell(final Player player, final IGame game);

    /**
     * Выбрать клетки для защиты
     *
     * @param player - игровая сущность симплбота
     * @param game   - объект, хранящий всю метаинформацию об игре
     * @return отображение выбранных для защиты клеток в списки юнитов для их защиты
     */
    Map<Position, List<Unit>> distributionUnits(final Player player, final IGame game);
}
