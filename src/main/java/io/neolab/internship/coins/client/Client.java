package io.neolab.internship.coins.client;

import io.neolab.internship.coins.common.answer.*;
import io.neolab.internship.coins.common.question.GameOverMessage;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.question.ServerMessage;
import io.neolab.internship.coins.common.serialization.Communication;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.Server;
import io.neolab.internship.coins.server.game.service.GameLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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
    public Answer getAnswer(final ServerMessage serverMessage) throws CoinsException, IOException {
        switch (serverMessage.getServerMessageType()) {
            case NICKNAME: {
                LOGGER.info("Nickname question: {}", serverMessage);
                return new NicknameAnswer(nickname);
            }
            case NICKNAME_DUPLICATE:
                LOGGER.info("Duplicate nickname question: {}", serverMessage);
                tryAgainEnterNickname();
                return new NicknameAnswer(nickname);
            case CONFIRMATION_OF_READINESS:
                LOGGER.info("Ready question: {}", serverMessage);
                return new Answer(ClientMessageType.GAME_READY);
            case GAME_QUESTION: {
                final PlayerQuestion playerQuestion = (PlayerQuestion) serverMessage;
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
            }
            case GAME_OVER: {
                LOGGER.info("Game over question: {} ", serverMessage);
                final GameOverMessage gameOverMessage = (GameOverMessage) serverMessage;
                GameLogger.printResultsInGameEnd(gameOverMessage.getWinners(), gameOverMessage.getPlayerList());
                downService();
                System.exit(0);
            }
        }
        throw new CoinsException(ErrorCode.QUESTION_TYPE_NOT_FOUND);
    }

    private void startClient() {
        try {
            socket = new Socket(this.ipAddress, this.port);
        } catch (final IOException e) {
            LOGGER.error("Socket failed");
            return;
        }
        try {
            keyboardReader = new BufferedReader(new InputStreamReader(System.in, "CP866"));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            LOGGER.info("Client started, ip: {}, port: {}", ip, port);
            enterNickname();
            play();
        } catch (final IOException e) {
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
                final Answer answer = getAnswer(serverMessage);
                LOGGER.info("Output answer: {} ", answer);
                sendMessage(Communication.serializeClientMessage(answer));
            }
        } catch (final IOException | CoinsException e) {
            LOGGER.error("Error", e);
            try {
                sendMessage(Communication.serializeClientMessage(new ClientMessage(ClientMessageType.DISCONNECTED)));
            } catch (final IOException exception) {
                LOGGER.error("Error", exception);
            }
            downService();
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
