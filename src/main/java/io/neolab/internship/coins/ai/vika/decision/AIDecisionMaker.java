package io.neolab.internship.coins.ai.vika.decision;

import io.neolab.internship.coins.ai.vika.decision.model.CatchCellDecision;
import io.neolab.internship.coins.ai.vika.decision.model.ChangeRaceDecision;
import io.neolab.internship.coins.ai.vika.decision.model.Decision;
import io.neolab.internship.coins.ai.vika.decision.model.DeclineRaceDecision;
import io.neolab.internship.coins.ai.vika.decision.tree.DecisionTreeNode;
import io.neolab.internship.coins.server.game.Game;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.service.GameLogger;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.neolab.internship.coins.server.service.GameLoopProcessor.*;
import static io.neolab.internship.coins.server.service.GameLoopProcessor.getAllNeighboringCells;

/**
 * Симуляция принятия решений в ходе игры
 */
public class AIDecisionMaker {

    /**
     * Создает новые решения - узлы потомки - об отправлении расы в упадок
     *
     * @param currentNode - текущий узел в симуляционном дереве
     * @param game        - текущее состояние игры
     * @param player      - игрок, относительно которого принимаются решение
     */
    public static void createDeclineRaceDecisions(@NotNull final DecisionTreeNode currentNode, @NotNull final Game game,
                                                  @NotNull final Player player) {
        final Decision declineRaceDecisionTrue = new DeclineRaceDecision(true);
        final Player playerCopy = player.getCopy();
        final IGame gameCopy = game.getCopy();
        final DecisionTreeNode trueChildNode = new DecisionTreeNode(
                currentNode, declineRaceDecisionTrue, playerCopy, gameCopy);
        currentNode.getChildDecisions().add(trueChildNode);
        simulateDeclineRaceDecision(playerCopy, gameCopy, (DeclineRaceDecision) declineRaceDecisionTrue);
        final Decision declineRaceDecisionFalse = new DeclineRaceDecision(true);
        final DecisionTreeNode falseChildNode = new DecisionTreeNode(
                currentNode, declineRaceDecisionFalse, player.getCopy(), game.getCopy());
        currentNode.getChildDecisions().add(falseChildNode);
        simulateDeclineRaceDecision(playerCopy, gameCopy, (DeclineRaceDecision) declineRaceDecisionFalse);
    }

    /**
     * Симулирует принятое решение об упадке расы для копий игровых сущностей
     *
     * @param player   - текущий игрок
     * @param game     - текуще состояние игры
     * @param decision - принятое решение
     */
    private static void simulateDeclineRaceDecision(final @NotNull Player player, final @NotNull IGame game,
                                                    @NotNull final DeclineRaceDecision decision) {
        if (decision.isDeclineRace()) {
            game.getOwnToCells().get(player).clear();
        }
    }

    /**
     * Создает новые решения - узлы потомки - о смене расы
     *
     * @param currentNode - текущий узел в симуляционном дереве
     * @param game        - текущее состояние игры
     * @param player      - игрок, относительно которого принимаются решение
     */
    public static void createChangeRaceDecisions(@NotNull final DecisionTreeNode currentNode, @NotNull final Game game,
                                                 @NotNull final Player player) {
        final List<Race> availableRaces = game.getRacesPool();
        final Player playerCopy = player.getCopy();
        final IGame gameCopy = game.getCopy();
        availableRaces.forEach(race -> {
            final Decision changeRaceDecision = new ChangeRaceDecision(race);
            final DecisionTreeNode childNode = new DecisionTreeNode(
                    currentNode, changeRaceDecision, player.getCopy(), game.getCopy());
            currentNode.getChildDecisions().add(childNode);
            simulateChangeRaceDecision(playerCopy, gameCopy, (ChangeRaceDecision) changeRaceDecision);
        });
    }

