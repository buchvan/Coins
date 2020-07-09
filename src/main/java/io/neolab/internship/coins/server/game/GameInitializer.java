package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.feature.CoefficientlyFeature;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.feature.FeatureType;
import io.neolab.internship.coins.utils.LoggerProcessor;
import io.neolab.internship.coins.utils.Pair;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GameInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameInitializer.class);

    public static Game gameInit() {
        LoggerProcessor.printDebug(LOGGER, "Init...");

        final Board board = initBoard();

        final Player neutralPlayer = createNeutralPlayer();
        final List<Player> playerList = initTestPlayers();

        final Map<Player, Set<Cell>> feudalToCells = initFeudalToCells(playerList);
        final Map<Player, List<Cell>> ownToCells =
                initMapWithPlayerKeyListValue(playerList, "ownToCells");
        final Map<Player, List<Cell>> playerToTransitCells =
                initMapWithPlayerKeyListValue(playerList, "playerToTransitCells");

        final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures = initRaceCellTypeFeatures();
        final List<Race> racesPool = createRacesPool();

        return new Game(board, 0,
                feudalToCells, ownToCells, playerToTransitCells,
                raceCellTypeFeatures, racesPool,
                playerList, neutralPlayer);
    }

    /**
     * Инициализация и создание борды
     *
     * @return инициализированную борду
     */
    private static Board initBoard() {
        final BidiMap<Position, Cell> positionToCellMap = new DualHashBidiMap<>();

        /* Доска из самой первой консультации по проекту */

        positionToCellMap.put(new Position(0, 0), new Cell(CellType.MUSHROOM));
        positionToCellMap.put(new Position(0, 1), new Cell(CellType.LAND));
        positionToCellMap.put(new Position(0, 2), new Cell(CellType.WATER));
        positionToCellMap.put(new Position(0, 3), new Cell(CellType.MOUNTAIN));

        positionToCellMap.put(new Position(1, 0), new Cell(CellType.MOUNTAIN));
        positionToCellMap.put(new Position(1, 1), new Cell(CellType.WATER));
        positionToCellMap.put(new Position(1, 2), new Cell(CellType.LAND));
        positionToCellMap.put(new Position(1, 3), new Cell(CellType.MUSHROOM));

        positionToCellMap.put(new Position(2, 0), new Cell(CellType.LAND));
        positionToCellMap.put(new Position(2, 1), new Cell(CellType.WATER));
        positionToCellMap.put(new Position(2, 2), new Cell(CellType.MUSHROOM));
        positionToCellMap.put(new Position(2, 3), new Cell(CellType.MOUNTAIN));

        /* --- */

        final Board board = new Board(positionToCellMap);
        LoggerProcessor.printDebug(LOGGER, "Board is created: {} ", board);
        return board;
    }


    /**
     * Инициализация нейтрального игрока
     *
     * @return нейтрального игрока
     */
    private static Player createNeutralPlayer() {
        final Player neutralPlayer = new Player("neutral");
        LoggerProcessor.printDebug(LOGGER, "Neutral player is created: {} ", neutralPlayer);
        return neutralPlayer;
    }

    /**
     * Инициализация тестовых игроков
     *
     * @return список тестовых игроков
     */
    private static List<Player> initTestPlayers() {
        final List<Player> playerList = new LinkedList<>(
                Arrays.asList(new Player("kvs"),
                        new Player("bim"))
        );
//        int i = 2;
//        while (i < PLAYERS_COUNT) {
//            playerList.add(new Player(IdGenerator.getCurrentId(), "F" + i));
//            i++;
//        }
        LoggerProcessor.printDebug(LOGGER, "Player list is created: {} ", playerList);
        return playerList;
    }

    /**
     * Инициализация мапы (игрок -> множество клеток) по всем игрокам списка
     *
     * @param playerList - список игроков
     * @return инициализированную мапу
     */
    private static Map<Player, Set<Cell>> initFeudalToCells(final List<Player> playerList) {
        final Map<Player, Set<Cell>> feudalToCells = new HashMap<>(playerList.size());
        for (final Player player : playerList) {
            feudalToCells.put(player, new HashSet<>());
        }
        LoggerProcessor.printDebug(LOGGER, "{} init: {} ", "feudalToCells", feudalToCells);
        return feudalToCells;
    }

    /**
     * Инициализация мапы (игрок -> список клеток) по всем игрокам списка
     *
     * @param playerList - список игроков
     * @return инициализированную мапу
     */
    private static Map<Player, List<Cell>> initMapWithPlayerKeyListValue(final List<Player> playerList,
                                                                         final String log) {
        final Map<Player, List<Cell>> mapWithPlayerKey = new HashMap<>(playerList.size());
        for (final Player player : playerList) {
            mapWithPlayerKey.put(player, new ArrayList<>());
        }
        LoggerProcessor.printDebug(LOGGER, "{} init: {} ", log, mapWithPlayerKey);
        return mapWithPlayerKey;
    }


    /**
     * Инициализация особенностей
     *
     * @return raceCellTypeFeatures
     */
    private static Map<Pair<Race, CellType>, List<Feature>> initRaceCellTypeFeatures() {
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

        LoggerProcessor.printDebug(LOGGER, "raceCellTypeFeatures init: {} ", raceCellTypeFeatures);
        return raceCellTypeFeatures;
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
                                                      final List<Feature> impossibleCatchCellFeature) {

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
    private static void addRaceCellTypeFeaturesByRaceMushroom(
            final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures,
            final List<Feature> impossibleCatchCellFeature) {

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
        LoggerProcessor.printDebug(LOGGER,
                "[{}] Features of Mushroom race added: {} ", i++, mushroomFeatures);
        LoggerProcessor.printDebug(LOGGER,
                "[{}] Features of Mushroom race added: {} ", i++, mushroomFeaturesSecond);
        LoggerProcessor.printDebug(LOGGER,
                "[{}] Features of Mushroom race added: {} ", i, mushroomFeaturesThird);
    }

    /**
     * Добавление особенностей для амфибий
     *
     * @param raceCellTypeFeatures - мапа, которую нужно обновить
     */
    private static void addRaceCellTypeFeaturesByRaceAmphibian(
            final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures) {

        final List<Feature> amphibianFeatures = new ArrayList<>();
        amphibianFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.AMPHIBIAN, CellType.MUSHROOM), amphibianFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.AMPHIBIAN, CellType.LAND), amphibianFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.AMPHIBIAN, CellType.MOUNTAIN), amphibianFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.AMPHIBIAN, CellType.WATER), amphibianFeatures);

        LoggerProcessor.printDebug(LOGGER, "Features of Amphibian race added: {} ", amphibianFeatures);
    }

    /**
     * Добавление особенностей для эльфов
     *
     * @param raceCellTypeFeatures       - мапа, которую нужно обновить
     * @param impossibleCatchCellFeature - список из одного свойства невозможности захвата клетки
     */
    private static void addRaceCellTypeFeaturesByRaceElf(
            final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures,
            final List<Feature> impossibleCatchCellFeature) {

        final List<Feature> elfFeatures = new ArrayList<>();
        elfFeatures.add(new CoefficientlyFeature(FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL_GROUP, 1));
        elfFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.ELF, CellType.MUSHROOM), elfFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ELF, CellType.LAND), elfFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ELF, CellType.MOUNTAIN), elfFeatures);

        final List<Feature> elfFeaturesSecond = new ArrayList<>(elfFeatures);
        elfFeaturesSecond.addAll(impossibleCatchCellFeature);
        raceCellTypeFeatures.put(new Pair<>(Race.ELF, CellType.WATER), elfFeaturesSecond);

        int i = 1;
        LoggerProcessor.printDebug(LOGGER,
                "[{}] Features of Elf race added: {} ", i++, elfFeatures);
        LoggerProcessor.printDebug(LOGGER,
                "[{}] Features of Elf race added: {} ", i, elfFeaturesSecond);
    }

    /**
     * Добавление особенностей для орков
     *
     * @param raceCellTypeFeatures       - мапа, которую нужно обновить
     * @param impossibleCatchCellFeature - список из одного свойства невозможности захвата клетки
     */
    private static void addRaceCellTypeFeaturesByRaceOrc(
            final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures,
            final List<Feature> impossibleCatchCellFeature) {

        final List<Feature> orcFeatures = new ArrayList<>();
        orcFeatures.add(new CoefficientlyFeature(FeatureType.CATCH_CELL_CHANGING_UNITS_NUMBER, 1));
        orcFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.MUSHROOM), orcFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.LAND), orcFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.MOUNTAIN), orcFeatures);

        final List<Feature> orcFeaturesSecond = new ArrayList<>(orcFeatures);
        orcFeaturesSecond.addAll(impossibleCatchCellFeature);
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.WATER), orcFeaturesSecond);

        int i = 1;
        LoggerProcessor.printDebug(LOGGER,
                "[{}] Features of Orc race added: {} ", i++, orcFeatures);
        LoggerProcessor.printDebug(LOGGER,
                "[{}] Features of Orc race added: {} ", i, orcFeaturesSecond);
    }

    /**
     * Добавление особенностей для гномов
     *
     * @param raceCellTypeFeatures       - мапа, которую нужно обновить
     * @param impossibleCatchCellFeature - список из одного свойства невозможности захвата клетки
     */
    private static void addRaceCellTypeFeaturesByRaceGnome(
            final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures,
            final List<Feature> impossibleCatchCellFeature) {

        final List<Feature> gnomeFeatures = new ArrayList<>();
        gnomeFeatures.add(new CoefficientlyFeature(FeatureType.DEFENSE_CELL_CHANGING_UNITS_NUMBER, 1));
        gnomeFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.GNOME, CellType.MUSHROOM), gnomeFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.GNOME, CellType.LAND), gnomeFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.GNOME, CellType.MOUNTAIN), gnomeFeatures);

        final List<Feature> gnomeFeaturesSecond = new ArrayList<>(gnomeFeatures);
        gnomeFeaturesSecond.addAll(impossibleCatchCellFeature);
        raceCellTypeFeatures.put(new Pair<>(Race.GNOME, CellType.WATER), gnomeFeaturesSecond);

        int i = 1;
        LoggerProcessor.printDebug(LOGGER,
                "[{}] Features of Gnome race added: {} ", i++, gnomeFeatures);
        LoggerProcessor.printDebug(LOGGER,
                "[{}] Features of Gnome race added: {} ", i, gnomeFeaturesSecond);
    }

    /**
     * Добавление особенностей для нежити
     *
     * @param raceCellTypeFeatures       - мапа, которую нужно обновить
     * @param impossibleCatchCellFeature - список из одного свойства невозможности захвата клетки
     */
    private static void addRaceCellTypeFeaturesByRaceUndead(
            final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures,
            final List<Feature> impossibleCatchCellFeature) {

        final List<Feature> undeadFeatures = new ArrayList<>();
        undeadFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.UNDEAD, CellType.MUSHROOM), undeadFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.UNDEAD, CellType.LAND), undeadFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.UNDEAD, CellType.MOUNTAIN), undeadFeatures);

        final List<Feature> undeadFeaturesSecond = new ArrayList<>(undeadFeatures);
        undeadFeaturesSecond.addAll(impossibleCatchCellFeature);
        raceCellTypeFeatures.put(new Pair<>(Race.UNDEAD, CellType.WATER), undeadFeaturesSecond);

        int i = 1;
        LoggerProcessor.printDebug(LOGGER,
                "[{}] Features of Undead race added: {} ", i++, undeadFeatures);
        LoggerProcessor.printDebug(LOGGER,
                "[{}] Features of Undead race added: {} ", i, undeadFeaturesSecond);
    }


    /**
     * Создание доступного для игроков пула рас
     *
     * @return пул рас
     */
    private static List<Race> createRacesPool() {
        final List<Race> racesPool = new ArrayList<>(Race.values().length - 1);
        racesPool.addAll(Arrays.asList(Race.values()).subList(0, Race.values().length - 1));
        LoggerProcessor.printDebug(LOGGER, "Pool of races created: {} ", racesPool);
        return racesPool;
    }
}
