package cloud_spark;

import lombok.Data;

import java.math.BigInteger;

@Data
public class Station {
    private Integer id;
    private BigInteger time;
    private Integer peopleNum;
    private Integer position;

    public Station(int id, int position){
        this.id = id;
        time = BigInteger.valueOf(0);
        peopleNum = 0;
        this.position = position;
    }

    public Station(int id, long time, int num, int pos){
        this.id = id;
        this.time = BigInteger.valueOf(time);
        peopleNum = num;
        position = pos;
    }
}
