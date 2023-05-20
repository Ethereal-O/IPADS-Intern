package com.example.sparkcloud;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomTrainReceiver extends Receiver<String> {
    private final Integer port;

    private static final Map<Integer, Socket> receiver = new ConcurrentHashMap<>();

    public CustomTrainReceiver(Integer port){
        super(StorageLevel.MEMORY_AND_DISK_2());
        this.port = port;
    }

    @Override
    public void onStart(){
        new Thread(this::receive).start();
    }

    @Override
    public void onStop(){

    }

    public void send(Integer train_id, String info){
        receiver.computeIfPresent(train_id, (key, socket) -> {
            String sendInfo = String.format("%d,%s", train_id, info);
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(sendInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return socket;
        });
    }

    private void receive(){
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.printf("Could not listen on port: %d.\n", port);
            System.exit(1);
        }

        while(true){
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.printf("Connected to client %s:%d\n",
                        clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                Thread t = new ClientThread(clientSocket);
                t.start();
            } catch (IOException e) {
                try { serverSocket.close(); } catch (IOException e1) { e1.printStackTrace(); }
                System.err.println("Accept failed.");
                System.exit(1);
            }
        }
    }

    private class ClientThread extends Thread{
        private final Socket clientSocket;

        public ClientThread(Socket clientSocket){
            super("client-"+clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
            this.clientSocket = clientSocket;
        }
        @Override
        public void run(){
            try{
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                inputLine = in.readLine();
                //TODO:we should handle wrong input
                if(inputLine != null && inputLine.length() > 5){
                    if(inputLine.startsWith("train-")){
                        int train_id = Integer.parseInt(inputLine.substring(6));
                        receiver.put(train_id, clientSocket);
                        while ((inputLine = in.readLine()) != null){
                            store(inputLine);
                        }
                        receiver.remove(train_id);
                    }
                }
                System.out.printf("Closing connection with %s:%d\n",
                        clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
