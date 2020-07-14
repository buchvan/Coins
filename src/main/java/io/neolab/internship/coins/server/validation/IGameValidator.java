package io.neolab.internship.coins.server.validation;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.implementations.CatchCellAnswer;
import io.neolab.internship.coins.common.answer.implementations.ChooseRaceAnswer;
import io.neolab.internship.coins.common.answer.implementations.DeclineRaceAnswer;
import io.neolab.internship.coins.common.answer.implementations.DistributionUnitsAnswer;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.game.GameFeatures;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.service.GameLogger;

import java.util.List;

import static io.neolab.internship.coins.server.game.service.GameLoopProcessor.getBonusAttackToCatchCell;
import static io.neolab.internship.coins.server.game.service.GameLoopProcessor.getUnitsCountNeededToCatchCell;

public interface IGameValidator {
    /**
     * Проверка на пустой ответ
     *
     * @param answer ответ, который нужно проверить
     * @throws CoinsException в случае пустого ответа выбрасывается исключение с кодом ошибки EMPTY_ANSWER
     */
    static void checkIfAnswerEmpty(final Answer answer) throws CoinsException {
        if (answer == null) {
            throw new CoinsException(ErrorCode.EMPTY_ANSWER);
        }
    }

    /**
     * Проверка ответа, отвечающего за выбор расы в начале игры
     *
     * @param answer            ответ, который нужно проверить
     * @param currentPlayerRace текущая раса игрока
     * @param racesPool         пул доступных рас
     * @throws CoinsException при совпадении новых и текущей расы - SAME_RACES, расы нет в пуле - UNAVAILABLE_NEW_RACE,
     *                        пустой ответ - EMPTY_ANSWER
     */
    static void validateChooseRaceAnswer(final ChooseRaceAnswer answer,
                                         final List<Race> racesPool,
                                         final Race currentPlayerRace) throws CoinsException {
        final Race newRace = answer.getNewRace();
        checkIfAnswerEmpty(answer);
        if (!racesPool.contains(newRace)) {
            throw new CoinsException(ErrorCode.UNAVAILABLE_NEW_RACE);
        }
        if (currentPlayerRace == newRace) {
            throw new CoinsException(ErrorCode.SAME_RACES);
        }

    }

    /**
     * Проверка ответа, отвечающего за уход игрока в упадок
     *
     * @param answer ответ, который нужно проверить
     * @throws CoinsException пустой ответ - EMPTY_ANSWER
     */
    static void validateDeclineRaceAnswer(final DeclineRaceAnswer answer) throws CoinsException {
        checkIfAnswerEmpty(answer);
    }

    /**
     * Проверка ответа, отвечающего за захват клетки игроком
     *
     * @param answer ответ, который нужно проверить
     * @throws CoinsException пустой ответ - EMPTY_ANSWER
     */
    static void validateCatchCellAnswer(final CatchCellAnswer answer,
                                        final IBoard currentBoard,
                                        final List<Cell> achievableCells,
                                        final List<Unit> availableUnits,
                                        final GameFeatures gameFeatures,
                                        final Player player) throws CoinsException {
        checkIfAnswerEmpty(answer);
        //есть ли клетка, соответствующая позиции
        checkIfCellExists(answer.getResolution().getFirst(), currentBoard);
        final Cell cellForAttempt = currentBoard.getCellByPosition(answer.getResolution().getFirst());
        if (!checkIfCellExists(answer.getResolution().getFirst(), currentBoard)) {
            throw new CoinsException(ErrorCode.WRONG_POSITION);
        }
        //есть что захватывать
        if (achievableCells.size() < 1) {
            throw new CoinsException(ErrorCode.NO_ACHIEVABLE_CELLS);
        }
        //есть ли войска для захвата
        if (availableUnits.size() < 1) {
            throw new CoinsException(ErrorCode.NO_AVAILABLE_UNITS);
        }
        //достаточно ли юнитов для захвата клетки
        final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(gameFeatures, cellForAttempt);
        final int bonusAttack = getBonusAttackToCatchCell(player, gameFeatures, cellForAttempt);
        if (!isCellCapturePossible(answer.getResolution().getSecond().size() + bonusAttack,
                unitsCountNeededToCatch)) {
            GameLogger.printCatchCellNotCapturedLog(player.getNickname());
            throw new CoinsException(ErrorCode.CELL_CAPTURE_IMPOSSIBLE);
        }

    }

    static void validateDistributionUnitsAnswer(final DistributionUnitsAnswer answer,
                                                final IBoard currentBoard) throws CoinsException {
        checkIfAnswerEmpty(answer);
        if (answer.getResolutions().keySet().stream().anyMatch(position -> !checkIfCellExists(position, currentBoard))) {
            throw new CoinsException(ErrorCode.WRONG_POSITION);
        }

    }

    static boolean isCellCapturePossible(final int attackPower, final int necessaryAttackPower) {
        return attackPower >= necessaryAttackPower;
    }

    static boolean checkIfCellExists(final Position position, final IBoard currentBoard) {
        //есть ли клетка, соответствующая позиции
        return currentBoard.getCellByPosition(position) == null;
    }
}
