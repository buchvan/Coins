package io.neolab.internship.coins.client;

import io.neolab.internship.coins.client.bot.SmartBot;
import io.neolab.internship.coins.client.bot.ai.bim.model.FunctionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import org.jetbrains.annotations.NotNull;

public class SmartClient extends Client {
    private static final int SMART_BOT_MAX_DEPTH = 2;

    /**
     * Для создания необходимо принять адрес и номер порта
     *
     * @param ip   - ip адрес клиента
     * @param port - порт соединения
     */
    private SmartClient(final @NotNull String ip, final int port, final @NotNull FunctionType botFunctionType)
            throws CoinsException {
        super(ip, port, new SmartBot(SMART_BOT_MAX_DEPTH, botFunctionType));
    }

    public static void main(final String[] args) {
        try {
            final ClientConfigResource clientConfig = new ClientConfigResource();
            final Client client = new SmartClient(clientConfig.getHost(), clientConfig.getPort(), FunctionType.MIN_MAX);
            client.startClient();
        } catch (final CoinsException exception) {
            LOGGER.error("Error!", exception);
        }
    }
}
