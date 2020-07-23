package io.neolab.internship.coins.server;

import io.neolab.internship.coins.common.answer.NicknameAnswer;
import io.neolab.internship.coins.common.serialization.Communication;
import io.neolab.internship.coins.common.answer.CatchCellAnswer;
import io.neolab.internship.coins.common.answer.DeclineRaceAnswer;
import io.neolab.internship.coins.common.question.*;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.service.*;
import io.neolab.internship.coins.utils.LogCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
            // если потоку ввода/вывода приведут к генерированию исключения, оно пробросится дальше
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            final ServerMessage serverMessage = new ServerMessage(ServerMessageType.NICKNAME);
            out.write(Communication.serializeServerMessage(serverMessage) + "\n");
            out.flush();
            String nickname;
            boolean isDuplicate = false;
            do {
                nickname = ((NicknameAnswer) Communication.deserializeAnswer(in.readLine())).getNickname();
                for (final ServerSomething serverSomething : server.serverList) {
                    isDuplicate = isDuplicate || nickname.equals(serverSomething.player.getNickname());
                }
            } while (isDuplicate);
            LOGGER.info("Nickname for player: {} ", nickname);
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
        try (final GameLoggerFile ignored = new GameLoggerFile()) {
            LogCleaner.clean();

            LOGGER.info("Server started, port: {}", PORT);
            connectClients();
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
                GameLogger.printRoundBeginLog(game.getCurrentRound());
                for (final ServerSomething serverSomething : serverList) {
                    GameLogger.printNextPlayerLog(serverSomething.player);
                    playerRound(serverSomething, game); // раунд игрока. Все свои решения он принимает здесь
                }

                /* обновление числа монет у каждого игрока */
                game.getPlayers()
                        .forEach(player ->
                                GameLoopProcessor.updateCoinsCount(player, game.getFeudalToCells().get(player),
                                        game.getGameFeatures(),
                                        game.getBoard()));

                GameLogger.printRoundEndLog(game.getCurrentRound(), game.getPlayers(), game.getOwnToCells(),
                        game.getFeudalToCells());
            }

            final List<Player> winners = GameFinalizer.finalize(game.getPlayers());
            final GameOverMessage gameOverMessage =
                    new GameOverMessage(ServerMessageType.GAME_OVER, winners, game.getPlayers());
            for (final ServerSomething serverSomething : serverList) {
                serverSomething.send(Communication.serializeServerMessage(gameOverMessage));
                serverSomething.downService();
            }
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
            int currentClientsCount = 1;
            while (currentClientsCount <= CLIENTS_COUNT) {
                final Socket socket = serverSocket.accept();
                try {
                    LOGGER.info("Client {} connects", currentClientsCount);
                    serverList.add(new ServerSomething(this, socket));
                    LOGGER.info("Client {} connected", currentClientsCount);
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

        final PlayerQuestion playerQuestion
                = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, serverSomething.player);
        serverSomething.send(Communication.serializeServerMessage(playerQuestion));
        GameAnswerProcessor.process(playerQuestion, Communication.deserializeAnswer(serverSomething.read()));
    }

    /**
     * Раунд в исполнении игрока
     *
     * @param serverSomething - клиент игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     */
    private void playerRound(final ServerSomething serverSomething, final IGame game)
            throws IOException {

        /* Активация данных игрока в начале раунда */
        GameLoopProcessor.playerRoundBeginUpdate(serverSomething.player);

        beginRoundChoice(serverSomething, game);
        captureCell(serverSomething, game);
        distributionUnits(serverSomething, game);

        /* "Затухание" (дезактивация) данных игрока в конце раунда */
        GameLoopProcessor.playerRoundEndUpdate(serverSomething.player);
    }

    /**
     * Выбор в начале раунда
     *
     * @param serverSomething - клиент текущего игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     * @throws IOException в случае ошибки общения с клиентом
     */
    private void beginRoundChoice(final ServerSomething serverSomething, final IGame game) throws IOException {
        while (true) {
            try {
                final PlayerQuestion declineRaceQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                        PlayerQuestionType.DECLINE_RACE, game, serverSomething.player);
                serverSomething.send(Communication.serializeServerMessage(declineRaceQuestion));
                final DeclineRaceAnswer answer = (DeclineRaceAnswer) Communication.deserializeAnswer(serverSomething.read());
                GameAnswerProcessor.process(declineRaceQuestion, answer);
                if (answer.isDeclineRace()) {
                    final PlayerQuestion changeRaceQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                            PlayerQuestionType.CHANGE_RACE, game, serverSomething.player);
                    serverSomething.send(Communication.serializeServerMessage(changeRaceQuestion));
                    GameAnswerProcessor.process(changeRaceQuestion,
                            Communication.deserializeAnswer(serverSomething.read()));
                }
                break;
            } catch (final CoinsException ignored) {
            }
        }
    }

    /**
     * Захват клеток
     *
     * @param serverSomething - клиент текущего игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     * @throws IOException в случае ошибки общения с клиентом
     */
    private void captureCell(final ServerSomething serverSomething, final IGame game) throws IOException {
        final Player player = serverSomething.player;
        final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
        GameLoopProcessor.updateAchievableCells(
                player, game.getBoard(), achievableCells, game.getOwnToCells().get(player));
        while (true) {
            try {
                PlayerQuestion catchCellQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                        PlayerQuestionType.CATCH_CELL, game, player);
                serverSomething.send(Communication.serializeServerMessage(catchCellQuestion));
                CatchCellAnswer catchCellAnswer = (CatchCellAnswer) Communication.deserializeAnswer(serverSomething.read());
                while (catchCellAnswer.getResolution() != null) {
                    GameAnswerProcessor.process(catchCellQuestion, catchCellAnswer);
                    catchCellQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                            PlayerQuestionType.CATCH_CELL, game, player);
                    serverSomething.send(Communication.serializeServerMessage(catchCellQuestion));
                    catchCellAnswer = (CatchCellAnswer) Communication.deserializeAnswer(serverSomething.read());
                }
                break;
            } catch (final CoinsException ignored) {
            }
        }
    }

    /**
     * Распределение войск
     *
     * @param serverSomething - клиент текущего игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     * @throws IOException    в случае ошибки общения с клиентом
     */
    private void distributionUnits(final ServerSomething serverSomething, final IGame game)
            throws IOException {

        final Player player = serverSomething.player;
        GameLoopProcessor.freeTransitCells(player, game.getPlayerToTransitCells().get(player),
                game.getOwnToCells().get(player));
        if (!game.getOwnToCells().get(player).isEmpty()) {
            while (true) {
                try {
                    final PlayerQuestion distributionQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                            PlayerQuestionType.DISTRIBUTION_UNITS, game, serverSomething.player);
                    serverSomething.send(Communication.serializeServerMessage(distributionQuestion));
                    GameAnswerProcessor.process(distributionQuestion,
                            Communication.deserializeAnswer(serverSomething.read()));
                    break;
                } catch (final CoinsException ignored) {
                }
            }
        }
    }

    public static void main(final String[] args) {
        final IServer server = new Server();
        server.startServer();
    }
}
