package io.neolab.internship.coins.server.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.neolab.internship.coins.common.serialization.deserialize.PlayerKeyDeserializer;
import io.neolab.internship.coins.common.serialization.serialize.PlayerSerializer;
import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import io.neolab.internship.coins.server.game.player.Unit;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Game implements IGame, Serializable {

    @JsonProperty
    private @NotNull IBoard board;

    @JsonProperty
    private int currentRound;

    public static final int ROUNDS_COUNT = 10;

    @JsonProperty
    @JsonSerialize(keyUsing = PlayerSerializer.class)
    @JsonDeserialize(keyUsing = PlayerKeyDeserializer.class)
    private final @NotNull Map<Player, Set<Cell>> feudalToCells; // игрок > множество клеток, приносящих ему монет

    @JsonProperty
    @JsonSerialize(keyUsing = PlayerSerializer.class)
    @JsonDeserialize(keyUsing = PlayerKeyDeserializer.class)
    private final @NotNull Map<Player, List<Cell>> ownToCells; // игрок -> список клеток, которые он контролирует

    @JsonProperty
    @JsonSerialize(keyUsing = PlayerSerializer.class)
    @JsonDeserialize(keyUsing = PlayerKeyDeserializer.class)
    private final @NotNull Map<Player, List<Cell>> playerToTransitCells; // игрок -> список клеток,
    // которые он контролирует, но которые не приносят ему монет

    /* Так можно найти список транзитных клетки одного игрока: */
//        final List<Cell> transitCells = new LinkedList<>(ownToCells.get(player));
//        transitCells.removeIf(feudalToCells.get(player)::contains);

    @JsonProperty
    @JsonSerialize(keyUsing = PlayerSerializer.class)
    @JsonDeserialize(keyUsing = PlayerKeyDeserializer.class)
    private final @NotNull Map<Player, Set<Cell>> playerToAchievableCells; // игрок -> множество достижимых за один ход
    // клеток

    @JsonProperty
    private final @NotNull GameFeatures gameFeatures;

    @JsonProperty
    private final @NotNull List<Race> racesPool;

    @JsonProperty
    private final @NotNull List<Player> players;

    public Game() {
        this(new Board(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new GameFeatures(),
                new LinkedList<>(), new LinkedList<>());
    }

    @Contract(pure = true)
    public Game(final @NotNull IBoard board, final @NotNull Map<Player, Set<Cell>> feudalToCells,
                final @NotNull Map<Player, List<Cell>> ownToCells,
                final @NotNull Map<Player, List<Cell>> playerToTransitCells,
                final @NotNull Map<Player, Set<Cell>> playerToAchievableCells,
                final @NotNull GameFeatures gameFeatures, final @NotNull List<Race> racesPool,
                final @NotNull List<Player> players) {

        this(board, 0, feudalToCells, ownToCells, playerToTransitCells, playerToAchievableCells,
                gameFeatures, racesPool, players);
    }

    @Contract(pure = true)
    @JsonCreator
    public Game(@NotNull @JsonProperty("board") final IBoard board,
                @JsonProperty("currentRound") final int currentRound,
                @NotNull @JsonProperty("feudalToCells") final Map<Player, Set<Cell>> feudalToCells,
                @NotNull @JsonProperty("ownToCells") final Map<Player, List<Cell>> ownToCells,
                @NotNull @JsonProperty("playerToTransitCells") final Map<Player, List<Cell>> playerToTransitCells,
                @NotNull @JsonProperty("playerToAchievableCells") final Map<Player, Set<Cell>> playerToAchievableCells,
                @NotNull @JsonProperty("gameFeatures") final GameFeatures gameFeatures,
                @NotNull @JsonProperty("racesPool") final List<Race> racesPool,
                @NotNull @JsonProperty("players") final List<Player> players) {
        this.board = board;
        this.currentRound = currentRound;
        this.feudalToCells = feudalToCells;
        this.ownToCells = ownToCells;
        this.playerToTransitCells = playerToTransitCells;
        this.playerToAchievableCells = playerToAchievableCells;
        this.gameFeatures = gameFeatures;
        this.racesPool = racesPool;
        this.players = players;
    }

    @JsonIgnore
    @Override
    public @NotNull Game getCopy() {
        final IBoard board = this.board.getCopy();
        final List<Player> players = new LinkedList<>();
        this.players.forEach(player -> players.add(player.getCopy()));
        final Map<Player, Set<Cell>> feudalToCells =
                getCopyPlayerToCellsSet(this.feudalToCells, true, this.board, board, players);
        final Map<Player, List<Cell>> ownToCells =
                getCopyPlayerToCellsList(this.ownToCells, true, this.board, board, players);
        final Map<Player, List<Cell>> playerToTransitCells =
                getCopyPlayerToCellsList(this.playerToTransitCells, false, this.board, board, players);
        final Map<Player, Set<Cell>> playerToAchievableCells =
                getCopyPlayerToCellsSet(this.playerToAchievableCells, false, this.board, board, players);

        return new Game(board, this.currentRound, feudalToCells, ownToCells, playerToTransitCells,
                playerToAchievableCells, this.gameFeatures.getCopy(), new LinkedList<>(this.racesPool), players);
    }

    /**
     * Взять копию мапы игрок -> список клеток
     *
     * @param playerToCellsList - мапа, чью копию нужно взять
     * @param isOwn             - если это мапа ownToCells
     * @param board             - оригинальная борда
     * @param boardCopy         - копия борды
     * @param copyPlayers       - список копий игроков
     * @return копию мапы игрок -> список клеток
     */
    private static Map<Player, List<Cell>> getCopyPlayerToCellsList(final Map<Player, List<Cell>> playerToCellsList,
                                                                    final boolean isOwn,
                                                                    final IBoard board, final IBoard boardCopy,
                                                                    final List<Player> copyPlayers) {
        final Map<Player, List<Cell>> playerToCellsListCopy = new HashMap<>(playerToCellsList.size());
        playerToCellsList.forEach((player, cells) -> {
            final List<Cell> cellsCopy = new LinkedList<>();
            cells.forEach(cell ->
                    cellsCopy.add(boardCopy.getCellByPosition(board.getPositionByCell(cell))));
            final Player playerCopy = findPlayerCopy(player, copyPlayers);
            if (isOwn) {
                /* Добавляем в копии клеток ссылки на тех юнитов, которые имеются у копии соответствующего игрока */
                cellsCopy.forEach(cell -> {
                    final List<Unit> unitList = cell.getUnits();
                    final int unitListSize = unitList.size();
                    playerCopy.getUnitStateToUnits().values().forEach(units ->
                            unitList.addAll(units.stream()
                                    .filter(unitList::contains)
                                    .collect(Collectors.toList())));
                    cell.getUnits().subList(0, unitListSize).clear();
                });
            }
            playerToCellsListCopy.put(playerCopy, cellsCopy);
        });
        return playerToCellsListCopy;
    }

    /**
     * Взять копию мапы игрок -> множество клеток
     *
     * @param playerToCellsSet - мапа, чью копию нужно взять
     * @param isFeudal         - если это мапа feudalToCells
     * @param board            - оригинальная борда
     * @param boardCopy        - копия борды
     * @param copyPlayers      - список копий игроков
     * @return копию мапы игрок -> список клеток
     */
    private static Map<Player, Set<Cell>> getCopyPlayerToCellsSet(final Map<Player, Set<Cell>> playerToCellsSet,
                                                                  final boolean isFeudal,
                                                                  final IBoard board, final IBoard boardCopy,
                                                                  final List<Player> copyPlayers) {
        final Map<Player, Set<Cell>> playerToCellsSetCopy = new HashMap<>(playerToCellsSet.size());
        playerToCellsSet.forEach((player, cells) -> {
            final Set<Cell> cellsCopy = new HashSet<>(cells.size());
            cells.forEach(cell ->
                    cellsCopy.add(boardCopy.getCellByPosition(board.getPositionByCell(cell))));
            final Player playerCopy = findPlayerCopy(player, copyPlayers);
            if (isFeudal) {
                cellsCopy.forEach(cell -> cell.setFeudal(playerCopy));
            }
            playerToCellsSetCopy.put(playerCopy, cellsCopy);
        });
        return playerToCellsSetCopy;
    }

    /**
     * Найти копию игрока в списке
     *
     * @param player      - игрок, копию которого нужно найти
     * @param copyPlayers - список копий игроков
     * @return копию игрока
     */
    private static Player findPlayerCopy(final Player player, final List<Player> copyPlayers) {
        return copyPlayers.stream()
                .filter(playerItem -> playerItem.equals(player))
                .findFirst()
                .orElseThrow();
    }

    @Override
    public void incrementCurrentRound() {
        currentRound++;
    }

    @Override
    public @NotNull IBoard getBoard() {
        return board;
    }

    @Override
    public void setBoard(final @NotNull IBoard board) {
        this.board = board;
    }

    @Override
    public int getCurrentRound() {
        return currentRound;
    }

    @Override
    public @NotNull Map<Player, Set<Cell>> getFeudalToCells() {
        return feudalToCells;
    }

    @Override
    public @NotNull  Map<Player, List<Cell>> getOwnToCells() {
        return ownToCells;
    }

    @Override
    public @NotNull Map<Player, List<Cell>> getPlayerToTransitCells() {
        return playerToTransitCells;
    }

    @Override
    public @NotNull Map<Player, Set<Cell>> getPlayerToAchievableCells() {
        return playerToAchievableCells;
    }

    @Override
    public @NotNull GameFeatures getGameFeatures() {
        return gameFeatures;
    }

    @Override
    public @NotNull List<Race> getRacesPool() {
        return racesPool;
    }

    @Override
    public @NotNull List<Player> getPlayers() {
        return players;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Game game = (Game) o;
        return currentRound == game.currentRound &&
                Objects.equals(board, game.board) &&
                Objects.equals(feudalToCells, game.feudalToCells) &&
                Objects.equals(ownToCells, game.ownToCells) &&
                Objects.equals(playerToTransitCells, game.playerToTransitCells) &&
                Objects.equals(playerToAchievableCells, game.playerToAchievableCells) &&
                Objects.equals(gameFeatures, game.gameFeatures) &&
                Objects.equals(racesPool, game.racesPool) &&
                Objects.equals(players, game.players);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentRound, feudalToCells, ownToCells, playerToTransitCells,
                playerToAchievableCells, gameFeatures, racesPool, players);
    }

    @Override
    public String toString() {
        return "Game{" +
                "board=" + board +
                ", currentRound=" + currentRound +
                ", feudalToCells=" + feudalToCells +
                ", ownToCells=" + ownToCells +
                ", playerToTransitCells=" + playerToTransitCells +
                ", playerAchievableCells=" + playerToAchievableCells +
                ", gameFeatures=" + gameFeatures +
                ", racesPool=" + racesPool +
                ", players=" + players +
                '}';
    }
}
