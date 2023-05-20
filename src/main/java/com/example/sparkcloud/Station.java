package com.example.sparkcloud;
import lombok.Data;
import scala.Serializable;

import java.math.BigInteger;

@Data
public class Station implements Serializable {
    private Integer id;
    private BigInteger time;
    private Integer peopleNum;
    private Integer position;

    public Station(Integer id, BigInteger time, Integer num, Integer pos){
        this.id = id;
        this.time = time;
        this.peopleNum = num;
        this.position = pos;
    }

    @Override
    public String toString(){
        return "Station{" +
                "id=" + id +
                ", time=" + time +
                ", peopleNum=" + peopleNum +
                ", position=" + position +
                '}';
    }
}
