package socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String []args) {
        int number = 0;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8080);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 8080.");
            System.exit(1);
        }

        while(true){
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.printf("Connected to client %s:%d\n",
                        clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                if(++number == 1){
                    Thread t = new ClientThread(clientSocket, true);
                    t.start();
                }else{
                    Thread t = new ClientThread(clientSocket, false);
                    t.start();
                }
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
        }

    }
}
