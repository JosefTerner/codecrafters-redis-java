import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        int port = 6379;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    ClientHandler clientSock = new ClientHandler(clientSocket);
                    new Thread(clientSock).start();
                } catch (IOException e) {
                    System.out.println("clientSocket: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("serverSocket: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        String OK = "+OK\r\n";
        String NIL = "$-1\r\n";
        String PONG = "+PONG\r\n";
        String NEXT_LINE = "\r\n";



        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            Map<String, String> keyValue = new HashMap<>();
            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))){
                String command;
                while (clientSocket.isConnected() && Objects.nonNull(command = in.readLine())) {
                    if (command.contains("ping")) {
                        out.write(PONG);
                        out.flush();
                    }
                    if (command.contains("echo")) {
                        in.readLine();
                        out.write(":" + in.readLine() + NEXT_LINE);
                        out.flush();
                    }
                    if (command.contains("set")) {
                        in.readLine();
                        String key = in.readLine();
                        in.readLine();
                        String value = in.readLine();
                        if (in.ready()) {
                            in.readLine();
                            if (in.readLine().equals("px")) {
                                in.readLine();
                                String expTime = in.readLine();
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(Long.parseLong(expTime));
                                        keyValue.remove(key);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }).start();
                            }
                        }
                        keyValue.put(key, value);
                        out.write(OK);
                        out.flush();
                    }
                    if (command.contains("get")) {
                        in.readLine();
                        String key = in.readLine();
                        String obj = keyValue.get(key);
                        if (Objects.isNull(obj)) {
                            out.write(NIL);
                        } else {
                            out.write(":" + keyValue.get(key) + NEXT_LINE);
                        }
                        out.flush();
                    }
                }
            } catch (IOException e) {
                System.out.println("ClientHandler: " + e.getMessage());

            }
        }
    }
}
