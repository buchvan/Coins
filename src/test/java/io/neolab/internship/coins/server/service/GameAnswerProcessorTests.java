package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.message.client.answer.Answer;
import io.neolab.internship.coins.common.message.client.answer.CatchCellAnswer;
import io.neolab.internship.coins.common.message.client.answer.DeclineRaceAnswer;
import io.neolab.internship.coins.common.message.server.question.PlayerQuestion;
import io.neolab.internship.coins.common.message.server.question.PlayerQuestionType;
import io.neolab.internship.coins.common.message.server.ServerMessageType;
import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.TestUtils;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.neolab.internship.coins.server.service.GameInitializer.gameInit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class GameAnswerProcessorTests extends TestUtils {

    @Test
    public void processWrongQuestionTypeTest() throws CoinsException {
        final IGame game = gameInit(2, 2, 2);
        final Player player = getSomePlayer(game);

        final PlayerQuestion question = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.WRONG_QUESTION_TYPE, game, player);
        final Answer answer = new DeclineRaceAnswer(true);

        final CoinsException exception = assertThrows(CoinsException.class,
                () -> GameAnswerProcessor.process(question, answer));
        assertEquals(CoinsErrorCode.QUESTION_TYPE_NOT_FOUND, exception.getErrorCode());
    }

    protected static Answer createCatchCellAnswer(final @NotNull Position position, final @NotNull Player player,
                                        final int resolutionUnitsAmount) {
        final List<Unit> units = new ArrayList<>();
        int i = 0;
        for (final Unit unit : player.getUnitsByState(AvailabilityType.AVAILABLE)) {
            if (i >= resolutionUnitsAmount) {
                break;
            }
            units.add(unit);
            i++;
        }
        return new CatchCellAnswer(new Pair<>(position, units));
    }

    protected static void setCellAsControlled(final @NotNull Cell cell, final @NotNull IGame game,
                                              final @NotNull Player player) {
        final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        final List<Cell> controlledCells = ownToCells.get(player);
        controlledCells.add(cell);
    }

    @SuppressWarnings("SameParameterValue")
    protected static void setUnitToCell(final @NotNull Cell cell, final int unitsAmount) {
        final List<Unit> cellUnits = new ArrayList<>();
        for (int i = 0; i < unitsAmount; i++) {
            cellUnits.add(new Unit());
        }

        cell.getUnits().clear();
        cell.getUnits().addAll(cellUnits);
    }
}
