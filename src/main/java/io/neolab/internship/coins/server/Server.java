package io.neolab.internship.coins.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.neolab.internship.coins.common.Communication;
import io.neolab.internship.coins.common.answer.CatchCellAnswer;
import io.neolab.internship.coins.common.answer.DeclineRaceAnswer;
import io.neolab.internship.coins.common.question.GameQuestion;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.service.GameFinalizer;
import io.neolab.internship.coins.server.game.service.GameInitializer;
import io.neolab.internship.coins.server.game.service.GameLogger;
import io.neolab.internship.coins.server.game.service.GameLoopProcessor;
import io.neolab.internship.coins.server.service.GameAnswerProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server implements IServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static final int PORT = 8081;
    private static final int CLIENTS_COUNT = 2;

    private static final int BOARD_SIZE_X = 3;
    private static final int BOARD_SIZE_Y = 4;

    private final ConcurrentLinkedQueue<ServerSomething> serverList = new ConcurrentLinkedQueue<>();

    private static class ServerSomething {

        private final Socket socket;
        private final BufferedReader in; // поток чтения из сокета
        private final BufferedWriter out; // поток записи в сокет
        private final Player player;

        /**
         * Для общения с клиентом необходим сокет (адресные данные)
         *
         * @param server - сервер
         * @param socket - сокет
         */
        private ServerSomething(final Server server, final Socket socket) throws IOException {
            this.socket = socket;
            // если потоку ввода/вывода приведут к генерированию искдючения, оно проброситься дальше
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            final Question question = new Question(QuestionType.NICKNAME);
            out.write(Communication.serializeQuestion(question) + "\n");
            out.flush();
            String nickname;
            boolean isDuplicate = false;
            do {
                nickname = in.readLine();
                for (final ServerSomething serverSomething : server.serverList) {
                    isDuplicate = isDuplicate || nickname.equals(serverSomething.player.getNickname());
                }
            } while (isDuplicate);
            player = new Player(nickname);
        }

        /**
         * Отправить сообщение клиенту
         *
         * @param message - сообщение
         * @throws IOException в случае ошибки отправки сообщения
         */
        private void send(final String message) throws IOException {
            out.write(message + "\n");
            out.flush();
        }

        /**
         * Прочитать сообщение от клиента
         *
         * @return сообщение от клиента
         * @throws IOException в случае ошибки получения сообщения
         */
        private String read() throws IOException {
            return in.readLine();
        }

        /**
         * закрытие сервера, удаление себя из списка нитей
         */
        private void downService() {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                    in.close();
                    out.close();
                }
            } catch (final IOException ignored) {
            }
        }
    }

    public enum Command {
        EXIT("exit"),
        ;

        private final String commandName;

        Command(final String commandName) {
            this.commandName = commandName;
        }

        public boolean equalCommand(final String message) {
            return commandName.equals(message);
        }
    }

    @Override
    public void startServer() {
        try {
            connectClients();
            LOGGER.info("Server started, port: {}", PORT);

            final List<Player> playerList = new LinkedList<>();
            serverList.forEach(serverSomething -> playerList.add(serverSomething.player));
            final IGame game = GameInitializer.gameInit(BOARD_SIZE_X, BOARD_SIZE_Y, playerList);
            GameLogger.printGameCreatedLog(game);

            GameLogger.printStartGameChoiceLog();
            for (final ServerSomething serverSomething : serverList) {
                chooseRace(serverSomething, game);
            }
            while (game.getCurrentRound() < Game.ROUNDS_COUNT) {
                game.incrementCurrentRound();
                serverList.forEach(serverSomething -> {
                    GameLogger.printNextPlayerLog(serverSomething.player);
                    try {
                        playerRound(serverSomething, game); // раунд игрока. Все свои решения он принимает здесь
                    } catch (final CoinsException | IOException exception) {
                        LOGGER.error("Error!", exception);
                        serverList.forEach(ServerSomething::downService);
                        serverList.clear();
                    }
                });

                /* обновление числа монет у каждого игрока */
                game.getPlayers()
                        .forEach(player ->
                                GameLoopProcessor.updateCoinsCount(player, game.getFeudalToCells(),
                                        game.getGameFeatures(),
                                        game.getBoard()));

                GameLogger.printRoundEndLog(game.getCurrentRound(), game.getPlayers(), game.getOwnToCells(),
                        game.getFeudalToCells());
            }

            GameFinalizer.finalize(game.getPlayers());

        } catch (final CoinsException | IOException exception) {
            LOGGER.error("Error!!!", exception);
            serverList.forEach(ServerSomething::downService);
            serverList.clear();
        }
    }

    /**
     * Подключает клиентов к серверу
     *
     * @throws IOException при ошибке подключения
     */
    private void connectClients() throws IOException {
        try (final ServerSocket serverSocket = new ServerSocket(PORT)) {
            int currentClientsCount = 0;
            while (currentClientsCount < CLIENTS_COUNT) {
                final Socket socket = serverSocket.accept();
                try {
                    serverList.add(new ServerSomething(this, socket));
                    currentClientsCount++;
                } catch (final IOException exception) {
                    LOGGER.error("Error!", exception);
                    socket.close();
                }
            }
        } catch (final BindException exception) {
            LOGGER.error("Error!", exception);
        }
    }

    /**
     * Выбор расы игрока
     *
     * @param serverSomething - клиент игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     * @throws IOException    при ошибке соединения
     * @throws CoinsException из GameAnswerProcessor
     */
    private void chooseRace(final ServerSomething serverSomething, final IGame game)
            throws IOException, CoinsException {

        final GameQuestion gameQuestion
                = new GameQuestion(QuestionType.CHANGE_RACE, game, serverSomething.player);
        serverSomething.send(Communication.serializeQuestion(gameQuestion));
        GameAnswerProcessor.process(gameQuestion, Communication.deserializeChangeRaceAnswer(serverSomething.read()));
    }

    /**
     * Раунд в исполнении игрока
     *
     * @param serverSomething - клиент игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     */
    private void playerRound(final ServerSomething serverSomething, final IGame game)
            throws IOException, CoinsException {

        /* Активация данных игрока в начале раунда */
        GameLoopProcessor.playerRoundBeginUpdate(serverSomething.player,
                game.getOwnToCells().get(serverSomething.player));

        beginRoundChoice(serverSomething, game);
        cellCapture(serverSomething, game);
        distribution(serverSomething, game);

        /* "Затухание" (дезактивация) данных игрока в конце раунда */
        GameLoopProcessor.playerRoundEndUpdate(serverSomething.player);
    }

    /**
     * Выбор в начале раунда
     *
     * @param serverSomething - клиент текущего игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     * @throws IOException    в случае ошибки общения с клиентом
     * @throws CoinsException в случае игровой ошибки
     */
    private void beginRoundChoice(final ServerSomething serverSomething, final IGame game) throws IOException, CoinsException {
        final GameQuestion declineRaceQuestion = new GameQuestion(QuestionType.DECLINE_RACE,
                game, serverSomething.player);
        serverSomething.send(Communication.serializeQuestion(declineRaceQuestion));
        final DeclineRaceAnswer answer = Communication.deserializeDeclineRaceAnswer(serverSomething.read());
        GameAnswerProcessor.process(declineRaceQuestion, answer);
        if (answer.isDeclineRace()) {
            final GameQuestion changeRaceQuestion = new GameQuestion(QuestionType.CHANGE_RACE,
                    game, serverSomething.player);
            serverSomething.send(Communication.serializeQuestion(changeRaceQuestion));
            GameAnswerProcessor.process(changeRaceQuestion,
                    Communication.deserializeChangeRaceAnswer(serverSomething.read()));
        }
    }

    /**
     * Захват клеток
     *
     * @param serverSomething - клиент текущего игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     * @throws IOException    в случае ошибки общения с клиентом
     * @throws CoinsException в случае игровой ошибки
     */
    private void cellCapture(final ServerSomething serverSomething, final IGame game) throws CoinsException, IOException {
        GameQuestion catchCellQuestion = new GameQuestion(QuestionType.CATCH_CELL, game, serverSomething.player);
        serverSomething.send(Communication.serializeQuestion(catchCellQuestion));
        CatchCellAnswer catchCellAnswer = Communication.deserializeCatchCellAnswer(serverSomething.read());
        while (catchCellAnswer.getResolution() != null) {
            GameAnswerProcessor.process(catchCellQuestion, catchCellAnswer);
            catchCellQuestion = new GameQuestion(QuestionType.CATCH_CELL, game, serverSomething.player);
            serverSomething.send(Communication.serializeQuestion(catchCellQuestion));
            catchCellAnswer = Communication.deserializeCatchCellAnswer(serverSomething.read());
        }
    }

    /**
     * Распределение войск
     *
     * @param serverSomething - клиент текущего игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     * @throws IOException    в случае ошибки общения с клиентом
     * @throws CoinsException в случае игровой ошибки
     */
    private void distribution(final ServerSomething serverSomething, final IGame game)
            throws IOException, CoinsException {

        final GameQuestion distributionQuestion = new GameQuestion(QuestionType.DISTRIBUTION_UNITS,
                game, serverSomething.player);
        serverSomething.send(Communication.serializeQuestion(distributionQuestion));
        GameAnswerProcessor.process(distributionQuestion,
                Communication.deserializeDistributionUnitsAnswer(serverSomething.read()));
    }

    public static void main(final String[] args) {
        final Server server = new Server();
        server.startServer();
    }
}
