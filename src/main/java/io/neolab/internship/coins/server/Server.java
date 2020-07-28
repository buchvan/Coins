package io.neolab.internship.coins.server;

import io.neolab.internship.coins.common.message.client.ClientMessage;
import io.neolab.internship.coins.common.message.client.answer.*;
import io.neolab.internship.coins.common.message.client.ClientMessageType;
import io.neolab.internship.coins.common.message.server.GameOverMessage;
import io.neolab.internship.coins.common.message.server.ServerMessage;
import io.neolab.internship.coins.common.message.server.ServerMessageType;
import io.neolab.internship.coins.common.serialization.Communication;
import io.neolab.internship.coins.common.message.server.question.*;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.server.game.*;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.service.GameAnswerProcessor;
import io.neolab.internship.coins.server.service.GameLogger;
import io.neolab.internship.coins.utils.ClientServerProcessor;
import io.neolab.internship.coins.utils.LoggerFile;
import io.neolab.internship.coins.server.service.*;
import io.neolab.internship.coins.utils.LogCleaner;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
    private static final int GAMES_COUNT = 2;

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
         * @param socket  - сокет
         */
        private ServerSomething(final @NotNull ConcurrentLinkedQueue<ServerSomething> clients,
                                final @NotNull Socket socket) throws IOException {
            this.socket = socket;
            final Pair<BufferedReader, BufferedWriter> ioPair = ClientServerProcessor.initReaderWriterBySocket(socket);
            in = ioPair.getFirst();
            out = ioPair.getSecond();

            String nickname;
            boolean isDuplicate = false;
            do {
                nickname = enterNickname(isDuplicate);
                isDuplicate = isDuplicateNickname(nickname, clients);
            } while (isDuplicate);

            LOGGER.info("Nickname for player: {} ", nickname);
            player = new Player(nickname);
        }

        /**
         * Ввод никнэйма клиентом
         *
         * @param isDuplicate - если никнэйм был дубликатом
         * @return никнэйм клиента
         * @throws IOException при ошибке связи с клиентом
         */
        private String enterNickname(final boolean isDuplicate) throws IOException {
            sendServerMessage(new ServerMessage(
                    !isDuplicate
                            ? ServerMessageType.NICKNAME
                            : ServerMessageType.NICKNAME_DUPLICATE)
            );
            return ((NicknameAnswer) readClientMessage()).getNickname();
        }

        /**
         * Является ли никнэйм дубликатом другого?
         *
         * @param nickname - никнэйм, который необходимо проверить
         * @param clients  - список клиентов, с чьими никнэймами необходимо свериться
         * @return true, если никнэйм является дубликатом, false - иначе
         */
        private boolean isDuplicateNickname(final @NotNull String nickname,
                                            final @NotNull ConcurrentLinkedQueue<ServerSomething> clients) {
            boolean isDuplicate = false;
            for (final ServerSomething serverSomething : clients) {
                isDuplicate = isDuplicate || nickname.equals(serverSomething.player.getNickname());
            }
            return isDuplicate;
        }

        /**
         * Отправить сообщение клиенту
         *
         * @param message - сообщение
         * @throws IOException в случае ошибки отправки сообщения
         */
        private void sendServerMessage(final @NotNull ServerMessage message) throws IOException {
            LOGGER.info("Output message: {} ", message);
            ClientServerProcessor.sendMessage(out, Communication.serializeServerMessage(message));
        }

        /**
         * Прочитать сообщение от клиента
         *
         * @return сообщение от клиента
         * @throws IOException в случае ошибки получения сообщения
         */
        private ClientMessage readClientMessage() throws IOException {
            final ClientMessage clientMessage = Communication.deserializeClientMessage(in.readLine());
            LOGGER.info("Input message: {} ", clientMessage);
            return clientMessage;
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
            disconnectAllClients();
        }
    }

    /**
     * Отключить всех клиентов
     */
    private void disconnectAllClients() {
        serverSomethings.forEach((key, value) -> disconnectGameClients(key));
        serverSomethings.clear();
        LOGGER.info("All clients disconnected");
    }

    /**
     * @param gameId - id игры
     */
    private void startGame(final int gameId) {
        final ConcurrentLinkedQueue<ServerSomething> clients = new ConcurrentLinkedQueue<>();
        serverSomethings.put(gameId, clients);
        try (final LoggerFile ignored = new LoggerFile("game_" + gameId)) {
            final IGame game = gameInit(clients);
            GameLogger.printStartGameChoiceLog();
            for (final ServerSomething serverSomething : clients) {
                chooseRace(serverSomething, game);
            }
            gameLoop(game, clients);
            finalizeGame(gameId, game, clients);
        } catch (final CoinsException exception) {
            if (exception.getErrorCode() != CoinsErrorCode.CLIENT_DISCONNECTION) {
                LOGGER.error("Error!!!", exception);
            } else {
                LOGGER.info("Client disconnection");
                LOGGER.debug("", exception);
            }
            disconnectGameClients(gameId);
        } catch (final ClassCastException | IOException exception) {
            LOGGER.error("Error!!!", exception);
            disconnectGameClients(gameId);
        }
    }

    /**
     * Отключить всех клиентов игры
     *
     * @param gameId - id игры
     */
    private void disconnectGameClients(final int gameId) {
        serverSomethings.get(gameId).forEach(serverSomething -> {
            try {
                serverSomething.sendServerMessage(new ServerMessage(ServerMessageType.DISCONNECTED));
                ClientMessage clientMessage;
                do {
                    clientMessage = serverSomething.readClientMessage();
                } while (clientMessage.getMessageType() != ClientMessageType.DISCONNECTED);
            } catch (final IOException exception) {
                LOGGER.error("Error!!!", exception);
            }
            serverSomething.downService();
            LOGGER.info("Client {} disconnected", serverSomething.player.getNickname());
        });
        serverSomethings.remove(gameId);
        LOGGER.info("Clients of game {} disconnected", gameId);
    }

    /**
     * Инициализация игры
     *
     * @param clients - клиенты игры
     * @return инициализированную игру
     * @throws IOException    при ошибке общения с каким-либо клиентом
     * @throws CoinsException при ошибке инициализации игры
     */
    private IGame gameInit(final @NotNull ConcurrentLinkedQueue<ServerSomething> clients)
            throws IOException, CoinsException {
        connectClients(clients);
        final List<Player> playerList = new LinkedList<>();
        clients.forEach(serverSomething -> playerList.add(serverSomething.player));
        final IGame game = GameInitializer.gameInit(BOARD_SIZE_X, BOARD_SIZE_Y, playerList);
        GameLogger.printGameCreatedLog(game);
        return game;
    }

    /**
     * Игровой цикл
     *
     * @param game    - собственно, игра
     * @param clients - список клиентов, играющих в данную игру
     * @throws IOException при ошибке связи с каким-либо клиентом
     */
    private void gameLoop(final @NotNull IGame game,
                          final @NotNull ConcurrentLinkedQueue<ServerSomething> clients)
            throws IOException, CoinsException {
        while (game.getCurrentRound() < Game.ROUNDS_COUNT) {
            game.incrementCurrentRound();
            GameLogger.printRoundBeginLog(game.getCurrentRound());
            for (final ServerSomething serverSomething : clients) {
                GameLogger.printNextPlayerLog(serverSomething.player);
                playerRound(serverSomething, game); // раунд игрока. Все свои решения он принимает здесь
            }
            playersCoinsUpdate(game);
            GameLogger.printRoundEndLog(
                    game.getCurrentRound(), game.getPlayers(), game.getOwnToCells(), game.getFeudalToCells());
        }
    }

    /**
     * Финализация игры
     *
     * @param gameId  - id игры
     * @param game    - игра
     * @param clients - клиенты игры
     */
    private void finalizeGame(final int gameId, final @NotNull IGame game,
                              final @NotNull ConcurrentLinkedQueue<ServerSomething> clients)
            throws CoinsException, IOException {
        final List<Player> winners = GameFinalizer.finalization(game.getPlayers());
        final GameOverMessage gameOverMessage =
                new GameOverMessage(ServerMessageType.GAME_OVER, winners, game.getPlayers());
        for (final ServerSomething serverSomething : clients) {
            serverSomething.sendServerMessage(gameOverMessage);
        }
        disconnectGameClients(gameId);
    }

    /**
     * Подключить клиентов к серверу
     *
     * @param clients - очередь клиентов игры
     * @throws IOException при ошибке подключения
     */
    private void connectClients(final @NotNull ConcurrentLinkedQueue<ServerSomething> clients)
            throws IOException, CoinsException {

        int currentClientsCount = 1;
        final ExecutorService connectClient = Executors.newFixedThreadPool(CLIENTS_COUNT);
        while (currentClientsCount <= CLIENTS_COUNT) {
            final int currentClientId = currentClientsCount;
            connectClient.execute(() -> connectClient(currentClientId, clients));
            currentClientsCount++;
        }
        try {
            connectClient.shutdown();
            connectClient.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            handShake(clients);
        } catch (final InterruptedException exception) {
            LOGGER.error("Error!", exception);
        }
    }

    /**
     * Подключить клиента
     *
     * @param currentClientId - текущий id клиента
     * @param clients         - очередь клиентов
     */
    private void connectClient(final int currentClientId,
                               final @NotNull ConcurrentLinkedQueue<ServerSomething> clients) {
        Socket socket = null;
        while (true) {
            try {
                socket = getSocket();
                LOGGER.info("Client {} connects", currentClientId);
                clients.add(new ServerSomething(clients, socket));
                LOGGER.info("Client {} connected", currentClientId);
            } catch (final IOException exception) {
                LOGGER.error("Error!", exception);
                if (socket != null) {
                    closeSocket(socket);
                }
                continue;
            }
            break;
        }
    }

    /**
     * Взять (accept) сокет
     *
     * @return взятый сокет
     * @throws IOException при ошибке взятия сокета
     */
    private synchronized @NotNull Socket getSocket() throws IOException {
        try (final ServerSocket serverSocket = new ServerSocket(PORT)) {
            return serverSocket.accept();
        }
    }

    /**
     * Закрыть сокет
     *
     * @param socket - сокет, который необходимо закрыть
     */
    private synchronized void closeSocket(final @NotNull Socket socket) {
        try {
            socket.close();
        } catch (final IOException e) {
            LOGGER.error("Error!", e);
        }
    }

    /**
     * HandShake: спросить клиентов о готовности к игре
     *
     * @param clients - очередь клиентов игры
     * @throws IOException    при ошибке общения с клиентом
     * @throws CoinsException если клиент ответил "не готов"
     */
    private void handShake(final @NotNull ConcurrentLinkedQueue<ServerSomething> clients)
            throws IOException, CoinsException {
        final ServerMessage question = new ServerMessage(ServerMessageType.CONFIRMATION_OF_READINESS);
        for (final ServerSomething serverSomething : clients) {
            serverSomething.sendServerMessage(question);
            final ClientMessage clientMessage = serverSomething.readClientMessage();
            if (clientMessage.getMessageType() != ClientMessageType.GAME_READY) {
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
            throws IOException, CoinsException {

        final PlayerQuestion playerQuestion =
                new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                        PlayerQuestionType.CHANGE_RACE, game, serverSomething.player);
        serverSomething.sendServerMessage(playerQuestion);
        GameAnswerProcessor.process(playerQuestion, castAnswer(serverSomething.readClientMessage()));
    }

    private @NotNull Answer castAnswer(final @NotNull ClientMessage clientMessage) throws CoinsException {
        if (clientMessage.getMessageType() == ClientMessageType.DISCONNECTED) {
            throw new CoinsException(CoinsErrorCode.CLIENT_DISCONNECTION);
        }
        return (Answer) clientMessage;
    }

    /**
     * Раунд в исполнении игрока
     *
     * @param serverSomething - клиент игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     */
    private void playerRound(final @NotNull ServerSomething serverSomething, final @NotNull IGame game)
            throws IOException, CoinsException {

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
            throws IOException, CoinsException {
        final PlayerQuestion declineRaceQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DECLINE_RACE, game, serverSomething.player);
        serverSomething.sendServerMessage(declineRaceQuestion);
        final Answer answer = castAnswer(serverSomething.readClientMessage());
        GameAnswerProcessor.process(declineRaceQuestion, answer);
        if (((DeclineRaceAnswer) answer).isDeclineRace()) {
            final PlayerQuestion changeRaceQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                    PlayerQuestionType.CHANGE_RACE, game, serverSomething.player);
            processChangeRace(changeRaceQuestion, serverSomething);
        }
    }

    /**
     * Обработка смены расы игроком клиента
     *
     * @param changeRaceQuestion - вопрос клиенту о смене расы
     * @param serverSomething    - клиент
     * @throws IOException    в случае ошибки общения с клиентом
     * @throws CoinsException при невалидном ответе от клиента
     */
    private void processChangeRace(final @NotNull PlayerQuestion changeRaceQuestion,
                                   final @NotNull ServerSomething serverSomething) throws IOException, CoinsException {
        serverSomething.sendServerMessage(changeRaceQuestion);
        GameAnswerProcessor.process(changeRaceQuestion, castAnswer(serverSomething.readClientMessage()));
    }

    /**
     * Захват клеток
     *
     * @param serverSomething - клиент текущего игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     * @throws IOException в случае ошибки общения с клиентом
     */
    private void captureCell(final @NotNull ServerSomething serverSomething, final @NotNull IGame game)
            throws IOException, CoinsException {
        final Player player = serverSomething.player;
        final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
        GameLoopProcessor.updateAchievableCells(
                player, game.getBoard(), achievableCells, game.getOwnToCells().get(player));
        processCaptureCell(player, serverSomething, game);
    }

    /**
     * Процесс взаимодействия с клиентом при захвате клеток
     *
     * @param player          - текущий игрок
     * @param serverSomething - клиент текущего игрока
     * @param game            - игра
     * @throws IOException в случае ошибки общения с клиентом
     */
    private void processCaptureCell(final @NotNull Player player, final @NotNull ServerSomething serverSomething,
                                    final @NotNull IGame game) throws IOException, CoinsException {
        PlayerQuestion catchCellQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.CATCH_CELL, game, player);
        CatchCellAnswer catchCellAnswer = getCatchCellAnswer(catchCellQuestion, serverSomething);
        while (catchCellAnswer.getResolution() != null) {
            GameAnswerProcessor.process(catchCellQuestion, catchCellAnswer);
            catchCellQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                    PlayerQuestionType.CATCH_CELL, game, player);
            catchCellAnswer = getCatchCellAnswer(catchCellQuestion, serverSomething);
        }
    }

    /**
     * Взять ответ от клиента на вопрос захвата клетки
     *
     * @param catchCellQuestion - вопрос о захвате клетки
     * @param serverSomething   - клиент
     * @return ответ от клиента на вопрос захвата клетки
     * @throws IOException при ошибке соединения с клиентом
     */
    private CatchCellAnswer getCatchCellAnswer(final @NotNull PlayerQuestion catchCellQuestion,
                                               final @NotNull ServerSomething serverSomething)
            throws IOException, CoinsException {
        serverSomething.sendServerMessage(catchCellQuestion);
        return (CatchCellAnswer) castAnswer(serverSomething.readClientMessage());
    }

    /**
     * Распределение войск
     *
     * @param serverSomething - клиент текущего игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     * @throws IOException в случае ошибки общения с клиентом
     */
    private void distributionUnits(final @NotNull ServerSomething serverSomething, final @NotNull IGame game)
            throws IOException, CoinsException {

        final Player player = serverSomething.player;
        GameLoopProcessor.freeTransitCells(player, game.getPlayerToTransitCells().get(player),
                game.getOwnToCells().get(player));
        if (!game.getOwnToCells().get(player).isEmpty()) { // если есть, где распределять войска
            processDistributionUnits(serverSomething, game);
        }
    }

    /**
     * Процесс взаимодействия с клиентом при распределении войск
     *
     * @param serverSomething - клиент текущего игрока
     * @param game            - игра
     * @throws IOException в случае ошибки общения с клиентом
     */
    private void processDistributionUnits(final @NotNull ServerSomething serverSomething, final @NotNull IGame game)
            throws IOException, CoinsException {
        final PlayerQuestion distributionQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DISTRIBUTION_UNITS, game, serverSomething.player);
        serverSomething.sendServerMessage(distributionQuestion);
        GameAnswerProcessor.process(distributionQuestion, castAnswer(serverSomething.readClientMessage()));
    }

    /**
     * Обновление числа монет у каждого игрока
     *
     * @param game - игра
     */
    private void playersCoinsUpdate(final @NotNull IGame game) {
        game.getPlayers()
                .forEach(player ->
                        GameLoopProcessor.updateCoinsCount(player, game.getFeudalToCells().get(player),
                                game.getGameFeatures(),
                                game.getBoard()));
    }

    public static void main(final String[] args) {
        final IServer server = new Server();
        server.startServer();
    }
}
