package io.neolab.internship.coins.server.validation;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.CatchCellAnswer;
import io.neolab.internship.coins.common.answer.ChangeRaceAnswer;
import io.neolab.internship.coins.common.answer.DeclineRaceAnswer;
import io.neolab.internship.coins.common.answer.DistributionUnitsAnswer;
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
import java.util.Map;
import java.util.Set;

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
     * @param racesPool         пул доступных рас
     * @throws CoinsException при совпадении новых и текущей расы - SAME_RACES, расы нет в пуле - UNAVAILABLE_NEW_RACE,
     *                        пустой ответ - EMPTY_ANSWER
     */
    static void validateChangeRaceAnswer(final ChangeRaceAnswer answer,
                                         final List<Race> racesPool) throws CoinsException {
        checkIfAnswerEmpty(answer);
        final Race newRace = answer.getNewRace();
        if (!racesPool.contains(newRace)) {
            throw new CoinsException(ErrorCode.UNAVAILABLE_NEW_RACE);
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
                                        final List<Cell> controlledCells,
                                        final IBoard currentBoard,
                                        final Set<Cell> achievableCells,
                                        final List<Unit> availableUnits,
                                        final GameFeatures gameFeatures,
                                        final Player player) throws CoinsException {
        checkIfAnswerEmpty(answer);
        final Cell cellForAttempt = currentBoard.getCellByPosition(answer.getResolution().getFirst());
        //есть ли клетка, соответствующая позиции
        if (checkIfCellDoesntExists(answer.getResolution().getFirst(), currentBoard)) {
            throw new CoinsException(ErrorCode.WRONG_POSITION);
        }
        //есть что захватывать
        if (!achievableCells.contains(cellForAttempt)) {
            throw new CoinsException(ErrorCode.NO_ACHIEVABLE_CELL);
        }
        //есть ли войска для захвата
        if (availableUnits.isEmpty()) {
            throw new CoinsException(ErrorCode.NO_AVAILABLE_UNITS);
        }

        final List<Unit> units = answer.getResolution().getSecond();
        if (controlledCells.contains(cellForAttempt) && units.size() < cellForAttempt.getType().getCatchDifficulty()) {
            throw new CoinsException(ErrorCode.CELL_CAPTURE_IMPOSSIBLE);
        }
        //достаточно ли юнитов для захвата клетки
        final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(gameFeatures, cellForAttempt);
        final int bonusAttack = getBonusAttackToCatchCell(player, gameFeatures, cellForAttempt);
        if (!isCellCapturePossible(units.size() + bonusAttack, unitsCountNeededToCatch)) {
            GameLogger.printCatchCellNotCapturedLog(player);
            throw new CoinsException(ErrorCode.CELL_CAPTURE_IMPOSSIBLE);
        }

    }

    static void validateDistributionUnitsAnswer(final DistributionUnitsAnswer answer,
                                                final IBoard currentBoard,
                                                final List<Cell> controlledCells,
                                                final int playerUnitsAmount) throws CoinsException {
        checkIfAnswerEmpty(answer);
        //Некуда распределять войска
        if (controlledCells.size() < 1) {
            throw new CoinsException(ErrorCode.NO_PLACE_FOR_DISTRIBUTION);
        }
        int answerUnitsAmount = 0;
        for (final Map.Entry<Position, List<Unit>> entry : answer.getResolutions().entrySet()) {
            final Position position = entry.getKey();
            final List<Unit> units = entry.getValue();
            answerUnitsAmount += units.size();
            if (checkIfCellDoesntExists(position, currentBoard)) {
                throw new CoinsException(ErrorCode.WRONG_POSITION);
            }
        }
        //игрок хочет распределить больше юнитов чем у него есть
        if (answerUnitsAmount > playerUnitsAmount) {
            throw new CoinsException(ErrorCode.NOT_ENOUGH_UNITS);
        }
    }

    static boolean isCellCapturePossible(final int attackPower, final int necessaryAttackPower) {
        return attackPower >= necessaryAttackPower;
    }

    static boolean checkIfCellDoesntExists(final Position position, final IBoard currentBoard) {
        //есть ли клетка, соответствующая позиции
        return currentBoard.getCellByPosition(position) == null;
    }
}
