package io.neolab.internship.coins.utils;

import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.player.Race;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PairTest {
    @Test
    public void testCreatingPair() {
        final Pair<Race, CellType> pair = new Pair<>(Race.ELF, CellType.MUSHROOM);
        assertEquals(Race.ELF, pair.getFirst());
        assertEquals(CellType.MUSHROOM, pair.getSecond());
    }

    @Test
    public void testSettersPair() {
        final Pair<Race, CellType> pair = new Pair<>(Race.ELF, CellType.MUSHROOM);
        pair.setFirst(Race.ORC);
        pair.setSecond(CellType.LAND);
        assertEquals(Race.ORC, pair.getFirst());
        assertEquals(CellType.LAND, pair.getSecond());
    }

    @Test
    public void testEqualsPairs() {
        final Pair<Race, CellType> pair1 = new Pair<>(Race.ELF, CellType.MUSHROOM);
        final Pair<Race, CellType> pair2 = new Pair<>(Race.ELF, CellType.MUSHROOM);
        assertEquals(pair1, pair2);
    }
}
