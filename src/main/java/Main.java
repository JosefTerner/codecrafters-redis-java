import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        int port = 6379;
        Socket clientSocket = null;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            while (true) {
                clientSocket = serverSocket.accept();
                ClientHandler clientSock = new ClientHandler(
                        clientSocket,
                        clientSocket.getOutputStream(),
                        clientSocket.getInputStream()
                );
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            System.out.println("serverSocket: " + e.getMessage());
        } finally {
            if (Objects.nonNull(clientSocket)) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("clientSocket: " + e.getMessage());
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final BufferedWriter out;
        private final BufferedReader in;
        private final Map<String, String> keyValue = new HashMap<>();
        private static final String OK = "+OK\r\n";
        private static final String NIL = "$-1\r\n";
        private static final String PONG = "+PONG\r\n";
        private static final String NEXT_LINE = "\r\n";

        public ClientHandler(Socket socket, OutputStream out, InputStream in) {
            this.clientSocket = socket;
            this.out = new BufferedWriter(new OutputStreamWriter(out));
            this.in = new BufferedReader(new InputStreamReader(in));
        }

        public void run() {
            try (BufferedReader in1 = in; BufferedWriter out1 = out) {
                String command;
                while (clientSocket.isConnected() && Objects.nonNull(command = readLine())) {
                    switch (command) {
                        case Command.PING:
                            write(PONG);
                            break;

                        case Command.ECHO:
                            write(":" + readLine(1) + NEXT_LINE);
                            break;

                        case Command.SET:
                            setCommand();
                            write(OK);
                            break;

                        case Command.GET:
                            write(getCommand());
                            break;
                    }
                }
            } catch (IOException e) {
                System.out.println("closable: " + e.getMessage());
            }
        }

        private void setCommand() {
            String setKey = readLine(1);
            String setValue = readLine(1);
            if (hasNext()) {
                if (readLine(1).equals("px")) {
                    String expTime = readLine(1);
                    new Thread(() -> {
                        try {
                            Thread.sleep(Long.parseLong(expTime));
                            keyValue.remove(setKey);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                }
            }
            keyValue.put(setKey, setValue);
        }

        private String getCommand() {
            String getKey = readLine(1);
            String getValue = keyValue.get(getKey);
            if (Objects.isNull(getValue)) {
                return NIL;
            } else {
                return ":" + getValue + NEXT_LINE;
            }
        }

        /**
         *
         * @param str string to be written to {@link BufferedWriter}
         */
        private void write(String str) {
            try {
                out.write(str);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException("write: " + e.getMessage());
            }
        }

        /**
         * skips a line
         */
        private void skipLine() {
            readLine();
        }

        /**
         * @return line values
         */
        private String readLine() {
            try {
                return in.readLine();
            } catch (IOException e) {
                throw new RuntimeException("readLine: " + e.getMessage());
            }
        }

        /**
         * @param skip the number of skipped lines before reading
         * @return line values
         */
        private String readLine(int skip) {
            for (int i = 0; i < skip; i++) {
                skipLine();
            }
            return readLine();
        }

        private boolean hasNext() {
            try {
                return in.ready();
            } catch (IOException e) {
                throw new RuntimeException("readLine: " + e.getMessage());
            }
        }
    }
}
