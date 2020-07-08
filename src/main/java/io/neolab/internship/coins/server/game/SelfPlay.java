package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.*;
import io.neolab.internship.coins.server.game.feature.CoefficientlyFeature;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.feature.FeatureType;
import io.neolab.internship.coins.utils.Pair;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SelfPlay {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfPlay.class);

    private static final int ROUNDS_COUNT = 100;
    private static final int BOARD_SIZE_X = 3;
    private static final int BOARD_SIZE_Y = 4;

    /**
     * Инициализация и создание борды
     *
     * @return инициализированную борду
     */
    private static Board initBoard() {
        final BidiMap<Position, Cell> positionToCellMap = new DualHashBidiMap<>();
        int cellId = 0;

        positionToCellMap.put(new Position(0, 0), new Cell(cellId++, CellType.MUSHROOM));
        positionToCellMap.put(new Position(0, 1), new Cell(cellId++, CellType.LAND));
        positionToCellMap.put(new Position(0, 2), new Cell(cellId++, CellType.WATER));
        positionToCellMap.put(new Position(0, 3), new Cell(cellId++, CellType.MOUNTAIN));

        positionToCellMap.put(new Position(1, 0), new Cell(cellId++, CellType.MOUNTAIN));
        positionToCellMap.put(new Position(1, 1), new Cell(cellId++, CellType.WATER));
        positionToCellMap.put(new Position(1, 2), new Cell(cellId++, CellType.LAND));
        positionToCellMap.put(new Position(1, 3), new Cell(cellId++, CellType.MUSHROOM));

        positionToCellMap.put(new Position(2, 0), new Cell(cellId++, CellType.LAND));
        positionToCellMap.put(new Position(2, 1), new Cell(cellId++, CellType.WATER));
        positionToCellMap.put(new Position(2, 2), new Cell(cellId++, CellType.MUSHROOM));
        positionToCellMap.put(new Position(2, 3), new Cell(cellId, CellType.MOUNTAIN)); // without increment !!!

        final Board board = new Board(positionToCellMap);
        printDebug("Board is created: {} ", board);
        return board;
    }

    /**
     * Инициализация нейтрального игрока
     *
     * @return нейтрального игрока
     */
    private static Player createNeutralPlayer() {
        final Player neutralPlayer = new Player(0, "neutral");
        printDebug("Neutral player is created: {} ", neutralPlayer);
        return neutralPlayer;
    }

    /**
     * Инициализация тестовых игроков
     *
     * @return список тестовых игроков
     */
    private static List<Player> initTestPlayers() {
        final List<Player> playerList = new LinkedList<>(Arrays.asList(new Player(1, "kvs"),
                new Player(2, "bim")));
        printDebug("Player list: {} ", playerList);
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
        printDebug("feudalToCells init: {} ", feudalToCells);
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

        printDebug("raceCellTypeFeatures init: {} ", raceCellTypeFeatures);
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

        printDebug("Features of Mushroom race added: {} ", raceCellTypeFeatures);
    }

    /**
     * Добавление особенностей для амфибий
     *
     * @param raceCellTypeFeatures       - мапа, которую нужно обновить
     * @param impossibleCatchCellFeature - список из одного свойства невозможности захвата клетки
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

        printDebug("Features of Amphibian race added: {} ", raceCellTypeFeatures);
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

        printDebug("Features of Elf race added: {} ", raceCellTypeFeatures);
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

        printDebug("Features of Orc race added: {} ", raceCellTypeFeatures);
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

        printDebug("Features of Gnome race added: {} ", raceCellTypeFeatures);
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

        printDebug("Features of Undead race added: {} ", raceCellTypeFeatures);
    }

    /**
     * Создание доступного для игроков пула рас
     *
     * @return пул рас
     */
    private static List<Race> createRacesPool() {
        final List<Race> racesPool = new ArrayList<>(Race.values().length - 1);
        racesPool.addAll(Arrays.asList(Race.values()).subList(0, Race.values().length - 1));
        printDebug("Pool of races created: {} ", racesPool);
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
            final Board board = initBoard();
            final Player neutralPlayer = createNeutralPlayer();
            final List<Player> playerList = initTestPlayers();
            final Map<Player, List<Cell>> feudalToCells = initFeudalToCells(playerList);
            final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures = initRaceCellTypeFeatures();
            final List<Race> racesPool = createRacesPool();

            final Game game = new Game(board, 0, feudalToCells, raceCellTypeFeatures, racesPool, playerList,
                    neutralPlayer);
            printDebug("Game is created: {} ", game);

            gameLoop(game);

            finalize(playerList);
        } catch (final Exception exception) {
            printError("ERROR!!! ", exception);
        }
    }

    /**
     * Игровой цикл, вся игровая логика начинается отсюда
     *
     * @param game - игра, хранящая всю метаинформацию
     */
    private static void gameLoop(final Game game) {
        final Random random = new Random();
        int lastUnitId = 0;
        while (game.getCurrentRound() < ROUNDS_COUNT) {
            game.setCurrentRound(game.getCurrentRound() + 1);

            for (final Player player : game.getPlayers()) {
                lastUnitId = playerRound(game.getCurrentRound(), lastUnitId, player, game, random);
            }
            for (final Player player : game.getPlayers()) {
                updateCoinsCount(player, game);
            }
            printDebug("Game after {} round: {}", game.getCurrentRound(), game);
        }
    }

    /**
     * Раунд в исполнении игрока
     *
     * @param currentRound - номер текущего раунда
     * @param lastUnitId   - id последнего созданного юнита
     * @param player       - игрок, который исполняет раунд
     * @param game         - игра, хранящая всю метаинформацию
     * @param random       - объект для "бросания монетки" (взятия рандомного числа)
     * @return id последнего созданного юнита
     */
    private static int playerRound(final int currentRound, int lastUnitId, final Player player, final Game game,
                                   final Random random) {
        player.setAvailableUnits(player.getUnits()); // доступными юнитами становятся все имеющиеся у игрока юниты
        if (currentRound == 1) { // В случае первого раунда
            lastUnitId = chooseRace(player, game.getRacesPool(), random, lastUnitId);
        } else if (isSayYes(player, random)) { // В случае ответа "ДА" от игрока на вопрос: "Идти в упадок?"
            declineRace(player, game.getNeutralPlayer(), game.getFeudalToCells().get(player));
            lastUnitId = chooseRace(player, game.getRacesPool(), random, lastUnitId);
        }
        catchCells(player, game, random);
        distributionUnits(player, game, random); // Распределение войск
        return lastUnitId;
    }

    /**
     * Метод для определения сказанного слова игроком
     *
     * @param player - игрок, делающий выбор
     * @param random - объект для "бросания монетки" (взятия рандомного числа)
     * @return true - если игрок сказал да, false - нет
     */
    private static boolean isSayYes(final Player player, final Random random) {
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
        printInfo("Player " + player.getId() + " in decline of race!");
    }

    /**
     * Метод для выбора новой расы игроком
     *
     * @param player     - игрок, выбирающий новую расу
     * @param racesPool  - пул всех доступных рас
     * @param random     - объект для "бросания монетки" (взятия рандомного числа)
     * @param lastUnitId - id последнего созданного юнита
     * @return id последнего созданного юнита
     */
    private static int chooseRace(final Player player, final List<Race> racesPool, final Random random,
                                  int lastUnitId) {
        final Race newRace = racesPool.get(random.nextInt(racesPool.size()));
        if (player.getRace() != null) {
            racesPool.add(player.getRace());
        }
        racesPool.remove(newRace);
        player.setRace(newRace);
        printInfo("Player " + player.getId() + " choose race " + newRace);

        /* Добавляем юниты выбранной расы */
        player.setUnits(new ArrayList<>(newRace.getUnitsAmount()));
        int i = 0;
        while (i < newRace.getUnitsAmount()) {
            player.getUnits().add(new Unit(++lastUnitId));
            i++;
        }
        return lastUnitId;
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
            while (availableUnits.size() > 0 && isSayYes(player, random)) {
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
        printDebug("Player {} catch attempt the cell {} ", player.getId(), catchingCell);
        final int unitsCount = random.nextInt(player.getAvailableUnits().size()); // число юнитов,
        // которое игрок хочет направить в эту клетку
        final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(game, catchingCell);
        final int bonusAttack = getBonusAttackToCatchCell(player, game, catchingCell);
        if (!cellIsCatching(unitsCount + bonusAttack, unitsCountNeededToCatch)) {
            printDebug("The cell is not captured. The aggressor {} retreated ", player.getId());
            return;
        } // else
        catchCell(player, game, catchingCell, unitsCountNeededToCatch - bonusAttack);
        printDebug("Cell after catching: {} ", catchingCell);
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
                    printDebug("Player stumbled upon a defense of {} in cellType {} of defending player {}",
                            defendingPlayer.getRace(), catchingCell.getType(), defendingPlayer.getId());
                }
            }
        }
        printDebug("Units count needed to catch: {} ", unitsCountNeededToCatch);
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
                printDebug("Player {} took advantage of the feature race {} and cellType of catchCell {}",
                        player.getId(), player.getRace(), catchingCell.getType());
            }
        }
        printDebug("Bonus attack: {} ", bonusAttack);
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
                    printDebug("{} units of player {} died ", deadUnitsCount, defendingPlayer.getId());
                }
            }
            game.getFeudalToCells().get(defendingPlayer).remove(catchingCell);
        }
        catchingCell.setOwn(player);
        game.getFeudalToCells().get(player).add(catchingCell);
        printInfo("Cell {} catched of player {} ", catchingCell, player.getId());
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
            while (availableUnits.size() > 0 && isSayYes(player, random)) {
                // Пока есть какие войска распределять и ответ "ДА" от игрока на вопрос: "Продолжить распределять войска?"

                final int numberOfCell = random.nextInt(controlledCells.size()); // номер клетки из списка,
                // в которую игрок хочет распределить войска
                final Cell protectedCell = controlledCells.get(numberOfCell); // клетка,
                // в которую игрок хочет распределить войска
                printDebug("Player {} protects the cell {}", player, protectedCell);
                final int unitsCount = random.nextInt(availableUnits.size()); // число юнитов,
                // которое игрок хочет распределить в эту клетку
                protectedCell.getUnits().addAll(availableUnits.subList(0, unitsCount)); // отправить первые unitsCount
                // доступных юнитов
                printDebug("Cell after defending: {} ", protectedCell);
                removeFirstN(unitsCount, availableUnits);
//                availableUnits.removeAll(availableUnits.subList(0, unitsCount)); // сделать их недоступными
            }
        }
        printInfo("Player {} distributed units ", player.getId());
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
        printDebug("Player {} freed his transit cells ", player.getId());
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

        printDebug("Controlled cells of player {} is {}", player.getId(), controlledCells.toString());
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
        printDebug("Player {} updated coins count", player.getId());
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
                printDebug("Player {} update coins by cellType {} ", player.getId(), cell.getType());
                continue;
            }
            if (feature.getType() == FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL_GROUP
                    && !cellTypeMet.get(cell.getType())) {
                cellTypeMet.put(cell.getType(), true);
                player.setCoins(player.getCoins() + ((CoefficientlyFeature) feature).getCoefficient());
                printDebug("Player {} update coins by group cellType {} ", player.getId(), cell.getType());
            }
        }
        printDebug("Player {} updated coins count by cell with features ", player.getId());
    }

    /**
     * Финализатор selfPlay'я. Выводит победителей в лог.
     *
     * @param playerList - список игроков.
     */
    private static void finalize(final List<Player> playerList) {
        final int maxCoinsCount = getMaxCoinsCount(playerList);
        if (maxCoinsCount == -1) {
            printError("max count of coins < 0 !!!");
            return;
        }
        final List<Player> winners = getWinners(maxCoinsCount, playerList);
        printInfo("---------------------------------------");
        printInfo("Game OVER !!!");
        printInfo("Winners: ");
        for (final Player winner : winners) {
            printInfo("Player {} - coins {} ", winner.getId(), winner.getCoins());
        }
        printInfo("***************************************");
        printInfo("Results of other players: ");
        for (final Player player : playerList) {
            if (winners.contains(player)) {
                continue;
            }
            printInfo("Player {} - coins {} ", player.getId(), player.getCoins());
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

    /**
     * Выводит лог уровня INFO
     *
     * @param message - сообщение, которое нужно вывести
     * @param objects - объекты, метаинформация о которых необходима в логе
     */
    private static void printInfo(final String message, final Object... objects) {
        LOGGER.info(message, objects);
    }

    /**
     * Выводит лог уровня DEBUG
     *
     * @param message - сообщение, которое нужно вывести
     * @param objects - объекты, метаинформация о которых необходима в логе
     */
    private static void printDebug(final String message, final Object... objects) {
        LOGGER.debug(message, objects);
    }

    /**
     * Выводит лог уровня ERROR
     *
     * @param message - сообщение, которое нужно вывести
     */
    private static void printError(final String message) {
        LOGGER.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LOGGER.error(message);
    }

    /**
     * Выводит лог уровня ERROR
     *
     * @param message   - сообщение, которое нужно вывести
     * @param exception - исключение, метаинформация о котором необходима в логе
     */
    private static void printError(final String message, final Exception exception) {
        LOGGER.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LOGGER.error(message, exception);
    }

    public static void main(final String[] args) {
        selfPlay();
    }
}
