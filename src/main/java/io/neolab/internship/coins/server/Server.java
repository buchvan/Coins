package io.neolab.internship.coins.server;

import io.neolab.internship.coins.Pair;
import io.neolab.internship.coins.client.Client;
import io.neolab.internship.coins.server.board.*;
import io.neolab.internship.coins.server.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Сервер
 */
public class Server implements IServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class); // логгер (slf4j) для сервера

    private final int MAX_CLIENTS_COUNT = 2; // максимально возможное число клиентов
    private final int ROUNDS_COUNT = 10; // число раундов

    private int currentRound = 0; // текущий раунд
    private Board board; // борда (карта/доска)
    private LinkedList<Pair<Client, Player>> clientToPlayerList; // клиент <-> игрок
    private Map<Pair<Race, CellType>, List<Feature>> raceCellTypeToFeatureList; // (раса, тип клетки) -> список особенностей
    private Map<Player, List<Cell>> feudalToCells; // игрок -> клетки, с которых он получает монеты

    private ServerValidator validator; // валидатор
    private ServerAnswerProcessor answerProcessor; // обработчик ответов

    private class ServerValidator {
    }

    private class ServerAnswerProcessor {
    }

    @Override
    public void startServer() {

    }

    public static void main(String[] args) {
        System.out.println("Hello world");
    }
}
