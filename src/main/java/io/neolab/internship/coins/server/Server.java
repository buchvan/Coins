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
    private static final int CLIENTS_COUNT_IN_LOBBY = 2;
    private static final int GAME_LOBBIES_COUNT = 3;
    private static final int GAMES_COUNT = 3;
    private static final int CLIENT_DISCONNECT_ATTEMPTS = 2;
    private static final int TIMEOUT_IN_MILLIS = 1000;

    private static final int BOARD_SIZE_X = 3;
    private static final int BOARD_SIZE_Y = 4;

    private final @NotNull List<GameLobby> gameLobbies = new LinkedList<>();

    private class GameLobby {
        private final int lobbyId;
        private final @NotNull ConcurrentLinkedQueue<ServerSomething> gameClients = new ConcurrentLinkedQueue<>();
        private final int clientsCount;
        private final int gamesCount;

        public GameLobby(final int lobbyId, final int clientsCount, final int gamesCount) {
            this.lobbyId = lobbyId;
            this.clientsCount = clientsCount;
            this.gamesCount = gamesCount;
        }

        /**
         * Подключить клиентов к лобби
         *
         * @throws IOException при ошибке подключения
         */
        private void connectClients()
                throws IOException, CoinsException {

            int currentClientsCount = 1;
            final ExecutorService clientConnectors = Executors.newFixedThreadPool(clientsCount);
            while (currentClientsCount <= clientsCount) {
                final int currentClientId = currentClientsCount;
                clientConnectors.execute(() -> connectClient(currentClientId, gameClients));
                currentClientsCount++;
            }
            try (final LoggerFile ignored = new LoggerFile("lobby-" + lobbyId + "_connecting")) {
                clientConnectors.shutdown();
                clientConnectors.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                LOGGER.info("All clients of lobby {} is connected", lobbyId);
                handShake();
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
            try (final LoggerFile ignored = new LoggerFile("lobby-" + lobbyId + "_connecting")) {
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
        }

        /**
         * HandShake: спросить клиентов о готовности к игре
         *
         * @throws IOException    при ошибке общения с клиентом
         * @throws CoinsException если клиент ответил "не готов"
         */
        private void handShake()
                throws IOException, CoinsException {
            final ServerMessage question = new ServerMessage(ServerMessageType.CONFIRMATION_OF_READINESS);
            for (final ServerSomething serverSomething : gameClients) {
                serverSomething.sendServerMessage(question);
                final ClientMessage clientMessage = serverSomething.readClientMessage();
                if (clientMessage.getMessageType() != ClientMessageType.GAME_READY) {
                    throw new CoinsException(CoinsErrorCode.CLIENT_DISCONNECTION);
                }
            }
            LOGGER.info("All clients of lobby {} is ready", lobbyId);
        }

        /**
         * Отключить всех клиентов игры
         */
        private void disconnectLobbyClients() {
            try (final LoggerFile ignored = new LoggerFile("lobby-" + lobbyId + "_disconnecting")) {
                gameClients.forEach(this::disconnectClient);
                gameLobbies.remove(this);
                LOGGER.info("All clients of lobby {} disconnected", lobbyId);
            }
        }

        /**
         * Отключить клиента
         *
         * @param serverSomething - клиент, которого необходимо отключить
         */
        private void disconnectClient(final @NotNull ServerSomething serverSomething) {
            try {
                serverSomething.sendServerMessage(new ServerMessage(ServerMessageType.DISCONNECTED));
                int i = 0;
                do {
                    if (serverSomething.isCameClientMessage() &&
                            serverSomething.readClientMessage().getMessageType() == ClientMessageType.DISCONNECTED) {
                        break;
                    }
                    Thread.sleep(TIMEOUT_IN_MILLIS);
                    i++;
                } while (i < CLIENT_DISCONNECT_ATTEMPTS);
            } catch (final IOException | InterruptedException exception) {
                LOGGER.error("Error!!!", exception);
            }
            serverSomething.downService();
            LOGGER.info("Client {} disconnected", serverSomething.player.getNickname());
        }
    }

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
            in = ClientServerProcessor.initReaderBySocket(socket);
            out = ClientServerProcessor.initWriterBySocket(socket);

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
         * Пришло сообщение от клиента?
         *
         * @return true, если да, false - иначе
         */
        private boolean isCameClientMessage() throws IOException {
            return in.ready();
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
            try {
                LogCleaner.clean();
                LOGGER.info("Server started, port: {}", PORT);
                final ExecutorService threadPool = Executors.newFixedThreadPool(GAME_LOBBIES_COUNT);
                for (int i = 1; i <= GAME_LOBBIES_COUNT; i++) {
                    final int lobbyId = i;
                    threadPool.execute(() -> startLobby(lobbyId));
                }
                threadPool.shutdown();
                threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (final IOException | InterruptedException exception) {
                LOGGER.error("Error!!!", exception);
            } finally {
                disconnectAllClients();
                LOGGER.info("Server finished");
            }
        }
    }

    /**
     * Отключить всех клиентов
     */
    private void disconnectAllClients() {
        gameLobbies.forEach(GameLobby::disconnectLobbyClients);
        gameLobbies.clear();
        LOGGER.info("All clients disconnected");
    }

    /**
     * @param lobbyId - id лобби
     */
    private void startLobby(final int lobbyId) {
        final GameLobby gameLobby = new GameLobby(lobbyId, CLIENTS_COUNT_IN_LOBBY, GAMES_COUNT);
        try {
            synchronized (GameLobby.class) { // чтобы лобби пополнялись по ходу подключений клиентов
                gameLobby.connectClients();
            }
            int currentGame = 1;
            while (currentGame <= gameLobby.gamesCount) {
                startGame(currentGame, gameLobby);
                currentGame++;
            }
        } catch (final CoinsException | ClassCastException | IOException exception) {
            if (exception instanceof CoinsException) {
                if (((CoinsException) exception).getErrorCode() != CoinsErrorCode.CLIENT_DISCONNECTION) {
                    LOGGER.error("Error!!!", exception);
                } else {
                    LOGGER.info("Client disconnection");
                    LOGGER.debug("", exception);
                }
            }
        } finally {
            gameLobby.disconnectLobbyClients();
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
     * @param gameId    - id игры
     * @param gameLobby - лобби игры
     * @throws CoinsException при внутренней ошибке игры
     * @throws IOException    при ошибке общения с каким-либо клиентом
     */
    private void startGame(final int gameId, final @NotNull GameLobby gameLobby) throws CoinsException, IOException {
        try (final LoggerFile ignored = new LoggerFile("lobby-" + gameLobby.lobbyId + "_game-" + gameId)) {
            final IGame game = gameInit(gameLobby.gameClients);
            GameLogger.printStartGameChoiceLog();
            for (final ServerSomething serverSomething : gameLobby.gameClients) {
                chooseRace(serverSomething, game);
            }
            gameLoop(game, gameLobby.gameClients);
            finalizeGame(game, gameLobby);
        }
    }

    /**
     * Инициализация игры
     *
     * @param clients - клиенты игры
     * @return инициализированную игру
     * @throws CoinsException при ошибке инициализации игры
     */
    private IGame gameInit(final @NotNull ConcurrentLinkedQueue<ServerSomething> clients) throws CoinsException {
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
     * @param game      - игра
     * @param gameLobby - игровое лобби
     */
    private void finalizeGame(final @NotNull IGame game,
                              final @NotNull GameLobby gameLobby)
            throws CoinsException, IOException {
        final List<Player> winners = GameFinalizer.finalization(game.getPlayers());
        final GameOverMessage gameOverMessage =
                new GameOverMessage(ServerMessageType.GAME_OVER, winners, game.getPlayers());
        for (final ServerSomething serverSomething : gameLobby.gameClients) {
            serverSomething.sendServerMessage(gameOverMessage);
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
        final ClientMessage clientMessage = serverSomething.readClientMessage();
        checkOnDisconnect(clientMessage);
        GameAnswerProcessor.process(playerQuestion, (Answer) clientMessage);
    }

    /**
     * Проверка на сообщение об отключении
     *
     * @param clientMessage - сообщение от клиента
     * @throws CoinsException если клиент сообщил об отключении
     */
    private void checkOnDisconnect(final @NotNull ClientMessage clientMessage) throws CoinsException {
        if (clientMessage.getMessageType() == ClientMessageType.DISCONNECTED) {
            throw new CoinsException(CoinsErrorCode.CLIENT_DISCONNECTION);
        }
    }

    /**
     * Раунд в исполнении игрока
     *
     * @param serverSomething - клиент игрока
     * @param game            - объект, хранящий всю метаинформацию об игре
     */
    private void playerRound(final @NotNull ServerSomething serverSomething, final @NotNull IGame game)
            throws IOException, CoinsException {
        final Player player = serverSomething.player;

        /* Активация данных игрока в начале раунда */
        GameLoopProcessor.playerRoundBeginUpdate(player);

        beginRoundChoice(serverSomething, game);
        captureCell(serverSomething, game);
        distributionUnits(serverSomething, game);

        /* "Затухание" (дезактивация) данных игрока в конце раунда */
        GameLoopProcessor.playerRoundEndUpdate(player);
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
        final Player player = serverSomething.player;
        final PlayerQuestion declineRaceQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                PlayerQuestionType.DECLINE_RACE, game, player);
        serverSomething.sendServerMessage(declineRaceQuestion);
        final ClientMessage clientMessage = serverSomething.readClientMessage();
        checkOnDisconnect(clientMessage);
        final Answer answer = (Answer) clientMessage;
        GameAnswerProcessor.process(declineRaceQuestion, answer);
        if (((DeclineRaceAnswer) answer).isDeclineRace()) {
            final PlayerQuestion changeRaceQuestion = new PlayerQuestion(ServerMessageType.GAME_QUESTION,
                    PlayerQuestionType.CHANGE_RACE, game, player);
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
        final ClientMessage clientMessage = serverSomething.readClientMessage();
        checkOnDisconnect(clientMessage);
        GameAnswerProcessor.process(changeRaceQuestion, (Answer) clientMessage);
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
        processCaptureCell(serverSomething, game);
    }

    /**
     * Процесс взаимодействия с клиентом при захвате клеток
     *
     * @param serverSomething - клиент текущего игрока
     * @param game            - игра
     * @throws IOException в случае ошибки общения с клиентом
     */
    private void processCaptureCell(final @NotNull ServerSomething serverSomething,
                                    final @NotNull IGame game) throws IOException, CoinsException {
        final Player player = serverSomething.player;
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
        final ClientMessage clientMessage = serverSomething.readClientMessage();
        checkOnDisconnect(clientMessage);
        return (CatchCellAnswer) clientMessage;
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
        GameLoopProcessor.loseCells(game.getOwnToCells().get(player), game.getOwnToCells().get(player),
                game.getFeudalToCells().get(player));
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
        final ClientMessage clientMessage = serverSomething.readClientMessage();
        checkOnDisconnect(clientMessage);
        GameAnswerProcessor.process(distributionQuestion, (Answer) clientMessage);
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
