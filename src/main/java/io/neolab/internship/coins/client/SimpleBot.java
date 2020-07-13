package io.neolab.internship.coins.client;

import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.utils.RandomGenerator;

import java.util.*;

public class SimpleBot implements ISimpleBot {
    Random random = new Random();

    @Override
    public boolean declineRaceChoose(final Player player, final IGame game) {
        return random.nextInt(2) == 1;
    }

    @Override
    public Race chooseRace(final IGame game) {
        return RandomGenerator.chooseItemFromList(game.getRacesPool());
    }

    @Override
    public boolean catchCellsContinued(final Player player, final IGame game) {
        return random.nextInt(2) == 1;
    }

    @Override
    public Pair<Cell, List<Unit>> catchCell(final Player player, final IGame game, final List<Cell> achievableCells) {
        return new Pair<>(RandomGenerator
                .chooseItemFromList(achievableCells),
                player.getUnitStateToUnits()
                        .get(AvailabilityType.AVAILABLE)
                        .subList(
                                0,
                                RandomGenerator.chooseNumber(player.getUnitsByState(AvailabilityType.AVAILABLE).size())
                        )
        );
    }

    @Override
    public Map<Cell, List<Unit>> distributionUnits(final Player player, final IGame game) {
        final Map<Cell, List<Unit>> distributionUnits = new HashMap<>();
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        List<Unit> units = new LinkedList<>();
        while (availableUnits.size() > 0 && random.nextInt(2) == 1) {
            final Cell protectedCell = RandomGenerator.chooseItemFromList(
                    game.getOwnToCells().get(player)); // клетка, в которую игрок хочет распределить войска
            units = availableUnits.subList(units.size(), RandomGenerator.chooseNumber(
                    availableUnits.size() - units.size()) + units.size()
            ); // список юнитов, которое игрок хочет распределить в эту клетку
            distributionUnits.put(protectedCell, units);
        }
        return distributionUnits;
    }
}
