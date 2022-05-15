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

        /** Client **/
        private final Socket clientSocket;

        /**
         * Writes text to the character output stream to the client
         */
        private final BufferedWriter out;

        /**
         * Reads text from a character-input stream from the client
          */
        private final BufferedReader in;

        /**
         * key-value storage for GET and SET commands
         */
        private final Map<String, String> keyValue = new HashMap<>();

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
                            write(Response.PONG);
                            break;

                        case Command.ECHO:
                            write(":" + readLine(1) + Response.CRLF);
                            break;

                        case Command.SET:
                            setCommand();
                            write(Response.OK);
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

        /**
         * write value to {@link HashMap kayValue} with key
         */
        private void setCommand() {
            String setKey = readLine(1);
            String setValue = readLine(1);
            if (hasNext()) {
                if (readLine(1).equals(SetOptions.PX)) {
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

        /**
         * @return value from {@link HashMap kayValue}
         */
        private String getCommand() {
            String getKey = readLine(1);
            String getValue = keyValue.get(getKey);
            if (Objects.isNull(getValue)) {
                return Response.NIL;
            } else {
                return ":" + getValue + Response.CRLF;
            }
        }

        /**
         * @param str string to be written to {@link BufferedWriter out}
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

        /**
         * @return true - if {@link BufferedReader in} has more lines and false if not
         */
        private boolean hasNext() {
            try {
                return in.ready();
            } catch (IOException e) {
                throw new RuntimeException("readLine: " + e.getMessage());
            }
        }
    }
}
