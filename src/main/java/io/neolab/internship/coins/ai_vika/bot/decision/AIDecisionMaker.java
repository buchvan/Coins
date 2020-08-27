package io.neolab.internship.coins.ai_vika.bot.decision;

import io.neolab.internship.coins.ai_vika.bot.decision.model.*;
import io.neolab.internship.coins.ai_vika.bot.exception.AIBotException;
import io.neolab.internship.coins.ai_vika.bot.exception.AIBotExceptionErrorCode;
import io.neolab.internship.coins.ai_vika.bot.utils.AIDecisionMakerUtils;
import io.neolab.internship.coins.ai_vika.bot.utils.ExecutorServiceProcessor;
import io.neolab.internship.coins.client.bot.SimpleBot;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.service.GameLoopProcessor;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static io.neolab.internship.coins.ai_vika.bot.decision.AIDecisionSimulationProcessor.*;
import static io.neolab.internship.coins.ai_vika.bot.utils.AIDecisionMakerUtils.isCatchCellPossible;
import static io.neolab.internship.coins.utils.RandomGenerator.chooseItemFromList;

/**
 * Симуляция принятия решений в ходе игры
 */
public class AIDecisionMaker {

    //глубина построения дерева (максимальное число раундов)
    private static final int MAX_DEPTH = 1;
    //игрок, относительно которого принимается решение
    private static int playerId = 0;

    private static final int MAX_NODES_AMOUNT = 100;

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(AIDecisionMaker.class);

    /**
     * Возвращает лучшее решение об упадке расы боту
     *
     * @param player - игрок
     * @param game   - игра
     * @return - решение
     */
    public static Decision getDeclineRaceDecision(final Player player, final IGame game) {
        playerId = player.getId();
        //return Objects.requireNonNull(createDeclineRaceDecision(game, player, 0)).getDecision();
        return Objects.requireNonNull(executeBestDeclineRaceDecision(player, game));
    }

    /**
     * Возвращает лучшее решение о выборе новой расы боту
     *
     * @param player - игрок
     * @param game   - игра
     * @return - решение
     */
    public static Decision getChooseRaceDecision(final Player player, final IGame game) {
        playerId = player.getId();
        return Objects.requireNonNull(executeBestChangeRaceDecision(player, game));
        //return Objects.requireNonNull(createChangeRaceDecision(game, player, 0)).getDecision();
    }


    /**
     * Возвращает лучшее решение о захвате клетки боту
     *
     * @param player - игрок
     * @param game   - игра
     * @return - решение
     */
    public static Decision getChooseCaptureCellDecision(final Player player, final IGame game) {
        playerId = player.getId();
        return Objects.requireNonNull(executeBestCatchCellDecision(player, game));
        //return Objects.requireNonNull(createCatchCellDecision(game, player, 0)).getDecision();
    }

    /**
     * Возвращает лучшее решение о перераспределении юнитов боту
     *
     * @param player - игрок
     * @param game   - игра
     * @return - решение
     */
    public static Decision getDistributionUnitsDecision(final Player player, final IGame game) {
        playerId = player.getId();
        return Objects.requireNonNull(executeBestDistributionUnitsDecision(player, game));
        //return Objects.requireNonNull(createDistributionUnitsDecision(game, player, 0)).getDecision();
    }

    /**
     * Функция-переход к построению следующего узла в дереве решений
     *
     * @param player       - игрок
     * @param game         - игра
     * @param decisionType - тип нужного решения
     * @param currentDepth - текущая глубина дерева
     * @return - информации о лучшем решении и соответствующем значении монет для этого решения
     * @throws AIBotException - тип решения не найден
     */
    private static DecisionAndWin getBestDecisionByGameTree(final Player player, final IGame game,
                                                            @NotNull final DecisionType decisionType,
                                                            final int currentDepth) throws AIBotException {
        switch (decisionType) {
            case DECLINE_RACE: {
                return createDeclineRaceDecision(game, player, currentDepth);
            }
            case CHANGE_RACE: {
                return createChangeRaceDecision(game, player, currentDepth);
            }
            case CATCH_CELL: {
                return createCatchCellDecision(game, player, currentDepth);
            }
            case DISTRIBUTION_UNITS: {
                return createDistributionUnitsDecision(game, player, currentDepth);
            }
            default:
                throw new AIBotException(AIBotExceptionErrorCode.DECISION_NOT_EXISTS);
        }
    }

