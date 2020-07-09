package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.IdGenerator;

import java.util.*;

public class Player {
    private int id;
    private String nickname;
    private Race race;
    private final Map<AvailabilityType, List<Unit>> unitStateToUnits;
    private int coins;

    public Player() {
        this(0, "Test");
    }

    public Player(final int id, final String nickname) {
        this(id, nickname, null, 0);
    }

    public Player(final int id, final String nickname, final Race race, final int coins) {
        this.id = id;
        this.nickname = nickname;
        this.race = race;
        this.unitStateToUnits = new HashMap<>(AvailabilityType.values().length);
        for (final AvailabilityType availabilityType : AvailabilityType.values()) {
            this.unitStateToUnits.put(availabilityType, new LinkedList<>());
        }
        this.coins = coins;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }

    public Race getRace() {
        return race;
    }

    /**
     * Сеттер, который помимо замены расы обновляет списки юнитов
     *
     * @param race - новая раса игрока
     */
    public void setRace(final Race race) {
        this.race = race;

        /* Чистим у игрока юниты */
        for (final AvailabilityType availabilityType : AvailabilityType.values()) {
            unitStateToUnits.get(availabilityType).clear();
        }

        if (race != null) {
            /* Добавляем юнитов выбранной расы */
            int i = 0;
            while (i < race.getUnitsAmount()) {
                unitStateToUnits.get(AvailabilityType.AVAILABLE).add(new Unit(IdGenerator.getCurrentId()));
                i++;
            }
        }
    }

    public Map<AvailabilityType, List<Unit>> getUnitStateToUnits() {
        return unitStateToUnits;
    }

    public List<Unit> getUnitsByState(final AvailabilityType availabilityType) {
        return unitStateToUnits.get(availabilityType);
    }

    /**
     * Перевести всех юнитов игрока в одно состояние
     *
     * @param availabilityType - состояние, в которое нужно перевести всех юнитов игрока
     */
    public void makeAllUnitsSomeState(final AvailabilityType availabilityType) {
        for (final AvailabilityType item : AvailabilityType.values()) {
            if (item != availabilityType) {
                unitStateToUnits.get(availabilityType).addAll(unitStateToUnits.get(item));
                unitStateToUnits.get(item).clear();
            }
        }
    }

    /**
     * Сделать первые N доступных юнитов недоступными
     *
     * @param N - то число доступных юнитов, которых необходимо сделать недоступными
     */
    public void makeNAvailableUnitsToNotAvailable(final int N) {
        final Iterator<Unit> iterator = getUnitsByState(AvailabilityType.AVAILABLE).iterator();
        int i = 0;
        while (iterator.hasNext() && i < N) {
            unitStateToUnits.get(AvailabilityType.NOT_AVAILABLE).add(iterator.next());
            iterator.remove();
            i++;
        }
//        int i = 0;
//        for (final Unit unit : unitStateToUnits.get(UnitState.AVAILABLE)) {
//            if (i >= N) {
//                break;
//            }
//            unitStateToUnits.get(UnitState.NOT_AVAILABLE).add(unit);
//            i++;
//        }
//        unitStateToUnits.get(UnitState.AVAILABLE)
//                .removeIf(unit -> unitStateToUnits.get(UnitState.NOT_AVAILABLE).contains(unit));
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(final int coins) {
        this.coins = coins;
    }

    public void increaseCoins(final int number) {
        this.coins += number;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Player player = (Player) o;
        return id == player.id &&
                coins == player.coins &&
                Objects.equals(nickname, player.nickname) &&
                race == player.race &&
                Objects.equals(unitStateToUnits, player.unitStateToUnits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", race=" + race +
                ", unitStateToUnits=" + unitStateToUnits +
                ", coins=" + coins +
                '}';
    }
}
