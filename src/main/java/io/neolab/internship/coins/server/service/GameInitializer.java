package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.board.*;
import io.neolab.internship.coins.server.game.board.factory.BoardFactory;
import io.neolab.internship.coins.server.game.feature.CoefficientlyFeature;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.feature.FeatureType;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GameInitializer {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(GameInitializer.class);

    @Contract("_, _, _ -> new")
    public static @NotNull IGame gameInit(final int boardSizeX, final int boardSizeY,
                                          final @NotNull List<Player> playerList) throws CoinsException {
        LOGGER.debug("Init...");

        // final IBoard board = initBoard(boardSizeX, boardSizeY);
        final IBoard board = new BoardFactory().generateBoard(boardSizeX, boardSizeY);

        final Map<Player, Set<Cell>> feudalToCells = initMapWithPlayerKeySetValue(playerList, "feudalToCells");
        final Map<Player, List<Cell>> ownToCells =
                initMapWithPlayerKeyListValue(playerList, "ownToCells");
        final Map<Player, List<Cell>> playerToTransitCells =
                initMapWithPlayerKeyListValue(playerList, "playerToTransitCells");
        final Map<Player, Set<Cell>> playerAchievableCells =
                initMapWithPlayerKeySetValue(playerList, "playerAchievableCells");

        final GameFeatures gameFeatures = initGameFeatures();
        final List<Race> racesPool = createRacesPool();

        return new Game(board, feudalToCells, ownToCells, playerToTransitCells, playerAchievableCells,
                gameFeatures, racesPool, playerList);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull IGame gameInit(final int boardSizeX, final int boardSizeY, final int playersCount)
            throws CoinsException {
        final List<Player> playerList = initTestPlayers(playersCount);
        return gameInit(boardSizeX, boardSizeY, playerList);
    }

    /**
     * Инициализация тестовых игроков
     *
     * @return список тестовых игроков
     */
    private static @NotNull List<Player> initTestPlayers(final int playersCount) {
        int i = 0;
        final List<Player> playerList = new LinkedList<>();
        while (i < playersCount) {
            i++;
            playerList.add(new Player("F" + i));
        }
        LOGGER.debug("Player list is created: {} ", playerList);
        return playerList;
    }

    /**
     * Инициализация мапы (игрок -> множество клеток) по всем игрокам списка
     *
     * @param playerList - список игроков
     * @return инициализированную мапу
     */
    private static @NotNull Map<Player, Set<Cell>> initMapWithPlayerKeySetValue(final @NotNull List<Player> playerList,
                                                                                final @NotNull String log) {
        final Map<Player, Set<Cell>> mapWithPlayerKey = new HashMap<>(playerList.size());
        playerList.forEach(player -> mapWithPlayerKey.put(player, new HashSet<>()));
        LOGGER.debug("{} init: {} ", log, mapWithPlayerKey);
        return mapWithPlayerKey;
    }

    /**
     * Инициализация мапы (игрок -> список клеток) по всем игрокам списка
     *
     * @param playerList - список игроков
     * @return инициализированную мапу
     */
    private static @NotNull Map<Player, List<Cell>> initMapWithPlayerKeyListValue(final @NotNull List<Player> playerList,
                                                                                  final @NotNull String log) {
        final Map<Player, List<Cell>> mapWithPlayerKey = new HashMap<>(playerList.size());
        playerList.forEach(player -> mapWithPlayerKey.put(player, new ArrayList<>()));
        LOGGER.debug("{} init: {} ", log, mapWithPlayerKey);
        return mapWithPlayerKey;
    }


    /**
     * Инициализация особенностей
     *
     * @return raceCellTypeFeatures
     */
    @Contract(" -> new")
    private static @NotNull GameFeatures initGameFeatures() {
        final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures = new HashMap<>();
        final List<Feature> impossibleCatchCellFeature = new ArrayList<>();
        impossibleCatchCellFeature.add(new Feature(FeatureType.CATCH_CELL_IMPOSSIBLE));

        /* Добавление особенностей по расам */
        addRaceCellTypeFeaturesByRace(Race.MUSHROOM, raceCellTypeFeatures, impossibleCatchCellFeature);
        addRaceCellTypeFeaturesByRace(Race.AMPHIBIAN, raceCellTypeFeatures, impossibleCatchCellFeature);
        addRaceCellTypeFeaturesByRace(Race.ELF, raceCellTypeFeatures, impossibleCatchCellFeature);
        addRaceCellTypeFeaturesByRace(Race.ORC, raceCellTypeFeatures, impossibleCatchCellFeature);
        addRaceCellTypeFeaturesByRace(Race.GNOME, raceCellTypeFeatures, impossibleCatchCellFeature);
        addRaceCellTypeFeaturesByRace(Race.UNDEAD, raceCellTypeFeatures, impossibleCatchCellFeature);

        LOGGER.debug("raceCellTypeFeatures init: {} ", raceCellTypeFeatures);
        return new GameFeatures(raceCellTypeFeatures);
    }

    /**
     * Добавление особенностей по расе
     *
     * @param race                       - раса, для которой нужно добавить особенности
     * @param raceCellTypeFeatures       - мапа, которую нужно обновить
     * @param impossibleCatchCellFeature - список из одного свойства невозможности захвата клетки
     */
    private static void addRaceCellTypeFeaturesByRace(final Race race,
                                                      final Map<Pair<Race, CellType>, List<Feature>>
                                                              raceCellTypeFeatures,
                                                      final @NotNull List<Feature> impossibleCatchCellFeature) {

        if (race == Race.MUSHROOM) { // Грибы
            addRaceCellTypeFeaturesByRaceMushroom(raceCellTypeFeatures, impossibleCatchCellFeature);
            return;
        }
        if (race == Race.AMPHIBIAN) { // Амфибии
            addRaceCellTypeFeaturesByRaceAmphibian(raceCellTypeFeatures);
            return;
        }
        if (race == Race.ELF) { // Эльфы
            addRaceCellTypeFeaturesByRaceElf(raceCellTypeFeatures, impossibleCatchCellFeature);
            return;
        }
        if (race == Race.ORC) { // Орки
            addRaceCellTypeFeaturesByRaceOrc(raceCellTypeFeatures, impossibleCatchCellFeature);
            return;
        }
        if (race == Race.GNOME) { // Гномы
            addRaceCellTypeFeaturesByRaceGnome(raceCellTypeFeatures, impossibleCatchCellFeature);
            return;
        }
        if (race == Race.UNDEAD) { // Нежить
            addRaceCellTypeFeaturesByRaceUndead(raceCellTypeFeatures, impossibleCatchCellFeature);
        }
    }

    /**
     * Добавление особенностей для грибов
     *
     * @param raceCellTypeFeatures       - мапа, которую нужно обновить
     * @param impossibleCatchCellFeature - список из одного свойства невозможности захвата клетки
     */
    private static void addRaceCellTypeFeaturesByRaceMushroom(final @NotNull Map<Pair<Race, CellType>, List<Feature>>
                                                                      raceCellTypeFeatures,
                                                              final @NotNull List<Feature> impossibleCatchCellFeature) {
        final List<Feature> mushroomFeatures = new ArrayList<>(2);
        mushroomFeatures.add(new CoefficientlyFeature(FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL, 1));
        mushroomFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.MUSHROOM, CellType.MUSHROOM), mushroomFeatures);

        final List<Feature> mushroomFeaturesSecond = new ArrayList<>(1);
        mushroomFeaturesSecond.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.MUSHROOM, CellType.LAND), mushroomFeaturesSecond);
        raceCellTypeFeatures.put(new Pair<>(Race.MUSHROOM, CellType.MOUNTAIN), mushroomFeaturesSecond);

        final List<Feature> mushroomFeaturesThird = new ArrayList<>(mushroomFeaturesSecond);
        mushroomFeaturesThird.addAll(impossibleCatchCellFeature);
        raceCellTypeFeatures.put(new Pair<>(Race.MUSHROOM, CellType.WATER), mushroomFeaturesThird);

        int i = 1;
        LOGGER.debug("[{}] Features of Mushroom race added: {} ", i++, mushroomFeatures);
        LOGGER.debug("[{}] Features of Mushroom race added: {} ", i++, mushroomFeaturesSecond);
        LOGGER.debug("[{}] Features of Mushroom race added: {} ", i, mushroomFeaturesThird);
    }

    /**
     * Добавление особенностей для амфибий
     *
     * @param raceCellTypeFeatures - мапа, которую нужно обновить
     */
    private static void addRaceCellTypeFeaturesByRaceAmphibian(final @NotNull Map<Pair<Race, CellType>, List<Feature>>
                                                                       raceCellTypeFeatures) {
        final List<Feature> amphibianFeatures = new ArrayList<>(1);
        amphibianFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.AMPHIBIAN, CellType.MUSHROOM), amphibianFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.AMPHIBIAN, CellType.LAND), amphibianFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.AMPHIBIAN, CellType.MOUNTAIN), amphibianFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.AMPHIBIAN, CellType.WATER), amphibianFeatures);

        LOGGER.debug("Features of Amphibian race added: {} ", amphibianFeatures);
    }

    /**
     * Добавление особенностей для эльфов
     *
     * @param raceCellTypeFeatures       - мапа, которую нужно обновить
     * @param impossibleCatchCellFeature - список из одного свойства невозможности захвата клетки
     */
    private static void addRaceCellTypeFeaturesByRaceElf(final @NotNull Map<Pair<Race, CellType>, List<Feature>>
                                                                 raceCellTypeFeatures,
                                                         final @NotNull List<Feature> impossibleCatchCellFeature) {
        final List<Feature> elfFeatures = new ArrayList<>(2);
        elfFeatures.add(new CoefficientlyFeature(FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL_GROUP, 1));
        elfFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.ELF, CellType.MUSHROOM), elfFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ELF, CellType.LAND), elfFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ELF, CellType.MOUNTAIN), elfFeatures);

        final List<Feature> elfFeaturesSecond = new ArrayList<>(elfFeatures);
        elfFeaturesSecond.addAll(impossibleCatchCellFeature);
        raceCellTypeFeatures.put(new Pair<>(Race.ELF, CellType.WATER), elfFeaturesSecond);

        int i = 1;
        LOGGER.debug("[{}] Features of Elf race added: {} ", i++, elfFeatures);
        LOGGER.debug("[{}] Features of Elf race added: {} ", i, elfFeaturesSecond);
    }

    /**
     * Добавление особенностей для орков
     *
     * @param raceCellTypeFeatures       - мапа, которую нужно обновить
     * @param impossibleCatchCellFeature - список из одного свойства невозможности захвата клетки
     */
    private static void addRaceCellTypeFeaturesByRaceOrc(final @NotNull Map<Pair<Race, CellType>, List<Feature>>
                                                                 raceCellTypeFeatures,
                                                         final @NotNull List<Feature> impossibleCatchCellFeature) {
        final List<Feature> orcFeatures = new ArrayList<>(2);
        orcFeatures.add(new CoefficientlyFeature(FeatureType.CATCH_CELL_CHANGING_UNITS_NUMBER, 1));
        orcFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.MUSHROOM), orcFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.LAND), orcFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.MOUNTAIN), orcFeatures);

        final List<Feature> orcFeaturesSecond = new ArrayList<>(orcFeatures);
        orcFeaturesSecond.addAll(impossibleCatchCellFeature);
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.WATER), orcFeaturesSecond);

        int i = 1;
        LOGGER.debug("[{}] Features of Orc race added: {} ", i++, orcFeatures);
        LOGGER.debug("[{}] Features of Orc race added: {} ", i, orcFeaturesSecond);
    }

    /**
     * Добавление особенностей для гномов
     *
     * @param raceCellTypeFeatures       - мапа, которую нужно обновить
     * @param impossibleCatchCellFeature - список из одного свойства невозможности захвата клетки
     */
    private static void addRaceCellTypeFeaturesByRaceGnome(final @NotNull Map<Pair<Race, CellType>, List<Feature>>
                                                                   raceCellTypeFeatures,
                                                           final @NotNull List<Feature> impossibleCatchCellFeature) {
        final List<Feature> gnomeFeatures = new ArrayList<>(2);
        gnomeFeatures.add(new CoefficientlyFeature(FeatureType.DEFENSE_CELL_CHANGING_UNITS_NUMBER, 1));
        gnomeFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.GNOME, CellType.MUSHROOM), gnomeFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.GNOME, CellType.LAND), gnomeFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.GNOME, CellType.MOUNTAIN), gnomeFeatures);

        final List<Feature> gnomeFeaturesSecond = new ArrayList<>(gnomeFeatures);
        gnomeFeaturesSecond.addAll(impossibleCatchCellFeature);
        raceCellTypeFeatures.put(new Pair<>(Race.GNOME, CellType.WATER), gnomeFeaturesSecond);

        int i = 1;
        LOGGER.debug("[{}] Features of Gnome race added: {} ", i++, gnomeFeatures);
        LOGGER.debug("[{}] Features of Gnome race added: {} ", i, gnomeFeaturesSecond);
    }

    /**
     * Добавление особенностей для нежити
     *
     * @param raceCellTypeFeatures       - мапа, которую нужно обновить
     * @param impossibleCatchCellFeature - список из одного свойства невозможности захвата клетки
     */
    private static void addRaceCellTypeFeaturesByRaceUndead(final @NotNull Map<Pair<Race, CellType>, List<Feature>>
                                                                    raceCellTypeFeatures,
                                                            final @NotNull List<Feature> impossibleCatchCellFeature) {

        final List<Feature> undeadFeatures = new ArrayList<>(1);
        undeadFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.UNDEAD, CellType.MUSHROOM), undeadFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.UNDEAD, CellType.LAND), undeadFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.UNDEAD, CellType.MOUNTAIN), undeadFeatures);

        final List<Feature> undeadFeaturesSecond = new ArrayList<>(undeadFeatures);
        undeadFeaturesSecond.addAll(impossibleCatchCellFeature);
        raceCellTypeFeatures.put(new Pair<>(Race.UNDEAD, CellType.WATER), undeadFeaturesSecond);

        int i = 1;
        LOGGER.debug("[{}] Features of Undead race added: {} ", i++, undeadFeatures);
        LOGGER.debug("[{}] Features of Undead race added: {} ", i, undeadFeaturesSecond);
    }


    /**
     * Создание доступного для игроков пула рас
     *
     * @return пул рас
     */
    private static @NotNull List<Race> createRacesPool() {
        final List<Race> racesPool = new ArrayList<>(Arrays.asList(Race.values()));
        LOGGER.debug("Pool of races created: {} ", racesPool);
        return racesPool;
    }
}
