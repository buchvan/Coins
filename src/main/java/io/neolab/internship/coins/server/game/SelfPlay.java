package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.*;
import io.neolab.internship.coins.server.game.feature.CoefficientlyFeature;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.feature.FeatureType;
import io.neolab.internship.coins.utils.*;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

public class SelfPlay {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfPlay.class);

    private static final int ROUNDS_COUNT = 10; // > 0
    private static final int PLAYERS_COUNT = 2; // > 0 && <= Race.values().length

    private static final int BOARD_SIZE_X = 3;
    private static final int BOARD_SIZE_Y = 4;

    /**
     * Инициализация и создание борды
     *
     * @return инициализированную борду
     */
    private static Board initBoard() {
        final BidiMap<Position, Cell> positionToCellMap = new DualHashBidiMap<>();

        /* Доска из самой первой консультации по проекту */

        positionToCellMap.put(new Position(0, 0), new Cell(IdGenerator.getCurrentId(), CellType.MUSHROOM));
        positionToCellMap.put(new Position(0, 1), new Cell(IdGenerator.getCurrentId(), CellType.LAND));
        positionToCellMap.put(new Position(0, 2), new Cell(IdGenerator.getCurrentId(), CellType.WATER));
        positionToCellMap.put(new Position(0, 3), new Cell(IdGenerator.getCurrentId(), CellType.MOUNTAIN));

        positionToCellMap.put(new Position(1, 0), new Cell(IdGenerator.getCurrentId(), CellType.MOUNTAIN));
        positionToCellMap.put(new Position(1, 1), new Cell(IdGenerator.getCurrentId(), CellType.WATER));
        positionToCellMap.put(new Position(1, 2), new Cell(IdGenerator.getCurrentId(), CellType.LAND));
        positionToCellMap.put(new Position(1, 3), new Cell(IdGenerator.getCurrentId(), CellType.MUSHROOM));

        positionToCellMap.put(new Position(2, 0), new Cell(IdGenerator.getCurrentId(), CellType.LAND));
        positionToCellMap.put(new Position(2, 1), new Cell(IdGenerator.getCurrentId(), CellType.WATER));
        positionToCellMap.put(new Position(2, 2), new Cell(IdGenerator.getCurrentId(), CellType.MUSHROOM));
        positionToCellMap.put(new Position(2, 3), new Cell(IdGenerator.getCurrentId(), CellType.MOUNTAIN));

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
        final Player neutralPlayer = new Player(IdGenerator.getCurrentId(), "neutral");
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
                Arrays.asList(new Player(IdGenerator.getCurrentId(), "kvs"),
                        new Player(IdGenerator.getCurrentId(), "bim"))
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
            addRaceCellTypeFeaturesByRaceAmphibian(raceCellTypeFeatures, impossibleCatchCellFeature);
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
     *                             // @param impossibleCatchCellFeature - список из одного свойства невозможности захвата клетки
     */
    private static void addRaceCellTypeFeaturesByRaceAmphibian(
            final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures,
            final List<Feature> impossibleCatchCellFeature) {

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


    /**
     * Игра сама с собой (self play)
     * - Создание борды
     * - Добавление метаинформации о борде (игроки, количество юнитов и т.п.)
     * - игровой цикл
     * - финализатор (результат работы)
     */
    private static void selfPlay() {

        /* генерируем имя файла-лога (self-play__HH-mm-ss) */
        final String logFileName = "self-play__" +
                new Date().toString().split(" ")[3].replaceAll(":", "-");

        try {
            MDC.put("logFileName", logFileName);
            LoggerProcessor.printDebug(LOGGER, "* Logging in file {} *", logFileName);

            /* init */
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

            final Game game = new Game(board, 0,
                    feudalToCells, ownToCells, playerToTransitCells,
                    raceCellTypeFeatures, racesPool,
                    playerList, neutralPlayer);

            LoggerProcessor.printDebug(LOGGER, "Game is created: {} ", game);
            /* --- */

            gameLoop(game);

            finalize(playerList);
        } catch (final Exception exception) { // TODO: своё исключение
            LoggerProcessor.printError(LOGGER, "ERROR!!! ", exception);
        } finally {
            LoggerProcessor.printDebug(LOGGER, "* Logs in file {} *", logFileName);
            MDC.remove("logFileName");
        }
    }

    /**
     * Игровой цикл, вся игровая логика начинается отсюда
     *
     * @param game - объект, хранящий всю метаинформацию об игровых сущностях
     */
    private static void gameLoop(final Game game) {
        final Random random = new Random();

        /* Выбор перед стартом игры */
        LoggerProcessor.printInfo(LOGGER, "--------------------------------------------------");
        LoggerProcessor.printInfo(LOGGER, "Choice at the beginning of the game");
        for (final Player player : game.getPlayers()) {
            chooseRace(player, game.getRacesPool(), random);
        }

        LoggerProcessor.printInfo(LOGGER, "---------------------------------------------------");
        LoggerProcessor.printInfo(LOGGER, "* Game is started *");

        while (game.getCurrentRound() < ROUNDS_COUNT) { // Непосредственно игровой цикл
            game.incrementCurrentRound();

            printRoundBeginLog(game);
            for (final Player player : game.getPlayers()) {
                LoggerProcessor.printInfo(LOGGER, "Next player: {} ", player.getNickname());
                playerRound(player, game, random); // раунд игрока. Все свои решения он принимает здесь
            }
            for (final Player player : game.getPlayers()) {
                updateCoinsCount(player, game);  // обновление числа монет у каждого игрока
            }

            printRoundEndLog(game);
        }
    }

    /**
     * Вывод информации в начале раунда
     *
     * @param game - объект, хранящий всю метаинформацию об игровых сущностях
     */
    private static void printRoundBeginLog(final Game game) {
        LoggerProcessor.printInfo(LOGGER, "---------------------------------------------------");
        LoggerProcessor.printInfo(LOGGER, "Round {} ", game.getCurrentRound());
    }

    /**
     * Вывод информации в конце раунда
     *
     * @param game - объект, хранящий всю метаинформацию об игровых сущностях
     */
    private static void printRoundEndLog(final Game game) {
        LoggerProcessor.printDebug(LOGGER, "* Game after {} rounds: {} *", game.getCurrentRound(), game);
        LoggerProcessor.printDebug(LOGGER, "* Players after {} rounds:", game.getCurrentRound());
        printPlayersInformation(game);
    }

    /**
     * Вывод информации об игроках
     *
     * @param game - объект, хранящий всю метаинформацию об игровых сущностях
     */
    private static void printPlayersInformation(final Game game) {

        for (final Player player : game.getPlayers()) {
            LoggerProcessor.printDebug(LOGGER,
                    "Player {}: [ coins {}, feudal for: {} cells, controled: {} cells ] ",
                    player.getNickname(), player.getCoins(),
                    game.getFeudalToCells().get(player).size(), game.getOwnToCells().get(player).size());
        }
    }

    /**
     * Раунд в исполнении игрока
     *
     * @param player - игрок, который исполняет раунд
     * @param game   - объект, хранящий всю метаинформацию об игровых сущностях
     * @param random - объект для "бросания монетки" (взятия рандомного числа)
     */
    private static void playerRound(final Player player, final Game game, final Random random) {
        playerRoundBeginUpdate(player, game);  // активация данных в начале раунда
        if (isSayYes(random)) { // В случае ответа "ДА" от игрока на вопрос: "Идти в упадок?"
            declineRace(player, game, random); // Уход в упадок
        }
        catchCells(player, game, random); // Завоёвывание клеток
        distributionUnits(player, game, random); // Распределение войск
        playerRoundEndUpdate(player); // "затухание" данных в конце раунда
    }

    /**
     * Начало раунда очередного игрового цикла игроком:
     * освобождение занятых клеток юнитами игрока,
     * статус каждого юнита игрока - доступен
     *
     * @param player - игрок, чьи данные нужно обновить согласно методу
     * @param game   - объект, хранящий всю метаинформацию об игровых сущностях
     */
    private static void playerRoundBeginUpdate(final Player player, final Game game) {

        for (final Cell cell : game.getOwnToCells().get(player)) {
            cell.getUnits()
                    .removeIf(unit -> player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).contains(unit) ||
                            player.getUnitsByState(AvailabilityType.AVAILABLE).contains(unit));
        }
        player.makeAllUnitsSomeState(AvailabilityType.AVAILABLE);
    }

    /**
     * Конец раунда очередного игрового цикла игроком:
     * всех юнитов игрока сделать недоступными
     *
     * @param player - игрок, чьи данные нужно обновить согласно методу
     */
    private static void playerRoundEndUpdate(final Player player) {
        player.makeAllUnitsSomeState(AvailabilityType.NOT_AVAILABLE);
    }

    /**
     * Метод для определения сказанного слова игроком
     *
     * @param random - объект для "бросания монетки" (взятия рандомного числа)
     * @return true - если игрок сказал да, false - нет
     */
    private static boolean isSayYes(final Random random) {
        return random.nextInt(2) == 1;
    }

    /**
     * Процесс упадка: потеря контроля над всеми клетками с сохранением от них дохода, выбор новой расы
     *
     * @param player - игрок, который решил идти в упадок
     * @param game   - объект, хранящий всю метаинформацию об игровых сущностях
     * @param random - объект для "бросания монетки" (взятия рандомного числа)
     */
    private static void declineRace(final Player player, final Game game, final Random random) {

        LoggerProcessor.printInfo(LOGGER, "* Player {} in decline of race! *", player.getNickname());
        for (final Cell cell : game.getFeudalToCells().get(player)) {
            cell.setOwn(game.getNeutralPlayer());
        }
        game.getOwnToCells().get(player).clear();
        final Race oldRace = player.getRace();
        player.setRace(null); // Сбрасываем расу игрока (списки юнитов почистятся в самом сеттере)
        chooseRace(player, game.getRacesPool(), random);
        game.getRacesPool().add(oldRace); // Возвращаем бывшую расу игрока в пул рас
    }

    /**
     * Метод для выбора новой расы игроком
     *
     * @param player    - игрок, выбирающий новую расу
     * @param racesPool - пул всех доступных рас
     * @param random    - объект для "бросания монетки" (взятия рандомного числа)
     */
    private static void chooseRace(final Player player, final List<Race> racesPool, final Random random) {
        final Race newRace = racesPool.get(random.nextInt(racesPool.size()));
        racesPool.remove(newRace); // Удаляем выбранную игроком расу из пула
        player.setRace(newRace);
        LoggerProcessor.printInfo(LOGGER,
                "* Player {} choose race {} *", player.getNickname(), newRace);
    }

    /**
     * Метод для завоёвывания клеток игроком
     *
     * @param player - игрок, проводящий завоёвывание
     * @param game   - объект, хранящий всю метаинформацию об игровых сущностях
     * @param random - объект для "бросания монетки" (взятия рандомного числа)
     */
    private static void catchCells(final Player player, final Game game, final Random random) {
        LoggerProcessor.printDebug(LOGGER, "* Player {} catch cells! *", player.getNickname());
        final List<Cell> achievableCells = getAchievableCells(player, game);
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        while (achievableCells.size() > 0 && availableUnits.size() > 0 && isSayYes(random)) {
            // Пока есть что захватывать и
            // какими войсками захватывать и
            // ответ "ДА" от игрока на вопрос: "Продолжить захват клеток?"

            final Cell catchCell = chooseCell(achievableCells, random); // клетка, которую игрок хочет захватить

            if (catchCellAttempt(player, game, catchCell, random)) { // если попытка захвата увеначалась успехом
                achievableCells.remove(catchCell);
                achievableCells.addAll(getAllNeighboringCells(game.getBoard(), catchCell));
                final List<Cell> controlledCells = game.getOwnToCells().get(player);
                achievableCells.removeIf(controlledCells::contains); // удаляем те клетки, которые уже заняты игроком
            }
        }
    }

    /**
     * Метод для получения достижимых в один ход игроком клеток, не подконтрольных ему
     *
     * @param player - игрок, чьи достижимые клетки мы хотим получить
     * @param game   -  - объект, хранящий всю метаинформацию об игровых сущностях
     * @return список достижимых в один ход игроком клеток, не подконтрольных ему
     */
    private static List<Cell> getAchievableCells(final Player player, final Game game) {
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final IBoard board = game.getBoard();
        if (controlledCells.isEmpty()) {
            return boardEdgeGetCells(board);
        } // else
        final Set<Cell> achievableCells = new HashSet<>();
        for (final Cell cell : controlledCells) {
            achievableCells.addAll(
                    getAllNeighboringCells(board, cell)); // добавляем всех соседей каждой клетки, занятой игроком
        }
        achievableCells.removeIf(controlledCells::contains); // удаляем те клетки, которые уже заняты игроком
        return new LinkedList<>(achievableCells);
    }

    /**
     * Метод взятия всех крайних клеток борды
     *
     * @param board - борда, крайние клетки которой мы хотим взять
     * @return список всех крайних клеток борды board
     */
    private static List<Cell> boardEdgeGetCells(final IBoard board) {
        final List<Cell> boardEdgeCells = new LinkedList<>();
        int strIndex;
        int colIndex = 0;
        while (colIndex < BOARD_SIZE_Y) { // обход по верхней границе борды
            boardEdgeCells.add(board.getCellByPosition(0, colIndex));
            colIndex++;
        }
        strIndex = 1;
        colIndex--; // colIndex = BOARD_SIZE_Y;
        while (strIndex < BOARD_SIZE_X) { // обход по правой границе борды
            boardEdgeCells.add(board.getCellByPosition(strIndex, colIndex));
            strIndex++;
        }
        strIndex--; // strIndex = BOARD_SIZE_X;
        colIndex--; // colIndex = BOARD_SIZE_Y - 1;
        while (colIndex >= 0) { // обход по нижней границе борды
            boardEdgeCells.add(board.getCellByPosition(strIndex, colIndex));
            colIndex--;
        }
        strIndex--; // strIndex = BOARD_SIZE_X - 1;
        colIndex++; // strIndex = 0;
        while (strIndex > 0) { // обход по левой границе борды
            boardEdgeCells.add(board.getCellByPosition(strIndex, colIndex));
            strIndex--;
        }
        return boardEdgeCells;
    }

    /**
     * Метод взятия всех соседей клетки на борде
     *
     * @param board - борда, в рамках которой мы ищем соседей клетки
     * @param cell  - клетка, чьих соседей мы ищем
     * @return список всех соседей клетки cell на борде board
     */
    private static List<Cell> getAllNeighboringCells(final IBoard board, final Cell cell) {
        final List<Cell> neighboringCells = new LinkedList<>();
        final List<Position> neighboringPositions = Position.getAllNeighboringPositions(board.getPositionByCell(cell));
        for (final Position neighboringPosition : neighboringPositions) {
            final Cell potentiallyAchievableCell = board.getCellByPosition(neighboringPosition);
            if (potentiallyAchievableCell != null) { // если не вышли за пределы борды
                neighboringCells.add(potentiallyAchievableCell);
            }
        }
        return neighboringCells;
    }

    /**
     * Метод попытки захвата одной клетки игроком
     *
     * @param player       - игрок, захватывающий клетку
     * @param game         - объект, хранящий всю метаинформацию об игровых сущностях
     * @param catchingCell - захватываемая клетка
     * @return true - если попытка увенчалась успехом, false - иначе
     */
    private static boolean catchCellAttempt(final Player player, final Game game, final Cell catchingCell,
                                            final Random random) {
        LoggerProcessor.printDebug(LOGGER,
                "Player {} catch attempt the cell {} ", player.getNickname(),
                game.getBoard().getPositionByCell(catchingCell));

        final int unitsCount = chooseNumber(player.getUnitsByState(
                AvailabilityType.AVAILABLE).size(), random); // число юнитов, которое игрок хочет направить в эту клетку

        LoggerProcessor.printDebug(LOGGER,
                "Player {} capture units in quantity {} ", player.getNickname(), unitsCount);

        final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(game, catchingCell);
        final int bonusAttack = getBonusAttackToCatchCell(player, game, catchingCell);
        if (!cellIsCatching(unitsCount + bonusAttack, unitsCountNeededToCatch)) {
            LoggerProcessor.printDebug(LOGGER,
                    "The cell is not captured. The aggressor {} retreated ", player.getNickname());

            return false;
        } // else
        catchCell(player, game, catchingCell, unitsCountNeededToCatch - bonusAttack);
        LoggerProcessor.printDebug(LOGGER, "Cell after catching: {} ", catchingCell);
        return true;
    }

    /**
     * Метод получения числа юнитов, необходимых для захвата клетки
     *
     * @param game         - объект, хранящий всю метаинформацию об игровых сущностях
     * @param catchingCell - захватываемая клетка
     * @return число юнитов, необходимое для захвата клетки catchCell
     */
    private static int getUnitsCountNeededToCatchCell(final Game game, final Cell catchingCell) {
        final Player defendingPlayer = catchingCell.getOwn();
        int unitsCountNeededToCatch = catchingCell.getType().getCatchDifficulty();
        for (final Feature feature : game.getFeaturesByRaceAndCellType(
                catchingCell.getRace(),
                catchingCell.getType())) { // Смотрим все особенности владельца

            if (feature.getType() == FeatureType.DEFENSE_CELL_CHANGING_UNITS_NUMBER) {
                unitsCountNeededToCatch += ((CoefficientlyFeature) feature).getCoefficient();

                LoggerProcessor.printDebug(LOGGER,
                        "Player stumbled upon a defense of {} in cellType {} of defending player {}",
                        catchingCell.getRace(), catchingCell.getType(),
                        defendingPlayer != null ? defendingPlayer.getNickname() : null);
            }
        }

        if (catchingCell.getUnits().size() > 0) { // если в захватываемой клетке есть юниты
            unitsCountNeededToCatch += catchingCell.getUnits().size() + 1;
        }
        LoggerProcessor.printDebug(LOGGER, "Units count needed to catch: {} ", unitsCountNeededToCatch);
        return unitsCountNeededToCatch;
    }

    /**
     * Метод получения бонуса атаки при захвате клетки
     *
     * @param player       - игрок-агрессор
     * @param game         - объект, хранящий всю метаинформацию об игровых сущностях
     * @param catchingCell - захватываемая клетка
     * @return бонус атаки (в числе юнитов) игрока player при захвате клетки catchCell
     */
    private static int getBonusAttackToCatchCell(final Player player, final Game game, final Cell catchingCell) {
        int bonusAttack = 0;
        for (final Feature feature : game.getFeaturesByRaceAndCellType(
                player.getRace(), catchingCell.getType())) { // Смотрим все особенности агрессора

            if (feature.getType() == FeatureType.CATCH_CELL_CHANGING_UNITS_NUMBER) {
                bonusAttack += ((CoefficientlyFeature) feature).getCoefficient();
                LoggerProcessor.printDebug(LOGGER,
                        "Player {} took advantage of the feature race {} and cellType of catchCell {}",
                        player.getNickname(), player.getRace(), catchingCell.getType());
            }
        }
        LoggerProcessor.printDebug(LOGGER, "Bonus attack: {} ", bonusAttack);
        return bonusAttack;
    }

    /**
     * Проверка на возможность захвата клетки
     *
     * @param attackPower          - сила атаки на клетку
     * @param necessaryAttackPower - необходимая сила атаки на эту клетку для её захвата
     * @return true - если клетку можно захватить, имея attackPower, false - иначе
     */
    private static boolean cellIsCatching(final int attackPower, final int necessaryAttackPower) {
        return attackPower >= necessaryAttackPower;
    }

    /**
     * Захватить клетку
     *
     * @param player          - игрок-агрессор
     * @param game            - объект, хранящий всю метаинформацию об игровых сущностях
     * @param catchingCell    - захватываемая клетка
     * @param tiredUnitsCount - количество "уставших юнитов" (юнитов, которые перестанут быть доступными в этом раунде)
     */
    private static void catchCell(final Player player, final Game game, final Cell catchingCell,
                                  final int tiredUnitsCount) {

        player.makeNAvailableUnitsToNotAvailable(tiredUnitsCount); // все юниты, задействованные в захвате клетки,
        // становятся недоступными

        final Player defendingPlayer = catchingCell.getOwn();
        boolean catchingCellIsFeudalizable = true;
        final boolean haveARival = isAlivePlayer(defendingPlayer, game);

        for (final Feature feature : game.getFeaturesByRaceAndCellType(
                player.getRace(), catchingCell.getType())) { // Смотрим все особенности агрессора

            catchingCellIsFeudalizable =
                    catchingCellIsFeudalizable &&
                            catchCellCheckFeature(player, catchingCell, haveARival, feature, game);
        }

        if (haveARival) {
            depriveCellFeudalAndOwner(defendingPlayer, catchingCell, game);
        }
        giveCellFeudalAndOwner(player, catchingCell, catchingCellIsFeudalizable, game);
        LoggerProcessor.printInfo(LOGGER, "Cell is catched of player {} ", player.getNickname());
    }

    /**
     * Является ли игрок "живым", т. е. не ссылкой null и не нейтральным игроком?
     *
     * @param player - игрок, про которого необходимо выяснить, является ли он нейтральным
     * @param game   - объект, хранящий всю метаинформацию об игровых сущностях
     * @return true - если игрок player не нейтрален в игре game, false - иначе
     */
    private static boolean isAlivePlayer(final Player player, final Game game) {
        return player != null && isNotNeutralPlayer(player, game);
    }

    /**
     * Является ли игрок нейтральным?
     *
     * @param player - игрок, про которого необходимо выяснить, является ли он нейтральным
     * @param game   - объект, хранящий всю метаинформацию об игровых сущностях
     * @return true - если игрок player не нейтрален в игре game, false - иначе
     */
    private static boolean isNotNeutralPlayer(final Player player, final Game game) {
        return player != game.getNeutralPlayer(); // можно сравнивать ссылки,
        // так как нейтральный игрок в игре имеется в единственном экземпляре
    }

    /**
     * Проверка особенности на CATCH_CELL_IMPOSSIBLE при захвате клетки и
     * попутная обработка всех остальных типов особенностей
     *
     * @param player       - игрок-агрессор
     * @param catchingCell - захватываемая клетка
     * @param haveARival   - true - если владелец захватываемой клетки "живой", т. е. не ссылка null и не нейтральный
     * @param feature      - особенность пары (раса, тип клетки), которая рассматривается
     * @param game         - объект, хранящий всю метаинформацию об игровых сущностях
     * @return true - если feature не CATCH_CELL_IMPOSSIBLE, false - иначе
     */
    private static boolean catchCellCheckFeature(final Player player, final Cell catchingCell, final boolean haveARival,
                                                 final Feature feature, final Game game) {

        if (haveARival) {
            if (feature.getType() == FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL) {
                final Player defendingPlayer = catchingCell.getOwn();
                int deadUnitsCount = ((CoefficientlyFeature) feature).getCoefficient();
                deadUnitsCount = Math.min(
                        deadUnitsCount,
                        defendingPlayer.getUnitsByState(AvailabilityType.NOT_AVAILABLE).size());
                killUnits(deadUnitsCount, defendingPlayer);
                return true;
            }
        }
        if (feature.getType() == FeatureType.CATCH_CELL_IMPOSSIBLE) { // то тогда клетка не будет давать монет
            game.getPlayerToTransitCells().get(player).add(catchingCell);
            return false;
        }
        return true;
    }

    /**
     * Убить какое-то количество юнитов игрока
     *
     * @param deadUnitsCount - кол-во, которое необходимо убить
     * @param player         - игрок, чьих юнитов необходимо убить
     */
    private static void killUnits(final int deadUnitsCount, final Player player) {
        ListProcessor.removeFirstN(deadUnitsCount, player.getUnitsByState(AvailabilityType.NOT_AVAILABLE));
        LoggerProcessor.printDebug(LOGGER, "{} units of player {} died ",
                deadUnitsCount, player.getNickname());
    }

    /**
     * Лишить клетку владельца и феодала
     *
     * @param player - владелец и феодал
     * @param cell   - клетка, которую нужно лишить владельца и феодала
     * @param game   - объект, хранящий всю метаинформацию об игровых сущностях
     */
    private static void depriveCellFeudalAndOwner(final Player player, final Cell cell, final Game game) {
        game.getFeudalToCells().get(player).remove(cell);
        game.getOwnToCells().get(player).remove(cell);
        cell.setOwn(null);
    }

    /**
     * Дать клетке владельца и, возможно, феодала
     *
     * @param player             - новый владелец и, возможно, феодал клетки
     * @param cell               - клетка, нуждающаяся в новом владельце и феодале
     * @param cellIsFeudalizable - true - если клетка может приносить монеты, false - иначе
     * @param game               - объект, хранящий всю метаинформацию об игровых сущностях
     */
    private static void giveCellFeudalAndOwner(final Player player, final Cell cell,
                                               final boolean cellIsFeudalizable, final Game game) {
        cell.setOwn(player);
        cell.setRace(player.getRace());
        game.getOwnToCells().get(player).add(cell);
        if (cellIsFeudalizable) {
            game.getFeudalToCells().get(player).add(cell);
            return;
        } // else
        game.getPlayerToTransitCells().get(player).add(cell);
    }

    /**
     * Метод для распределения юнитов игроком
     *
     * @param player - игрок, делающий выбор
     * @param game   - объект, хранящий всю метаинформацию об игровых сущностях
     * @param random - объект для "бросания монетки" (взятия рандомного числа)
     */
    private static void distributionUnits(final Player player, final Game game, final Random random) {
        LoggerProcessor.printDebug(LOGGER, "* Player {} is distributes units! *", player.getNickname());
        freeTransitCells(player, game);
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        player.makeAllUnitsSomeState(AvailabilityType.AVAILABLE); // доступными юнитами становятся все имеющиеся у игрока юниты
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        if (controlledCells.size() > 0) { // Если есть куда распределять войска
            while (availableUnits.size() > 0 && isSayYes(random)) {
                /* Пока есть какие войска распределять и
                ответ "ДА" от игрока на вопрос: "Продолжить распределять войска?" */

                final Cell protectedCell = chooseCell(controlledCells,
                        random); // клетка, в которую игрок хочет распределить войска
                final int unitsCount = chooseNumber(availableUnits.size(),
                        random); // число юнитов, которое игрок хочет распределить в эту клетку
                LoggerProcessor.printDebug(LOGGER, "Player {} protects by {} units the cell {}",
                        player.getNickname(), unitsCount, game.getBoard().getPositionByCell(protectedCell));
                protectCell(player, availableUnits, protectedCell, unitsCount);
            }
        }
        LoggerProcessor.printInfo(LOGGER, "Player {} distributed units ", player.getNickname());
    }

    /**
     * Освобождение игроком всех его транзитных клеток
     *
     * @param player - игрок, который должен освободить все свои транзитные клетки
     * @param game   - объект, хранящий всю метаинформацию об игровых сущностях
     */
    private static void freeTransitCells(final Player player, final Game game) {
        final List<Cell> transitCells = game.getPlayerToTransitCells().get(player);

        /* Так находятся транзитные клетки: */
//        final List<Cell> transitCells = new LinkedList<>(game.getOwnToCells().get(player));
//        transitCells.removeIf(game.getFeudalToCells().get(player)::contains);

        game.getOwnToCells().get(player).removeIf(transitCells::contains);
        for (final Cell transitCell : transitCells) {
            transitCell.getUnits().removeIf(
                    unit -> player.getUnitsByState(AvailabilityType.AVAILABLE).contains(unit) ||
                            player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).contains(unit)
            ); // Убираем юниты игрока с каждой транзитной клетки
            transitCell.setOwn(null);
        }
        transitCells.clear();
        LoggerProcessor.printDebug(LOGGER, "Player {} freed his transit cells ", player.getNickname());
    }

    /**
     * Выбрать клетку (подбросить монетку)
     *
     * @param cells  - список доступных на выбор клеток
     * @param random - объект для "бросания монетки" (взятия рандомного числа)
     * @return выбранную клетку
     */
    private static Cell chooseCell(final List<Cell> cells, final Random random) {
        final int numberOfCell = chooseNumber(cells.size(), random); // номер выбранной клетки из списка
        return cells.get(numberOfCell);
    }

    /**
     * Выбрать число (подбросить монетку)
     *
     * @param bound  - граница подходящего числа
     * @param random - объект для "бросания монетки" (взятия рандомного числа)
     * @return выбранное число
     */
    private static int chooseNumber(final int bound, final Random random) {
        return random.nextInt(bound);
    }

    /**
     * Защитить клетку: владелец помещает в ней своих юнитов
     *
     * @param player         - владелец (в этой ситуации он же - феодал)
     * @param availableUnits - список доступных юнитов
     * @param protectedCell  - защищаемая клетка
     * @param unitsCount     - число юнитов, которое игрок хочет направить в клетку
     */
    private static void protectCell(final Player player, final List<Unit> availableUnits,
                                    final Cell protectedCell, final int unitsCount) {
        protectedCell.getUnits()
                .addAll(availableUnits.subList(0, unitsCount)); // отправить первые unitsCount доступных юнитов
        player.makeNAvailableUnitsToNotAvailable(unitsCount);
        LoggerProcessor.printDebug(LOGGER, "Cell after defending: {} ", protectedCell);
    }

    /**
     * Обновить число монет у игрока
     *
     * @param player - игрок, чьё число монет необходимо обновить
     * @param game   - объект, хранящий всю метаинформацию об игровых сущностях
     */
    private static void updateCoinsCount(final Player player, final Game game) {
        LoggerProcessor.printDebug(LOGGER, "* Count of coins of player {} updated! *", player.getNickname());
        for (final Cell cell : game.getFeudalToCells().get(player)) {
            updateCoinsCountByCellWithFeatures(player, game, cell);
            player.setCoins(player.getCoins() + cell.getType().getCoinYield());
        }
        LoggerProcessor.printDebug(LOGGER,
                "Player {} updated coins count. Now he has {} ", player.getNickname(), player.getCoins());
    }

    /**
     * Обновить число монет у игрока, учитывая только особенности одной клетки
     *
     * @param player - игрок, чьё число монет необходимо обновить
     * @param game   - объект, хранящий всю метаинформацию об игровых сущностях
     * @param cell   - клетка, чьи особенности мы рассматриваем
     */
    private static void updateCoinsCountByCellWithFeatures(final Player player, final Game game, final Cell cell) {
        final Map<CellType, Boolean> cellTypeMet = new HashMap<>();
        for (final CellType cellType : CellType.values()) {
            cellTypeMet.put(cellType, false);
        }
        for (final Feature feature : game.getFeaturesByRaceAndCellType(player.getRace(), cell.getType())) {
            if (feature.getType() == FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL) {
                final int coefficient = ((CoefficientlyFeature) feature).getCoefficient();
                player.increaseCoins(coefficient);
                LoggerProcessor.printDebug(LOGGER,
                        "Player {} update coins by cellType {} ", player.getNickname(), cell.getType());
                continue;
            }
            if (feature.getType() == FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL_GROUP
                    && !cellTypeMet.get(cell.getType())) {
                cellTypeMet.put(cell.getType(), true);
                final int coefficient = ((CoefficientlyFeature) feature).getCoefficient();
                player.increaseCoins(coefficient);
                LoggerProcessor.printDebug(LOGGER,
                        "Player {} update coins by group cellType {} ", player.getNickname(), cell.getType());
            }
        }
    }

    /**
     * Финализатор selfPlay'я. Выводит победителей в лог.
     *
     * @param playerList - список игроков.
     */
    private static void finalize(final List<Player> playerList) {
        LoggerProcessor.printDebug(LOGGER, "* Finalize *");
        final int maxCoinsCount = getMaxCoinsCount(playerList);
        if (maxCoinsCount == -1) {
            LoggerProcessor.printError(LOGGER, "max count of coins < 0 !!!");
            return;
        }
        final List<Player> winners = getWinners(maxCoinsCount, playerList);
        LoggerProcessor.printInfo(LOGGER, "---------------------------------------");
        LoggerProcessor.printInfo(LOGGER, "Game OVER !!!");
        LoggerProcessor.printInfo(LOGGER, "Winners: ");
        for (final Player winner : winners) {
            LoggerProcessor.printInfo(LOGGER, "Player {} - coins {} ", winner.getNickname(), winner.getCoins());
        }
        LoggerProcessor.printInfo(LOGGER, "***************************************");
        LoggerProcessor.printInfo(LOGGER, "Results of other players: ");
        for (final Player player : playerList) {
            if (winners.contains(player)) {
                continue;
            }
            LoggerProcessor.printInfo(LOGGER, "Player {} - coins {} ", player.getNickname(), player.getCoins());
        }
    }

    /**
     * @param playerList - список игроков
     * @return максимальное кол-во монет, имеющихся у одного игрока
     */
    private static int getMaxCoinsCount(final List<Player> playerList) {
        return playerList.stream()
                .map(Player::getCoins)
                .max(Integer::compareTo)
                .orElse(-1);
    }

    /**
     * @param maxCoinsCount - максимальное кол-во монет, имеющихся у одного игрока
     * @param playerList    - - список игроков
     * @return список победителей (игроков, имеющих монет в кол-ве Давай наверное сделаем как в ТЗ
     */
    private static List<Player> getWinners(final int maxCoinsCount, final List<Player> playerList) {
        final List<Player> winners = new LinkedList<>();
        for (final Player player : playerList) {
            if (player.getCoins() == maxCoinsCount) {
                winners.add(player);
            }
        }
        return winners;
    }


    public static void main(final String[] args) {
        selfPlay();
    }
}
