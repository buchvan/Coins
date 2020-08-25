package io.neolab.internship.coins.ai_vika;

import io.neolab.internship.coins.ai_vika.bot.AIBot;
import io.neolab.internship.coins.ai_vika.bot.exception.AIBotException;
import io.neolab.internship.coins.client.ClientConfigResource;
import io.neolab.internship.coins.exceptions.CoinsException;
import org.jetbrains.annotations.NotNull;

public class AIClient extends Client {
    /**
     * @param ip   - ip адрес клиента
     * @param port - порт соединения
     */
    private AIClient(final @NotNull String ip, final int port)
            throws CoinsException {
        super(ip, port, new AIBot());
    }

    public static void main(final String[] args) throws AIBotException {
        try {
            final ClientConfigResource clientConfig = new ClientConfigResource();
            new AIClient(clientConfig.getHost(), clientConfig.getPort()).startClient();
        } catch (final CoinsException | NullPointerException exception) {
            LOGGER.error("Error!", exception);
        }
    }

}
