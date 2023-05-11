package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class Client extends Thread{
    private final Integer train_id;

    public Client(Integer id){
        super("client-" + id);
        train_id = id;
    }

    public static void main(String []args){
        for(int i = 1; i <= 4; i++){
            Thread t = new Client(i);
            t.start();
        }
    }

    @Override
    public void run(){
        try {
            Socket socket = new Socket("localhost", 8080);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(train_id);
            int position = 0;
            int lastSpeed = 0;
            AtomicReference<String> inputLine = new AtomicReference<>("");
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while(true){
                        inputLine.set(in.readLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            while (true) {
                String in = inputLine.getAndSet("");
                if(in != null && !in.equals("")){
                    System.out.printf("[%s] Receive a message from server: %s\n", currentThread().getName(), in);
                    String[] text = in.split(",");
                    if(Integer.parseInt(text[0]) == train_id){
                        position = Integer.parseInt(text[2]);
                        lastSpeed = 0;
                        System.out.printf("[%s] Receive a message from server: %s(Sleeping 5s...)\n", currentThread().getName(), in);
                        Thread.sleep(5000);
                    }
                }
                long time = System.currentTimeMillis();
                Random r = new Random(time);
                int speed = r.nextInt(30);
                position += (speed + lastSpeed) / 2;
                lastSpeed = speed;
                String info = String.format("%d,%d,%d,%d", time, train_id, speed, position);
                out.println(info);
                Thread.sleep(500);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
