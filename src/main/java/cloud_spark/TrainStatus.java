package cloud_spark;

import lombok.Data;

import java.math.BigInteger;

@Data
public class TrainStatus implements Comparable<TrainStatus>{
    private BigInteger id;

    private BigInteger time;

    private Integer train_id;

    private Integer speed;

    private Integer position;

    private Integer passenger_num;

    public TrainStatus(BigInteger time, int id, int speed, int position, int passenger_num) {
        this.time = time;
        this.train_id = id;
        this.speed = speed;
        this.position = position;
        this.passenger_num = passenger_num;
    }

    @Override
    public int compareTo(TrainStatus s1){
        if(this.time.compareTo(s1.time) > 0)
            return 1;
        else if(this.time.compareTo(s1.time) == 0){
            if(this.train_id > s1.train_id)
                return 1;
            else return -1;
        }
        else return -1;
    }

}
