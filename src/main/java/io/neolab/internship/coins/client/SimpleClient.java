package io.neolab.internship.coins.client;

import io.neolab.internship.coins.client.bot.SimpleBot;
import io.neolab.internship.coins.exceptions.CoinsException;
import org.jetbrains.annotations.NotNull;

public class SimpleClient extends Client {

    /**
     * Для создания необходимо принять адрес и номер порта
     *
     * @param ip   - ip адрес клиента
     * @param port - порт соединения
     */
    private SimpleClient(final @NotNull String ip, final int port) throws CoinsException {
        super(ip, port, new SimpleBot());
    }

    public static void main(final String[] args) {
        try {
            final ClientConfigResource clientConfig = new ClientConfigResource();
            final Client client = new SimpleClient(clientConfig.getHost(), clientConfig.getPort());
            client.startClient();
        } catch (final CoinsException exception) {
            LOGGER.error("Error!", exception);
        }
    }
}
