package utils;

import cloud_spark.CustomReceiver;
import cloud_spark.Station;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import org.apache.spark.streaming.receiver.Receiver;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScheduleUtil {

    public static void schedule(Dataset<Row> data, CustomReceiver receiver, Map<Integer, Station> map){
        data = data.sort(data.col("time").desc(), data.col("train_id").asc());
        Set<Integer> set = new HashSet<>();
        data.foreach(row -> {
            int id = row.getInt(2);
            if(!set.contains(id)){
                set.add(id);
                int pos = row.getInt(4);
                for(Station s : map.values()){
                    if(Math.abs(pos - s.getPosition()) <= 500){
                        receiver.send(id, String.format("%d,%d,%d", s.getId(), s.getPosition(), s.getPeopleNum()));
                        break;
                    }
                }
            }
        });
    }
}
