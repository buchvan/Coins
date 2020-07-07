package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.Server;
import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.feature.CoefficientlyFeature;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.server.game.feature.FeatureType;
import io.neolab.internship.coins.utils.Pair;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class SelfPlay {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

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

        final Board board = new Board(positionToCellMap);
        LOGGER.debug("Board is created: " + board.toString());
        return board;
    }

    /**
     * Инициализация нейтрального игрока
     *
     * @return нейтрального игрока
     */
    private static Player createNeutralPlayer() {
        final Player neutralPlayer = new Player(0, "neutral");
        LOGGER.debug("Neutral player is created: " + neutralPlayer.toString());
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
        LOGGER.debug("Player list: " + playerList.toString());
        return playerList;
    }

    /**
     * Инициализация мапы с клетками, приносящими монеты игроку, по умолчанию
     *
     * @param playerList - список игроков
     * @return инициализированную feudalToCells
     */
    private static Map<Player, List<Cell>> initFeudalToCells(final List<Player> playerList) {
        final Map<Player, List<Cell>> feudalToCells = new HashMap<>(playerList.size());
        for (final Player player : playerList) {
            feudalToCells.put(player, new ArrayList<>());
        }
        LOGGER.debug("feudalToCells init: " + feudalToCells.toString());
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

        LOGGER.debug("raceCellTypeFeatures init: " + raceCellTypeFeatures.toString());
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
                                                      final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures,
                                                      final List<Feature> impossibleCatchCellFeature) {
        if (race == Race.MUSHROOM) { // Грибы
            addRaceCellTypeFeaturesByRaceMushroom(raceCellTypeFeatures, impossibleCatchCellFeature);
            return;
        }
        if (race == Race.AMPHIBIAN) { // Амфибии
            addRaceCellTypeFeaturesByRaceMushroom(raceCellTypeFeatures, impossibleCatchCellFeature);
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
            addRaceCellTypeFeaturesByRaceGnome(raceCellTypeFeatures, impossibleCatchCellFeature);
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

        final List<Feature> mushroomFeatures = new ArrayList<>();
        mushroomFeatures.add(new CoefficientlyFeature(FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL, 1));
        raceCellTypeFeatures.put(new Pair<>(Race.MUSHROOM, CellType.MUSHROOM), mushroomFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.MUSHROOM, CellType.WATER), impossibleCatchCellFeature);
        LOGGER.debug("Features of Mushroom race added: " + raceCellTypeFeatures.toString());
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

        LOGGER.debug("Features of Amphibian race added: " + raceCellTypeFeatures.toString());
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
        raceCellTypeFeatures.put(new Pair<>(Race.ELF, CellType.MUSHROOM), elfFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ELF, CellType.LAND), elfFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ELF, CellType.MOUNTAIN), elfFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ELF, CellType.WATER), impossibleCatchCellFeature);
        LOGGER.debug("Features of Elf race added: " + raceCellTypeFeatures.toString());
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
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.MUSHROOM), orcFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.LAND), orcFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.MOUNTAIN), orcFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.ORC, CellType.WATER), impossibleCatchCellFeature);
        LOGGER.debug("Features of Orc race added: " + raceCellTypeFeatures.toString());
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
        raceCellTypeFeatures.put(new Pair<>(Race.GNOME, CellType.MUSHROOM), gnomeFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.GNOME, CellType.LAND), gnomeFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.GNOME, CellType.MOUNTAIN), gnomeFeatures);
        raceCellTypeFeatures.put(new Pair<>(Race.GNOME, CellType.WATER), impossibleCatchCellFeature);
        LOGGER.debug("Features of Gnome race added: " + raceCellTypeFeatures.toString());
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

        LOGGER.debug("Features of Undead race added: " + raceCellTypeFeatures.toString());
    }

    /**
     * Создание доступного для игроков пула рас
     *
     * @return пул рас
     */
    private static List<Race> createRacesPool() {
        final List<Race> racesPool = new ArrayList<>(Race.values().length - 1);
        racesPool.addAll(Arrays.asList(Race.values()).subList(0, Race.values().length - 1));
        LOGGER.debug("Pool of races created: " + racesPool.toString());
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
        /* init */
        final Board board = initBoard();
        final Player neutralPlayer = createNeutralPlayer();
        final List<Player> playerList = initTestPlayers();
        final Map<Player, List<Cell>> feudalToCells = initFeudalToCells(playerList);
        final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures = initRaceCellTypeFeatures();
        final List<Race> racesPool = createRacesPool();

        final Game game = new Game(board, 0, feudalToCells, raceCellTypeFeatures, racesPool, playerList,
                neutralPlayer);
        LOGGER.debug("Game is created: " + game.toString());

        gameLoop(game);

        finalize(playerList);
    }

    /**
     * Игровой цикл, вся игровая логика
     *
     * @param game - игра, хранящая всю метаинформацию
     */
    private static void gameLoop(final Game game) {
        int currentRound = 0;
        final Random random = new Random();
        int lastUnitId = 0;
        while (currentRound <= ROUNDS_COUNT) {
            currentRound++;

            for (final Player player : game.getPlayers()) {
                lastUnitId = playerRound(currentRound, lastUnitId, player, game, random);
            }
            for (final Player player : game.getPlayers()) {
                updateCoinsCount(player, game);
            }
            LOGGER.debug("Game after" + currentRound + " round: " + game.toString());
        }
        LOGGER.info("Round " + currentRound + " is finished! Players: " + game.getPlayers().toString());
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
        if (currentRound == 1 || isSayYes(player, random)) {
            // В случае первого раунда, или вопроса: "Идти в упадок?"
            declineRace(player, game.getNeutralPlayer(), game.getFeudalToCells().get(player));
            lastUnitId = chooseRace(player, game.getRacesPool(), random, lastUnitId);
        }
        while (isSayYes(player, random)) { // Вопрос: "Захватить клетку?"
            catchCell(player, game, random);
        }
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
        LOGGER.info("Player " + player.getId() + " in decline of race!");
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
        player.setRace(newRace);
        LOGGER.info("Player " + player.getId() + " choose race " + newRace);

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
     * Метод для захватывания клетки игроком
     *
     * @param player - игрок, делающий выбор
     * @param game   - игра, хранящая всю метаинформацию
     * @param random - объект для "бросания монетки" (взятия рандомного числа)
     */
    private static void catchCell(final Player player, final Game game, final Random random) {
        final List<Cell> controlledCells = getAchievableCells(player, game);
        player.setAvailableUnits(player.getUnits()); // сделать все имеющиеся у игрока юнита доступными
        final List<Unit> availableUnits = player.getAvailableUnits();
        while (availableUnits.size() > 0 && isSayYes(player, random)) {
            // Пока есть какие войска распределять и ответ "ДА" от игрока на вопрос: "Продолжить распределять войска?"

            final int numberOfCell = random.nextInt(controlledCells.size());
            final Cell protectedCell = controlledCells.get(numberOfCell);
            LOGGER.info("Player " + player + " protects the cell " + protectedCell);
            final int unitsCount = random.nextInt(availableUnits.size());
            protectedCell.getUnits().addAll(availableUnits.subList(0, unitsCount));
            LOGGER.info("Cell after defending: " + protectedCell);
            availableUnits.removeAll(availableUnits.subList(0, unitsCount));
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
        final Board board = game.getBoard();
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
    private static List<Cell> boardEdgeGetCells(final Board board) {
        final Position tempPosition = new Position();
        final List<Cell> boardEdgeCells = new LinkedList<>();
        int strIndex = 0;
        int colIndex;
        while (strIndex < BOARD_SIZE_X) { // обход по верхней границе борды
            tempPosition.setX(strIndex);
            tempPosition.setY(0);
            boardEdgeCells.add(board.getPositionToCellMap().get(tempPosition));
            strIndex++;
        }
        strIndex--; // strIndex = BOARD_SIZE_X;
        colIndex = 0;
        while (colIndex < BOARD_SIZE_Y) { // обход по правой границе борды
            tempPosition.setX(strIndex);
            tempPosition.setY(colIndex);
            boardEdgeCells.add(board.getPositionToCellMap().get(tempPosition));
            colIndex++;
        }
        colIndex--; // colIndex = BOARD_SIZE_Y;
        while (strIndex >= 0) { // обход по нижней границе борды
            tempPosition.setX(strIndex);
            tempPosition.setY(colIndex);
            boardEdgeCells.add(board.getPositionToCellMap().get(tempPosition));
            strIndex--;
        }
        strIndex++; // strIndex = 0;
        while (colIndex >= 0) { // обход по левой границе борды
            tempPosition.setX(strIndex);
            tempPosition.setY(colIndex);
            boardEdgeCells.add(board.getPositionToCellMap().get(tempPosition));
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
    private static List<Cell> getAllNeighboringCells(final Board board, final Cell cell) {
        final Position tempPosition = new Position(); // чтобы каждый раз не создавать новый объект
        final List<Cell> neighboringCells = new LinkedList<>();
        int strIndex;
        int colIndex = -1;
        /* в целом это обход всех клеток единичного квадрата с центром в cell
        без обработки самого центра cell */
        while (colIndex <= 1) { // в общем это проход по строчкам (слева направо) снизу вверх.
            // То есть, сначала просматриваем нижнюю строчку слева направо, потом среднюю слева направо,
            // и в конце верхнюю также - слева направо
            strIndex = -1;
            tempPosition.setY(board.getPositionToCellMap().getKey(cell).getY() + colIndex);
            while (strIndex <= 1) {
                if (strIndex == 0 && colIndex == 0) { // если мы сейчас в центре единичного квадрата с центом в cell
                    continue;
                }
                tempPosition.setX(board.getPositionToCellMap().getKey(cell).getX() + strIndex);
                final Cell potentiallyAchievableCell = board.getPositionToCellMap().get(tempPosition);
                if (potentiallyAchievableCell == null) { // если вышли за пределы борды
                    continue;
                }
                neighboringCells.add(potentiallyAchievableCell);
                strIndex++;
            }
            colIndex++;
        }
        return neighboringCells;
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
        while (availableUnits.size() > 0 && isSayYes(player, random)) {
            // Пока есть какие войска распределять и ответ "ДА" от игрока на вопрос: "Продолжить распределять войска?"

            final int numberOfCell = random.nextInt(controlledCells.size());
            final Cell protectedCell = controlledCells.get(numberOfCell);
            LOGGER.debug("Player " + player + " protects the cell " + protectedCell);
            final int unitsCount = random.nextInt(availableUnits.size());
            protectedCell.getUnits().addAll(availableUnits.subList(0, unitsCount));
            LOGGER.debug("Cell after defending: " + protectedCell);
            availableUnits.removeAll(availableUnits.subList(0, unitsCount));
        }
        LOGGER.info("Player " + player.getId() + " distributed units");
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
        LOGGER.debug("Player " + player.getId() + " freed his transit cells");
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
        LOGGER.debug("Controlled cells of player " + player.getId() + " is " + controlledCells.toString());
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
        LOGGER.debug("Player " + player.getId() + " updated coins count");
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
        for (final Feature feature : game.getRaceCellTypeFeatures().get(new Pair<>(player.getRace(), cell.getType()))) {
            if (feature.getType() == FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL) {
                player.setCoins(player.getCoins() + ((CoefficientlyFeature) feature).getCoefficient());
                LOGGER.debug("Player " + player.getId() + " update coins by cellType " + cell.getType());
                continue;
            }
            if (feature.getType() == FeatureType.CHANGING_RECEIVED_COINS_NUMBER_FROM_CELL_GROUP
                    && !cellTypeMet.get(cell.getType())) {
                cellTypeMet.put(cell.getType(), true);
                player.setCoins(player.getCoins() + ((CoefficientlyFeature) feature).getCoefficient());
                LOGGER.debug("Player " + player.getId() + " update coins by group of cellType " + cell.getType());
            }
        }
        LOGGER.debug("Player " + player.getId() + " updated coins count by cell with features");
    }

    /**
     * Финализатор selfPlay'я. Выводит победителей в лог.
     *
     * @param playerList - список игроков.
     */
    private static void finalize(final List<Player> playerList) {
        final int maxCoinsCount = getMaxCoinsCount(playerList);
        if (maxCoinsCount == -1) {
            LOGGER.error("max count of coins < 0 !!!");
            return;
        }
        LOGGER.info("Winners: " + getWinners(maxCoinsCount, playerList).toString());
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
