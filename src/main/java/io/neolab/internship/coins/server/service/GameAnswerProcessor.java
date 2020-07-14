package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.implementations.CatchCellAnswer;
import io.neolab.internship.coins.common.answer.implementations.ChooseRaceAnswer;
import io.neolab.internship.coins.common.answer.implementations.DeclineRaceAnswer;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.service.GameLogger;
import io.neolab.internship.coins.server.validation.IGameValidator;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.RandomGenerator;

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
            distributionUnits(player,
                    currentGame.getPlayerToTransitCells().get(player),
                    currentGame.getOwnToCells().get(player),
                    currentGame.getBoard());
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
     * @param transitCells    - транзитные клетки игрока
     *                        (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     * @param controlledCells - принадлежащие игроку клетки
     * @param board           - борда
     */
    public static void distributionUnits(final Player player, final List<Cell> transitCells,
                                         final List<Cell> controlledCells, final IBoard board) {
        GameLogger.printBeginUnitsDistributionLog(player);
        freeTransitCells(player, transitCells, controlledCells);
        makeAllUnitsSomeState(player,
                AvailabilityType.AVAILABLE); // доступными юнитами становятся все имеющиеся у игрока юниты
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        if (controlledCells.size() > 0) { // Если есть куда распределять войска
            if (availableUnits.size() > 0) {
                /* Пока есть какие войска распределять и
                ответ "ДА" от игрока на вопрос: "Продолжить распределять войска?" */

                final Cell protectedCell = RandomGenerator.chooseItemFromList(
                        controlledCells); // клетка, в которую игрок хочет распределить войска

                final int unitsCount = RandomGenerator.chooseNumber(
                        availableUnits.size()); // число юнитов, которое игрок хочет распределить в эту клетку

                GameLogger.printCellDefendingLog(player, unitsCount, board.getPositionByCell(protectedCell));
                protectCell(player, availableUnits, protectedCell, unitsCount);
            }
        }
        GameLogger.printAfterDistributedUnitsLog(player);
    }

    /**
     * Освобождение игроком всех его транзитных клеток
     *
     * @param player          - игрок, который должен освободить все свои транзитные клетки
     * @param transitCells    - транзитные клетки игрока
     *                        (т. е. те клетки, которые принадлежат игроку, но не приносят ему монет)
     * @param controlledCells - принадлежащие игроку клетки
     */
    public static void freeTransitCells(final Player player, final List<Cell> transitCells,
                                        final List<Cell> controlledCells) {
        GameLogger.printTransitCellsLog(player, transitCells);

        /* Игрок покидает каждую транзитную клетку */
        controlledCells.removeIf(transitCells::contains);
        transitCells.forEach(transitCell -> transitCell.setOwn(null));
        transitCells.clear();

        GameLogger.printFreedTransitCellsLog(player);
    }

    /**
     * Перевести всех юнитов игрока в одно состояние
     *
     * @param player           - игрок, чьих юнитов нужно перевести в одно состояние
     * @param availabilityType - состояние, в которое нужно перевести всех юнитов игрока
     */
    public static void makeAllUnitsSomeState(final Player player, final AvailabilityType availabilityType) {
        for (final AvailabilityType item : AvailabilityType.values()) {
            if (item != availabilityType) {
                player.getUnitStateToUnits().get(availabilityType).addAll(player.getUnitStateToUnits().get(item));
                player.getUnitStateToUnits().get(item).clear();
            }
        }
    }

    /**
     * Защитить клетку: владелец помещает в ней своих юнитов
     *
     * @param player         - владелец (в этой ситуации он же - феодал)
     * @param availableUnits - список доступных юнитов
     * @param protectedCell  - защищаемая клетка
     * @param unitsCount     - число юнитов, которое игрок хочет направить в клетку
     */
    public static void protectCell(final Player player, final List<Unit> availableUnits,
                                   final Cell protectedCell, final int unitsCount) {
        protectedCell.getUnits()
                .addAll(availableUnits.subList(0, unitsCount)); // отправить первые unitsCount доступных юнитов
        makeNAvailableUnitsToNotAvailable(player, unitsCount);
        GameLogger.printCellAfterDefendingLog(player, protectedCell);
    }

    /**
     * Сделать первые N доступных юнитов игрока недоступными
     *
     * @param player - игрок, первые N доступных юнитов которого нужно сделать недоступными
     * @param N      - то число доступных юнитов, которых необходимо сделать недоступными
     */
    public static void makeNAvailableUnitsToNotAvailable(final Player player, final int N) {
        final Iterator<Unit> iterator = player.getUnitsByState(AvailabilityType.AVAILABLE).iterator();
        int i = 0;
        while (iterator.hasNext() && i < N) {
            player.getUnitStateToUnits().get(AvailabilityType.NOT_AVAILABLE).add(iterator.next());
            iterator.remove();
            i++;
        }
//        int i = 0;
//        for (final Unit unit : unitStateToUnits.get(UnitState.AVAILABLE)) {
//            if (i >= N) {
//                break;
//            }
//            unitStateToUnits.get(UnitState.NOT_AVAILABLE).add(unit);
//            i++;
//        }
//        unitStateToUnits.get(UnitState.AVAILABLE)
//                .removeIf(unit -> unitStateToUnits.get(UnitState.NOT_AVAILABLE).contains(unit));
    }

}
