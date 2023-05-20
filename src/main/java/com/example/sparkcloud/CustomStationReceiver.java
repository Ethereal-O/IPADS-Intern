package com.example.sparkcloud;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class CustomStationReceiver extends Receiver<String> {
    private final Integer port;

    public CustomStationReceiver(Integer port){
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
                    if(inputLine.startsWith("station-")){
                        int train_id = Integer.parseInt(inputLine.substring(6));
                        while ((inputLine = in.readLine()) != null){
                            store(inputLine);
                        }
                        ScheduleUtil.STATIONS.set(train_id, null);
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
