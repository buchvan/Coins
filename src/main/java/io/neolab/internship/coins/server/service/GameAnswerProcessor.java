package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.CatchCellAnswer;
import io.neolab.internship.coins.common.answer.ChangeRaceAnswer;
import io.neolab.internship.coins.common.answer.DeclineRaceAnswer;
import io.neolab.internship.coins.common.answer.DistributionUnitsAnswer;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.question.PlayerQuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.AvailabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static io.neolab.internship.coins.server.service.GameLoopProcessor.*;

/**
 * Класс, отвечащий за обработку ответов от игроков
 */
public class GameAnswerProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameAnswerProcessor.class);

    /**
     * Основной метод данного процессора, в котором происходит вся обработка ответов
     *
     * @param playerQuestion - вопрос игроку
     * @param answer         - ответ игрока
     * @throws CoinsException в случае невалидности ответа
     */
    public static void process(final PlayerQuestion playerQuestion, final Answer answer) throws CoinsException {
        final IGame currentGame = playerQuestion.getGame();
        final Player player = playerQuestion.getPlayer();
        if (playerQuestion.getPlayerQuestionType() == PlayerQuestionType.DECLINE_RACE) {
            declineRaceProcess(answer, player, currentGame.getOwnToCells().get(player));
            return;
        }
        if (playerQuestion.getPlayerQuestionType() == PlayerQuestionType.CHANGE_RACE) {
            changeRaceProcess(answer, player, currentGame.getRacesPool());
            return;
        }
        if (playerQuestion.getPlayerQuestionType() == PlayerQuestionType.CATCH_CELL) {
            captureCellProcess(answer, player, currentGame.getBoard(), currentGame.getGameFeatures(),
                    currentGame.getOwnToCells(), currentGame.getFeudalToCells(),
                    currentGame.getPlayerToTransitCells().get(player),
                    currentGame.getPlayerToAchievableCells().get(player));
            return;
        }
        if (playerQuestion.getPlayerQuestionType() == PlayerQuestionType.DISTRIBUTION_UNITS) {
            distributionUnitsProcess(answer, player, currentGame.getBoard(),
                    currentGame.getOwnToCells().get(player), currentGame.getPlayerToTransitCells().get(player));
        }
    }

    /**
     * Процесс ухода в упадок
     *
     * @param answer          - ответ
     * @param player          - игрок
     * @param controlledCells - подконтрольные игроку клетки
     * @throws CoinsException при невалидном ответе
     */
    private static void declineRaceProcess(final Answer answer, final Player player, final List<Cell> controlledCells)
            throws CoinsException {
        final DeclineRaceAnswer declineRaceAnswer = (DeclineRaceAnswer) answer;
        LOGGER.debug("Decline race answer: {} ", declineRaceAnswer);
        GameValidator.validateDeclineRaceAnswer(declineRaceAnswer);
        LOGGER.debug("Answer is valid");
        if (declineRaceAnswer.isDeclineRace()) {
            declineRace(player, controlledCells);
        }
    }

    /**
     * Уйти в упадок: потеря контроля над всеми клетками с сохранением от них дохода
     *
     * @param player          - игрок, который решил идти в упадок
     * @param controlledCells - принадлежащие игроку клетки
     */
    private static void declineRace(final Player player, final List<Cell> controlledCells) {
        GameLogger.printDeclineRaceLog(player);
        controlledCells.clear();
    }

    /**
     * Процесс смены расы игроком
     *
     * @param answer    - ответ
     * @param player    - игрок
     * @param racesPool - пул рас
     * @throws CoinsException при невалидном ответе
     */
    private static void changeRaceProcess(final Answer answer, final Player player, final List<Race> racesPool)
            throws CoinsException {
        final ChangeRaceAnswer changeRaceAnswer = (ChangeRaceAnswer) answer;
        LOGGER.debug("Change race answer: {} ", changeRaceAnswer);
        GameValidator.validateChangeRaceAnswer(changeRaceAnswer, racesPool);
        LOGGER.debug("Answer is valid");
        changeRace(player, changeRaceAnswer.getNewRace(), racesPool);
    }

    /**
     * Сменить расу игроку
     *
     * @param player    - игрок, который решил идти в упадок
     * @param racesPool - пул всех доступных рас
     */
    static void changeRace(final Player player, final Race newRace, final List<Race> racesPool) {
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
    static void chooseRace(final Player player, final List<Race> racesPool, final Race newRace) {
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
     * Процесс захвата клетки
     *
     * @param answer          - ответ
     * @param player          - игрок
     * @param board           - борда
     * @param gameFeatures    - особенности игры
     * @param ownToCells      - отображение игроков в списки подконтрольных им клеток
     * @param feudalToCells   - отображение игроков во множества приносящих им монеты клеток
     * @param transitCells    - список транзитных клеток игрока
     * @param achievableCells - множество достижимых за одних ход игроком клеток
     * @throws CoinsException в случае если ответ невалиден
     */
    private static void captureCellProcess(final Answer answer,
                                           final Player player,
                                           final IBoard board,
                                           final GameFeatures gameFeatures,
                                           final Map<Player, List<Cell>> ownToCells,
                                           final Map<Player, Set<Cell>> feudalToCells,
                                           final List<Cell> transitCells,
                                           final Set<Cell> achievableCells) throws CoinsException {
        final CatchCellAnswer catchCellAnswer = (CatchCellAnswer) answer;
        LOGGER.debug("Catch cell answer: {} ", catchCellAnswer);
        final List<Cell> controlledCells = ownToCells.get(player); //список подконтрольных клеток для игрока
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        GameValidator.validateCatchCellAnswer(catchCellAnswer, controlledCells, board,
                achievableCells, availableUnits, gameFeatures, player);
        LOGGER.debug("Answer is valid");
        final Cell captureCell = board.getCellByPosition(catchCellAnswer.getResolution().getFirst());
        final List<Unit> units = catchCellAnswer.getResolution().getSecond();
        pretendToCell(player, captureCell, units, board, gameFeatures,
                ownToCells, feudalToCells, transitCells, achievableCells);
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
        final List<Cell> controlledCells = ownToCells.get(player);
        final boolean isControlled = controlledCells.contains(captureCell);
        if (isControlled) {
            final int tiredUnitsCount = captureCell.getType().getCatchDifficulty();
            enterToCell(player, captureCell, units, tiredUnitsCount, board);
            return;
        }
        GameLogger.printCellCatchAttemptLog(player, board.getPositionByCell(captureCell));
        GameLogger.printCatchCellUnitsQuantityLog(player.getNickname(), units.size());
        final List<Cell> neighboringCells = getAllNeighboringCells(board, captureCell);
        neighboringCells.removeIf(neighboringCell -> !controlledCells.contains(neighboringCell));
        final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(gameFeatures, captureCell);
        final int bonusAttack = getBonusAttackToCatchCell(player, gameFeatures, captureCell);
        catchCell(player, captureCell, neighboringCells, units.subList(0, unitsCountNeededToCatch - bonusAttack),
                units, gameFeatures, ownToCells, feudalToCells, transitCells);
        if (controlledCells.size() == 1) { // если до этого у игрока не было клеток
            achievableCells.clear();
            achievableCells.add(captureCell);
        }
        achievableCells.addAll(getAllNeighboringCells(board, captureCell));
    }

    /**
     * Процесс распределения юнитов
     *
     * @param answer          - ответ
     * @param player          - игрок
     * @param board           - борда
     * @param controlledCells - список подконтрольных игроку клеток
     * @param transitCells    - список транизтных клеток игрока
     * @throws CoinsException в случае если ответ невалиден
     */
    private static void distributionUnitsProcess(final Answer answer, final Player player, final IBoard board,
                                                 final List<Cell> controlledCells, final List<Cell> transitCells)
            throws CoinsException {
        final DistributionUnitsAnswer distributionUnitsAnswer = (DistributionUnitsAnswer) answer;
        LOGGER.debug("Distribution units answer: {} ", distributionUnitsAnswer);
        final int playerUnitsAmount = player.getUnitsByState(AvailabilityType.AVAILABLE).size()
                + player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).size();
        GameValidator.validateDistributionUnitsAnswer(distributionUnitsAnswer, board,
                controlledCells, playerUnitsAmount);
        LOGGER.debug("Answer is valid");
        distributionUnits(player, distributionUnitsAnswer.getResolutions(), transitCells, controlledCells, board);
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
    private static void distributionUnits(final Player player, final Map<Position, List<Unit>> resolutions,
                                         final List<Cell> transitCells, final List<Cell> controlledCells,
                                         final IBoard board) {
        GameLogger.printBeginUnitsDistributionLog(player);
        freeTransitCells(player, transitCells, controlledCells);
        makeAllUnitsSomeState(player,
                AvailabilityType.AVAILABLE); // доступными юнитами становятся все имеющиеся у игрока юниты
        resolutions.forEach((position, units) -> {
            GameLogger.printCellDefendingLog(player, units.size(), position);
            GameLoopProcessor.protectCell(player, board.getCellByPosition(position), units);
        });
        GameLogger.printAfterDistributedUnitsLog(player);
    }
}
