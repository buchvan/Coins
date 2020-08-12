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

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static io.neolab.internship.coins.ai.vika.decision.AIDecisionSimulationProcessor.*;
import static io.neolab.internship.coins.server.service.GameLoopProcessor.getBonusAttackToCatchCell;
import static io.neolab.internship.coins.server.service.GameLoopProcessor.getUnitsCountNeededToCatchCell;
import static io.neolab.internship.coins.utils.RandomGenerator.chooseItemFromList;

/**
 * Симуляция принятия решений в ходе игры
 */
public class AIDecisionMaker {

    //TODO
    public static Decision getDeclineRaceDecision(final Player player, final IGame game) {
        //return getBestDecisionByGameTree(player, game, DecisionType.DECLINE_RACE);
        return null;
    }

    //TODO
    public static Decision getChooseRaceDecision(final Player player, final IGame game) {
        //return getBestDecisionByGameTree(player, game, DecisionType.CHANGE_RACE);
        return null;
    }

    //TODO
    public static Decision getChooseCaptureCellDecision(final Player player, final IGame game) {
        //return getBestDecisionByGameTree(player, game, DecisionType.CATCH_CELL);
        return null;
    }

    //TODO
    public static Decision getDistributionUnitsDecision(final Player player, final IGame game) {
        //return getBestDecisionByGameTree(player, game, DecisionType.DISTRIBUTION_UNITS);
        return null;
    }

