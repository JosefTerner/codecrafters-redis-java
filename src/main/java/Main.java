import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
  public static void main(String[] args){
        ServerSocket serverSocket;
        Socket clientSocket = null;
        int port = 6379;
        try {
          serverSocket = new ServerSocket(port);
          serverSocket.setReuseAddress(true);
          clientSocket = serverSocket.accept();
        } catch (IOException e) {
          log.error("IOException: {}", e.getMessage());
        } finally {
          try {
            if (clientSocket != null) {
              clientSocket.close();
            }
          } catch (IOException e) {
              log.error("IOException: {}", e.getMessage());
          }
        }
  }
}
