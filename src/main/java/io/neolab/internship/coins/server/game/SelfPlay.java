package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.*;
import io.neolab.internship.coins.server.game.feature.CoefficientlyFeature;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.feature.FeatureType;
import io.neolab.internship.coins.utils.IdGenerator;
import io.neolab.internship.coins.utils.LoggerProcessor;
import io.neolab.internship.coins.utils.Pair;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SelfPlay {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfPlay.class);

    private static final int ROUNDS_COUNT = 10;
    private static final int BOARD_SIZE_X = 3;
    private static final int BOARD_SIZE_Y = 4;

    /**
     * Инициализация и создание борды
     *
     * @return инициализированную борду
     */
    private static Board initBoard() {
        final BidiMap<Position, Cell> positionToCellMap = new DualHashBidiMap<>();

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
        LoggerProcessor.printDebug(LOGGER, "Player list: {} ", playerList);
        return playerList;
    }

    /**
     * Инициализация мапы с клетками, приносящими монеты игроку, по умолчанию
     *
     * @param playerList - список игроков
     * @return инициализированную feudalToCells
     */
    private static Map<Player, List<Cell>> initFeudalToCells(final List<Player> playerList) {
        final Map<Player, List<Cell>> feudalToCells = new HashMap<>(2);
        for (final Player player : playerList) {
            feudalToCells.put(player, new ArrayList<>());
        }
        LoggerProcessor.printDebug(LOGGER, "feudalToCells init: {} ", feudalToCells);
        return feudalToCells;
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
        addRaceCellTypeFeaturesByRace(Race.ELF, raceCellTypeFeatures, impossibleCatchCellFeature);
        addRaceCellTypeFeaturesByRace(Race.ORC, raceCellTypeFeatures, impossibleCatchCellFeature);
        addRaceCellTypeFeaturesByRace(Race.GNOME, raceCellTypeFeatures, impossibleCatchCellFeature);

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

        List<Feature> mushroomFeatures = new ArrayList<>();
        mushroomFeatures.add(new CoefficientlyFeature(FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL, 1));
        mushroomFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.MUSHROOM, CellType.MUSHROOM), mushroomFeatures);

        mushroomFeatures = new ArrayList<>();
        mushroomFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.MUSHROOM, CellType.LAND), mushroomFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.MUSHROOM, CellType.MOUNTAIN), mushroomFeatures);
        mushroomFeatures.addAll(impossibleCatchCellFeature);
        raceCellTypeFeatures.put(new Pair<>(Race.MUSHROOM, CellType.WATER), mushroomFeatures);

        LoggerProcessor.printDebug(LOGGER, "Features of Mushroom race added: {} ", raceCellTypeFeatures);
    }

    /**
     * Добавление особенностей для амфибий
     *
     * @param raceCellTypeFeatures - мапа, которую нужно обновить
     *                             // @param impossibleCatchCellFeature - список из одного свойства невозможности захвата клетки
     */
    private static void addRaceCellTypeFeaturesByRaceAmphibian(
            final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures /*,
            final List<Feature> impossibleCatchCellFeature */) {

        final List<Feature> amphibianFeatures = new ArrayList<>();
        amphibianFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.AMPHIBIAN, CellType.MUSHROOM), amphibianFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.AMPHIBIAN, CellType.LAND), amphibianFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.AMPHIBIAN, CellType.MOUNTAIN), amphibianFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.AMPHIBIAN, CellType.WATER), amphibianFeatures);

        LoggerProcessor.printDebug(LOGGER, "Features of Amphibian race added: {} ", raceCellTypeFeatures);
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

        LoggerProcessor.printDebug(LOGGER, "Features of Elf race added: {} ", raceCellTypeFeatures);
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
        orcFeatures.add(new CoefficientlyFeature(FeatureType.CATCH_CELL_CHANGING_UNITS_NUMBER, -1));
        orcFeatures.add(new CoefficientlyFeature(FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.MUSHROOM), orcFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.LAND), orcFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.MOUNTAIN), orcFeatures);
        final List<Feature> orcFeaturesSecond = new ArrayList<>(orcFeatures);
        orcFeaturesSecond.addAll(impossibleCatchCellFeature);
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.WATER), orcFeaturesSecond);

        LoggerProcessor.printDebug(LOGGER, "Features of Orc race added: {} ", raceCellTypeFeatures);
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

        LoggerProcessor.printDebug(LOGGER, "Features of Gnome race added: {} ", raceCellTypeFeatures);
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

        LoggerProcessor.printDebug(LOGGER, "Features of Undead race added: {} ", raceCellTypeFeatures);
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
        try {
            /* init */
            LoggerProcessor.printDebug(LOGGER, "Init...");
            final Board board = initBoard();
            final Player neutralPlayer = createNeutralPlayer();
            final List<Player> playerList = initTestPlayers();
            final Map<Player, List<Cell>> feudalToCells = initFeudalToCells(playerList);
            final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures = initRaceCellTypeFeatures();
            final List<Race> racesPool = createRacesPool();

            final Game game = new Game(board, 0, feudalToCells, raceCellTypeFeatures, racesPool, playerList,
                    neutralPlayer);
            LoggerProcessor.printDebug(LOGGER, "Game is created: {} ", game);

            gameLoop(game);

            finalize(playerList);
        } catch (final Exception exception) {
            LoggerProcessor.printError(LOGGER, "ERROR!!! ", exception);
        }
    }

    /**
     * Игровой цикл, вся игровая логика начинается отсюда
     *
     * @param game - игра, хранящая всю метаинформацию
     */
    private static void gameLoop(final Game game) {
        final Random random = new Random();
        LoggerProcessor.printInfo(LOGGER, "---------------------------------------------------");
        LoggerProcessor.printInfo(LOGGER, "Game is started");
        while (game.getCurrentRound() < ROUNDS_COUNT) {
            game.setCurrentRound(game.getCurrentRound() + 1);

            for (final Player player : game.getPlayers()) {
                playerRound(game.getCurrentRound(), player, game, random);
            }
            for (final Player player : game.getPlayers()) {
                updateCoinsCount(player, game);
            }
            LoggerProcessor.printDebug(LOGGER, "Game after {} rounds: {}", game.getCurrentRound(), game);
        }
    }

    /**
     * Раунд в исполнении игрока
     *
     * @param currentRound - номер текущего раунда
     * @param player       - игрок, который исполняет раунд
     * @param game         - игра, хранящая всю метаинформацию
     * @param random       - объект для "бросания монетки" (взятия рандомного числа)
     */
    private static void playerRound(final int currentRound, final Player player, final Game game,
                                    final Random random) {
        player.setAvailableUnits(player.getUnits()); // доступными юнитами становятся все имеющиеся у игрока юниты
        if (isSayYes(random)) { // В случае ответа "ДА" от игрока на вопрос: "Идти в упадок?"
            declineRace(player, game.getNeutralPlayer(), game.getFeudalToCells().get(player));
            chooseRace(player, game.getRacesPool(), random);
        }
        catchCells(player, game, random);
        distributionUnits(player, game, random); // Распределение войск
    }

    /**
     * Метод для определения сказанного слова игроком
     * <p>
     * // @param player - игрок, делающий выбор
     *
     * @param random - объект для "бросания монетки" (взятия рандомного числа)
     * @return true - если игрок сказал да, false - нет
     */
    private static boolean isSayYes(/* final Player player, */ final Random random) {
        return random.nextInt(2) == 1;
    }

    /**
     * Процесс упадка
     *
     * @param player        - игрок, который решил идти в упадок
     * @param neutralPlayer - нейтральный игрок
     * @param cells         - список клеток, которые должен занять нейтральный игрок
     */
    private static void declineRace(final Player player, final Player neutralPlayer, final List<Cell> cells) {
        for (final Cell cell : cells) {
            cell.setOwn(neutralPlayer);
        }
        player.getUnits().clear(); // чистим у игрока юниты
        player.getAvailableUnits().clear();
        LoggerProcessor.printInfo(LOGGER, "Player " + player.getNickname() + " in decline of race!");
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
        if (hasARace(player)) {
            racesPool.add(player.getRace()); // Возвращаем расу игрока в пул рас
        }
        racesPool.remove(newRace); // Удаляем выбранную игроком расу из пула
        player.setRace(newRace);
        LoggerProcessor.printInfo(LOGGER, "Player " + player.getNickname() + " choose race " + newRace);
    }

    /**
     * Игрок имеет расу?
     *
     * @param player - игрок, про которого мы хотим узнать: имеет он расу, или нет
     * @return true - если игрок player имеет расу, false - иначе
     */
    private static boolean hasARace(final Player player) {
        return player.getRace() != null;
    }

    /**
     * Метод для завоёвывания клеток игроком
     *
     * @param player - игрок, проводящий завоёвывание
     * @param game   - игра, хранящая всю метаинформацию
     * @param random - объект для "бросания монетки" (взятия рандомного числа)
     */
    private static void catchCells(final Player player, final Game game, final Random random) {
        final List<Cell> achievableCells = getAchievableCells(player, game);
        final List<Unit> availableUnits = player.getAvailableUnits();
        if (achievableCells.size() > 0) {
            while (availableUnits.size() > 0 && isSayYes(random)) {
                // Пока есть какими войсками захватывать и ответ "ДА" от игрока на вопрос: "Захватить клетку?"
                final int numberOfCell = random.nextInt(achievableCells.size()); // номер клетки из списка,
                // которую игрок хочет захватить
                final Cell catchCell = achievableCells.get(numberOfCell); // клетка, которую игрок хочет захватить
                catchCellAttempt(player, game, catchCell, random);
            }
        }
    }

    /**
     * Метод для получения достижимых в один ход игроком клеток, не подконтрольных ему
     *
     * @param player - игрок, чьи достижимые клетки мы хотим получить
     * @param game   -  - игра, хранящая всю метаинформацию
     * @return список достижимых в один ход игроком клеток, не подконтрольных ему
     */
    private static List<Cell> getAchievableCells(final Player player, final Game game) {
        final List<Cell> controlledCells = getControlledCells(player, game);
        final IBoard board = game.getBoard();
        if (controlledCells.isEmpty()) {
            return boardEdgeGetCells(board);
        } // else
        final List<Cell> achievableCells = new LinkedList<>();
        for (final Cell cell : controlledCells) {
            achievableCells.addAll(getAllNeighboringCells(board, cell));
        }
        achievableCells.removeIf(controlledCells::contains);
        return achievableCells;
    }

    /**
     * Метод взятия всех крайних клеток борды
     *
     * @param board - борда, крайние клетки которой мы хотим взять
     * @return список всех крайних клеток борды board
     */
    private static List<Cell> boardEdgeGetCells(final IBoard board) {
        final List<Cell> boardEdgeCells = new LinkedList<>();
        int strIndex = 0;
        int colIndex;
        while (strIndex < BOARD_SIZE_X) { // обход по верхней границе борды
            boardEdgeCells.add(board.getCellByPosition(strIndex, 0));
            strIndex++;
        }
        strIndex--; // strIndex = BOARD_SIZE_X;
        colIndex = 0;
        while (colIndex < BOARD_SIZE_Y) { // обход по правой границе борды
            boardEdgeCells.add(board.getCellByPosition(strIndex, colIndex));
            colIndex++;
        }
        colIndex--; // colIndex = BOARD_SIZE_Y;
        while (strIndex >= 0) { // обход по нижней границе борды
            boardEdgeCells.add(board.getCellByPosition(strIndex, colIndex));
            strIndex--;
        }
        strIndex++; // strIndex = 0;
        while (colIndex >= 0) { // обход по левой границе борды
            boardEdgeCells.add(board.getCellByPosition(strIndex, colIndex));
            colIndex--;
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
            if (potentiallyAchievableCell != null) { // если вышли за пределы борды
                neighboringCells.add(potentiallyAchievableCell);
            }
        }
        return neighboringCells;
    }

    /**
     * Метод попытки захвата одной клетки игроком
     *
     * @param player       - игрок, захватывающий клетку
     * @param game         - игра, хранящая всю метаинформацию
     * @param catchingCell - захватываемая клетка
     */
    private static void catchCellAttempt(final Player player, final Game game, final Cell catchingCell,
                                         final Random random) {
        LoggerProcessor.printDebug(LOGGER, "Player {} catch attempt the cell {} ", player.getNickname(), catchingCell);
        final int unitsCount = random.nextInt(player.getAvailableUnits().size()); // число юнитов,
        // которое игрок хочет направить в эту клетку
        final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(game, catchingCell);
        final int bonusAttack = getBonusAttackToCatchCell(player, game, catchingCell);
        if (!cellIsCatching(unitsCount + bonusAttack, unitsCountNeededToCatch)) {
            LoggerProcessor.printDebug(LOGGER, "The cell is not captured. The aggressor {} retreated ", player.getNickname());
            return;
        } // else
        catchCell(player, game, catchingCell, unitsCountNeededToCatch - bonusAttack);
        LoggerProcessor.printDebug(LOGGER, "Cell after catching: {} ", catchingCell);
    }

    /**
     * Метод получения числа юнитов, необходимых для захвата клетки
     *
     * @param game         - игра, хранящая всю метаинформацию
     * @param catchingCell - захватываемая клетка
     * @return число юнитов, необходимое для захвата клетки catchCell
     */
    private static int getUnitsCountNeededToCatchCell(final Game game, final Cell catchingCell) {
        final Player defendingPlayer = catchingCell.getOwn();
        int unitsCountNeededToCatch = catchingCell.getUnits().size();
        if (defendingPlayer != null && isNotNeutral(defendingPlayer, game)) { // если есть владелец (не нейтрал)
            unitsCountNeededToCatch++;
            for (final Feature feature : game.getFeaturesByRaceAndCellType(
                    defendingPlayer.getRace(), catchingCell.getType())
            ) { // Смотрим все особенности владельца

                if (feature.getType() == FeatureType.DEFENSE_CELL_CHANGING_UNITS_NUMBER) {
                    unitsCountNeededToCatch += ((CoefficientlyFeature) feature).getCoefficient();
                    LoggerProcessor.printDebug(LOGGER, "Player stumbled upon a defense of {} in cellType {} of defending player {}",
                            defendingPlayer.getRace(), catchingCell.getType(), defendingPlayer.getNickname());
                }
            }
        }
        LoggerProcessor.printDebug(LOGGER, "Units count needed to catch: {} ", unitsCountNeededToCatch);
        return unitsCountNeededToCatch;
    }

    /**
     * Метод получения бонуса атаки при захвате клетки
     *
     * @param player       - игрок-агрессор
     * @param game         - игра, хранящая всю метаинформацию
     * @param catchingCell - захватываемая клетка
     * @return бонус атаки (в числе юнитов) игрока player при захвате клетки catchCell
     */
    private static int getBonusAttackToCatchCell(final Player player, final Game game, final Cell catchingCell) {
        int bonusAttack = 0;
        for (final Feature feature : game.getFeaturesByRaceAndCellType(
                player.getRace(), catchingCell.getType())
        ) { // Смотрим все особенности агрессора

            if (feature.getType() == FeatureType.CATCH_CELL_CHANGING_UNITS_NUMBER) {
                bonusAttack += ((CoefficientlyFeature) feature).getCoefficient();
                LoggerProcessor.printDebug(LOGGER, "Player {} took advantage of the feature race {} and cellType of catchCell {}",
                        player.getNickname(), player.getRace(), catchingCell.getType());
            }
        }
        LoggerProcessor.printDebug(LOGGER, "Bonus attack: {} ", bonusAttack);
        return bonusAttack;
    }

    /**
     * Проверка на захватываемость клетки
     *
     * @param attackPower          - сила атаки на клетку
     * @param necessaryAttackPower - необходимая сила атаки на эту клетку для её захвата
     * @return true - если клетка захватываема, false - иначе
     */
    private static boolean cellIsCatching(final int attackPower, final int necessaryAttackPower) {
        return attackPower >= necessaryAttackPower;
    }

    /**
     * Захватить клетку
     *
     * @param player          - игрок-агрессор
     * @param game            - игра, хранящая всю метаинформацию
     * @param catchingCell    - захватываемая клетка
     * @param tiredUnitsCount - количество "уставших юнитов" (юнитов, которые перестанут быть доступными в этом раунде)
     */
    private static void catchCell(final Player player, final Game game, final Cell catchingCell,
                                  final int tiredUnitsCount) {
        removeFirstN(tiredUnitsCount, player.getAvailableUnits());
        final Player defendingPlayer = catchingCell.getOwn();
        if (defendingPlayer != null && isNotNeutral(defendingPlayer, game)) { // если есть владелец (не нейтрал)
            for (final Feature feature : game.getFeaturesByRaceAndCellType(
                    player.getRace(), catchingCell.getType())
            ) { // Смотрим все особенности агрессора

                if (feature.getType() == FeatureType.DEAD_UNITS_NUMBER_AFTER_CATCH_CELL) {
                    int deadUnitsCount = ((CoefficientlyFeature) feature).getCoefficient();
                    deadUnitsCount = Math.min(deadUnitsCount, defendingPlayer.getUnits().size());
                    removeFirstN(deadUnitsCount, defendingPlayer.getUnits());
                    LoggerProcessor.printDebug(LOGGER, "{} units of player {} died ", deadUnitsCount, defendingPlayer.getNickname());
                }
            }
            game.getFeudalToCells().get(defendingPlayer).remove(catchingCell);
        }
        catchingCell.setOwn(player);
        game.getFeudalToCells().get(player).add(catchingCell);
        LoggerProcessor.printInfo(LOGGER, "Cell {} catched of player {} ", catchingCell, player.getNickname());
    }

    /**
     * Является ли игрок нейтральным?
     *
     * @param player - игрок, про которого необходимо выяснить, является ли он нейтральным
     * @param game   - игра, хранящая всю метаинформацию
     * @return true - если игрок player не нейтрален в игре game, false - иначе
     */
    private static boolean isNotNeutral(final Player player, final Game game) {
        return player != game.getNeutralPlayer();
    }

    /**
     * Метод для распределения юнитов игроком
     *
     * @param player - игрок, делающий выбор
     * @param game   - игра, хранящая всю метаинформацию
     * @param random - объект для "бросания монетки" (взятия рандомного числа)
     */
    private static void distributionUnits(final Player player, final Game game, final Random random) {
        freeTransitCells(player);
        final List<Cell> controlledCells = getControlledCells(player, game);
        player.setAvailableUnits(player.getUnits()); // сделать все имеющиеся у игрока юнита доступными
        final List<Unit> availableUnits = player.getAvailableUnits();
        if (controlledCells.size() > 0) {
            while (availableUnits.size() > 0 && isSayYes(random)) {
                // Пока есть какие войска распределять и ответ "ДА" от игрока на вопрос: "Продолжить распределять войска?"

                final int numberOfCell = random.nextInt(controlledCells.size()); // номер клетки из списка,
                // в которую игрок хочет распределить войска
                final Cell protectedCell = controlledCells.get(numberOfCell); // клетка,
                // в которую игрок хочет распределить войска
                LoggerProcessor.printDebug(LOGGER, "Player {} protects the cell {}", player, protectedCell);
                final int unitsCount = random.nextInt(availableUnits.size()); // число юнитов,
                // которое игрок хочет распределить в эту клетку
                protectedCell.getUnits().addAll(availableUnits.subList(0, unitsCount)); // отправить первые unitsCount
                // доступных юнитов
                LoggerProcessor.printDebug(LOGGER, "Cell after defending: {} ", protectedCell);
                removeFirstN(unitsCount, availableUnits);
//                availableUnits.removeAll(availableUnits.subList(0, unitsCount)); // сделать их недоступными
            }
        }
        LoggerProcessor.printInfo(LOGGER, "Player {} distributed units ", player.getNickname());
    }

    /**
     * Освобождение игроком всех его транзитных клеток
     *
     * @param player - игрок, который должен освободить все свои транзитные клетки
     */
    private static void freeTransitCells(final Player player) {
        for (final Cell transitCell : player.getTransitCells()) {
            transitCell.getUnits().removeIf(unit -> player.getUnits().contains(unit));
            transitCell.setOwn(null);
        }
        player.getTransitCells().clear();
        LoggerProcessor.printDebug(LOGGER, "Player {} freed his transit cells ", player.getNickname());
    }

    /**
     * Взять подконтрольные игроку клетки
     *
     * @param player - игрок, чьи подконтрольные клетки мы хотим получить
     * @param game   - игра, хранящая всю метаинформацию
     * @return список подконтрольных игроку клеток
     */
    private static List<Cell> getControlledCells(final Player player, final Game game) {
        final List<Cell> controlledCells = new ArrayList<>(game.getFeudalToCells().get(player).size());
        // TODO Внимание вопрос: может добавить этот список в поля класса Game ???

        for (final Cell vassalCell : game.getFeudalToCells().get(player)) {
            if (vassalCell.getOwn() == player) {
                controlledCells.add(vassalCell);
            }
        }
        controlledCells.addAll(player.getTransitCells());

        LoggerProcessor.printDebug(LOGGER, "Controlled cells of player {} is {}", player.getNickname(), controlledCells.toString());
        return controlledCells;
    }

    /**
     * Обновить число монет у игрока
     *
     * @param player - игрок, чьё число монет необходимо обновить
     * @param game   - игра, хранящая всю метаинформацию
     */
    private static void updateCoinsCount(final Player player, final Game game) {
        for (final Cell cell : game.getFeudalToCells().get(player)) {
            updateCoinsCountByCellWithFeatures(player, game, cell);
            player.setCoins(player.getCoins() + cell.getType().getCoinYield());
        }
        LoggerProcessor.printDebug(LOGGER, "Player {} updated coins count", player.getNickname());
    }

    /**
     * Обновить число монет у игрока, учитывая только особенности одной клетки
     *
     * @param player - игрок, чьё число монет необходимо обновить
     * @param game   - игра, хранящая всю метаинформацию
     * @param cell   - клетка, чьи особенности мы рассматриваем
     */
    private static void updateCoinsCountByCellWithFeatures(final Player player, final Game game, final Cell cell) {
        final Map<CellType, Boolean> cellTypeMet = new HashMap<>(CellType.values().length);
        for (final CellType cellType : CellType.values()) {
            cellTypeMet.put(cellType, false);
        }
        for (final Feature feature : game.getFeaturesByRaceAndCellType(player.getRace(), cell.getType())) {
            if (feature.getType() == FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL) {
                player.setCoins(player.getCoins() + ((CoefficientlyFeature) feature).getCoefficient());
                LoggerProcessor.printDebug(LOGGER, "Player {} update coins by cellType {} ", player.getNickname(), cell.getType());
                continue;
            }
            if (feature.getType() == FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL_GROUP
                    && !cellTypeMet.get(cell.getType())) {
                cellTypeMet.put(cell.getType(), true);
                player.setCoins(player.getCoins() + ((CoefficientlyFeature) feature).getCoefficient());
                LoggerProcessor.printDebug(LOGGER, "Player {} update coins by group cellType {} ", player.getNickname(), cell.getType());
            }
        }
        LoggerProcessor.printDebug(LOGGER, "Player {} updated coins count by cell with features ", player.getNickname());
    }

    /**
     * Финализатор selfPlay'я. Выводит победителей в лог.
     *
     * @param playerList - список игроков.
     */
    private static void finalize(final List<Player> playerList) {
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

    /**
     * Удаление из списка list первых N элементов. Если N превышает размер списка, то список очищается
     *
     * @param N    - целое число
     * @param list - произвольный список
     * @param <T>  - любой параметр
     */
    private static <T> void removeFirstN(final int N, final List<T> list) {
        int i = 0;
        while (i < N && i < list.size()) {
            list.remove(0);
            i++;
        }
    }


    public static void main(final String[] args) {
        selfPlay();
    }
}