    /**
     * Создает дерево решений об упадке расы и возвращает лучшее, используя ExecutorService
     *
     * @param player - игрок
     * @param game   - текущее состояние игры
     * @return - лучшее решение
     */
    private static Decision executeBestDeclineRaceDecision(final Player player, final IGame game) {
        final int DECLINE_RACE_THREADS_AMOUNT = 2;
        final ExecutorService executorService = Executors.newFixedThreadPool(DECLINE_RACE_THREADS_AMOUNT);
        final List<DecisionAndWin> decisionAndWins = Collections.synchronizedList(new LinkedList<>());
        final boolean[] declineRaceTypes = {true, false};
        for (final boolean declineRaceType : declineRaceTypes) {
            final int currentDepth = 0;
            executorService.execute(() ->
                    addDeclineRaceDecision(declineRaceType, decisionAndWins, player, game, currentDepth));
        }
        ExecutorServiceProcessor.completeExecutorService(executorService);
        return getBestDecision(decisionAndWins).getDecision();
    }

    /**
     * Создает решения об упадке расы
     *
     * @param game   - игра
     * @param player - игрок
     * @return - информации о лучшем решении и соответствующем значении монет для этого решения
     */
    private static @NotNull DecisionAndWin createDeclineRaceDecision(@NotNull final IGame game,
                                                                     @NotNull final Player player,
                                                                     final int currentDepth) {
        final List<DecisionAndWin> decisionAndWins = Collections.synchronizedList(new LinkedList<>());
        final boolean[] declineRaceTypes = {true, false};
        for (final boolean declineRaceType : declineRaceTypes) {
            addDeclineRaceDecision(declineRaceType, decisionAndWins, player, game, currentDepth);
        }
        return getBestDecision(decisionAndWins);
    }


