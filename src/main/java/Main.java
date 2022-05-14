import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        ServerSocket serverSocket;
        Socket clientSocket = null;
        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
//            while (true) {
                clientSocket = serverSocket.accept();
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                out = new PrintWriter(new ObjectOutputStream(clientSocket.getOutputStream()));
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                while (Objects.nonNull(s = in.readLine())) {
                while (clientSocket.isConnected()) {
                    String s = in.readLine();
                    System.out.println(s);
                    if (s.contains("ping")) {
                        out.write("+PONG" + "\r\n");
                    }
                }
                out.flush();
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


//                ClientHandler clientSock
//                        = new ClientHandler(clientSocket);
//
//                new Thread(clientSock).start();
//            }
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

//    private static class ClientHandler implements Runnable {
//        private final Socket clientSocket;
//
//        // Constructor
//        public ClientHandler(Socket socket) {
//            this.clientSocket = socket;
//        }
//
//        public void run() {
//            PrintWriter out = null;
//            BufferedReader in = null;
//            try {
//                out = new PrintWriter(new ObjectOutputStream(clientSocket.getOutputStream()));
//                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
////                while (Objects.nonNull(s = in.readLine())) {
//                    while (clientSocket.isConnected()) {
//                        String s = in.readLine();
//                        System.out.println(s);
//                        if (s.contains("ping")) {
//                            out.write("+PONG" + "\r\n");
//                        }
//                    }
//                    out.flush();
//                    out.close();
//                    in.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//
//                        clientSocket.close();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
//    }
}