    public WinCollector getBestDecisionByGameTree(final Player player, final IGame game, final DecisionType decisionType) throws AIBotException {
        //add is opponent checking
        //add finish tree creating
        switch (decisionType) {
            case DECLINE_RACE: {
                final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
                final boolean[] declineRaceTypes = {true, false};
                for (final boolean declineRaceType : declineRaceTypes) {
                    final ExecutorService executorService = Executors.newFixedThreadPool(2);
                    executorService.execute(() -> {
                        final Decision declineRaceDecision = new DeclineRaceDecision(declineRaceType);
                        try {
                            final IGame gameCopy = game.getCopy();
                            final Player playerCopy = getPlayerCopy(gameCopy, player.getId());
                            simulateDeclineRaceDecision(playerCopy, gameCopy, (DeclineRaceDecision) declineRaceDecision);
                            if (declineRaceType) {
                                final WinCollector winCollector = getBestDecisionByGameTree(playerCopy, gameCopy,
                                        DecisionType.CHANGE_RACE);
                                decisionAndWins.add(new DecisionAndWin(declineRaceDecision, winCollector));
                            } else {
                                final WinCollector winCollector = getBestDecisionByGameTree(playerCopy, gameCopy,
                                        DecisionType.CATCH_CELL);
                                decisionAndWins.add(new DecisionAndWin(declineRaceDecision, winCollector));
                            }
                        } catch (final AIBotException e) {
                            e.printStackTrace();
                        }
                    });
                }
                return getBestDecision(decisionAndWins).getWinCollector();
            }
            case CHANGE_RACE: {
                final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
                final List<Race> availableRaces = game.getRacesPool();
                final ExecutorService executorService = Executors.newFixedThreadPool(availableRaces.size());
                availableRaces.forEach(race -> executorService.execute(() -> {
                    final IGame gameCopy = game.getCopy();
                    try {
                        final Player playerCopy = getPlayerCopy(gameCopy, player.getId());
                        final Decision changeRaceDecision = new ChangeRaceDecision(race);
                        simulateChangeRaceDecision(playerCopy, gameCopy, (ChangeRaceDecision) changeRaceDecision);
                        final WinCollector winCollector = getBestDecisionByGameTree(playerCopy, gameCopy,
                                DecisionType.CATCH_CELL);
                        decisionAndWins.add(new DecisionAndWin(changeRaceDecision, winCollector));
                    } catch (final AIBotException e) {
                        e.printStackTrace();
                    }
                }));
                return getBestDecision(decisionAndWins).getWinCollector();
            }
            case CATCH_CELL: {
                final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
                final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
                // + 1 for null catch cell decision
                final ExecutorService executorService = Executors.newFixedThreadPool(achievableCells.size() + 1);
                achievableCells.forEach(cell -> executorService.execute(() -> {
                    if (checkCellCaptureOpportunity(cell, player, game)) {
                        final Position position = game.getBoard().getPositionByCell(cell);
                        final List<Unit> unitsForCapture = new ArrayList<>(); //TODO:
                        final Decision decision = new CatchCellDecision(new Pair<>(position, unitsForCapture));
                        final IGame gameCopy = game.getCopy();
                        try {
                            final Player playerCopy = getPlayerCopy(game, player.getId());
                            simulateCatchCellDecision(playerCopy, gameCopy, (CatchCellDecision) decision);
                            final WinCollector winCollector = getBestDecisionByGameTree(playerCopy, gameCopy,
                                    DecisionType.CATCH_CELL);
                            decisionAndWins.add(new DecisionAndWin(decision, winCollector));
                        } catch (final AIBotException e) {
                            e.printStackTrace();
                        }
                    }
                }));
                executorService.execute(() -> {
                    final IGame gameCopy = game.getCopy();
                    try {
                        final Player playerCopy = getPlayerCopy(game, player.getId());
                        final Decision decision = new CatchCellDecision(null);
                        simulateCatchCellDecision(playerCopy, gameCopy, (CatchCellDecision) decision);
                        final WinCollector winCollector = getBestDecisionByGameTree(playerCopy, gameCopy,
                                DecisionType.DISTRIBUTION_UNITS);
                        decisionAndWins.add(new DecisionAndWin(decision, winCollector));
                    } catch (final AIBotException e) {
                        e.printStackTrace();
                    }
                });
                return getBestDecision(decisionAndWins).getWinCollector();
            }
            case DISTRIBUTION_UNITS: {
                final List<DecisionAndWin> decisionAndWins = new LinkedList<>();
                // 1. составить все возможные комбинации перераспределний
                final List<Cell> controlledCells = game.getOwnToCells().get(player);
                final List<Unit> playerUnits = new LinkedList<>();
                playerUnits.addAll(player.getUnitsByState(AvailabilityType.AVAILABLE));
                playerUnits.addAll(player.getUnitsByState(AvailabilityType.NOT_AVAILABLE));
                final List<List<Pair<Cell, Integer>>> combinations = getDistributionUnitsCombination(
                        new LinkedList<>(controlledCells), playerUnits.size());
                // 2. построить узлы
                // 3. применить решение для копий игры и игрока
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
                            //TODO: stop recursion checking
                            //TODO: switch player
                            updateDecisionNodeCoinsAmount(gameCopy, playerCopy);
                            final WinCollector winCollector = getBestDecisionByGameTree(playerCopy, gameCopy,
                                    DecisionType.DECLINE_RACE);
                            decisionAndWins.add(new DecisionAndWin(decision, winCollector));
                        } catch (final AIBotException e) {
                            e.printStackTrace();
                        }
                    });
                }
                return getBestDecision(decisionAndWins).getWinCollector();
            }
            default:
                throw new AIBotException(AIBotExceptionErrorCode.DECISION_NOT_EXISTS);
        }
    }

    private DecisionAndWin getBestDecision(final List<DecisionAndWin> decisionAndWins) {
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
    private boolean checkCellCaptureOpportunity(@NotNull final Cell cell, @NotNull final Player player,
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
    private List<List<Pair<Cell, Integer>>> getDistributionUnitsCombination(final List<Cell> cellForDistribution,
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

    private void updateDecisionNodeCoinsAmount(final IGame game, final Player player) {
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
    private Player getPlayerCopy(@NotNull final IGame game, final int currentPlayerId) throws AIBotException {
        return game.getPlayers()
                .stream()
                .filter(player -> player.getId() == currentPlayerId)
                .findFirst()
                .orElseThrow(() -> new AIBotException(AIBotExceptionErrorCode.NO_FOUND_PLAYER));
    }
}
