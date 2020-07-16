package io.neolab.internship.coins.client;

import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.service.GameLoopProcessor;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.utils.RandomGenerator;

import java.util.*;

public class SimpleBot implements IBot {
    Random random = new Random();

    @Override
    public boolean declineRaceChoose(final Player player, final IGame game) {
        return random.nextInt(2) == 1;
    }

    @Override
    public Race chooseRace(final Player player, final IGame game) {
        return RandomGenerator.chooseItemFromList(game.getRacesPool());
    }

    @Override
    public Pair<Position, List<Unit>> catchCell(final Player player, final IGame game) {
        return random.nextInt(2) == 1 ?
                new Pair<>(game.getBoard().getPositionByCell(RandomGenerator
                        .chooseItemFromList(GameLoopProcessor
                                .getAchievableCells(game.getBoard(), game.getOwnToCells().get(player))
                        )),
                        player.getUnitStateToUnits()
                                .get(AvailabilityType.AVAILABLE)
                                .subList(
                                        0,
                                        RandomGenerator.chooseNumber(player.getUnitsByState(AvailabilityType.AVAILABLE).size())
                                )
                ) : null;
    }

    @Override
    public Map<Position, List<Unit>> distributionUnits(final Player player, final IGame game) {
        final Map<Position, List<Unit>> distributionUnits = new HashMap<>();
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        List<Unit> units = new LinkedList<>();
        while (availableUnits.size() > 0 && random.nextInt(2) == 1) {
            final Cell protectedCell = RandomGenerator.chooseItemFromList(
                    game.getOwnToCells().get(player)); // клетка, в которую игрок хочет распределить войска
            units.addAll(availableUnits.subList(units.size(), RandomGenerator.chooseNumber(
                    availableUnits.size() - units.size()) + units.size()
            )); // список юнитов, которое игрок хочет распределить в эту клетку
            distributionUnits.put(game.getBoard().getPositionByCell(protectedCell), units);
            units = new LinkedList<>();
        }
        return distributionUnits;
    }
}
