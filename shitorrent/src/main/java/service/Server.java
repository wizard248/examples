package service;

import service.FrontService;
import service.Operation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by vojta on 12/01/16.
 */
public class Server {
    private final static Logger LOGGER = Logger.getAnonymousLogger();

    private final int port;
    private final ExecutorService executorService;
    private final FrontService frontService;

    public Server(final int port, final FrontService frontService) {
        this.port = port;
        this.frontService = frontService;
        this.executorService = Executors.newFixedThreadPool(8);
    }

    public void start() throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port);
        LOGGER.info("Started server at port: " + port);

        while (true) {
            final Socket clientSocket = serverSocket.accept();
            final Runnable clientHandler = createClientHandler(clientSocket);
            executorService.submit(clientHandler);
        }
    }

    private Runnable createClientHandler(final Socket clientSocket) {
        return new Runnable() {
            @Override
            public void run() {
                try (
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8))
                ) {
                    Operation.HELP.perform(frontService, bufferedReader, bufferedWriter);

                    while (!clientSocket.isClosed()) {
                        Operation.findAndPerform(frontService, bufferedReader, bufferedWriter);
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
