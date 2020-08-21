package io.neolab.internship.coins.ai.vika.decision;

import io.neolab.internship.coins.ai.vika.decision.model.*;
import io.neolab.internship.coins.ai.vika.exception.AIBotException;
import io.neolab.internship.coins.ai.vika.exception.AIBotExceptionErrorCode;
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

import static io.neolab.internship.coins.ai.vika.decision.AIDecisionSimulationProcessor.*;
import static io.neolab.internship.coins.ai.vika.utils.AIDecisionMakerUtils.*;
import static io.neolab.internship.coins.ai.vika.utils.ExecutorServiceProcessor.completeExecutorService;
import static io.neolab.internship.coins.utils.RandomGenerator.chooseItemFromList;

/**
 * Симуляция принятия решений в ходе игры
 */
public class AIDecisionMaker {

    //глубина построения дерева (максимальное число раундов)
    private static final int MAX_DEPTH = 1;
    private static int roundTreeCreationCounter = 0;
    //игрок, относительно которого принимается решение
    private static int playerId = 0;
    //счетчик узлов дерева
    private static int nodesCounter = 0;

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
        nodesCounter = 0;
        LOGGER.info("PLAYER ID: {}", player.getId());
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
        nodesCounter = 0;
        LOGGER.info("PLAYER ID: {}", player.getId());
        return Objects.requireNonNull(executeBestChangeRaceDecision(player, game));
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
        nodesCounter = 0;
        LOGGER.info("PLAYER ID: {}", player.getId());
        return Objects.requireNonNull(executeBestCatchCellDecision(player, game));
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
        nodesCounter = 0;
        LOGGER.info("PLAYER ID: {}", player.getId());
        return Objects.requireNonNull(executeBestDistributionUnitsDecision(player, game));
    }

    /**
     * Функция-переход к построению следующего узла в дереве решений
     *
     * @param player       - игрок
     * @param game         - игра
     * @param decisionType - тип нужного решения
     * @return - информации о лучшем решении и соответствующем значении монет для этого решения
     * @throws AIBotException - тип решения не найден
     */
    private static DecisionAndWin getBestDecisionByGameTree(
            final Player player, final IGame game, @NotNull final DecisionType decisionType) throws AIBotException {
        nodesCounter++;
        LOGGER.info("Current node: {}", nodesCounter);
        LOGGER.info("DECISION TYPE: {}", decisionType);
        switch (decisionType) {
            case DECLINE_RACE: {
                return createDeclineRaceDecision(game, player);
            }
            case CHANGE_RACE: {
                return createChangeRaceDecision(game, player);
            }
            case CATCH_CELL: {
                return createCatchCellDecision(game, player);
            }
            case DISTRIBUTION_UNITS: {
                return createDistributionUnitsDecision(game, player);
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
        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
        final boolean[] declineRaceTypes = {true, false};
        for (final boolean declineRaceType : declineRaceTypes) {
            executorService.execute(() -> addDeclineRaceDecision(declineRaceType, decisionAndWins, player, game));
        }
        completeExecutorService(executorService);
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
                                                                     @NotNull final Player player) {
        final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
        final boolean[] declineRaceTypes = {true, false};
        for (final boolean declineRaceType : declineRaceTypes) {
            addDeclineRaceDecision(declineRaceType, decisionAndWins, player, game);
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
                                               final IGame game) {
        final Decision declineRaceDecision = new DeclineRaceDecision(declineRaceType);
        LOGGER.info("DECLINE RACE DECISION: {}", declineRaceDecision);
        try {
            final IGame gameCopy = game.getCopy();
            final Player playerCopy = getPlayerCopy(gameCopy, player.getId());
            simulateDeclineRaceDecision(playerCopy, gameCopy,
                    (DeclineRaceDecision) declineRaceDecision);
            if (declineRaceType) {
                final WinCollector winCollector = Objects.requireNonNull(
                        getBestDecisionByGameTree(playerCopy, gameCopy,
                                DecisionType.CHANGE_RACE)).getWinCollector();
                decisionAndWins.add(new DecisionAndWin(declineRaceDecision, winCollector));
            } else {
                final WinCollector winCollector = Objects.requireNonNull(
                        getBestDecisionByGameTree(playerCopy, gameCopy,
                                DecisionType.CATCH_CELL)).getWinCollector();
                decisionAndWins.add(new DecisionAndWin(declineRaceDecision, winCollector));
            }
            LOGGER.info("DECISION AND WINS: {}", decisionAndWins);
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
        final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
        final List<Race> availableRaces = game.getRacesPool();
        final ExecutorService executorService = Executors.newFixedThreadPool(availableRaces.size());
        availableRaces.forEach(race -> executorService.execute(
                () -> addChangeRaceDecision(race, decisionAndWins, player, game)
        ));
        completeExecutorService(executorService);
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
                                                                    @NotNull final Player player) {
        final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
        final List<Race> availableRaces = game.getRacesPool();
        availableRaces.forEach(race -> addChangeRaceDecision(race, decisionAndWins, player, game));
        return getBestDecision(decisionAndWins);
    }

    /**
     * Создает решение о смене расы
     *
     * @param race            - новая расв
     * @param decisionAndWins - содержит все решения на данном этапе построения дерева
     * @param player          - игрок
     * @param game            - текущее состояние игры
     */
    private static void addChangeRaceDecision(final Race race, final List<DecisionAndWin> decisionAndWins,
                                              final Player player, final IGame game) {
        final IGame gameCopy = game.getCopy();
        try {
            final Player playerCopy = getPlayerCopy(gameCopy, player.getId());
            final Decision changeRaceDecision = new ChangeRaceDecision(race);
            LOGGER.info("CHANGE RACE DECISION: {}", changeRaceDecision);
            simulateChangeRaceDecision(playerCopy, gameCopy, (ChangeRaceDecision) changeRaceDecision);
            if (roundTreeCreationCounter == 0) {
                final List<Player> players = gameCopy.getPlayers();
                //все игроки выбрали расу, можно приступать к игровому циклу
                if (getPlayerIndexFromGame(players, playerCopy.getId()) + 1 == players.size()) {
                    roundTreeCreationCounter++;
                    final WinCollector winCollector = Objects.requireNonNull(getBestDecisionByGameTree(
                            getNextPlayer(gameCopy, playerCopy.getId()),
                            gameCopy, DecisionType.DECLINE_RACE)).getWinCollector();
                    decisionAndWins.add(new DecisionAndWin(changeRaceDecision, winCollector));
                }
                if (getPlayerIndexFromGame(players, playerCopy.getId()) < players.size()) {
                    final WinCollector winCollector = Objects.requireNonNull(getBestDecisionByGameTree(
                            getNextPlayer(gameCopy, playerCopy.getId()), gameCopy,
                            DecisionType.CHANGE_RACE)).getWinCollector();
                    decisionAndWins.add(new DecisionAndWin(changeRaceDecision, winCollector));
                }
            } else {
                final WinCollector winCollector = Objects.requireNonNull(getBestDecisionByGameTree(
                        playerCopy, gameCopy, DecisionType.CATCH_CELL)).getWinCollector();
                decisionAndWins.add(new DecisionAndWin(changeRaceDecision, winCollector));
            }
            LOGGER.info("DECISION AND WINS: {}", decisionAndWins);
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
        final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
        final Set<Cell> achievableCells = new HashSet<>(game.getPlayerToAchievableCells().get(player));
        GameLoopProcessor.updateAchievableCells(player, game.getBoard(), achievableCells, game.getOwnToCells().get(player), false);
        LOGGER.info("CONTROLLED CELLS: {}", game.getOwnToCells().get(player));
        LOGGER.info("ACHIEVABLE CELLS SIZE: {}", achievableCells.size());
        final ExecutorService executorService = Executors.newFixedThreadPool(achievableCells.size() + 1);
        achievableCells.forEach(cell -> executorService.execute(() -> {
            if (checkCellCaptureOpportunity(cell, player, game)) {
                addCatchCellDecision(cell, decisionAndWins, player, game);
            }
        }));
        addCatchCellNullDecision(decisionAndWins, player, game);
        completeExecutorService(executorService);
        return getBestDecision(decisionAndWins).getDecision();
    }

    /**
     * Создает решения о захвате клетки
     *
     * @param game   - игра
     * @param player - игрок
     * @return - информации о лучшем решении и соответствующем значении монет для этого решения
     */
    private static @NotNull DecisionAndWin createCatchCellDecision(@NotNull final IGame game,
                                                                   @NotNull final Player player) {
        final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
        final Set<Cell> achievableCells = new HashSet<>(game.getPlayerToAchievableCells().get(player));
        GameLoopProcessor.updateAchievableCells(player, game.getBoard(), achievableCells,
                game.getOwnToCells().get(player), false);
        LOGGER.info("CONTROLLED CELLS: {}", game.getOwnToCells().get(player));
        LOGGER.info("ACHIEVABLE CELLS SIZE: {}", achievableCells.size());
        achievableCells.forEach(cell -> {
                    if (checkCellCaptureOpportunity(cell, player, game)) {
                        addCatchCellDecision(cell, decisionAndWins, player, game);
                    }
                }
        );
        addCatchCellNullDecision(decisionAndWins, player, game);
        LOGGER.info("DECISION AND WINS: {}", decisionAndWins);
        return getBestDecision(decisionAndWins);
    }

    /**
     * Создает решение о захвате клетки
     *
     * @param cell            - клетка для захвата
     * @param decisionAndWins - содержит все решения на данном этапе построения дерева
     * @param player          - игрок
     * @param game            - текущее состояние игры
     */
    private static void addCatchCellDecision(final Cell cell, final List<DecisionAndWin> decisionAndWins,
                                             final Player player, final IGame game) {
        final Position position = game.getBoard().getPositionByCell(cell);
        final List<Unit> unitsForCapture = new LinkedList<>(player.getUnitsByState(AvailabilityType.AVAILABLE));
        final Decision decision = new CatchCellDecision(new Pair<>(position, unitsForCapture));
        LOGGER.info("CATCH CELL DECISION: {}", decision);
        final IGame gameCopy = game.getCopy();
        try {
            final Player playerCopy = getPlayerCopy(game, player.getId());
            simulateCatchCellDecision(playerCopy, gameCopy, (CatchCellDecision) decision);
            final WinCollector winCollector = Objects.requireNonNull(
                    getBestDecisionByGameTree(playerCopy, gameCopy, DecisionType.CATCH_CELL)).getWinCollector();
            decisionAndWins.add(new DecisionAndWin(decision, winCollector));
        } catch (final AIBotException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создает решение о прекращении захвата клеток
     *
     * @param decisionAndWins - содержит все решения на данном этапе построения дерева
     * @param player          - игрок
     * @param game            - текущее состояние игры
     */
    private static void addCatchCellNullDecision(final List<DecisionAndWin> decisionAndWins, final Player player,
                                                 final IGame game) {
        final IGame gameCopy = game.getCopy();
        try {
            final Player playerCopy = getPlayerCopy(game, player.getId());
            final Decision decision = new CatchCellDecision(null);
            LOGGER.info("CATCH CELL NULL DECISION: {}", decision);
            final WinCollector winCollector = Objects.requireNonNull(getBestDecisionByGameTree(playerCopy, gameCopy,
                    DecisionType.DISTRIBUTION_UNITS)).getWinCollector();
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
        final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        LOGGER.info("Start create distribution units decisions...");
        LOGGER.info("CONTROLLED CELLS: {}", controlledCells);
        final List<Unit> playerUnits = new LinkedList<>();
        playerUnits.addAll(player.getUnitsByState(AvailabilityType.AVAILABLE));
        playerUnits.addAll(player.getUnitsByState(AvailabilityType.NOT_AVAILABLE));
        final List<List<Pair<Cell, Integer>>> combinations = getDistributionUnitsCombination(
                new LinkedList<>(controlledCells), playerUnits.size());
        LOGGER.info("DISTRIBUTION UNITS COMBINATIONS: {}", combinations);
        final ExecutorService executorService = Executors.newFixedThreadPool(combinations.size());
        for (final List<Pair<Cell, Integer>> combination : combinations) {
            executorService.execute(() -> {
                addDistributionUnitsDecision(combination, player, game, decisionAndWins, playerUnits);
            });
        }
        completeExecutorService(executorService);
        return getBestDecision(decisionAndWins).getDecision();
    }

    /**
     * Создает решения о перераспределении юнитов
     *
     * @param game   - игра
     * @param player - игрок
     * @return - информации о лучшем решении и соответствующем значении монет для этого решения
     */
    private static @NotNull DecisionAndWin createDistributionUnitsDecision(@NotNull final IGame game,
                                                                           @NotNull final Player player) {
        final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        LOGGER.info("Start create distribution units decisions...");
        LOGGER.info("CONTROLLED CELLS: {}", controlledCells);
        final List<Unit> playerUnits = new LinkedList<>();
        playerUnits.addAll(player.getUnitsByState(AvailabilityType.AVAILABLE));
        playerUnits.addAll(player.getUnitsByState(AvailabilityType.NOT_AVAILABLE));
        if (controlledCells.size() == 0) {
            LOGGER.info("NO CONTROLLED CELLS DISTRIBUTION");
            final List<DecisionAndWin> emptyDecisionList = new LinkedList<>();
            emptyDecisionList.add(new DecisionAndWin(new DistributionUnitsDecision(new HashMap<>()),
                    new WinCollector(player.getCoins())));
            return getBestDecision(emptyDecisionList);
        }
        final List<List<Pair<Cell, Integer>>> combinations = getDistributionUnitsCombination(
                new LinkedList<>(controlledCells), playerUnits.size());
        LOGGER.info("DISTRIBUTION UNITS COMBINATIONS: {}", combinations);
        for (final List<Pair<Cell, Integer>> combination : combinations) {
            LOGGER.info("DISTRIBUTION UNITS COMBINATION: {}", combination);
            addDistributionUnitsDecision(combination, player, game, decisionAndWins, playerUnits);
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
                                                     final List<Unit> playerUnits) {
        final IGame gameCopy = game.getCopy();
        try {
            final Player playerCopy = getPlayerCopy(gameCopy, player.getId());
            final Map<Position, List<Unit>> resolutions = new HashMap<>();
            combination
                    .forEach(cellUnitsAmountsPair
                            -> resolutions
                            .put(gameCopy.getBoard().getPositionByCell(cellUnitsAmountsPair.getFirst()),
                                    playerUnits.subList(0, cellUnitsAmountsPair.getSecond())));
            final Decision decision = new DistributionUnitsDecision(resolutions);
            LOGGER.info("DISTRIBUTION UNITS DECISION: {}", decision);
            simulateDistributionUnitsDecision((DistributionUnitsDecision) decision, playerCopy, game);
            updateDecisionNodeCoinsAmount(gameCopy, playerCopy);
            decisionAndWins.add(new DecisionAndWin(decision, new WinCollector(playerCopy.getCoins())));
                    /*if (isDecisionTreeCreationFinished(gameCopy, playerCopy.getId())) {
                        decisionAndWins.add(new DecisionAndWin(decision, new WinCollector(playerCopy.getCoins())));
                    } else {
                        roundTreeCreationCounter++;
                        final Player nextPlayer = getNextPlayer(gameCopy, playerCopy.getId());
                        final WinCollector winCollector = Objects.requireNonNull(getBestDecisionByGameTree(nextPlayer,
                                gameCopy, DecisionType.DECLINE_RACE)).getWinCollector();
                        decisionAndWins.add(new DecisionAndWin(decision, winCollector));
                    }*/
        } catch (final AIBotException e) {
            e.printStackTrace();
        }
    }

    /**
     * Выбирает решение, приносящее максимально число монет
     *
     * @param decisionAndWins - информации о решении и соответствующем значении монет для этого решения
     * @return - лучшее решение
     */
    private static DecisionAndWin getBestDecision(@NotNull final List<DecisionAndWin> decisionAndWins) {
        LOGGER.info("BEST DECISIONS: {}", decisionAndWins);
        LOGGER.info("BEST DECISIONS SIZE: {}", decisionAndWins.size());
        decisionAndWins.sort(Comparator.comparingInt(o -> o.getWinCollector().getCoinsAmount()));
        final int maxCoinsAmount = decisionAndWins.get(0).getWinCollector().getCoinsAmount();
        final List<DecisionAndWin> bestDecisions = decisionAndWins
                .stream()
                .filter(decisionTreeNode -> decisionTreeNode.getWinCollector().getCoinsAmount() == maxCoinsAmount)
                .collect(Collectors.toList());
        return chooseItemFromList(bestDecisions);
    }

    /**
     * Закончено ли построение всего дерева решений: последний игрок совершил последний ход
     *
     * @param game            - игра
     * @param currentPlayerId - id теущего игрока
     * @return - закончено ли построение дерева
     */
    private static boolean isDecisionTreeCreationFinished(final IGame game, final int currentPlayerId) {
        final List<Player> players = game.getPlayers();
        return /*MAX_DEPTH == roundTreeCreationCounter &&*/ players.get(players.size() - 1).getId() == currentPlayerId;
    }

    /**
     * Проверкяет, является ли игрок, совершающий ход, оппонентом
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
        final int currentPlayerIndex = getPlayerIndexFromGame(players, currentPlayerId);
        final int nextPlayerIndex = currentPlayerIndex + 1 == players.size() ? 0 : currentPlayerIndex + 1;
        if (nextPlayerIndex == 0) {
            roundTreeCreationCounter++;
        }
        return players.get(nextPlayerIndex);
    }

}
