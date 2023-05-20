package com.example.sparkcloud;
import java.math.BigInteger;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;

import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import scala.Tuple2;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.spark.api.java.function.Function;

public class SparkCloud {
    public static void main(String[] args) {

        Properties prop = new Properties();
        InputStream input;
        try {
            input = new FileInputStream("./src/main/resources/config.properties");
            prop.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SparkConf conf = new SparkConf().setAppName("SparkCloud").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        // 创建Spark Streaming上下文，每隔1秒接受和处理一次数据
        JavaStreamingContext jsc = new JavaStreamingContext(sc, Durations.seconds(1));

        // 创建Receiver通过Socket Server接收小车和车站的实时数据流
        CustomStationReceiver stationReceiver = new CustomStationReceiver(8079);
        CustomTrainReceiver trainReceiver = new CustomTrainReceiver(8081);

        // 创建ReceiverInputStream接收Receiver存储下来的实时数据
        JavaReceiverInputDStream<String> trainReceiverInputDStream = jsc.receiverStream(trainReceiver);
        JavaReceiverInputDStream<String> stationReceiverInputDStream = jsc.receiverStream(stationReceiver);

        // trainReceiverInputDStream.print();
        // stationReceiverInputDStream.print();

        // 解析和转换实时数据流中的数据
        JavaPairDStream<Integer, Train>  parsedTrainPairDStream = trainReceiverInputDStream
               .map(SparkCloud::parseTrainData)
               .filter(row -> row.length == 5)
               .mapToPair(row -> {
                    BigInteger time = BigInteger.valueOf(Long.parseLong(row[0]));
                    Integer id = Integer.parseInt(row[1]);
                    Integer speed = Integer.parseInt(row[2]);
                    Integer position = Integer.parseInt(row[3]);
                    Integer passenger_num = Integer.parseInt(row[4]);
                    Train train = new Train(time, id, speed, position, passenger_num);
                    return new Tuple2<>(id, train);
               });

        JavaPairDStream<Integer, Station> parsedStationPairStream = stationReceiverInputDStream
               .map(SparkCloud::parseStationData)
               .filter(row -> row.length == 4)
               .mapToPair(row -> {
                    Integer id = Integer.parseInt(row[0]);
                    BigInteger time = BigInteger.valueOf(Long.parseLong(row[1]));
                    Integer passenger_num = Integer.parseInt(row[2]);
                    Integer position = Integer.parseInt(row[3]);
                    Station station = new Station(id, time, passenger_num, position);
                    return new Tuple2<>(id, station);
               });

        // parsedTrainPairDStream.print();
        // parsedStationPairStream.print();

        // 保留同一辆车或者同一个车站最新的状态数据
        JavaPairDStream<Integer, Train> latestTrainPairDStream = parsedTrainPairDStream
                .reduceByKey((trainStatus1, trainStatus2) -> {
                    if(trainStatus1.getTime().compareTo(trainStatus2.getTime()) > 0)
                        return trainStatus1;
                    else return trainStatus2;
                });
        JavaPairDStream<Integer, Station> latestStationPairDStream = parsedStationPairStream
                .reduceByKey((stationStatus1, stationStatus2) -> {
                    if(stationStatus1.getTime().compareTo(stationStatus2.getTime()) > 0)
                        return stationStatus1;
                    else return stationStatus2;
                });

        // latestTrainPairDStream.print();
        // latestStationPairDStream.print();

        // 将RDD中小车和车站的最新状态数据按照位置从大到小排序
        JavaPairDStream<Integer, Train> latestTrainPairDStreamSortedByPosition = latestTrainPairDStream
                .mapToPair(pair -> new Tuple2<>(pair._2.getPosition(), pair._2))
                .transformToPair(rdd -> rdd.sortByKey(false));

        JavaPairDStream<Integer, Station> latestStationPairDStreamSortedByPosition = latestStationPairDStream
                .mapToPair(pair -> new Tuple2<>(pair._2.getPosition(), pair._2))
                .transformToPair(rdd -> rdd.sortByKey(false));

        // latestTrainPairDStreamSortedByPosition.print();
        // latestStationPairDStreamSortedByPosition.print();

        // 调度策略的计算逻辑
        latestStationPairDStreamSortedByPosition.foreachRDD((rdd, time) -> {
            if(rdd != null) {
                List<Tuple2<Integer, Station>> stations = rdd.collect();
                for(Tuple2<Integer, Station> station : stations) {
                    if(station._2().getTime().compareTo(ScheduleUtil.STATIONS.get(station._2().getId()).getTime()) > 0) {
                        ScheduleUtil.STATIONS.set(station._2().getId(), station._2());
                    }
                }
            }
        });

        latestTrainPairDStreamSortedByPosition.foreachRDD((rdd, time) -> {
            if(rdd != null) {
                ScheduleUtil.schedule(trainReceiver, rdd.collect());

                SparkSession spark = JavaSparkSessionSingleton.getInstance(conf);
                JavaRDD<Train> trainRDD = rdd.map(new Function<Tuple2<Integer, Train>, Train>() {
                    @Override
                    public Train call(Tuple2<Integer, Train> tuple2) throws Exception {
                        return tuple2._2;
                    }
                });

                Dataset<Row> dataFrame = spark.createDataFrame(trainRDD, Train.class);
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

        jsc.start();
        try {
            jsc.awaitTermination();
            jsc.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 解析小车数据
    private static String[] parseTrainData(String line) {
       return line.split(",");
    }

    // 解析车站数据
    private static String[] parseStationData(String line) {
        return line.split(",");
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
