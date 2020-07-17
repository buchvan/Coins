package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.CatchCellAnswer;
import io.neolab.internship.coins.common.answer.ChangeRaceAnswer;
import io.neolab.internship.coins.common.answer.DeclineRaceAnswer;
import io.neolab.internship.coins.common.answer.DistributionUnitsAnswer;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.service.GameLogger;
import io.neolab.internship.coins.server.validation.IGameValidator;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;

import java.util.*;

import static io.neolab.internship.coins.server.game.service.GameLoopProcessor.*;

/**
 * Класс отвечает за обработку ответов от игроков
 */
public class GameAnswerProcessor {
    public static void process(final PlayerQuestion playerQuestion, final Answer answer) throws CoinsException {
        final IGame currentGame = playerQuestion.getGame();
        final Player player = playerQuestion.getPlayer();
        if (playerQuestion.getQuestionType() == QuestionType.DECLINE_RACE) {
            final DeclineRaceAnswer declineRaceAnswer = (DeclineRaceAnswer) answer;
            IGameValidator.validateDeclineRaceAnswer(declineRaceAnswer);
            if (declineRaceAnswer.isDeclineRace()) {
                declineRace(player, currentGame.getOwnToCells().get(player));
            }
            return;
        }
        if (playerQuestion.getQuestionType() == QuestionType.CHANGE_RACE) {
            final ChangeRaceAnswer changeRaceAnswer = (ChangeRaceAnswer) answer;
            final List<Race> currentRacesPool = currentGame.getRacesPool();
            IGameValidator.validateChangeRaceAnswer(changeRaceAnswer, currentRacesPool);
            changeRace(player, changeRaceAnswer.getNewRace(), currentRacesPool);
            return;
        }
        if (playerQuestion.getQuestionType() == QuestionType.CATCH_CELL) {
            final IBoard currentBoard = currentGame.getBoard();
            final CatchCellAnswer catchCellAnswer = (CatchCellAnswer) answer;
            final Map<Player, List<Cell>> ownToCells = currentGame.getOwnToCells();
            final List<Cell> controlledCells = ownToCells.get(player); //список подконтрольных клеток для игрока
            final Map<Player, Set<Cell>> playerToAchievableCells = currentGame.getPlayerToAchievableCells();
            final Set<Cell> achievableCells = playerToAchievableCells.get(player);
            final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
            IGameValidator.validateCatchCellAnswer(catchCellAnswer, controlledCells, currentGame.getBoard(),
                    achievableCells, availableUnits, currentGame.getGameFeatures(), player);
            final Cell captureCell = currentBoard.getCellByPosition(catchCellAnswer.getResolution().getFirst());
            final List<Unit> units = catchCellAnswer.getResolution().getSecond();
            pretendToCell(player, captureCell, units, currentBoard, currentGame.getGameFeatures(), ownToCells,
                    currentGame.getFeudalToCells(), currentGame.getPlayerToTransitCells().get(player),
                    achievableCells);
            return;
        }
        if (playerQuestion.getQuestionType() == QuestionType.DISTRIBUTION_UNITS) {
            final DistributionUnitsAnswer distributionUnitsAnswer = (DistributionUnitsAnswer) answer;
            final IBoard currentBoard = currentGame.getBoard();
            final int playerUnitsAmount = player.getUnitsByState(AvailabilityType.AVAILABLE).size()
                    + player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).size();
            IGameValidator.validateDistributionUnitsAnswer(distributionUnitsAnswer,
                    currentBoard, currentGame.getOwnToCells().get(player), playerUnitsAmount);
            distributionUnitsToCell(player, distributionUnitsAnswer.getResolutions(),
                    currentGame.getPlayerToTransitCells().get(player),
                    currentGame.getOwnToCells().get(player),
                    currentBoard);
        }
    }

    /**
     * Уйти в упадок: потеря контроля над всеми клетками с сохранением от них дохода
     *
     * @param player          - игрок, который решил идти в упадок
     * @param controlledCells - принадлежащие игроку клетки
     */
    private static void declineRace(final Player player,
                                    final List<Cell> controlledCells) {
        GameLogger.printDeclineRaceLog(player);
        controlledCells.clear();
    }

    /**
     * Сменить расу игроку
     *
     * @param player    - игрок, который решил идти в упадок
     * @param racesPool - пул всех доступных рас
     */
    private static void changeRace(final Player player, final Race newRace, final List<Race> racesPool) {
        final Race oldRace = player.getRace();
        Arrays.stream(AvailabilityType.values())
                .forEach(availabilityType ->
                        player.getUnitStateToUnits().get(availabilityType).clear()); // Чистим у игрока юниты
        chooseRace(player, racesPool, newRace);
        racesPool.add(oldRace); // Возвращаем бывшую расу игрока в пул рас
    }

    /**
     * Выбрать игроку новую расу
     *
     * @param player    - игрок, выбирающий новую расу
     * @param racesPool - пул всех доступных рас
     */
    public static void chooseRace(final Player player, final List<Race> racesPool, final Race newRace) {
        racesPool.remove(newRace); // Удаляем выбранную игроком расу из пула
        player.setRace(newRace);
        /* Добавляем юнитов выбранной расы */
        int i = 0;
        while (i < newRace.getUnitsAmount()) {
            player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).add(new Unit());
            i++;
        }
        GameLogger.printChooseRaceLog(player, newRace);
    }

    /**
     * Завоевание клеток игроком
     *
     * @param player          - игрок, проводящий завоёвывание
     * @param captureCell     - клетка, которую игрок хочет захватить
     * @param units           - список юнитов, направленных на захват клетки
     * @param board           - борда
     * @param gameFeatures    - особенности игры
     * @param ownToCells      - список подконтрольных клеток для каждого игрока
     * @param feudalToCells   - множества клеток для каждого феодала
     * @param transitCells    - транзитные клетки игрока
     * @param achievableCells - множество достижимых клеток
     */
    private static void pretendToCell(final Player player,
                                      final Cell captureCell, final List<Unit> units,
                                      final IBoard board,
                                      final GameFeatures gameFeatures,
                                      final Map<Player, List<Cell>> ownToCells,
                                      final Map<Player, Set<Cell>> feudalToCells,
                                      final List<Cell> transitCells,
                                      final Set<Cell> achievableCells) {
        GameLogger.printBeginCatchCellsLog(player);
        final List<Cell> controlledCells = ownToCells.get(player);
        final List<Cell> neighboringCells = getAllNeighboringCells(board, captureCell);
        final boolean isControlled = controlledCells.contains(captureCell);
        if (isControlled) {
            enterToCell(player, captureCell, neighboringCells, units, board);
            return;
        }
        final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(gameFeatures, captureCell);
        final int bonusAttack = getBonusAttackToCatchCell(player, gameFeatures, captureCell);
        catchCell(player, captureCell, neighboringCells, units.subList(0, unitsCountNeededToCatch - bonusAttack),
                units, gameFeatures, ownToCells, feudalToCells, transitCells);
        achievableCells.remove(captureCell);
        achievableCells.addAll(getAllNeighboringCells(board, captureCell));
        achievableCells.removeIf(controlledCells::contains); // удаляем те клетки, которые уже заняты игроком
    }


    /**
     * Метод для распределения юнитов игроком
     *
     * @param player          - игрок, делающий выбор
     * @param resolutions     - мапа: клетка, в которую игрок хочет распределить войска
     *                        -> юниты, которые игрок хочет распределить в клетку
     * @param transitCells    - транзитные клетки игрока
     *                        (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     * @param controlledCells - принадлежащие игроку клетки
     * @param board           - борда
     */
    public static void distributionUnitsToCell(final Player player, final Map<Position, List<Unit>> resolutions,
                                               final List<Cell> transitCells, final List<Cell> controlledCells,
                                               final IBoard board) {
        GameLogger.printBeginUnitsDistributionLog(player);
        freeTransitCells(player, transitCells, controlledCells);
        makeAllUnitsSomeState(player,
                AvailabilityType.AVAILABLE); // доступными юнитами становятся все имеющиеся у игрока юниты
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        for (final Map.Entry<Position, List<Unit>> entry : resolutions.entrySet()) {
            final Position position = entry.getKey();
            final List<Unit> units = entry.getValue();
            GameLogger.printCellDefendingLog(player, units.size(), position);
            protectCell(player, board.getCellByPosition(position), units);
        }
        GameLogger.printAfterDistributedUnitsLog(player);
    }
}
