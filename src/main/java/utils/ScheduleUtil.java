package utils;

import cloud_spark.CustomReceiver;
import cloud_spark.Station;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.Map;

import cloud_spark.TrainStatus;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import java.util.*;

public class ScheduleUtil {
    public static final Map<Integer, TrainStatus> trains = new ConcurrentHashMap<>();
    public static final Map<Integer, Station> stations = new ConcurrentHashMap<>();

    public static final Map<Integer, TrainStatus> trainStatusMap = new ConcurrentSkipListMap<>(Collections.reverseOrder());
    public static final Map<Integer, Station> stationStatusMap = new ConcurrentSkipListMap<>(Collections.reverseOrder());

    private final static Integer TRAIN_CAPACITY = 150;
    private final static Integer MIN_DISTANCE = 2000;
    private final static Double GO_OFF_TRAIN_RATE = 0.3;
    private final static Double GO_TO_STATION_RATE = 0.3;
    private final static Integer STOP_TIME = 30;
    private final static Integer GO_ON_OFF_SPEED = 20;
    private final static Double FULL_LOAD_RATE = 0.9;
    private final static Double EMPTY_LOAD_RATE = 0.3;

    private final static Integer FASTEST_SPEED = 750;
    private final static Integer FASTER_SPEED = 600;
    private final static Integer NORMAL_SPEED = 500;
    private final static Integer SLOWER_SPEED = 375;
    private final static Integer SLOWEST_SPEED = 300;
    private final static Integer[] SPEEDS = {SLOWEST_SPEED, SLOWER_SPEED, NORMAL_SPEED, FASTER_SPEED, FASTEST_SPEED};

    private static class SchedResult {
        Integer oldSpeed;
        Integer speed;
        Integer train_id;
        Boolean send;

        public SchedResult(Integer speed, Integer train_id, Boolean send, Integer oldSpeed) {
            this.speed = speed;
            this.train_id = train_id;
            this.send = send;
            this.oldSpeed = oldSpeed;
        }

        public Integer getSpeed() {
            return speed;
        }

        public Integer getTrain_id() {
            return train_id;
        }

        public Boolean isScheduled() {
            return send;
        }

        public Integer getOldSpeed() {
            return oldSpeed;
        }

        public void setSpeed(Integer speed) {
            this.speed = speed;
        }

        public void setSend(Boolean send) {
            this.send = send;
        }
    }

    private static Integer getSpeedIndex(Integer speed) {
        if(speed >= FASTER_SPEED + (FASTEST_SPEED - FASTER_SPEED) / 2)
            return 4;
        else if(speed >= NORMAL_SPEED + (FASTER_SPEED - NORMAL_SPEED) / 2)
            return 3;
        else if(speed >= SLOWER_SPEED + (NORMAL_SPEED - SLOWER_SPEED) / 2)
            return 2;
        else if(speed >= SLOWEST_SPEED + (SLOWER_SPEED - SLOWEST_SPEED) / 2)
            return 1;
        else return 0;
    }

