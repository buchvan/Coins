package io.neolab.internship.coins.client;

import io.neolab.internship.coins.client.bot.SmartBot;
import io.neolab.internship.coins.client.bot.FunctionType;
import io.neolab.internship.coins.exceptions.CoinsException;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SmartClient extends Client {
    /**
     * Для создания необходимо принять адрес и номер порта
     *
     * @param ip   - ip адрес клиента
     * @param port - порт соединения
     */
    private SmartClient(final @NotNull String ip, final int port, final @NotNull FunctionType botFunctionType,
                        final int smartBotMaxDepth)
            throws CoinsException {
        super(ip, port, new SmartBot(smartBotMaxDepth, botFunctionType));
    }

    public static void main(final String[] args) {
        try {
            final ClientConfigResource clientConfig = new ClientConfigResource();
            final BufferedReader keyboardReader =
                    new BufferedReader(
                            new InputStreamReader(
                                    System.in, "CP866"));
            System.out.println("Choose type of bot:");
            System.out.println("--- 1. MAX_PERCENT");
            System.out.println("--- 2. MIN_PERCENT");
            System.out.println("--- 3. MIN_MAX_PERCENT");
            System.out.println("--- 4. MAX_VALUE");
            System.out.println("--- 5. MIN_VALUE");
            System.out.println("--- 6. MIN_MAX_VALUE");
            System.out.println("--- 7. MAX_VALUE_DIFFERENCE");
            System.out.println("--- 8. MIN_MAX_VALUE_DIFFERENCE");
            System.out.println();
            final int botType = Integer.parseInt(keyboardReader.readLine());
            System.out.println("Enter max depth of bot: ");
            final int botMaxDepth = Integer.parseInt(keyboardReader.readLine());
            new SmartClient(clientConfig.getHost(), clientConfig.getPort(),
                    FunctionType.values()[botType - 1], botMaxDepth).startClient();
        } catch (final CoinsException | IOException | NullPointerException exception) {
            LOGGER.error("Error!", exception);
        }
    }
}
