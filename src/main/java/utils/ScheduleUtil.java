package utils;

import cloud_spark.CustomReceiver;
import cloud_spark.Station;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ScheduleUtil {
    public static final Map<Integer, Station> peopleTraffic = new ConcurrentHashMap<>();

    public static void schedule(Dataset<Row> data, CustomReceiver receiver){
        data = data.sort(data.col("time").desc(), data.col("train_id").asc());
        Set<Integer> set = new HashSet<>();
        data.foreach(row -> {
            int id = row.getInt(4);
            if(!set.contains(id)){
                set.add(id);
                int pos = row.getInt(1);
                for(Station s : peopleTraffic.values()){
                    if(Math.abs(pos - s.getPosition()) <= 500){
                        receiver.send(id, String.format("%d,%d,%d", s.getId(), s.getPosition(), s.getPeopleNum()));
                        break;
                    }
                }
            }
        });
    }
}