    public static void schedule(Dataset<Row> data, CustomReceiver receiver){
        List<Station> waitingPeople = new LinkedList<>();
        ArrayList<SchedResult> schedResult = new ArrayList<>();
        Iterator<Map.Entry<Integer, Station>> stationItr = stationStatusMap.entrySet().iterator();
        Iterator<Map.Entry<Integer, TrainStatus>> trainItr = trainStatusMap.entrySet().iterator();
        Map.Entry<Integer, Station> entry1 = null;
        Integer lastTrainPosition = 0;
        boolean firstTrain = true;
        boolean lastTrainStopMore = true;
        while(trainItr.hasNext()) {
            Map.Entry<Integer, TrainStatus> entry = trainItr.next();
            Integer position = entry.getKey();
            TrainStatus trainStatus = entry.getValue();

            if(position <= 0 || trainStatus.getSpeed() == 0) continue;

            if(!firstTrain) {
                assert entry1 != null;
                if(entry1.getValue().getPosition() > position){
                    Station stationStatus = new Station(
                            entry1.getValue().getId(),
                            entry1.getValue().getTime().longValue(),
                            entry1.getValue().getPeopleNum(),
                            entry1.getValue().getPosition());
                    waitingPeople.add(0, stationStatus);
                }
            }

            while(stationItr.hasNext()){
                entry1 = stationItr.next();
                if(entry1.getValue().getPosition() > position){
                    Station stationStatus = new Station(
                            entry1.getValue().getId(),
                            entry1.getValue().getTime().longValue(),
                            entry1.getValue().getPeopleNum(),
                            entry1.getValue().getPosition());
                    waitingPeople.add(0, stationStatus);
                }
                else {
                    break;
                }
            }

            int reduceSpeed = 0;
            if(firstTrain) {
                firstTrain = false;

                Integer passengerNumOnTrain = trainStatus.getPassenger_num();
                int arrivalTime = 0;
                Integer lastStationPosition = 0;
                Iterator<Station> waitingIterator = waitingPeople.iterator();
                boolean nextStation = true;
                int fullLoadTimes = 0, emptyLoadTimes = 0;
                while(waitingIterator.hasNext()) {
                    Station stationStatus = waitingIterator.next();
                    Integer goOffNum = (int) Math.round(passengerNumOnTrain * GO_OFF_TRAIN_RATE);
                    Integer remainCapacity = TRAIN_CAPACITY - passengerNumOnTrain + goOffNum;

                    System.out.println("train" + trainStatus.getTrain_id() + " " + "station" + stationStatus.getId() + " " + "passengerNumOnTrain" + passengerNumOnTrain + " " + "goOffNum" + goOffNum + " " + "remainCapacity" + remainCapacity);

                    if(passengerNumOnTrain > (int)Math.round(TRAIN_CAPACITY * FULL_LOAD_RATE)){
                        fullLoadTimes++;
                    }
                    if(passengerNumOnTrain < (int)Math.round(TRAIN_CAPACITY * EMPTY_LOAD_RATE)) {
                        emptyLoadTimes++;
                    }

                    if (nextStation) {
                        arrivalTime = (stationStatus.getPosition() - position) / trainStatus.getSpeed();
                        lastStationPosition = stationStatus.getPosition();
                        nextStation = false;
                        stationStatus.setPeopleNum(stationStatus.getPeopleNum() + (int) Math.round(arrivalTime * GO_TO_STATION_RATE));

                        if(remainCapacity >= stationStatus.getPeopleNum()){
                            passengerNumOnTrain = passengerNumOnTrain - goOffNum + stationStatus.getPeopleNum();
                            lastTrainStopMore = stationStatus.getPeopleNum() + goOffNum > STOP_TIME * GO_ON_OFF_SPEED;
                            stationStatus.setPeopleNum(0);
                        }
                        else{
                            passengerNumOnTrain = passengerNumOnTrain - goOffNum + remainCapacity;
                            lastTrainStopMore = remainCapacity + goOffNum > STOP_TIME * GO_ON_OFF_SPEED;
                            stationStatus.setPeopleNum(stationStatus.getPeopleNum() - remainCapacity);
                        }
                    }
                    else {
                        arrivalTime = arrivalTime + STOP_TIME +
                                (stationStatus.getPosition() - lastStationPosition) / trainStatus.getSpeed();
                        lastStationPosition = stationStatus.getPosition();
                        stationStatus.setPeopleNum(stationStatus.getPeopleNum() + (int) Math.round(arrivalTime * GO_TO_STATION_RATE));
                        if(remainCapacity >= stationStatus.getPeopleNum()){
                            passengerNumOnTrain = passengerNumOnTrain - goOffNum + stationStatus.getPeopleNum();
                            stationStatus.setPeopleNum(0);
                        }
                        else{
                            passengerNumOnTrain = passengerNumOnTrain - goOffNum + remainCapacity;
                            stationStatus.setPeopleNum(stationStatus.getPeopleNum() - remainCapacity);
                        }
                    }
                }

                System.out.println("train" + trainStatus.getTrain_id() + " " + "passengerNumOnTrain" + passengerNumOnTrain);

                if(passengerNumOnTrain > (int)Math.round(TRAIN_CAPACITY * FULL_LOAD_RATE)){
                    fullLoadTimes++;
                }
                if(passengerNumOnTrain < (int)Math.round(TRAIN_CAPACITY * EMPTY_LOAD_RATE)) {
                    emptyLoadTimes++;
                }

                // schedule result
                int stationPassed = waitingPeople.size() + 1;
                System.out.println("train" + trainStatus.getTrain_id() + ":" + "stationPassed: " + stationPassed + "fullLoadTimes: " + fullLoadTimes + "emptyLoadTimes: " + emptyLoadTimes);

                int stillWaitingPeople = 0;
                for(Station station1 : waitingPeople) {
                    System.out.println("station" + station1.getId() + ":" + "peopleNum: " + station1.getPeopleNum());
                    stillWaitingPeople += station1.getPeopleNum();
                }

                if(fullLoadTimes > (int)Math.round(stationPassed * 0.5) || stillWaitingPeople > TRAIN_CAPACITY) {
                    int speedIndex = getSpeedIndex(trainStatus.getSpeed());
                    if(speedIndex >= SPEEDS.length - 1) {
                        schedResult.add(new SchedResult(SPEEDS[SPEEDS.length - 1], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed()));
                    }
                    else {
                        schedResult.add(new SchedResult(SPEEDS[speedIndex + 1], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed()));
                    }
                }
                else if(emptyLoadTimes > (int)Math.round(stationPassed * 0.5) || stillWaitingPeople < TRAIN_CAPACITY / 10) {
                    int speedIndex = getSpeedIndex(trainStatus.getSpeed());
                    if(speedIndex <= 0) {
                        schedResult.add(new SchedResult(SPEEDS[0], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed()));
                    }
                    else {
                        schedResult.add(new SchedResult(SPEEDS[speedIndex - 1], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed()));
                    }
                }
                else {
                    schedResult.add(new SchedResult(trainStatus.getSpeed(), trainStatus.getTrain_id(),
                            false, trainStatus.getSpeed()));
                }

                lastTrainPosition = trainStatus.getPosition();
            }
            else {
                if(lastTrainPosition - trainStatus.getPosition() < MIN_DISTANCE && lastTrainStopMore) {
                    reduceSpeed++;   // schedule
                }

                Integer passengerNumOnTrain = trainStatus.getPassenger_num();
                int arrivalTime = 0;
                Integer lastStationPosition = 0;
                Iterator<Station> waitingIterator = waitingPeople.iterator();
                boolean nextStation = true;
                int fullLoadTimes = 0, emptyLoadTimes = 0;
                while(waitingIterator.hasNext()) {
                    Station stationStatus = waitingIterator.next();
                    Integer goOffNum = (int) Math.round(passengerNumOnTrain * GO_OFF_TRAIN_RATE);
                    Integer remainCapacity = TRAIN_CAPACITY - passengerNumOnTrain + goOffNum;

                    System.out.println("train" + trainStatus.getTrain_id() + " " + "station" + stationStatus.getId() + " " + "passengerNumOnTrain" + passengerNumOnTrain + " " + "goOffNum" + goOffNum + " " + "remainCapacity" + remainCapacity);

                    if(passengerNumOnTrain > (int)Math.round(TRAIN_CAPACITY * FULL_LOAD_RATE)){
                        fullLoadTimes++;
                    }
                    if(passengerNumOnTrain < (int)Math.round(TRAIN_CAPACITY * EMPTY_LOAD_RATE)) {
                        emptyLoadTimes++;
                    }

                    if (nextStation) {
                        arrivalTime = (stationStatus.getPosition() - position) / trainStatus.getSpeed();
                        lastStationPosition = stationStatus.getPosition();
                        nextStation = false;
                        stationStatus.setPeopleNum(stationStatus.getPeopleNum() + (int) Math.round(arrivalTime * GO_TO_STATION_RATE));

                        if(remainCapacity >= stationStatus.getPeopleNum()){
                            passengerNumOnTrain = passengerNumOnTrain - goOffNum + stationStatus.getPeopleNum();
                            lastTrainStopMore = stationStatus.getPeopleNum() + goOffNum > STOP_TIME * GO_ON_OFF_SPEED;
                            stationStatus.setPeopleNum(0);
                        }
                        else{
                            passengerNumOnTrain = passengerNumOnTrain - goOffNum + remainCapacity;
                            lastTrainStopMore = remainCapacity + goOffNum > STOP_TIME * GO_ON_OFF_SPEED;
                            stationStatus.setPeopleNum(stationStatus.getPeopleNum() - remainCapacity);
                        }
                    }
                    else {
                        if(stationStatus.getPosition() <= lastTrainPosition) {
                            arrivalTime = arrivalTime + STOP_TIME +
                                    (stationStatus.getPosition() - lastStationPosition) / trainStatus.getSpeed();
                            lastStationPosition = stationStatus.getPosition();
                            stationStatus.setPeopleNum(stationStatus.getPeopleNum() + (int) Math.round(arrivalTime * GO_TO_STATION_RATE));
                            if (remainCapacity >= stationStatus.getPeopleNum()) {
                                passengerNumOnTrain = passengerNumOnTrain - goOffNum + stationStatus.getPeopleNum();
                                stationStatus.setPeopleNum(0);
                            }
                            else {
                                passengerNumOnTrain = passengerNumOnTrain - goOffNum + remainCapacity;
                                stationStatus.setPeopleNum(stationStatus.getPeopleNum() - remainCapacity);
                            }
                        }
                        else {
                            if(lastStationPosition <= lastTrainPosition) {
                                arrivalTime = arrivalTime + STOP_TIME + (lastTrainPosition - lastStationPosition) / trainStatus.getSpeed();
                            }
                            lastStationPosition = stationStatus.getPosition();
                            stationStatus.setPeopleNum(stationStatus.getPeopleNum() + (int) Math.round(arrivalTime * GO_TO_STATION_RATE));
                            if (remainCapacity >= stationStatus.getPeopleNum()) {
                                passengerNumOnTrain = passengerNumOnTrain - goOffNum + stationStatus.getPeopleNum();
                                stationStatus.setPeopleNum(0);
                            }
                            else {
                                passengerNumOnTrain = passengerNumOnTrain - goOffNum + remainCapacity;
                                stationStatus.setPeopleNum(stationStatus.getPeopleNum() - remainCapacity);
                            }
                        }
                    }
                }

                System.out.println("train" + trainStatus.getTrain_id() + " " + "passengerNumOnTrain" + passengerNumOnTrain);
                if(passengerNumOnTrain > (int)Math.round(TRAIN_CAPACITY * FULL_LOAD_RATE)){
                    fullLoadTimes++;
                }
                if(passengerNumOnTrain < (int)Math.round(TRAIN_CAPACITY * EMPTY_LOAD_RATE)) {
                    emptyLoadTimes++;
                }

                int stillWaitingPeople = 0;
                for(Station station1 : waitingPeople) {
                    System.out.println("station" + station1.getId() + ":" + "peopleNum: " + station1.getPeopleNum());
                    stillWaitingPeople += station1.getPeopleNum();
                }

                // schedule result
                int stationPassed = waitingPeople.size() + 1;
                System.out.println("train" + trainStatus.getTrain_id() + ":" + "stationPassed: " + stationPassed + "fullLoadTimes: " + fullLoadTimes + "emptyLoadTimes: " + emptyLoadTimes);
                if(fullLoadTimes > (int)Math.round(stationPassed * 0.5) || stillWaitingPeople > TRAIN_CAPACITY) {
                    int speedIndex = getSpeedIndex(trainStatus.getSpeed());
                    if(speedIndex + 1 - reduceSpeed >= SPEEDS.length) {
                        schedResult.add(new SchedResult(SPEEDS[SPEEDS.length - 1], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed()));
                    }
                    else if(speedIndex + 1 - reduceSpeed < 0) {
                        schedResult.add(new SchedResult(SPEEDS[0], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed()));
                    }
                    else {
                        schedResult.add(new SchedResult(SPEEDS[speedIndex + 1 - reduceSpeed], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed()));
                    }
                }
                else if(emptyLoadTimes > (int)Math.round(stationPassed * 0.5) || stillWaitingPeople < TRAIN_CAPACITY / 10) {
                    int speedIndex = getSpeedIndex(trainStatus.getSpeed());
                    if(speedIndex - 1 - reduceSpeed >= SPEEDS.length) {
                        schedResult.add(new SchedResult(SPEEDS[SPEEDS.length - 1], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed()));
                    }
                    else if(speedIndex - 1 - reduceSpeed < 0) {
                        schedResult.add(new SchedResult(SPEEDS[0], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed()));
                    }
                    else {
                        schedResult.add(new SchedResult(SPEEDS[speedIndex - 1 - reduceSpeed], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed()));
                    }
                }
                else {
                    schedResult.add(new SchedResult(trainStatus.getSpeed(), trainStatus.getTrain_id(),
                            false, trainStatus.getSpeed()));
                }

                lastTrainPosition = trainStatus.getPosition();
            }
        }

        int schedResultSize = schedResult.size();
        for(int i = 0; i < schedResultSize; i++) {
            SchedResult schedResultItem = schedResult.get(i);
            if(i > 0) {
                if(schedResultItem.getSpeed() > schedResult.get(i - 1).getSpeed()) {
                    schedResultItem.setSpeed(schedResult.get(i - 1).getSpeed());
                    schedResultItem.setSend(true);
                }
            }
            if (schedResultItem.isScheduled()) {
                System.out.println("train" + schedResultItem.getTrain_id() + ": " +
                       schedResultItem.getOldSpeed() + "->"  + schedResultItem.getSpeed());
                if(receiver != null) {
                    receiver.send(schedResultItem.getTrain_id(), String.valueOf(schedResultItem.getSpeed()));
                }
            } else {
                System.out.println("train" + schedResultItem.getTrain_id() + ": " +
                        schedResultItem.getOldSpeed() + "->"  + schedResultItem.getSpeed() + " (not scheduled)");
            }
        }
    }
}
