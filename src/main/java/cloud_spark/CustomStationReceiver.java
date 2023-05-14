package cloud_spark;

import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

import static utils.ScheduleUtil.stations;


public class CustomStationReceiver extends Receiver<String> {
    private final Integer port;

    public CustomStationReceiver(int port){
        super(StorageLevel.MEMORY_AND_DISK_2());
        this.port = port;
    }


    @Override
    public void onStart() {
        new Thread(this::receive).start();
    }

    @Override
    public void onStop() {

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
                System.err.println("Accept failed.");
                System.exit(1);
            }
        }
    }

    private static class ClientThread extends Thread{
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
                while ((inputLine = in.readLine()) != null){
                    String[] cols = inputLine.split(",");
                    int id = Integer.parseInt(cols[0]);
                    Station station;
                    if(stations.containsKey(id)){
                        station = stations.get(id);
                        BigInteger t = BigInteger.valueOf(Long.parseLong(cols[1]));
                        if(t.compareTo(station.getTime()) > 0){
                            station.setTime(t);
                            station.setPeopleNum(Integer.parseInt(cols[2]));
                        }
                        stations.put(id, station);
                    }else{
                        station = new Station(id, Long.parseLong(cols[1]), Integer.parseInt(cols[2]), Integer.parseInt(cols[3]));
                        stations.put(id, station);
                    }
                    //store(inputLine);
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
