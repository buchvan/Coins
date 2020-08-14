package io.neolab.internship.coins.ai.vika.decision;

import io.neolab.internship.coins.ai.vika.decision.model.*;
import io.neolab.internship.coins.ai.vika.exception.AIBotException;
import io.neolab.internship.coins.ai.vika.exception.AIBotExceptionErrorCode;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.neolab.internship.coins.ai.vika.decision.AIDecisionSimulationProcessor.*;
import static io.neolab.internship.coins.server.service.GameLoopProcessor.getBonusAttackToCatchCell;
import static io.neolab.internship.coins.server.service.GameLoopProcessor.getUnitsCountNeededToCatchCell;
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

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(AIDecisionMaker.class);

    /**
     * Возвращает лучшее решение об упадке расы боту
     *
     * @param player - игрок
     * @param game   - игра
     * @return - решение
     * @throws AIBotException
     */
    public static Decision getDeclineRaceDecision(final Player player, final IGame game) throws AIBotException {
        playerId = player.getId();
        return Objects.requireNonNull(getBestDecisionByGameTree(player, game, DecisionType.DECLINE_RACE)).getDecision();
    }

    /**
     * Возвращает лучшее решение о выборе новой расы боту
     *
     * @param player - игрок
     * @param game   - игра
     * @return - решение
     * @throws AIBotException
     */
    public static Decision getChooseRaceDecision(final Player player, final IGame game) throws AIBotException {
        playerId = player.getId();
        return Objects.requireNonNull(getBestDecisionByGameTree(player, game, DecisionType.CHANGE_RACE)).getDecision();
    }


    /**
     * Возвращает лучшее решение о захвате клетки боту
     *
     * @param player - игрок
     * @param game   - игра
     * @return - решение
     * @throws AIBotException
     */
    public static Decision getChooseCaptureCellDecision(final Player player, final IGame game) throws AIBotException {
        playerId = player.getId();
        return Objects.requireNonNull(getBestDecisionByGameTree(player, game, DecisionType.CATCH_CELL)).getDecision();
    }

    /**
     * Возвращает лучшее решение о перераспределении юнитов боту
     *
     * @param player - игрок
     * @param game   - игра
     * @return - решение
     * @throws AIBotException
     */
    public static Decision getDistributionUnitsDecision(final Player player, final IGame game) throws AIBotException {
        playerId = player.getId();
        return Objects.requireNonNull
                (getBestDecisionByGameTree(player, game, DecisionType.DISTRIBUTION_UNITS)).getDecision();
    }

    /**
     * Возвращет лучшее решение по построенному дереву решений
     *
     * @param player       - игрок
     * @param game         - игра
     * @param decisionType - тип нужного решения
     * @return - информации о лучшем решении и соответствующем значении монет для этого решения
     * @throws AIBotException
     */
    private static DecisionAndWin getBestDecisionByGameTree(
            final Player player, final IGame game, @NotNull final DecisionType decisionType) throws AIBotException {
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
     * Создает решения об упадке расы
     *
     * @param game   - игра
     * @param player - игрок
     * @return - информации о лучшем решении и соответствующем значении монет для этого решения
     */
    private static @NotNull DecisionAndWin createDeclineRaceDecision(@NotNull final IGame game, @NotNull final Player player) {
        final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
        final boolean[] declineRaceTypes = {true, false};
        for (final boolean declineRaceType : declineRaceTypes) {
            final ExecutorService executorService = Executors.newFixedThreadPool(2);
            executorService.execute(() -> {
                final Decision declineRaceDecision = new DeclineRaceDecision(declineRaceType);
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
                } catch (final AIBotException e) {
                    e.printStackTrace();
                }
            });
            executorService.shutdown();
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (isOpponent(player)) {
            //отдельная целевая функция
            return getBestDecision(decisionAndWins);
        }
        return getBestDecision(decisionAndWins);
    }

    /**
     * Создает решения о смене расы
     *
     * @param game   - игра
     * @param player - игрок
     * @return - информации о лучшем решении и соответствующем значении монет для этого решения
     */
    private static @NotNull DecisionAndWin createChangeRaceDecision(@NotNull final IGame game, @NotNull final Player player) {
        final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
        final List<Race> availableRaces = game.getRacesPool();
        final ExecutorService executorService = Executors.newFixedThreadPool(availableRaces.size());
        availableRaces.forEach(race -> executorService.execute(() -> {
            final IGame gameCopy = game.getCopy();
            try {
                final Player playerCopy = getPlayerCopy(gameCopy, player.getId());
                final Decision changeRaceDecision = new ChangeRaceDecision(race);
                simulateChangeRaceDecision(playerCopy, gameCopy, (ChangeRaceDecision) changeRaceDecision);
                if (gameCopy.getCurrentRound() == 0) {
                    final WinCollector winCollector = Objects.requireNonNull(getBestDecisionByGameTree(
                            playerCopy, gameCopy, DecisionType.DECLINE_RACE)).getWinCollector();
                    decisionAndWins.add(new DecisionAndWin(changeRaceDecision, winCollector));
                } else {
                    final WinCollector winCollector = Objects.requireNonNull(getBestDecisionByGameTree(
                            playerCopy, gameCopy, DecisionType.CATCH_CELL)).getWinCollector();
                    decisionAndWins.add(new DecisionAndWin(changeRaceDecision, winCollector));
                }
            } catch (final AIBotException e) {
                e.printStackTrace();
            }
        }));
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        if (isOpponent(player)) {
            //отдельная целевая функция
            return getBestDecision(decisionAndWins);
        }
        return getBestDecision(decisionAndWins);
    }

    /**
     * Создает решения о захвате клетки
     *
     * @param game   - игра
     * @param player - игрок
     * @return - информации о лучшем решении и соответствующем значении монет для этого решения
     */
    private static @NotNull DecisionAndWin createCatchCellDecision(@NotNull final IGame game, @NotNull final Player player) {
        final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
        final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
        // + 1 for null catch cell decision
        final ExecutorService executorService = Executors.newFixedThreadPool(
                achievableCells.size() + 1);
        achievableCells.forEach(cell -> executorService.execute(() -> {
            if (checkCellCaptureOpportunity(cell, player, game)) {
                final Position position = game.getBoard().getPositionByCell(cell);
                final List<Unit> unitsForCapture = player.getUnitsByState(AvailabilityType.AVAILABLE);
                final Decision decision = new CatchCellDecision(new Pair<>(position, unitsForCapture));
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
        }));
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        executorService.execute(() -> {
            final IGame gameCopy = game.getCopy();
            try {
                final Player playerCopy = getPlayerCopy(game, player.getId());
                final Decision decision = new CatchCellDecision(null);
                simulateCatchCellDecision(playerCopy, gameCopy, (CatchCellDecision) decision);
                final WinCollector winCollector = Objects.requireNonNull(getBestDecisionByGameTree(playerCopy, gameCopy,
                        DecisionType.DISTRIBUTION_UNITS)).getWinCollector();
                decisionAndWins.add(new DecisionAndWin(decision, winCollector));
            } catch (final AIBotException e) {
                e.printStackTrace();
            }
        });
        if (isOpponent(player)) {
            //отдельная целевая функция
            return getBestDecision(decisionAndWins);
        }
        return getBestDecision(decisionAndWins);
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
        final List<Unit> playerUnits = new LinkedList<>();
        playerUnits.addAll(player.getUnitsByState(AvailabilityType.AVAILABLE));
        playerUnits.addAll(player.getUnitsByState(AvailabilityType.NOT_AVAILABLE));
        final List<List<Pair<Cell, Integer>>> combinations = getDistributionUnitsCombination(
                new LinkedList<>(controlledCells), playerUnits.size());
        for (final List<Pair<Cell, Integer>> combination : combinations) {
            final ExecutorService executorService = Executors.newFixedThreadPool(combination.size());
            executorService.execute(() -> {
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
                    simulateDistributionUnitsDecision((DistributionUnitsDecision) decision, playerCopy, game);
                    updateDecisionNodeCoinsAmount(gameCopy, playerCopy);
                    if (isDecisionTreeCreationFinished(gameCopy, playerCopy.getId())) {
                        decisionAndWins.add(new DecisionAndWin(decision, new WinCollector(playerCopy.getCoins())));
                    } else {
                        final Player nextPlayer = getNextPlayer(gameCopy, playerCopy.getId());
                        final WinCollector winCollector = Objects.requireNonNull(getBestDecisionByGameTree(nextPlayer,
                                gameCopy, DecisionType.DECLINE_RACE)).getWinCollector();
                        decisionAndWins.add(new DecisionAndWin(decision, winCollector));
                    }
                } catch (final AIBotException e) {
                    e.printStackTrace();
                }
            });
            executorService.shutdown();
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (isOpponent(player)) {
            //отдельная целевая функция
            return getBestDecision(decisionAndWins);
        }
        return getBestDecision(decisionAndWins);
    }

    /**
     * Выбирает решение, приносящее максимально число монет
     *
     * @param decisionAndWins - информации о решении и соответствующем значении монет для этого решения
     * @return - лучшее решение
     */
    private static DecisionAndWin getBestDecision(@NotNull final List<DecisionAndWin> decisionAndWins) {
        LOGGER.info("BEST DECISIONS: {}", decisionAndWins);
        decisionAndWins.sort(Comparator.comparingInt(o -> o.getWinCollector().getCoinsAmount()));
        final int maxCoinsAmount = decisionAndWins.get(0).getWinCollector().getCoinsAmount();
        final List<DecisionAndWin> bestDecisions = decisionAndWins
                .stream()
                .filter(decisionTreeNode -> decisionTreeNode.getWinCollector().getCoinsAmount() == maxCoinsAmount)
                .collect(Collectors.toList());
        return chooseItemFromList(bestDecisions);
    }

    /**
     * Проверка возможности захвата клетки
     *
     * @param cell   -клетка
     * @param player - текущий игрок
     * @param game   - текущее состояние игры
     * @return - возможность захвата клетки
     */
    private static boolean checkCellCaptureOpportunity(@NotNull final Cell cell, @NotNull final Player player,
                                                       @NotNull final IGame game) {
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<Unit> playerAvailableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        if (controlledCells.contains(cell)) {
            return playerAvailableUnits.size() >= cell.getType().getCatchDifficulty();
        }
        final GameFeatures features = game.getGameFeatures();
        return playerAvailableUnits.size() >=
                getUnitsCountNeededToCatchCell(features, cell)
                        + getBonusAttackToCatchCell(player, features, cell);
    }

    /**
     * Рекурсивная функция, которая возвращает всевозможные комбинации для клеток и количества юнитов,
     * которые туда отправляются
     *
     * @param cellForDistribution  - клетки, для которых нужно решить сколько юнитов туда отправлять
     * @param remainingUnitsAmount - оставшееся количество юнитов
     * @return - всевозможные комбинации клетка->количество юнитов для нее
     */
    @NotNull
    private static List<List<Pair<Cell, Integer>>> getDistributionUnitsCombination(final List<Cell> cellForDistribution,
                                                                                   final int remainingUnitsAmount) {
        final List<List<Pair<Cell, Integer>>> combinations = new LinkedList<>();
        if (remainingUnitsAmount <= 0) {
            return combinations;
        }
        for (int i = 0; i < remainingUnitsAmount; i++) {
            for (final Cell cell : cellForDistribution) {
                final List<Pair<Cell, Integer>> currentCombination = new LinkedList<>();
                currentCombination.add(new Pair<>(cell, i));
                cellForDistribution.remove(cell);
                final List<List<Pair<Cell, Integer>>> remainingCellCombinations =
                        getDistributionUnitsCombination(cellForDistribution, remainingUnitsAmount - i);
                for (final List<Pair<Cell, Integer>> remainingCellCombination : remainingCellCombinations) {
                    currentCombination.addAll(remainingCellCombination);
                }
                combinations.add(currentCombination);
            }
        }
        return combinations;
    }

    /**
     * Обновляет монеты игрока
     *
     * @param game   - игра
     * @param player - игрок
     */
    private static void updateDecisionNodeCoinsAmount(@NotNull final IGame game, final Player player) {
        GameLoopProcessor.updateCoinsCount(player, game.getFeudalToCells().get(player),
                game.getGameFeatures(), game.getBoard());
    }

    /**
     * Возвращает копию текущего игрока из копии игры
     *
     * @param game            - копия игры
     * @param currentPlayerId - id текущего игрока
     * @return - копия нужного игрока
     * @throws AIBotException - если копия не найдена
     */
    private static Player getPlayerCopy(@NotNull final IGame game, final int currentPlayerId) throws AIBotException {
        return game.getPlayers()
                .stream()
                .filter(player -> player.getId() == currentPlayerId)
                .findFirst()
                .orElseThrow(() -> new AIBotException(AIBotExceptionErrorCode.NO_FOUND_PLAYER));
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
        return MAX_DEPTH == roundTreeCreationCounter && players.get(players.size() - 1).getId() == currentPlayerId;
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
        final Player currentPlayer = players
                .stream()
                .filter(player -> player.getId() == currentPlayerId)
                .findFirst()
                .orElseThrow();
        final int currentPlayerIndex = players.indexOf(currentPlayer);
        final int nextPlayerIndex = currentPlayerIndex + 1 > players.size() ? 0 : currentPlayerIndex + 1;
        if (nextPlayerIndex == 0) {
            roundTreeCreationCounter++;
        }
        return players.get(nextPlayerIndex);
    }
}
