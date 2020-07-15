package io.neolab.internship.coins.server;

import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.common.question.QuestionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.service.GameFinalizer;
import io.neolab.internship.coins.server.game.service.GameInitializer;
import io.neolab.internship.coins.server.game.service.GameLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server implements IServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static final int PORT = 8081;
    private static final int CLIENTS_COUNT = 2;

    private static final int BOARD_SIZE_X = 3;
    private static final int BOARD_SIZE_Y = 4;

    private final ConcurrentLinkedQueue<ServerSomething> serverList = new ConcurrentLinkedQueue<>();

    private static class ServerSomething {

        private final Server server;
        private final Socket socket;
        private final BufferedReader in; // поток чтения из сокета
        private final BufferedWriter out; // поток записи в сокет
        private final Player player;

        /**
         * Для общения с клиентом необходим сокет (адресные данные)
         *
         * @param server сервер
         * @param socket сокет
         */
        private ServerSomething(final Server server, final Socket socket) throws IOException {
            this.server = server;
            this.socket = socket;
            // если потоку ввода/вывода приведут к генерированию искдючения, оно проброситься дальше
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            final Question question = new Question(QuestionType.NICKNAME);
            player = new Player();
        }
    }

    @Override
    public void startServer() {
        try {
            try (final ServerSocket serverSocket = new ServerSocket(PORT)) {
                int currentClientsCount = 0;
                while (currentClientsCount < CLIENTS_COUNT) {
                    final Socket socket = serverSocket.accept();
                    serverList.add(new ServerSomething(this, socket));
                    currentClientsCount++;
                }
            } catch (final BindException e) {
                e.printStackTrace();
            }
            LOGGER.info("Server started, port: {}", PORT);
            final IGame game = GameInitializer.gameInit(BOARD_SIZE_X, BOARD_SIZE_Y, CLIENTS_COUNT);
            GameLogger.printGameCreatedLog(game);
            GameLogger.printStartGameChoiceLog();
//            for (final Player player : game.getPlayers()) {
//                chooseRace(player, game.getRacesPool());
//            }
            GameFinalizer.finalize(game.getPlayers());
        } catch (final CoinsException | IOException ex) {
            LOGGER.error("Error!!!", ex);
        }
    }

    public static void main(final String[] args) {
        final Server server = new Server();
        server.startServer();
    }
}
