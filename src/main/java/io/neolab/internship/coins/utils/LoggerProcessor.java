package io.neolab.internship.coins.utils;

import org.slf4j.Logger;

public class LoggerProcessor {

    /**
     * Выводит лог уровня INFO
     *
     * @param message - сообщение, которое нужно вывести
     * @param objects - объекты, метаинформация о которых необходима в логе
     */
    public static void printInfo(final Logger LOGGER, final String message, final Object... objects) {
        LOGGER.info(message, objects);
    }

    /**
     * Выводит лог уровня DEBUG
     *
     * @param message - сообщение, которое нужно вывести
     * @param objects - объекты, метаинформация о которых необходима в логе
     */
    public static void printDebug(final Logger LOGGER, final String message, final Object... objects) {
        LOGGER.debug(message, objects);
    }

    /**
     * Выводит лог уровня ERROR
     *
     * @param message - сообщение, которое нужно вывести
     */
    public static void printError(final Logger LOGGER, final String message) {
        LOGGER.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LOGGER.error(message);
    }

    /**
     * Выводит лог уровня ERROR
     *
     * @param message   - сообщение, которое нужно вывести
     * @param exception - исключение, метаинформация о котором необходима в логе
     */
    public static void printError(final Logger LOGGER, final String message, final Exception exception) {
        LOGGER.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LOGGER.error(message, exception);
    }

}
