package io.neolab.internship.coins.server;

import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class Server implements IServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static final int PORT = 8081;
    private final int CLIENTS_COUNT = 2;

    /**
     * Пока реализовали для одной игры на сервере, поменять будет несложно
     */
    private List<Pair<Client, Player>> clientToPlayerList = new LinkedList<>();
    private IGame game;

    public enum Command {
        EXIT("exit"),
        ;

        private final String commandName;

        Command(final String commandName) {
            this.commandName = commandName;
        }

        public boolean equalCommand(final String message) {
            return commandName.equals(message);
        }
    }

    @Override
    public void startServer() {

    }

    public static void main(final String[] args) {
        final Server server = new Server();
        server.startServer();
    }
}
