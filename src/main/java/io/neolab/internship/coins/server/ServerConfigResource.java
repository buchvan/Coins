package io.neolab.internship.coins.server;

import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.exceptions.CoinsException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerConfigResource {
    private static final String CONFIG_PATH = "src/main/resources/server.properties";
    private int port;
    private int clientsCount;
    private int gamesCount;

    private int boardSizeX;
    private int boardSizeY;

    ServerConfigResource() throws CoinsException {
        try (final FileInputStream serverConfigFis = new FileInputStream(CONFIG_PATH)) {
            final Properties property = new Properties();
            property.load(serverConfigFis);
            port = Integer.parseInt(property.getProperty("port"));
            clientsCount = Integer.parseInt(property.getProperty("clients.count"));
            gamesCount = Integer.parseInt(property.getProperty("games.count"));
            boardSizeX = Integer.parseInt(property.getProperty("board.size_x"));
            boardSizeY = Integer.parseInt(property.getProperty("board.size_y"));

        } catch (final IOException e) {
            throw new CoinsException(CoinsErrorCode.SERVER_CONFIG_LOADING_FAILED);
        }
    }

    public int getPort() {
        return port;
    }

    int getClientsCount() {
        return clientsCount;
    }

    int getGamesCount() {
        return gamesCount;
    }

    int getBoardSizeX() {
        return boardSizeX;
    }

    int getBoardSizeY() {
        return boardSizeY;
    }
}
