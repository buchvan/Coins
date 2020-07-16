package io.neolab.internship.coins.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.common.answer.*;
import io.neolab.internship.coins.common.question.GameQuestion;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class Client implements IClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private static final String IP = "127.0.0.1";//"localhost";
    private static final int PORT = Server.PORT;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String ip; // ip адрес клиента
    private final int port; // порт соединения

    private Socket socket = null;
    private BufferedReader in = null; // поток чтения из сокета
    private BufferedWriter out = null; // поток записи в сокет

    private final IBot simpleBot;

    /**
     * для создания необходимо принять адрес и номер порта
     *
     * @param ip   ip адрес клиента
     * @param port порт соединения
     */
    private Client(final String ip, final int port) {
        this.ip = ip;
        this.port = port;
        this.simpleBot = new SimpleBot();
    }

    @Override
    public Answer getAnswer(final Question question) throws CoinsException, IOException {
        switch (question.getQuestionType()) {
            case CATCH_CELL -> {
                final GameQuestion gameQuestion = ((GameQuestion) question);
                return new CatchCellAnswer(simpleBot.catchCell(gameQuestion.getPlayer(),
                        gameQuestion.getGame()));
            }
            case DISTRIBUTION_UNITS -> {
                final GameQuestion gameQuestion = ((GameQuestion) question);
                return new DistributionUnitsAnswer(
                        simpleBot.distributionUnits(gameQuestion.getPlayer(),
                                gameQuestion.getGame()));
            }
            case DECLINE_RACE -> {
                final GameQuestion gameQuestion = ((GameQuestion) question);
                return new DeclineRaceAnswer(simpleBot.declineRaceChoose(gameQuestion.getPlayer(),
                        gameQuestion.getGame()));
            }
            case CHANGE_RACE -> {
                final GameQuestion gameQuestion = ((GameQuestion) question);
                return new ChangeRaceAnswer(simpleBot.chooseRace(gameQuestion.getPlayer(),
                        gameQuestion.getGame()));
            }
            case RESULTS -> throw new IOException();
        }
        throw new CoinsException(ErrorCode.QUESTION_TYPE_NOT_FOUND);
    }

    private void startClient() {
        try {
            socket = new Socket(this.ip, this.port);
        } catch (final IOException e) {
            LOGGER.error("Socket failed");
            return;
        }
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (final IOException e) {
            downService();
            return;
        }
        LOGGER.info("Client started, ip: {}, port: {}", ip, port);
        play();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void play() {
        try {
            while (true) {
                final Question question =
                        OBJECT_MAPPER.readValue(in.readLine(), Question.class); // ждем сообщения с сервера
                LOGGER.info("Input question: {} ", question);
                final Answer answer = getAnswer(question);
                LOGGER.info("Output answer: {} ", answer);
                sendAnswer(answer);
            }
        } catch (final IOException | CoinsException e) {
            downService();
        }
    }

    private void sendAnswer(final Answer answer) throws IOException {
        out.write(OBJECT_MAPPER.writeValueAsString(answer) + "\n");
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
            }
        } catch (final IOException ignored) {
        }
    }

    public static void main(final String[] args) {
        final Client client = new Client(IP, PORT);
        client.startClient();
    }
}
