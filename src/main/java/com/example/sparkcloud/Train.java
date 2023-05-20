package com.example.sparkcloud;
import lombok.Data;
import scala.Serializable;

import java.math.BigInteger;

@Data
public class Train implements Serializable {
    private BigInteger id;
    private BigInteger time;
    private Integer train_id;
    private Integer speed;
    private Integer position;
    private Integer passenger_num;

    public Train(BigInteger time, int id, int speed, int position, int passenger_num) {
        this.time = time;
        this.train_id = id;
        this.speed = speed;
        this.position = position;
        this.passenger_num = passenger_num;
    }

    @Override
    public String toString() {
        return "Train{" +
                "id=" + id +
                ", time=" + time +
                ", train_id=" + train_id +
                ", speed=" + speed +
                ", position=" + position +
                ", passenger_num=" + passenger_num +
                '}';
    }
}
