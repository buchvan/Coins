package io.neolab.internship.coins.client;

import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.ServerConfigResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Класс для загрузки информации для конфигурации клиента
 */
public class ClientConfigResource {
    private static final String CONFIG_PATH = "src/main/resources/client.properties";
    private final String host;
    private final int port;

    public ClientConfigResource() throws CoinsException {
        try (final FileInputStream clientConfigFis = new FileInputStream(CONFIG_PATH);
        final FileInputStream serverConfigFis = new FileInputStream(ServerConfigResource.CONFIG_PATH)) {
            final Properties property = new Properties();
            property.load(clientConfigFis);
            property.load(serverConfigFis);
            host = property.getProperty("ip");
            port = Integer.parseInt(property.getProperty("port"));
        } catch (final IOException e) {
            throw new CoinsException(CoinsErrorCode.CLIENT_CONFIG_LOADING_FAILED);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
