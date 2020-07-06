package io.neolab.internship.coins.server;

import io.neolab.internship.coins.Pair;
import io.neolab.internship.coins.client.Client;
import io.neolab.internship.coins.server.board.Board;
import io.neolab.internship.coins.server.board.Player;

import java.util.LinkedList;

public class Server implements IServer {
    private final int MAX_CLIENTS_COUNT = 2;
    private final int ROUNDS_COUNT = 10;

    private int currentRound = 0;
    private Board board;
    private LinkedList<Pair<Client, Player>> clientToPlayerList;


    @Override
    public void startServer() {

    }

    public static void main(String[] args) {
        System.out.println("Hello world");
    }
}
