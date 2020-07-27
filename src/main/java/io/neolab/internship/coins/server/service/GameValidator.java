package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.CatchCellAnswer;
import io.neolab.internship.coins.common.answer.ChangeRaceAnswer;
import io.neolab.internship.coins.common.answer.DeclineRaceAnswer;
import io.neolab.internship.coins.common.answer.DistributionUnitsAnswer;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.neolab.internship.coins.server.service.GameLoopProcessor.getBonusAttackToCatchCell;
import static io.neolab.internship.coins.server.service.GameLoopProcessor.getUnitsCountNeededToCatchCell;

/**
 * Валидатор ответов игрока
 */
class GameValidator {
    /**
     * Проверка на пустой ответ
     *
     * @param answer - ответ, который нужно проверить
     * @throws CoinsException в случае пустого ответа выбрасывается исключение с кодом ошибки EMPTY_ANSWER
     */
    @Contract("null -> fail")
    private static void checkIfAnswerEmpty(final @Nullable Answer answer) throws CoinsException {
        if (answer == null) {
            throw new CoinsException(CoinsErrorCode.ANSWER_VALIDATION_ERROR_EMPTY_ANSWER);
        }
    }

    /**
     * Проверка ответа, отвечающего за выбор расы в начале игры
     *
     * @param answer    - ответ, который нужно проверить
     * @param racesPool - пул доступных рас
     * @throws CoinsException при совпадении новых и текущей расы - SAME_RACES, расы нет в пуле - UNAVAILABLE_NEW_RACE,
     *                        пустой ответ - EMPTY_ANSWER
     */
    static void validateChangeRaceAnswer(final @Nullable ChangeRaceAnswer answer,
                                         final @NotNull List<Race> racesPool) throws CoinsException {
        checkIfAnswerEmpty(answer);
        final Race newRace = answer.getNewRace();
        if (!racesPool.contains(newRace)) {
            throw new CoinsException(CoinsErrorCode.ANSWER_VALIDATION_UNAVAILABLE_NEW_RACE);
        }
    }

    /**
     * Проверка ответа, отвечающего за уход игрока в упадок
     *
     * @param answer - ответ, который нужно проверить
     * @throws CoinsException пустой ответ - EMPTY_ANSWER
     */
    @Contract("null -> fail")
    static void validateDeclineRaceAnswer(final @Nullable DeclineRaceAnswer answer) throws CoinsException {
        checkIfAnswerEmpty(answer);
    }

    /**
     * Проверка ответа, отвечающего за захват клетки игроком
     *
     * @param answer - ответ, который нужно проверить
     * @throws CoinsException пустой ответ - EMPTY_ANSWER,
     *                        несуществующая позиция - WRONG_POSITION,
     *                        недостижимая клетка - INVALID_ACHIEVABLE_CELL,
     *                        нет доступных юнитов - NO_AVAILABLE_UNITS,
     *                        недостаточно юнитов для захвата - CELL_CAPTURE_IMPOSSIBLE
     */
    static void validateCatchCellAnswer(final @Nullable CatchCellAnswer answer,
                                        final @NotNull List<Cell> controlledCells,
                                        final @NotNull IBoard currentBoard,
                                        final @NotNull Set<Cell> achievableCells,
                                        final @NotNull List<Unit> availableUnits,
                                        final @NotNull GameFeatures gameFeatures,
                                        final @NotNull Player player) throws CoinsException {
        checkIfAnswerEmpty(answer);
        final Cell cellForAttempt =
                currentBoard.getCellByPosition(Objects.requireNonNull(answer.getResolution()).getFirst());
        //есть ли клетка, соответствующая позиции
        if (checkIfCellDoesntExists(answer.getResolution().getFirst(), currentBoard)) {
            throw new CoinsException(CoinsErrorCode.ANSWER_VALIDATION_WRONG_POSITION);
        }
        //клетка достижима
        if (!achievableCells.contains(cellForAttempt)) {
            throw new CoinsException(CoinsErrorCode.ANSWER_VALIDATION_UNREACHABLE_CELL);
        }
        //есть ли войска для захвата
        if (availableUnits.isEmpty()) {
            throw new CoinsException(CoinsErrorCode.ANSWER_VALIDATION_NO_AVAILABLE_UNITS);
        }
        final List<Unit> units = answer.getResolution().getSecond();
        if (controlledCells.contains(cellForAttempt) &&
                units.size() < Objects.requireNonNull(cellForAttempt).getType().getCatchDifficulty()) {
            throw new CoinsException(CoinsErrorCode.ANSWER_VALIDATION_ENTER_CELL_IMPOSSIBLE);
        }
        //достаточно ли юнитов для захвата клетки
        final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(gameFeatures,
                Objects.requireNonNull(cellForAttempt));
        final int bonusAttack = getBonusAttackToCatchCell(player, gameFeatures, cellForAttempt);
        if (!isCellCapturePossible(units.size() + bonusAttack, unitsCountNeededToCatch)) {
            GameLogger.printCatchCellNotCapturedLog(player);
            throw new CoinsException(CoinsErrorCode.ANSWER_VALIDATION_CELL_CAPTURE_IMPOSSIBLE);
        }
    }

    /**
     * Проверка ответа, отвечающего за захват клетки игроком
     *
     * @param answer - ответ, который нужно проверить
     * @throws CoinsException пустой ответ - EMPTY_ANSWER,
     *                        нет доступных для распределения клеток - NO_PLACE_FOR_DISTRIBUTION,
     *                        несуществующая позиция - WRONG_POSITION,
     *                        число выбранных для распределения юнитов больше, чем число доступных - NOT_ENOUGH_UNITS,
     */
    static void validateDistributionUnitsAnswer(final @Nullable DistributionUnitsAnswer answer,
                                                final @NotNull IBoard currentBoard,
                                                final @NotNull List<Cell> controlledCells,
                                                final int playerUnitsAmount) throws CoinsException {
        checkIfAnswerEmpty(answer);
        //Некуда распределять войска
        if (controlledCells.isEmpty()) {
            throw new CoinsException(CoinsErrorCode.ANSWER_VALIDATION_NO_PLACE_FOR_DISTRIBUTION);
        }
        int answerUnitsAmount = 0;
        for (final Map.Entry<Position, List<Unit>> entry : Objects.requireNonNull(answer.getResolutions()).entrySet()) {
            final Position position = entry.getKey();
            final List<Unit> units = entry.getValue();
            answerUnitsAmount += units.size();
            if (checkIfCellDoesntExists(position, currentBoard)) {
                throw new CoinsException(CoinsErrorCode.ANSWER_VALIDATION_WRONG_POSITION);
            }
        }
        //игрок хочет распределить больше юнитов чем у него есть
        if (answerUnitsAmount > playerUnitsAmount) {
            throw new CoinsException(CoinsErrorCode.ANSWER_VALIDATION_NOT_ENOUGH_UNITS);
        }
    }

    /**
     * @param attackPower          - сила атаки
     * @param necessaryAttackPower - необходимая для захвата сила атаки
     * @return true, если клетка захватываема, false - иначе
     */
    @Contract(pure = true)
    private static boolean isCellCapturePossible(final int attackPower, final int necessaryAttackPower) {
        return attackPower >= necessaryAttackPower;
    }

    /**
     * Есть ли клетка, соответствующая позиции
     *
     * @param position     - позиция
     * @param currentBoard - борда
     * @return true, если на борде существует клетка с такой позицией
     */
    private static boolean checkIfCellDoesntExists(final @NotNull Position position,
                                                   final @NotNull IBoard currentBoard) {
        return currentBoard.getCellByPosition(position) == null;
    }
}
