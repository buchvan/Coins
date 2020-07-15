package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.implementations.CatchCellAnswer;
import io.neolab.internship.coins.common.answer.implementations.ChooseRaceAnswer;
import io.neolab.internship.coins.common.answer.implementations.DeclineRaceAnswer;
import io.neolab.internship.coins.common.answer.implementations.DistributionUnitsAnswer;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.service.GameLogger;
import io.neolab.internship.coins.server.validation.IGameValidator;
import io.neolab.internship.coins.utils.AvailabilityType;

import java.util.*;

import static io.neolab.internship.coins.server.game.service.GameLoopProcessor.*;

/**
 * Класс отвечает за обработку ответов от игроков
 */
public class GameAnswerProcessor implements IGameAnswerProcessor {
    @Override
    public void process(final Player player, final Question question, final Answer answer) throws CoinsException {
        final Game currentGame = question.getGame();
        if (question.getQuestionType() == QuestionType.CHOOSE_RACE) {
            final ChooseRaceAnswer chooseRaceAnswer = (ChooseRaceAnswer) answer;
            final List<Race> currentRacesPool = currentGame.getRacesPool();
            IGameValidator.validateChooseRaceAnswer(chooseRaceAnswer, currentRacesPool, player.getRace());
            chooseRace(player, currentRacesPool, chooseRaceAnswer.getNewRace());
            return;
        }
        if (question.getQuestionType() == QuestionType.DECLINE_RACE) {
            final DeclineRaceAnswer declineRaceAnswer = (DeclineRaceAnswer) answer;
            IGameValidator.validateDeclineRaceAnswer(declineRaceAnswer);
            declineRace(player,
                    currentGame.getOwnToCells().get(player),
                    currentGame.getFeudalToCells().get(player));
            return;
        }
        if (question.getQuestionType() == QuestionType.CHANGE_RACE) {
            final ChooseRaceAnswer chooseRaceAnswer = (ChooseRaceAnswer) answer;
            final List<Race> currentRacesPool = currentGame.getRacesPool();
            IGameValidator.validateChooseRaceAnswer(chooseRaceAnswer, currentRacesPool, player.getRace());
            changeRace(player, chooseRaceAnswer.getNewRace(), currentRacesPool);
            return;
        }
        if (question.getQuestionType() == QuestionType.CATCH_CELL) {
            final IBoard currentBoard = currentGame.getBoard();
            final CatchCellAnswer catchCellAnswer = (CatchCellAnswer) answer;
            final Map<Player, List<Cell>> ownToCells = currentGame.getOwnToCells();
            final List<Cell> controlledCells = ownToCells.get(player); //список подконтрольных клеток для игрока
            //TODO: check getAchievableCells
            final List<Cell> achievableCells = getAchievableCells(currentBoard, controlledCells);
            final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
            IGameValidator.validateCatchCellAnswer(catchCellAnswer, currentGame.getBoard(),
                    achievableCells, availableUnits, currentGame.getGameFeatures(), player);
            final Cell captureCell = currentBoard.getCellByPosition(catchCellAnswer.getResolution().getFirst());
            catchCells(player, captureCell, currentBoard, currentGame.getGameFeatures(), ownToCells,
                    currentGame.getFeudalToCells(), currentGame.getPlayerToTransitCells().get(player));
            return;
        }
        if (question.getQuestionType() == QuestionType.DISTRIBUTION_UNITS) {
            //TODO: complete...
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
     * Уйти в упадок: потеря контроля над всеми клетками с сохранением от них дохода
     *
     * @param player          - игрок, который решил идти в упадок
     * @param controlledCells - принадлежащие игроку клетки
     * @param feudalCells     - клетки, приносящие монеты игроку
     */
    public static void declineRace(final Player player,
                                   final List<Cell> controlledCells,
                                   final Set<Cell> feudalCells) {
        GameLogger.printDeclineRaceLog(player);
        feudalCells
                .forEach(cell ->
                        cell.setOwn(null)); // Освобождаем все занятые игроком клетки (юниты остаются там же)
        controlledCells.clear();
    }

    /**
     * Сменить расу игроку
     *
     * @param player    - игрок, который решил идти в упадок
     * @param racesPool - пул всех доступных рас
     */
    public static void changeRace(final Player player, final Race newRace, final List<Race> racesPool) {
        final Race oldRace = player.getRace();
        Arrays.stream(AvailabilityType.values())
                .forEach(availabilityType ->
                        player.getUnitStateToUnits().get(availabilityType).clear()); // Чистим у игрока юниты
        chooseRace(player, racesPool, newRace);
        racesPool.add(oldRace); // Возвращаем бывшую расу игрока в пул рас
    }

    /**
     * Завоевание клеток игроком
     *
     * @param player        - игрок, проводящий завоёвывание
     * @param captureCell   - клетка, которую игрок хочет захватить
     * @param board         - борда
     * @param gameFeatures  - особенности игры
     * @param ownToCells    - список подконтрольных клеток для каждого игрока
     * @param feudalToCells - множества клеток для каждого феодала
     * @param transitCells  - транзитные клетки игрока
     */
    //TODO: rename
    public static void catchCells(final Player player,
                                  final Cell captureCell,
                                  final IBoard board,
                                  final GameFeatures gameFeatures,
                                  final Map<Player, List<Cell>> ownToCells,
                                  final Map<Player, Set<Cell>> feudalToCells,
                                  final List<Cell> transitCells) {
        GameLogger.printBeginCatchCellsLog(player);
        final List<Cell> controlledCells = ownToCells.get(player);
        final List<Cell> achievableCells = getAchievableCells(board, controlledCells);
        final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(gameFeatures, captureCell);
        final int bonusAttack = getBonusAttackToCatchCell(player, gameFeatures, captureCell);
        //TODO: rename
        catchCell(player, captureCell, unitsCountNeededToCatch - bonusAttack,
                gameFeatures, ownToCells, feudalToCells, transitCells);
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
            final int unitsSize = units.size();
            GameLogger.printCellDefendingLog(player, unitsSize, position);
            protectCell(player, availableUnits, board.getCellByPosition(position), unitsSize);
        }
        GameLogger.printAfterDistributedUnitsLog(player);
    }
}
