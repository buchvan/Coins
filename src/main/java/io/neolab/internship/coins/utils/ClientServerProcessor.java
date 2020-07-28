package io.neolab.internship.coins.utils;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;

public class ClientServerProcessor {

    /**
     * Инициализация потока ввода по сокету
     *
     * @param socket - сокет, для которого нужно открыть поток ввода
     * @return поток ввода
     * @throws IOException при ошибке открытия потока ввода
     */
    public static @NotNull BufferedReader initReaderBySocket(final @NotNull Socket socket)
            throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * Инициализация потока вывода по сокету
     *
     * @param socket - сокет, для которого нужно открыть поток вывода
     * @return поток вывода
     * @throws IOException при ошибке открытия потока вывода
     */
    public static @NotNull BufferedWriter initWriterBySocket(final @NotNull Socket socket)
            throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    /**
     * Отправить сообщение
     *
     * @param out  - поток вывода
     * @param json - строка (json), которую нужно отправить
     * @throws IOException при ошибке отправки сообщения
     */
    public static void sendMessage(final @NotNull BufferedWriter out, final @NotNull String json) throws IOException {
        out.write(json + "\n");
        out.flush();
    }
}
