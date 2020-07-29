package io.neolab.internship.coins.client;

import io.neolab.internship.coins.client.bot.IBot;
import io.neolab.internship.coins.client.bot.SimpleBot;
import io.neolab.internship.coins.common.message.client.answer.*;
import io.neolab.internship.coins.common.message.client.ClientMessage;
import io.neolab.internship.coins.common.message.client.ClientMessageType;
import io.neolab.internship.coins.common.message.server.GameOverMessage;
import io.neolab.internship.coins.common.message.server.question.PlayerQuestion;
import io.neolab.internship.coins.common.message.server.ServerMessage;
import io.neolab.internship.coins.common.serialization.Communication;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.server.Server;
import io.neolab.internship.coins.server.service.GameLogger;
import io.neolab.internship.coins.utils.ClientServerProcessor;
import org.jetbrains.annotations.NotNull;
import io.neolab.internship.coins.utils.LoggerFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements IClient {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private static final @NotNull String IP = "localhost";
    private static final int PORT = Server.PORT;

    private final @NotNull String ip; // ip адрес клиента
    private final @NotNull InetAddress ipAddress;
    private final int port; // порт соединения

    private Socket socket = null;
    private BufferedReader keyboardReader = null; // поток чтения с консоли
    private BufferedReader in = null; // поток чтения из сокета
    private BufferedWriter out = null; // поток записи в сокет

    private @NotNull String nickname = "";

    private final @NotNull IBot simpleBot;

    /**
     * Для создания необходимо принять адрес и номер порта
     *
     * @param ip   - ip адрес клиента
     * @param port - порт соединения
     */
    private Client(final @NotNull String ip, final int port) throws CoinsException {
        try {
            this.ip = ip;
            this.ipAddress = InetAddress.getByName(ip);
            this.port = port;
            this.simpleBot = new SimpleBot();
        } catch (final UnknownHostException exception) {
            throw new CoinsException(CoinsErrorCode.CLIENT_CREATION_FAILED);
        }
    }

    @Override
    public @NotNull Answer getAnswer(final @NotNull PlayerQuestion playerQuestion) throws CoinsException {
        switch (playerQuestion.getPlayerQuestionType()) {
            case CATCH_CELL: {
                LOGGER.info("Catch cell question: {} ", playerQuestion);
                return new CatchCellAnswer(
                        simpleBot.chooseCatchingCell(playerQuestion.getPlayer(), playerQuestion.getGame()));
            }
            case DISTRIBUTION_UNITS: {
                LOGGER.info("Distribution units question: {} ", playerQuestion);
                return new DistributionUnitsAnswer(
                        simpleBot.distributionUnits(playerQuestion.getPlayer(), playerQuestion.getGame()));
            }
            case DECLINE_RACE: {
                LOGGER.info("Decline race question: {} ", playerQuestion);
                return new DeclineRaceAnswer(
                        simpleBot.declineRaceChoose(playerQuestion.getPlayer(), playerQuestion.getGame()));
            }
            case CHANGE_RACE: {
                LOGGER.info("Change race question: {} ", playerQuestion);
                return new ChangeRaceAnswer(
                        simpleBot.chooseRace(playerQuestion.getPlayer(), playerQuestion.getGame()));
            }
            default: {
                throw new CoinsException(CoinsErrorCode.QUESTION_TYPE_NOT_FOUND);
            }
        }
    }

    @Override
    public void processMessage(final @NotNull ServerMessage serverMessage) throws CoinsException, IOException {
        LOGGER.info("Input message: {} ", serverMessage);
        switch (serverMessage.getServerMessageType()) {
            case NICKNAME: {
                sendMessage(new NicknameAnswer(nickname));
                break;
            }
            case NICKNAME_DUPLICATE: {
                tryAgainEnterNickname();
                sendMessage(new NicknameAnswer(nickname));
                break;
            }
            case CONFIRMATION_OF_READINESS: {
                sendMessage(new ClientMessage(ClientMessageType.GAME_READY));
                break;
            }
            case GAME_QUESTION: {
                sendMessage(getAnswer((PlayerQuestion) serverMessage));
                break;
            }
            case GAME_OVER: {
                final GameOverMessage gameOverMessage = (GameOverMessage) serverMessage;
                GameLogger.printResultsInGameEnd(gameOverMessage.getWinners(), gameOverMessage.getPlayerList());
                break;
            }
            case DISCONNECTED: {
                throw new CoinsException(CoinsErrorCode.CLIENT_DISCONNECTION);
            }
            default: {
                throw new CoinsException(CoinsErrorCode.MESSAGE_TYPE_NOT_FOUND);
            }
        }
    }

    /**
     * Отправить сообщение серверу
     *
     * @param message - сообщение
     * @throws IOException при ошибке отправки сообщения
     */
    private void sendMessage(final @NotNull ClientMessage message) throws IOException {
        LOGGER.info("Output message: {} ", message);
        ClientServerProcessor.sendMessage(out, Communication.serializeClientMessage(message));
    }

    /**
     * Запуск клиента
     */
    private void startClient() {
        try (final LoggerFile ignored = new LoggerFile("client")) {
            try {
                socket = new Socket(this.ipAddress, this.port);
            } catch (final IOException e) {
                LOGGER.error("Socket failed");
                return;
            }
            initIO();
            LOGGER.info("Client started, ip: {}, port: {}", ip, port);
            enterNickname();
            serverInteract();
        } catch (final IOException e) {
            LOGGER.error("Error", e);
            sendDisconnectMessage();
            downService();
        }
    }

    /**
     * Инициализация средств ввода-вывода
     *
     * @throws IOException при ошибке инициализации
     */
    private void initIO() throws IOException {
        keyboardReader = new BufferedReader(new InputStreamReader(System.in, "CP866"));
        in = ClientServerProcessor.initReaderBySocket(socket);
        out = ClientServerProcessor.initWriterBySocket(socket);
    }

    /**
     * Метод всего взаимодействия с сервером
     */
    @SuppressWarnings("InfiniteLoopStatement")
    private void serverInteract() {
        try {
            while (true) {
                processMessage(Communication.deserializeServerMessage(in.readLine())); // ждем сообщения с сервера
            }
        } catch (final CoinsException | IOException exception) {
            if (!(exception instanceof CoinsException) ||
                    ((CoinsException) exception).getErrorCode() != CoinsErrorCode.CLIENT_DISCONNECTION) {
                LOGGER.error("Error", exception);
            }
            sendDisconnectMessage();
            downService();
        }
    }

    /**
     * Уведомить сервер об отключении
     */
    private void sendDisconnectMessage() {
        try {
            sendMessage(new ClientMessage(ClientMessageType.DISCONNECTED));
        } catch (final IOException exception) {
            LOGGER.error("Error", exception);
        }
    }

    /**
     * Закрытие сокета
     */
    private void downService() {
        try {
            if (!socket.isClosed()) {
                LOGGER.info("Service is downed");
                socket.close();
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (keyboardReader != null) {
                    keyboardReader.close();
                }
            }
        } catch (final IOException ignored) {
        }
    }

    /**
     * Определение никнэйма
     *
     * @throws IOException при ошибке ввода никнэйма
     */
    private void enterNickname() throws IOException {
        System.out.println("Welcome to the game!");
        while (nickname.isEmpty()) {
            System.out.println("Please, enter nickname");
            nickname = keyboardReader.readLine();
            LOGGER.info("Entered nickname: {}", nickname);
        }
    }

    /**
     * Попытка заново ввести никнэйм
     *
     * @throws IOException при ошибке ввода никнэйма
     */
    private void tryAgainEnterNickname() throws IOException {
        String nickname;
        do {
            System.out.println("Nickname is duplicate! Try again");
            System.out.println("Please, enter nickname");
            nickname = keyboardReader.readLine();
            LOGGER.info("Entered nickname: {}", nickname);
        } while (nickname.isEmpty() || nickname.equals(this.nickname));
        this.nickname = nickname;
    }

    public static void main(final String[] args) {
        try {
            final Client client = new Client(IP, PORT);
            client.startClient();
        } catch (final CoinsException exception) {
            LOGGER.error("Error!", exception);
        }
    }
}
