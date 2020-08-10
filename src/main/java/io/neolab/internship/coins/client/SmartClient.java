package io.neolab.internship.coins.client;

import io.neolab.internship.coins.client.bot.SmartBot;
import io.neolab.internship.coins.client.bot.ai.bim.model.FunctionType;
import io.neolab.internship.coins.exceptions.CoinsErrorCode;
import io.neolab.internship.coins.exceptions.CoinsException;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
            final BufferedReader keyboardReader =
                    new BufferedReader(
                            new InputStreamReader(
                                    System.in, "CP866"));
            System.out.println("Choose type of bot:");
            System.out.println("--- 1. MAX");
            System.out.println("--- 2. MIN");
            System.out.println("--- 3. MIN_MAX");
            final int choose = Integer.parseInt(keyboardReader.readLine());
            final Client client;
            switch (choose) {
                case 1:
                    client = new SmartClient(clientConfig.getHost(), clientConfig.getPort(), FunctionType.MAX);
                    break;
                case 2:
                    client = new SmartClient(clientConfig.getHost(), clientConfig.getPort(), FunctionType.MIN);
                    break;
                case 3:
                    client = new SmartClient(clientConfig.getHost(), clientConfig.getPort(), FunctionType.MIN_MAX);
                    break;
                default:
                    throw new CoinsException(CoinsErrorCode.MESSAGE_TYPE_NOT_FOUND);
            }

            client.startClient();
        } catch (final CoinsException | IOException exception) {
            LOGGER.error("Error!", exception);
        }
    }
}
