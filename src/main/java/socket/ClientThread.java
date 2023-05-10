package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientThread extends Thread{
    private static final Map<Integer, Socket> receiver = new HashMap<>();

    private final Socket clientSocket;

    private static Socket mainSocket = null;

    private final boolean isMain;


    public ClientThread(Socket socket, boolean flag){
        super("client-"+socket.getInetAddress().getHostAddress()+":"+socket.getPort());
        clientSocket = socket;
        isMain = flag;
        if(isMain) mainSocket = socket;
    }

    public void run(){
        try{
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            if(!isMain){
                int train_id;
                inputLine = in.readLine();
                if(inputLine == null){
                    System.out.printf("Closing connection with %s:%d\n",
                            clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                    return;
                }else{
                    train_id = Integer.parseInt(inputLine);
                    receiver.put(train_id, clientSocket);
                }

                PrintWriter mainOut = mainSocket == null? null: new PrintWriter(mainSocket.getOutputStream(), true);
                while ((inputLine = in.readLine()) != null) {
                    if(mainSocket == null){
                        out.println("[ERROR] Cloud service can not support!");
                        if(mainOut != null){
                            mainOut.close();
                            mainOut = null;
                        }
                    }else{
                        mainOut = mainOut == null? new PrintWriter(mainSocket.getOutputStream(), true) : mainOut;
                        mainOut.println(inputLine);
                    }
                }
                System.out.printf("Closing connection with %s:%d\n",
                        clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                receiver.remove(train_id);
            }else{
                while((inputLine = in.readLine()) != null){
                    String []list = split(inputLine);
                    if(list != null){
                        int train_id = Integer.parseInt(list[0]);
                        Socket target = receiver.get(train_id);
                        if(target != null){
                            PrintWriter targetOut = new PrintWriter(target.getOutputStream(), true);
                            targetOut.println(list[1]);
                            targetOut.close();
                        }else{
                            System.out.printf("The train %d is disconnected from server!\n", train_id);
                        }
                    }else{
                        System.err.printf("The info format(%s) from cloud is wrong!\n", inputLine);
                    }
                }
                System.out.printf("Closing connection with %s:%d\n",
                        clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                mainSocket = null;
            }

            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] split(String info){
        int pos = info.indexOf(",");
        if(pos != -1){
            return new String[]{info.substring(0, pos), info.substring(pos + 1)};
        }
        else return null;
    }
}
