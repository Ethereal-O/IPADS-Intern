package cloud_spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.*;
import org.apache.spark.streaming.api.java.*;
import utils.ScheduleUtil;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Properties;

import static utils.ScheduleUtil.peopleTraffic;

public class sparkStreaming {
    public static void main(String[] args) throws Exception{

        Properties prop = new Properties();
        InputStream input = new FileInputStream("./src/main/resources/config.properties");
        prop.load(input);

        SparkConf conf = new SparkConf().setMaster("local[4]").setAppName("TrainSchedule");
        JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(1));

        CustomReceiver customReceiver = new CustomReceiver(8080);

        JavaReceiverInputDStream<String> trainInfos = jsc.receiverStream(customReceiver);

        //jsc.receiverStream(new CustomStationReceiver(9090));

        trainInfos.foreachRDD((VoidFunction2<JavaRDD<String>, Time>) (rdd, time) -> {
            if(rdd != null){
                SparkSession spark = JavaSparkSessionSingleton.getInstance(conf);
                JavaRDD<TrainStatus> rowRDD = rdd.map((Function<String, TrainStatus>) line -> {
                    String[] cols = line.split(",");
                    return new TrainStatus(BigInteger.valueOf(Long.parseLong(cols[0])),Integer.parseInt(cols[1]),
                            Integer.parseInt(cols[2]), Integer.parseInt(cols[3]));
                });

                Dataset<Row> dataFrame = spark.createDataFrame(rowRDD, TrainStatus.class);
                ScheduleUtil.schedule(dataFrame, customReceiver);

                dataFrame = dataFrame.sort(dataFrame.col("time").asc(), dataFrame.col("train_id").asc());
                dataFrame.show();
                dataFrame.write()
                        .mode(SaveMode.Append)
                        .format("jdbc")
                        .option("url", prop.getProperty("jdbc.url"))
                        .option("dbtable", "status")
                        .option("user", prop.getProperty("jdbc.user"))
                        .option("password", prop.getProperty("jdbc.password"))
                        .save();
            }
        });

//        stationInfos.foreachRDD((VoidFunction2<JavaRDD<String>, Time>) (rdd, time) -> {
//            if(rdd != null){
//                SparkSession spark = JavaSparkSessionSingleton.getInstance(conf);
//                JavaRDD<Station> rowRDD = rdd.map((Function<String, Station>) line -> {
//                    String[] cols = line.split(",");
//                    int id = Integer.parseInt(cols[0]);
//                    Station station;
//                    if(peopleTraffic.containsKey(id)){
//                        station = peopleTraffic.get(id);
//                        BigInteger t = BigInteger.valueOf(Long.parseLong(cols[1]));
//                        if(t.compareTo(station.getTime()) > 0){
//                            station.setTime(t);
//                            station.setPeopleNum(Integer.parseInt(cols[2]));
//                        }
//                        peopleTraffic.put(id, station);
//                    }else{
//                        station = new Station(id, Long.parseLong(cols[1]), Integer.parseInt(cols[2]), Integer.parseInt(cols[3]));
//                        peopleTraffic.put(id, station);
//                    }
//                    return station;
//                });

//                Dataset<Row> dataFrame = spark.createDataFrame(rowRDD, Station.class);
//                dataFrame.show();
//            }
//        });

        jsc.start();
        jsc.awaitTermination();
    }

    private static class JavaSparkSessionSingleton {
        private static transient SparkSession instance = null;

        static SparkSession getInstance(SparkConf sparkConf) {
            if (instance == null) {
                instance = SparkSession.builder()
                        .config(sparkConf)
                        .getOrCreate();
            }
            return instance;
        }
    }
}
