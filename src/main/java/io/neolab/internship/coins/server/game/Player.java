package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.utils.IdGenerator;

import java.util.*;

public class Player {
    private int id;
    private String nickname;
    private Race race;
    private final Map<UnitState, List<Unit>> unitStateToUnits;
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
        this.unitStateToUnits = new HashMap<>(UnitState.values().length);
        for (final UnitState unitState : UnitState.values()) {
            this.unitStateToUnits.put(unitState, new LinkedList<>());
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
        for (final UnitState unitState : UnitState.values()) {
            unitStateToUnits.get(unitState).clear();
        }

        if (race != null) {
            /* Добавляем юнитов выбранной расы */
            int i = 0;
            while (i < race.getUnitsAmount()) {
                unitStateToUnits.get(UnitState.AVAILABLE).add(new Unit(IdGenerator.getCurrentId()));
                i++;
            }
        }
    }

    public Map<UnitState, List<Unit>> getUnitStateToUnits() {
        return unitStateToUnits;
    }

    /**
     * Сделать всех юнитов доступными. Соответственно недоступных не останется
     */
    public void makeAllUnitsAvailable() {
        unitStateToUnits
                .get(UnitState.AVAILABLE).addAll(
                unitStateToUnits.get(UnitState.NOT_AVAILABLE)
        );
        unitStateToUnits.get(UnitState.NOT_AVAILABLE).clear();
    }

    /**
     * Сделать всех юнитов недоступными. Соответственно доступных не останется
     */
    public void makeAllUnitsNotAvailable() {
        unitStateToUnits
                .get(UnitState.NOT_AVAILABLE).addAll(
                unitStateToUnits.get(UnitState.AVAILABLE)
        );
        unitStateToUnits.get(UnitState.AVAILABLE).clear();
    }

    /**
     * Сделать первые N доступных юнитов недоступными
     *
     * @param N - то число доступных юнитов, которых необходимо сделать недоступными
     */
    public void makeNAvailableUnitsNotAvailable(final int N) {
        int i = 0;
        for (final Unit unit : unitStateToUnits.get(UnitState.AVAILABLE)) {
            unitStateToUnits.get(UnitState.NOT_AVAILABLE).add(unit);
            unitStateToUnits.get(UnitState.AVAILABLE).remove(unit);
            i++;
            if (i >= N) {
                break;
            }
        }
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(final int coins) {
        this.coins = coins;
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
