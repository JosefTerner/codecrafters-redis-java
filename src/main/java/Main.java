import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        ServerSocket serverSocket;
        Socket clientSocket = null;
        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            while (true) {
                clientSocket = serverSocket.accept();

                ClientHandler clientSock
                        = new ClientHandler(clientSocket);

                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        // Constructor
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            BufferedWriter out;
            BufferedReader in;
            Map<String, String> keyValue = new HashMap<>();
            try {
                out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String s;
                while (clientSocket.isConnected() && Objects.nonNull(s = in.readLine())) {
                    System.out.println(s);
                    if (s.contains("ping")) {
                        out.write("+PONG" + "\r\n");
                        out.flush();
                    }
                    if (s.contains("echo")) {
                        in.readLine();
                        out.write(":" + in.readLine() + "\r\n");
                        out.flush();
                    }
                    if (s.contains("set")) {
                        System.out.println(in.readLine());
                        String key = in.readLine();
                        System.out.println(in.readLine());
                        String value = in.readLine();
                        keyValue.put(key, value);
                        out.write("+OK" + "\r\n");
                        out.flush();
                    }
                    if (s.contains("get")) {
                        System.out.println(in.readLine());
                        String key = in.readLine();
                        out.write(":" + keyValue.get(key) + "\r\n");
                        out.flush();
                    }

                }
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {

                    clientSocket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
