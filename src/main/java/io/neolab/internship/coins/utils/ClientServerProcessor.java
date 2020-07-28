package io.neolab.internship.coins.utils;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;

public class ClientServerProcessor {

    /**
     * Инициализация потоков ввода-вывода по сокету
     *
     * @param socket - сокет, для которого нужно открыть потоки ввода-вывода
     * @return пару (BufferedReader, BufferedWriter)
     * @throws IOException при ошибке открытия потоков ввода-вывода
     */
    public static @NotNull Pair<BufferedReader, BufferedWriter> initReaderWriterBySocket(final @NotNull Socket socket)
            throws IOException {
        return new Pair<>(
                new BufferedReader(new InputStreamReader(socket.getInputStream())),
                new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        );
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
