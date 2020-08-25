package io.neolab.internship.coins.ai_vika;

import io.neolab.internship.coins.ai_vika.bot.exception.AIBotException;
import io.neolab.internship.coins.client.IClient;
import io.neolab.internship.coins.client.bot.IBot;
import io.neolab.internship.coins.common.message.client.ClientMessage;
import io.neolab.internship.coins.common.message.client.ClientMessageType;
import io.neolab.internship.coins.common.message.client.answer.*;
import io.neolab.internship.coins.common.message.server.GameOverMessage;
import io.neolab.internship.coins.common.message.server.ServerMessage;
import io.neolab.internship.coins.common.message.server.question.PlayerQuestion;
import io.neolab.internship.coins.common.serialization.Communication;
import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.service.GameLogger;
import io.neolab.internship.coins.utils.ClientServerProcessor;
import io.neolab.internship.coins.utils.LoggerFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements IClient {
    protected static final @NotNull Logger LOGGER = LoggerFactory.getLogger(Client.class);

    protected final @NotNull String ip; // ip адрес клиента
    protected final @NotNull InetAddress ipAddress;
    protected final int port; // порт соединения

    protected Socket socket = null;
    protected BufferedReader keyboardReader = null; // поток чтения с консоли
    protected BufferedReader in = null; // поток чтения из сокета
    protected BufferedWriter out = null; // поток записи в сокет

    protected @NotNull String nickname = "";

    protected final @NotNull IBot bot;

    /**
     * Для создания необходимо принять адрес и номер порта
     *
     * @param ip   - ip адрес клиента
     * @param port - порт соединения
     */
    Client(final @NotNull String ip, final int port, final @NotNull IBot bot) throws CoinsException {
        try {
            this.ip = ip;
            this.ipAddress = InetAddress.getByName(ip);
            this.port = port;
            this.bot = bot;
        } catch (final UnknownHostException exception) {
            throw new CoinsException(CoinsErrorCode.CLIENT_CREATION_FAILED);
        }
    }

    @Override
    public @NotNull Answer getAnswer(final @NotNull PlayerQuestion playerQuestion) throws CoinsException, AIBotException {
        switch (playerQuestion.getPlayerQuestionType()) {
            case CATCH_CELL: {
                LOGGER.info("Catch cell question: {} ", playerQuestion);
                return new CatchCellAnswer(
                        bot.chooseCatchingCell(playerQuestion.getPlayer(), playerQuestion.getGame()));
            }
            case DISTRIBUTION_UNITS: {
                LOGGER.info("Distribution units question: {} ", playerQuestion);
                return new DistributionUnitsAnswer(
                        bot.distributionUnits(playerQuestion.getPlayer(), playerQuestion.getGame()));
            }
            case DECLINE_RACE: {
                LOGGER.info("Decline race question: {} ", playerQuestion);
                return new DeclineRaceAnswer(
                        bot.declineRaceChoose(playerQuestion.getPlayer(), playerQuestion.getGame()));
            }
            case CHANGE_RACE: {
                LOGGER.info("Change race question: {} ", playerQuestion);
                return new ChangeRaceAnswer(
                        bot.chooseRace(playerQuestion.getPlayer(), playerQuestion.getGame()));
            }
            default: {
                throw new CoinsException(CoinsErrorCode.QUESTION_TYPE_NOT_FOUND);
            }
        }
    }

    @Override
    public void processMessage(final @NotNull ServerMessage serverMessage) throws CoinsException, IOException, AIBotException {
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
    void startClient() throws AIBotException {
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
    private void serverInteract() throws AIBotException {
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
}