    /**
     * Создает решение об упадке расы
     *
     * @param declineRaceType - идем ли в упадок
     * @param decisionAndWins - содержит все решения на данном этапе построения дерева
     * @param player          - игрок
     * @param game            - текущее состояние игры
     */
    private static void addDeclineRaceDecision(final boolean declineRaceType,
                                               final List<DecisionAndWin> decisionAndWins, final Player player,
                                               final IGame game, final int currentDepth) {
        final Decision declineRaceDecision = new DeclineRaceDecision(declineRaceType);
        try {
            final IGame gameCopy = game.getCopy();
            final Player playerCopy = AIDecisionMakerUtils.getPlayerCopy(gameCopy, player.getId());
            simulateDeclineRaceDecision(playerCopy, gameCopy,
                    (DeclineRaceDecision) declineRaceDecision);
            if (declineRaceType) {
                final WinCollector winCollector = Objects.requireNonNull(
                        getBestDecisionByGameTree(playerCopy, gameCopy,
                                DecisionType.CHANGE_RACE, currentDepth)).getWinCollector();
                decisionAndWins.add(new DecisionAndWin(declineRaceDecision, winCollector));
            } else {
                final WinCollector winCollector = Objects.requireNonNull(
                        getBestDecisionByGameTree(playerCopy, gameCopy,
                                DecisionType.CATCH_CELL, currentDepth)).getWinCollector();
                decisionAndWins.add(new DecisionAndWin(declineRaceDecision, winCollector));
            }
        } catch (final AIBotException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создает дерево решений о выборе новой расы и возвращает лучшее, используя ExecutorService
     *
     * @param player - игрок
     * @param game   - текущее состояние игры
     * @return - лучшее решение
     */
    private static Decision executeBestChangeRaceDecision(final Player player, final IGame game) {
        final SimpleBot simpleBot = new SimpleBot();
        if (game.getCurrentRound() == 0) {
            return new ChangeRaceDecision(simpleBot.chooseRace(player, game));
        }
        final List<DecisionAndWin> decisionAndWins = Collections.synchronizedList(new LinkedList<>());
        final List<Race> availableRaces = game.getRacesPool();
        final int CHANGE_RACE_THREADS_AMOUNT = availableRaces.size();
        final ExecutorService executorService = Executors.newFixedThreadPool(CHANGE_RACE_THREADS_AMOUNT);
        availableRaces.forEach(race -> executorService.execute(() -> {
                    final int currentDepth = 0;
                    addChangeRaceDecision(race, decisionAndWins, player, game, currentDepth);
                }
        ));
        ExecutorServiceProcessor.completeExecutorService(executorService);
        return getBestDecision(decisionAndWins).getDecision();
    }

    /**
     * Создает решения о смене расы
     *
     * @param game   - игра
     * @param player - игрок
     * @return - информации о лучшем решении и соответствующем значении монет для этого решения
     */
    private static @NotNull DecisionAndWin createChangeRaceDecision(@NotNull final IGame game,
                                                                    @NotNull final Player player,
                                                                    final int currentDepth) {
        final List<DecisionAndWin> decisionAndWins = Collections.synchronizedList(new LinkedList<>());
        final List<Race> availableRaces = game.getRacesPool();
        availableRaces.forEach(race -> addChangeRaceDecision(race, decisionAndWins, player, game, currentDepth));
        return getBestDecision(decisionAndWins);
    }

    /**
     * Создает решение о смене расы
     *
     * @param race            - новая расв
     * @param decisionAndWins - содержит все решения на данном этапе построения дерева
     * @param player          - игрок
     * @param game            - текущее состояние игры
     * @param currentDepth    - текущая глубина дерева
     */
    private static void addChangeRaceDecision(final Race race, final List<DecisionAndWin> decisionAndWins,
                                              final Player player, final IGame game, final int currentDepth) {
        final IGame gameCopy = game.getCopy();
        try {
            final Player playerCopy = AIDecisionMakerUtils.getPlayerCopy(gameCopy, player.getId());
            final Decision changeRaceDecision = new ChangeRaceDecision(race);
            simulateChangeRaceDecision(playerCopy, gameCopy, (ChangeRaceDecision) changeRaceDecision);
            final WinCollector winCollector = Objects.requireNonNull(getBestDecisionByGameTree(
                    playerCopy, gameCopy, DecisionType.CATCH_CELL, currentDepth)).getWinCollector();
            decisionAndWins.add(new DecisionAndWin(changeRaceDecision, winCollector));
        } catch (final AIBotException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создает дерево решений о выборе клетки для захвата и возвращает лучшее, используя ExecutorService
     *
     * @param player - игрок
     * @param game   - текущее состояние игры
     * @return - лучшее решение
     */
    private static Decision executeBestCatchCellDecision(final Player player, final IGame game) {
        final List<DecisionAndWin> decisionAndWins = Collections.synchronizedList(new LinkedList<>());
        final Set<Cell> achievableCells = new HashSet<>(game.getPlayerToAchievableCells().get(player));
        GameLoopProcessor.updateAchievableCells(player, game.getBoard(), achievableCells,
                game.getOwnToCells().get(player), false);
        final int CATCH_CELL_THREADS_AMOUNT = achievableCells.size() + 1;
        final ExecutorService executorService = Executors.newFixedThreadPool(CATCH_CELL_THREADS_AMOUNT);
        achievableCells.forEach(cell -> executorService.execute(() -> {
            if (AIDecisionMakerUtils.checkCellCaptureOpportunity(cell, player, game)) {
                final int currentDepth = 0;
                addCatchCellDecision(cell, decisionAndWins, player, game, currentDepth);
            }
        }));
        final int currentDepth = 0;
        addCatchCellNullDecision(decisionAndWins, player, game, currentDepth);
        ExecutorServiceProcessor.completeExecutorService(executorService);
        return getBestDecision(decisionAndWins).getDecision();
    }

    /**
     * Создает решения о захвате клетки
     *
     * @param game         - игра
     * @param player       - игрок
     * @param currentDepth - текущая глубина дерева
     * @return - информации о лучшем решении и соответствующем значении монет для этого решения
     */
    private static @NotNull DecisionAndWin createCatchCellDecision(@NotNull final IGame game,
                                                                   @NotNull final Player player,
                                                                   final int currentDepth) {
        final List<DecisionAndWin> decisionAndWins = Collections.synchronizedList(new LinkedList<>());
        final Set<Cell> achievableCells = new HashSet<>(game.getPlayerToAchievableCells().get(player));
        GameLoopProcessor.updateAchievableCells(player, game.getBoard(), achievableCells,
                game.getOwnToCells().get(player), false);
        achievableCells.forEach(cell -> {
                    if (AIDecisionMakerUtils.checkCellCaptureOpportunity(cell, player, game)) {
                        addCatchCellDecision(cell, decisionAndWins, player, game, currentDepth);
                    }
                }
        );
        addCatchCellNullDecision(decisionAndWins, player, game, currentDepth);
        return getBestDecision(decisionAndWins);
    }

    /**
     * Создает решение о захвате клетки
     *
     * @param cell            - клетка для захвата
     * @param decisionAndWins - содержит все решения на данном этапе построения дерева
     * @param player          - игрок
     * @param game            - текущее состояние игры
     * @param currentDepth    - текущая глубина дерева
     */
    private static void addCatchCellDecision(final Cell cell, final List<DecisionAndWin> decisionAndWins,
                                             final Player player, final IGame game, final int currentDepth) {
        final Position position = game.getBoard().getPositionByCell(cell);
        final List<Unit> unitsForCapture = new LinkedList<>(player.getUnitsByState(AvailabilityType.AVAILABLE));
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<Cell> catchingCellNeighboringCells =
                new LinkedList<>(
                        Objects.requireNonNull(game.getBoard().getNeighboringCells(
                                Objects.requireNonNull(cell))));
        GameLoopProcessor.removeNotAvailableForCaptureUnits(game.getBoard(), unitsForCapture, catchingCellNeighboringCells,
                cell, controlledCells);
        if (isCatchCellPossible(cell, unitsForCapture, game, player)) {
            final Decision decision = new CatchCellDecision(new Pair<>(position, unitsForCapture));
            final IGame gameCopy = game.getCopy();
            try {
                final Player playerCopy = AIDecisionMakerUtils.getPlayerCopy(gameCopy, player.getId());
                simulateCatchCellDecision(playerCopy, gameCopy, (CatchCellDecision) decision);
                final WinCollector winCollector = Objects.requireNonNull(
                        getBestDecisionByGameTree(playerCopy, gameCopy, DecisionType.DISTRIBUTION_UNITS, currentDepth))
                        .getWinCollector();
                decisionAndWins.add(new DecisionAndWin(decision, winCollector));
            } catch (final AIBotException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Создает решение о прекращении захвата клеток
     *
     * @param decisionAndWins - содержит все решения на данном этапе построения дерева
     * @param player          - игрок
     * @param game            - текущее состояние игры
     * @param currentDepth    - текущая глубина дерева
     */
    private static void addCatchCellNullDecision(final List<DecisionAndWin> decisionAndWins, final Player player,
                                                 final IGame game, final int currentDepth) {
        final IGame gameCopy = game.getCopy();
        try {
            final Player playerCopy = AIDecisionMakerUtils.getPlayerCopy(gameCopy, player.getId());
            final Decision decision = new CatchCellDecision(null);
            final WinCollector winCollector = Objects.requireNonNull(getBestDecisionByGameTree(playerCopy, gameCopy,
                    DecisionType.DISTRIBUTION_UNITS, currentDepth)).getWinCollector();
            decisionAndWins.add(new DecisionAndWin(decision, winCollector));
        } catch (final AIBotException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создает дерево решений о перераспределении юнитов и возвращает лучшее, используя ExecutorService
     *
     * @param player - игрок
     * @param game   - текущее состояние игры
     * @return - лучшее решение
     */
    private static Decision executeBestDistributionUnitsDecision(final Player player, final IGame game) {
        final List<DecisionAndWin> decisionAndWins = Collections.synchronizedList(new LinkedList<>());
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        if (controlledCells.size() == 0) {
            final List<DecisionAndWin> emptyDecisionList = Collections.synchronizedList(new LinkedList<>());
            emptyDecisionList.add(new DecisionAndWin(new DistributionUnitsDecision(new HashMap<>()),
                    new WinCollector(player.getCoins())));
            return getBestDecision(emptyDecisionList).getDecision();
        }
        final Set<Unit> playerUnits = new HashSet<>();
        playerUnits.addAll(player.getUnitsByState(AvailabilityType.AVAILABLE));
        playerUnits.addAll(player.getUnitsByState(AvailabilityType.NOT_AVAILABLE));
        final List<List<Pair<Cell, Integer>>> combinations = AIDecisionMakerUtils.getDistributionUnitsCombination(
                new LinkedList<>(controlledCells), playerUnits.size());
        final int DISTRIBUTION_UNITS_THREADS_AMOUNT = combinations.size();
        final ExecutorService executorService = Executors.newFixedThreadPool(DISTRIBUTION_UNITS_THREADS_AMOUNT);
        for (final List<Pair<Cell, Integer>> combination : combinations) {
            executorService.execute(() -> {
                final int currentDepth = 0;
                addDistributionUnitsDecision(combination, player, game, decisionAndWins,
                        new LinkedList<>(playerUnits), currentDepth);
            });
        }
        ExecutorServiceProcessor.completeExecutorService(executorService);
        return getBestDecision(decisionAndWins).getDecision();
    }

    /**
     * Создает решения о перераспределении юнитов
     *
     * @param game         - игра
     * @param player       - игрок
     * @param currentDepth - текущая глубина дерева
     * @return - информации о лучшем решении и соответствующем значении монет для этого решения
     */
    private static @NotNull DecisionAndWin createDistributionUnitsDecision(@NotNull final IGame game,
                                                                           @NotNull final Player player,
                                                                           final int currentDepth) {
        final List<DecisionAndWin> decisionAndWins = Collections.synchronizedList(new LinkedList<>());
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        LOGGER.info("Start create distribution units decisions...");
        LOGGER.info("CONTROLLED CELLS SIZE: {}", controlledCells.size());
        final Set<Unit> playerUnits = new HashSet<>();
        playerUnits.addAll(player.getUnitsByState(AvailabilityType.AVAILABLE));
        playerUnits.addAll(player.getUnitsByState(AvailabilityType.NOT_AVAILABLE));
        if (controlledCells.size() == 0) {
            final List<DecisionAndWin> emptyDecisionList = Collections.synchronizedList(new LinkedList<>());
            emptyDecisionList.add(new DecisionAndWin(new DistributionUnitsDecision(new HashMap<>()),
                    new WinCollector(player.getCoins())));
            return getBestDecision(emptyDecisionList);
        }
        LOGGER.info("UNITS FOR DISTRIBUTION: {}", playerUnits.size());
        LOGGER.info("CELLS FOR DISTRIBUTION: {}", controlledCells.size());
        final List<List<Pair<Cell, Integer>>> combinations = AIDecisionMakerUtils.getDistributionUnitsCombination(
                new LinkedList<>(controlledCells), playerUnits.size());
        LOGGER.info("DISTRIBUTION UNITS COMBINATIONS: {}", combinations);
        for (final List<Pair<Cell, Integer>> combination : combinations) {
            LOGGER.info("DISTRIBUTION UNITS COMBINATION: {}", combination);
            addDistributionUnitsDecision(combination, player, game, decisionAndWins, new LinkedList<>(playerUnits), currentDepth);
        }
        LOGGER.info("DECISION AND WINS: {}", decisionAndWins);
        return getBestDecision(decisionAndWins);
    }

    /**
     * Создает решение о перераспределении юнитов
     *
     * @param combination     - комбинация перераспределения
     * @param decisionAndWins - содержит все решения на данном этапе построения дерева
     * @param player          - игрок
     * @param game            - текущее состояние игры
     */
    private static void addDistributionUnitsDecision(final List<Pair<Cell, Integer>> combination, final Player player,
                                                     final IGame game, final List<DecisionAndWin> decisionAndWins,
                                                     final List<Unit> playerUnits, final int currentDepth) {
        final IGame gameCopy = game.getCopy();
        final int units = combination.stream().mapToInt(Pair::getSecond).sum();
        LOGGER.info("UNITS AMOUNT IN COMBINATION: {}", units);
        LOGGER.info("UNITS AMOUNT IN PLAYER: {}", playerUnits.size());
        LOGGER.info("CORRECT COMBINATION: {}", units == playerUnits.size());
        final Player playerCopy = AIDecisionMakerUtils.getPlayerCopy(gameCopy, player.getId());
        final Map<Position, List<Unit>> resolutions = new HashMap<>();
        combination
                .forEach(cellUnitsAmountsPair
                        -> resolutions
                        .put(gameCopy.getBoard().getPositionByCell(cellUnitsAmountsPair.getFirst()),
                                playerUnits.subList(0, cellUnitsAmountsPair.getSecond())));
        final Decision decision = new DistributionUnitsDecision(resolutions);
        simulateDistributionUnitsDecision((DistributionUnitsDecision) decision, playerCopy, gameCopy);
        updateDecisionNodeCoinsAmount(gameCopy, playerCopy);
           /* LOGGER.info("CURRENT DEPTH: {}", currentDepth);
            LOGGER.info("IS CURRENT PLAYER: {}", playerCopy.getId() != playerId);
            LOGGER.info("IS FINISHED: {}", isDecisionTreeCreationFinished(currentDepth, playerCopy));
            if (isDecisionTreeCreationFinished(currentDepth, playerCopy)) {
                decisionAndWins.add(new DecisionAndWin(decision, new WinCollector(playerCopy.getCoins())));
            } else {
                final int newDepth = currentDepth + 1;
                final Player nextPlayer = getNextPlayer(gameCopy, playerCopy.getId());
                final WinCollector winCollector = Objects.requireNonNull(getBestDecisionByGameTree(nextPlayer,
                        gameCopy, DecisionType.DECLINE_RACE, newDepth)).getWinCollector();
                decisionAndWins.add(new DecisionAndWin(decision, winCollector));
          }*/
        decisionAndWins.add(new DecisionAndWin(decision, new WinCollector(playerCopy.getCoins())));
    }

    /**
     * Выбирает решение, приносящее максимально число монет
     *
     * @param decisionAndWins - информации о решении и соответствующем значении монет для этого решения
     * @return - лучшее решение
     */
    private static DecisionAndWin getBestDecision(@NotNull final List<DecisionAndWin> decisionAndWins) {
        decisionAndWins.sort(Comparator.comparingInt(o -> o.getWinCollector().getCoinsAmount()));
        final int maxCoinsAmount = decisionAndWins.get(decisionAndWins.size() - 1).getWinCollector().getCoinsAmount();
        final List<DecisionAndWin> bestDecisions = decisionAndWins
                .stream()
                .filter(decisionTreeNode -> decisionTreeNode.getWinCollector().getCoinsAmount() == maxCoinsAmount)
                .collect(Collectors.toList());
        return chooseItemFromList(bestDecisions);
    }

    /**
     * Закончено ли построение всего дерева решений: последний игрок совершил последний ход
     *
     * @param currentDepth - текущая глубина дерева
     * @param player       - текущий игрок, принимающий решение
     * @return - закончено ли построение дерева
     */
    private static boolean isDecisionTreeCreationFinished(final int currentDepth, final Player player) {
        return currentDepth >= MAX_DEPTH && player.getId() != playerId;
    }

    /**
     * Проверяет, является ли игрок, совершающий ход, оппонентом
     *
     * @param player - игрок для проверки
     * @return - оппонент/не оппонент
     */
    private static boolean isOpponent(@NotNull final Player player) {
        return player.getId() != playerId;
    }

    /**
     * Возвращает следующего игрока, который будет совершать ход
     *
     * @param game            - игра
     * @param currentPlayerId - текущий Id игрока
     * @return - следующий игрок
     */
    private static Player getNextPlayer(@NotNull final IGame game, final int currentPlayerId) {
        final List<Player> players = game.getPlayers();
        final int currentPlayerIndex = AIDecisionMakerUtils.getPlayerIndexFromGame(players, currentPlayerId);
        final int nextPlayerIndex = currentPlayerIndex + 1 == players.size() ? 0 : currentPlayerIndex + 1;
        return players.get(nextPlayerIndex);
    }
}
