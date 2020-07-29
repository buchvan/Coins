package io.neolab.internship.coins.client;

import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.exceptions.CoinsException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

class ClientConfigResource {
    private static final String CONFIG_PATH = "src/main/resources/client.properties";
    private String host;

    ClientConfigResource() throws CoinsException {
        try (final FileInputStream clientConfigFis = new FileInputStream(CONFIG_PATH)) {
            final Properties property = new Properties();
            property.load(clientConfigFis);
            host = property.getProperty("ip");
        } catch (final IOException e) {
            throw new CoinsException(CoinsErrorCode.CLIENT_CONFIG_LOADING_FAILED);
        }
    }

    String getHost() {
        return host;
    }
}
