package io.neolab.internship.coins.server;

import io.neolab.internship.coins.common.answer.*;
import io.neolab.internship.coins.common.serialization.Communication;
import io.neolab.internship.coins.common.answer.CatchCellAnswer;
import io.neolab.internship.coins.common.answer.DeclineRaceAnswer;
import io.neolab.internship.coins.common.question.*;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.exceptions.UtilsException;
import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.service.GameAnswerProcessor;
import io.neolab.internship.coins.utils.LoggerFile;
import io.neolab.internship.coins.server.service.*;
import io.neolab.internship.coins.utils.LogCleaner;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server implements IServer {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static final int PORT = 8081;
    private static final int CLIENTS_COUNT = 2;
    private static final int GAMES_COUNT = 1;

    private static final int BOARD_SIZE_X = 3;
    private static final int BOARD_SIZE_Y = 4;

    private final @NotNull Map<Integer, ConcurrentLinkedQueue<ServerSomething>> serverSomethings = new HashMap<>();

    private static class ServerSomething {

        private final @NotNull Socket socket;
        private final @NotNull BufferedReader in; // поток чтения из сокета
        private final @NotNull BufferedWriter out; // поток записи в сокет
        private final @NotNull Player player;

        /**
         * Для общения с клиентом необходим сокет (адресные данные)
         *
         * @param clients - список уже подключённых к игре клиентов
         * @param socket     - сокет
         */
        private ServerSomething(final @NotNull ConcurrentLinkedQueue<ServerSomething> clients, final @NotNull Socket socket)
                throws IOException {
            this.socket = socket;
            // если потоку ввода/вывода приведут к генерированию исключения, оно пробросится дальше
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            ServerMessage serverMessage = new ServerMessage(ServerMessageType.NICKNAME);
            out.write(Communication.serializeServerMessage(serverMessage) + "\n");
            out.flush();
            String nickname;
            boolean isDuplicate = false;
            nickname = ((NicknameAnswer) Communication.deserializeClientMessage(in.readLine())).getNickname();
            for (final ServerSomething serverSomething : clients) {
                isDuplicate = isDuplicate || nickname.equals(serverSomething.player.getNickname());
            }
            while (isDuplicate) {
                serverMessage = new ServerMessage(ServerMessageType.NICKNAME_DUPLICATE);
                out.write(Communication.serializeServerMessage(serverMessage) + "\n");
                out.flush();
                nickname = ((NicknameAnswer) Communication.deserializeClientMessage(in.readLine())).getNickname();
                isDuplicate = false;
                for (final ServerSomething serverSomething : clients) {
                    isDuplicate = isDuplicate || nickname.equals(serverSomething.player.getNickname());
                }
            }
            LOGGER.info("Nickname for player: {} ", nickname);
            player = new Player(nickname);
        }

        /**
         * Отправить сообщение клиенту
         *
         * @param message - сообщение
         * @throws IOException в случае ошибки отправки сообщения
         */
        private void send(final @NotNull String message) throws IOException {
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

    @Override
    public void startServer() {
        try (final LoggerFile ignored = new LoggerFile("server")) {
            LogCleaner.clean();

            LOGGER.info("Server started, port: {}", PORT);
            final ExecutorService threadPool = Executors.newFixedThreadPool(GAMES_COUNT);
            for (int i = 1; i <= GAMES_COUNT; i++) {
                final int gameId = i;
                threadPool.execute(() -> startGame(gameId));
            }
            threadPool.shutdown();
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (final IOException | InterruptedException exception) {
            LOGGER.error("Error!!!", exception);
            serverSomethings.values()
                    .forEach(serverSomethingsList -> serverSomethingsList
                            .forEach(ServerSomething::downService));
            serverSomethings.clear();
        }
    }

    /**
     * @param gameId - id игры
     */
    private void startGame(final int gameId) {
        final ConcurrentLinkedQueue<ServerSomething> clients = new ConcurrentLinkedQueue<>();
        serverSomethings.put(gameId, clients);
        try (final LoggerFile ignored = new LoggerFile("game_" + gameId)) {
            connectClients(clients);
            final List<Player> playerList = new LinkedList<>();
            clients.forEach(serverSomething -> playerList.add(serverSomething.player));
            final IGame game = GameInitializer.gameInit(BOARD_SIZE_X, BOARD_SIZE_Y, playerList);
            GameLogger.printGameCreatedLog(game);

            GameLogger.printStartGameChoiceLog();
            for (final ServerSomething serverSomething : clients) {
                chooseRace(serverSomething, game);
            }
            while (game.getCurrentRound() < Game.ROUNDS_COUNT) {
                game.incrementCurrentRound();
                GameLogger.printRoundBeginLog(game.getCurrentRound());
                for (final ServerSomething serverSomething : clients) {
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
            for (final ServerSomething serverSomething : clients) {
                serverSomething.send(Communication.serializeServerMessage(gameOverMessage));
                serverSomething.downService();
            }
        } catch (final CoinsException | ClassCastException | IOException | UtilsException exception) {
            LOGGER.error("Error!!!", exception);
            clients.forEach(ServerSomething::downService);
            serverSomethings.remove(gameId);
        }
    }

    /**
     * Подключает клиентов к серверу
     *
     * @param clients - очередь клиентов игры
     * @throws IOException при ошибке подключения
     */
    private synchronized void connectClients(final ConcurrentLinkedQueue<ServerSomething> clients) throws IOException, CoinsException {
        try (final ServerSocket serverSocket = new ServerSocket(PORT)) {
            int currentClientsCount = 1;
            while (currentClientsCount <= CLIENTS_COUNT) {
                final Socket socket = serverSocket.accept();
                try {
                    LOGGER.info("Client {} connects", currentClientsCount);
                    clients.add(new ServerSomething(clients, socket));
                    LOGGER.info("Client {} connected", currentClientsCount);
                    currentClientsCount++;
                } catch (final IOException exception) {
                    LOGGER.error("Error!", exception);
                    socket.close();
                }
            }
            handShake(clients);
        } catch (final BindException exception) {
            LOGGER.error("Error!", exception);
        }
    }

    /**
     * HandShake
     *
     * @param clients - очередь клиентов игры
     * @throws IOException    при ошибке общения с клиентом
     * @throws CoinsException если клиент ответил "не готов"
     */
    private void handShake(final ConcurrentLinkedQueue<ServerSomething> clients) throws IOException, CoinsException {
        final ServerMessage question = new ServerMessage(ServerMessageType.CONFIRMATION_OF_READINESS);
        for (final ServerSomething serverSomething : clients) {
            serverSomething.send(Communication.serializeServerMessage(question));
            final Answer answer = (Answer) Communication.deserializeClientMessage(serverSomething.read());
            if (answer.getMessageType() != ClientMessageType.GAME_READY) {
                throw new CoinsException(CoinsErrorCode.CLIENT_DISCONNECTION);
            }
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
    private void chooseRace(final @NotNull ServerSomething serverSomething, final @NotNull IGame game)
            throws IOException, CoinsException, UtilsException {

        final PlayerQuestion playerQuestion
                = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CHANGE_RACE, game, serverSomething.player);
        serverSomething.send(Communication.serializeServerMessage(playerQuestion));
        GameAnswerProcessor.process(playerQuestion,
                (Answer) Communication.deserializeClientMessage(serverSomething.read()));
    }

    /**
     * Раунд в исполнении игрока
     *
     * @param serverSomething - клиент игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     */
    private void playerRound(final @NotNull ServerSomething serverSomething, final @NotNull IGame game)
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
    private void beginRoundChoice(final @NotNull ServerSomething serverSomething, final @NotNull IGame game)
            throws IOException {
        while (true) {
            try {
                final PlayerQuestion declineRaceQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                        PlayerQuestionType.DECLINE_RACE, game, serverSomething.player);
                serverSomething.send(Communication.serializeServerMessage(declineRaceQuestion));
                final DeclineRaceAnswer answer =
                        (DeclineRaceAnswer) Communication.deserializeClientMessage(serverSomething.read());
                GameAnswerProcessor.process(declineRaceQuestion, answer);
                if (answer.isDeclineRace()) {
                    final PlayerQuestion changeRaceQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                            PlayerQuestionType.CHANGE_RACE, game, serverSomething.player);
                    serverSomething.send(Communication.serializeServerMessage(changeRaceQuestion));
                    GameAnswerProcessor.process(changeRaceQuestion,
                            (Answer) Communication.deserializeClientMessage(serverSomething.read()));
                }
                break;
            } catch (final CoinsException | UtilsException ignored) {
                // TODO: сообщение клиенту, что он что-то сделал неправильно
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
    private void captureCell(final @NotNull ServerSomething serverSomething, final @NotNull IGame game)
            throws IOException {
        final Player player = serverSomething.player;
        final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
        GameLoopProcessor.updateAchievableCells(
                player, game.getBoard(), achievableCells, game.getOwnToCells().get(player));
        while (true) {
            try {
                PlayerQuestion catchCellQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                        PlayerQuestionType.CATCH_CELL, game, player);
                serverSomething.send(Communication.serializeServerMessage(catchCellQuestion));
                CatchCellAnswer catchCellAnswer =
                        (CatchCellAnswer) Communication.deserializeClientMessage(serverSomething.read());
                while (catchCellAnswer.getResolution() != null) {
                    GameAnswerProcessor.process(catchCellQuestion, catchCellAnswer);
                    catchCellQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                            PlayerQuestionType.CATCH_CELL, game, player);
                    serverSomething.send(Communication.serializeServerMessage(catchCellQuestion));
                    catchCellAnswer = (CatchCellAnswer) Communication.deserializeClientMessage(serverSomething.read());
                }
                break;
            } catch (final CoinsException | UtilsException ignored) {
                // TODO: сообщение клиенту, что он что-то сделал неправильно
            }
        }
    }

    /**
     * Распределение войск
     *
     * @param serverSomething - клиент текущего игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     * @throws IOException в случае ошибки общения с клиентом
     */
    private void distributionUnits(final @NotNull ServerSomething serverSomething, final @NotNull IGame game)
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
                            (Answer) Communication.deserializeClientMessage(serverSomething.read()));
                    break;
                } catch (final CoinsException | UtilsException ignored) {
                    // TODO: сообщение клиенту, что он что-то сделал неправильно
                }
            }
        }
    }

    public static void main(final String[] args) {
        final IServer server = new Server();
        server.startServer();
    }
}
