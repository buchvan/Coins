package io.neolab.internship.coins.server;

import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.service.implementations.ServerAnswerProcessor;
import io.neolab.internship.coins.server.service.implementations.ServerValidator;
import io.neolab.internship.coins.server.service.interfaces.IAnswerProcessor;
import io.neolab.internship.coins.server.service.interfaces.IValidator;
import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class Server implements IServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private final int CLIENTS_COUNT = 2;

    private List<Pair<Client, Player>> clientToPlayerList = new LinkedList<>();
    private IGame game;

    private IValidator validator = new ServerValidator(this); // валидатор
    private IAnswerProcessor answerProcessor = new ServerAnswerProcessor(this); // обработчик ответов

    @Override
    public void startServer() {

    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }
}
