package io.neolab.internship.coins.client;

import io.neolab.internship.coins.common.answer.*;
import io.neolab.internship.coins.common.question.GameOverMessage;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.question.ServerMessage;
import io.neolab.internship.coins.common.serialization.Communication;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.Server;
import io.neolab.internship.coins.server.service.GameLogger;
import io.neolab.internship.coins.utils.LoggerFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static io.neolab.internship.coins.common.question.ServerMessageType.*;

public class Client implements IClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private static final String IP = "localhost";
    private static final int PORT = Server.PORT;

    private final String ip; // ip адрес клиента
    private final InetAddress ipAddress;
    private final int port; // порт соединения

    private Socket socket = null;
    private BufferedReader keyboardReader = null; // поток чтения с консоли
    private BufferedReader in = null; // поток чтения из сокета
    private BufferedWriter out = null; // поток записи в сокет

    private String nickname = "";


    private final IBot simpleBot;

    /**
     * для создания необходимо принять адрес и номер порта
     *
     * @param ip   ip адрес клиента
     * @param port порт соединения
     */
    private Client(final String ip, final int port) throws CoinsException {
        try {
            this.ip = ip;
            this.ipAddress = InetAddress.getByName(ip);
            this.port = port;
            this.simpleBot = new SimpleBot();
        } catch (final UnknownHostException exception) {
            throw new CoinsException(ErrorCode.CLIENT_CREATION_FAILED);
        }
    }

    @Override
    public Answer getAnswer(final PlayerQuestion playerQuestion) throws CoinsException {
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
        }
        throw new CoinsException(ErrorCode.QUESTION_TYPE_NOT_FOUND);
    }

    @Override
    public void readMessage(final ServerMessage serverMessage) throws CoinsException {
        if (serverMessage.getServerMessageType() == GAME_OVER) {
            LOGGER.info("Game over question: {} ", serverMessage);
            final GameOverMessage gameOverMessage = (GameOverMessage) serverMessage;
            GameLogger.printResultsInGameEnd(gameOverMessage.getWinners(), gameOverMessage.getPlayerList());
            throw new CoinsException(ErrorCode.GAME_OVER);
        }
        throw new CoinsException(ErrorCode.QUESTION_TYPE_NOT_FOUND);
    }

    private void startClient() {
        try (final LoggerFile ignored = new LoggerFile("client")) {
            try {
                socket = new Socket(this.ipAddress, this.port);
            } catch (final IOException e) {
                LOGGER.error("Socket failed");
                return;
            }
            keyboardReader = new BufferedReader(new InputStreamReader(System.in, "CP866"));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            LOGGER.info("Client started, ip: {}, port: {}", ip, port);
            enterNickname();
            play();
        } catch (final IOException e) {
            LOGGER.error("Error", e);
            downService();
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void play() {
        try {
            while (true) {
                final ServerMessage serverMessage =
                        Communication.deserializeServerMessage(in.readLine()); // ждем сообщения с сервера
                LOGGER.info("Input question: {} ", serverMessage);
                if (serverMessage.getServerMessageType() == GAME_QUESTION) {
                    final Answer answer = getAnswer((PlayerQuestion) serverMessage);
                    LOGGER.info("Output answer: {} ", answer);
                    sendMessage(Communication.serializeClientMessage(answer));
                    continue;
                } else if (serverMessage.getServerMessageType() == CONFIRMATION_OF_READINESS) {
                    LOGGER.info("Nickname question: {}", serverMessage);
                    final Answer answer = new Answer(ClientMessageType.GAME_READY);
                    LOGGER.info("Output answer: {} ", answer);
                    sendMessage(Communication.serializeClientMessage(answer));
                    continue;
                } else if (serverMessage.getServerMessageType() == NICKNAME) {
                    LOGGER.info("Nickname question: {}", serverMessage);
                    final Answer answer = new NicknameAnswer(nickname);
                    LOGGER.info("Output answer: {} ", answer);
                    sendMessage(Communication.serializeClientMessage(answer));
                    continue;
                } else if (serverMessage.getServerMessageType() == NICKNAME_DUPLICATE) {
                    LOGGER.info("Nickname duplicate question: {}", serverMessage);
                    tryAgainEnterNickname();
                    final Answer answer = new NicknameAnswer(nickname);
                    LOGGER.info("Output answer: {} ", answer);
                    sendMessage(Communication.serializeClientMessage(answer));
                    continue;
                } else if (serverMessage.getServerMessageType() == GAME_OVER) {
                    readMessage(serverMessage);
                    continue;
                }
                throw new CoinsException(ErrorCode.QUESTION_TYPE_NOT_FOUND);
            }
        } catch (final IOException | CoinsException e) {
            if (!(e instanceof CoinsException) || ((CoinsException) e).getErrorCode() != ErrorCode.GAME_OVER) {
                LOGGER.error("Error", e);
            }
            sendDisconnectMessage();
            downService();
        }
    }

    private void sendDisconnectMessage() {
        try {
            sendMessage(Communication.serializeClientMessage(new ClientMessage(ClientMessageType.DISCONNECTED)));
        } catch (final IOException exception) {
            LOGGER.error("Error", exception);
        }
    }

    private void sendMessage(final String json) throws IOException {
        out.write(json + "\n");
        out.flush();
    }

    /**
     * закрытие сокета
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

    private void enterNickname() throws IOException {
        System.out.println("Welcome to the game!");
        while (nickname.isEmpty()) {
            System.out.println("Please, enter nickname");
            nickname = keyboardReader.readLine();
            LOGGER.info("Entered nickname: {}", nickname);
        }
    }

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
