package io.neolab.internship.coins.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

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

    private final ISimpleBot simpleBot;

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
    public Answer getAnswer(final Question question) {
        return null;
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
        try {
            while (true) {
                final Question question = OBJECT_MAPPER.readValue(in.readLine(), Question.class); // ждем сообщения с сервера
                LOGGER.info("Input question: {} ", question);
                final Answer answer = getAnswer(question);
            }
        } catch (final IOException e) {
            downService();
        }
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
