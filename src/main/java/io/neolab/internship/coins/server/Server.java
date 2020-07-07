package io.neolab.internship.coins.server;

import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.answer.Answer;
import io.neolab.internship.coins.client.Client;
import io.neolab.internship.coins.question.Question;
import io.neolab.internship.coins.server.board.*;
import io.neolab.internship.coins.server.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Server implements IServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class); // логгер (slf4j) для сервера

    private final int CLIENTS_COUNT = 2; // число клиентов, которое ожидает сервер
    private final int ROUNDS_COUNT = 10; // число раундов

    private int currentRound = 0; // текущий раунд
    private Board board; // борда (карта/доска)
    private List<Pair<Client, Player>> clientToPlayerList = new LinkedList<>(); // взаимооднозначное соответствие между клиентом и игроком
    private Map<Pair<Race, CellType>, List<Feature>> raceCellTypeToFeatureList = new HashMap<>(); // (раса, тип клетки) -> список особенностей
    private Map<Player, List<Cell>> feudalToCells = new HashMap<>(); // игрок -> клетки, с которых он получает монеты

    private IValidator validator = new ServerValidator(this); // валидатор
    private IAnswerProcessor answerProcessor = new ServerAnswerProcessor(this); // обработчик ответов

    /**
     * Валидатор ответов клиента для сервера
     */
    private static class ServerValidator implements IValidator {
        private final IServer server;

        public ServerValidator(IServer server) {
            this.server = server;
        }

        @Override
        public boolean isValid(Question question, Answer answer) {
            return false;
        }
    }

    /**
     * Обработчик ответов клиента для сервера
     */
    private static class ServerAnswerProcessor implements IAnswerProcessor {
        private final IServer server;

        public ServerAnswerProcessor(IServer server) {
            this.server = server;
        }

        @Override
        public void process(Question question, Answer answer) {

        }
    }

    @Override
    public void startServer() {

    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }
}