    /**
     * Симулирует принятое решение о смене расы для копий игровых сущностей
     *
     * @param player   - текущий игрок
     * @param game     - текуще состояние игры
     * @param decision - принятое решение
     */
    private static void simulateChangeRaceDecision(final @NotNull Player player, final @NotNull IGame game,
                                                   @NotNull final ChangeRaceDecision decision) {
        Arrays.stream(AvailabilityType.values())
                .forEach(availabilityType ->
                        player.getUnitStateToUnits().get(availabilityType).clear()); // Чистим у игрока юниты

        final List<Race> racesPool = game.getRacesPool();
        final Race newRace = decision.getNewRace();
        final Race oldRace = player.getRace();
        racesPool.remove(newRace); // Удаляем выбранную игроком расу из пула
        player.setRace(newRace);

        if (oldRace != null) {
            racesPool.add(oldRace);
        }

        /* Добавляем юнитов выбранной расы */
        int i = 0;
        while (i < newRace.getUnitsAmount()) {
            player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).add(new Unit());
            i++;
        }
    }

    /**
     * Создает новые решения - узлы потомки - о захвате клеток
     *
     * @param currentNode - текущий узел в симуляционном дереве
     * @param game        - текущее состояние игры
     * @param player      - игрок, относительно которого принимаются решение
     */
    public static void createCatchCellDecisions(@NotNull final DecisionTreeNode currentNode, @NotNull final Game game,
                                                @NotNull final Player player) {
        // 1. получить все доступные для захвата клетки +
        // 2. отфильтровать по принципу достаточно ли юнитов для захвата +
        // 3. построить узлы по оставшимся +
        // 4. применить решение для копий игры и игрока

        final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
        achievableCells.forEach(cell -> {
            if (checkCellCaptureOpportunity(cell, player, game)) {
                final Position position = game.getBoard().getPositionByCell(cell);
                final List<Unit> unitsForCapture = new ArrayList<>(); //TODO:
                final Decision decision = new CatchCellDecision(new Pair<>(position, unitsForCapture));
                final IGame gameCopy = game.getCopy();
                final Player playerCopy = player.getCopy();
                createDecisionNode(currentNode, decision, playerCopy, gameCopy);
                simulateCatchCellDecision(playerCopy, gameCopy, (CatchCellDecision) decision);
            }
        });
        createCatchCellNullDecision(currentNode, game.getCopy(), player.getCopy());
    }

    private static void simulateCatchCellDecision(final @NotNull Player player, final @NotNull IGame game,
                                                  @NotNull final CatchCellDecision decision) {
        if (decision.getResolution() != null) {
            final GameFeatures gameFeatures = game.getGameFeatures();
            final IBoard board = game.getBoard();

            final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
            final List<Cell> controlledCells = ownToCells.get(player);
            final Position captureCellPosition = decision.getResolution().getFirst();
            final Cell captureCell = board.getCellByPosition(captureCellPosition);
            final boolean isControlled = controlledCells.contains(captureCell);

            final List<Unit> unitsForCapture = decision.getResolution().getSecond();

            final Map<Player, Set<Cell>> feudalToCells = game.getFeudalToCells();

            final List<Cell> transitCells = game.getPlayerToTransitCells().get(player);
            final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
            if (isControlled) {
                final int tiredUnitsCount = captureCell.getType().getCatchDifficulty();
                enterToCell(player, captureCell, ownToCells.get(player), feudalToCells.get(player),
                        unitsForCapture, tiredUnitsCount, board);
                return;
            }
            final List<Cell> neighboringCells = getAllNeighboringCells(board, captureCell);
            neighboringCells.removeIf(neighboringCell -> !controlledCells.contains(neighboringCell));
            final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(gameFeatures, captureCell);
            final int bonusAttack = getBonusAttackToCatchCell(player, gameFeatures, captureCell);
            catchCell(player, captureCell, neighboringCells, unitsForCapture.subList(0, unitsCountNeededToCatch - bonusAttack),
                    unitsForCapture, gameFeatures, ownToCells, feudalToCells, transitCells);
            if (controlledCells.size() == 1) { // если до этого у игрока не было клеток
                achievableCells.clear();
                achievableCells.add(captureCell);
            }
            achievableCells.addAll(getAllNeighboringCells(board, captureCell));
        }
    }

    /**
     * Создает узел с null захватом клетки(прекратить захват)
     *
     * @param currentNode - текущий узел-родитель
     * @param game        - текущее состояние игры
     * @param player      - текущий игрок
     */
    private static void createCatchCellNullDecision(@NotNull final DecisionTreeNode currentNode,
                                                    @NotNull final Game game, @NotNull final Player player) {
        final Decision decision = new CatchCellDecision(null);
        createDecisionNode(currentNode, decision, player, game);
        simulateCatchCellDecision(player, game, (CatchCellDecision) decision);
    }

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
     * Создает новые решения - узлы потомки - о перераспределении юнитов
     *
     * @param currentNode - текущий узел в симуляционном дереве
     * @param game        - текущее состояние игры
     * @param player      - игрок, относительно которого принимаются решение
     */
    public static void createDistributionUnitsDecisions(@NotNull final DecisionTreeNode currentNode,
                                                        @NotNull final Game game, @NotNull final Player player) {
        // 1. составить все возможные комбинации перераспределний
        // 2. построить узлы
        // 3. применить решение для копийй игры и игрока
    }

    /**
     * Создает узел по новому решению
     *
     * @param currentNode - текущий узел
     * @param newDecision - новое решение
     * @param player      - текущий игрок
     * @param game        - игра
     */
    private static void createDecisionNode(@NotNull final DecisionTreeNode currentNode,
                                           @NotNull final Decision newDecision,
                                           @NotNull final Player player,
                                           @NotNull final IGame game) {
        final DecisionTreeNode childNode = new DecisionTreeNode(currentNode, newDecision, player, game);
        addChildNodeToParentNode(currentNode, childNode);
    }

    /**
     * Добавляет созданный узел к списку потомков текущего узла
     *
     * @param currentNode - текущий узел
     * @param childNode   - новый узел-потомок
     */
    private static void addChildNodeToParentNode(@NotNull final DecisionTreeNode currentNode,
                                                 @NotNull final DecisionTreeNode childNode) {
        currentNode.getChildDecisions().add(childNode);
    }
}
