package ru.netology;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        server.addHandler("GET", "/err", (request, responseStream) -> {
            var str = "Nooooooooooo! All right - it's not right";
            try {
                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + "text/plain" + "\r\n" +
                                "Content-Length: " + str.getBytes().length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                responseStream.write(str.getBytes());
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.listen(9999, 64);
    }
}


