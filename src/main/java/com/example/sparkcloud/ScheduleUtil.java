package com.example.sparkcloud;
import java.util.Iterator;

import scala.Tuple2;

import java.util.*;

public class ScheduleUtil {
    public final static ArrayList<Station> STATIONS = new ArrayList<>(Collections.nCopies(100, null));

    private final static Integer TRAIN_CAPACITY = 150;
    private final static Integer MIN_DISTANCE = 1500;
    private final static Integer STATION_DISTANCE  = 1000;
    private final static Double GO_OFF_TRAIN_RATE = 0.3;
    private final static Double GO_TO_STATION_RATE = 0.3;
    private final static Integer STOP_TIME = 30;
    private final static Integer GO_ON_OFF_SPEED = 20;
    private final static Double FULL_LOAD_RATE = 0.9;
    private final static Double EMPTY_LOAD_RATE = 0.3;
    private final static Double FULL_LOAD_LIMIT = 0.4;
    private final static Double EMPTY_LOAD_LIMIT = 0.4;

    private final static Integer FASTEST_SPEED = 750;
    private final static Integer FASTER_SPEED = 600;
    private final static Integer NORMAL_SPEED = 500;
    private final static Integer SLOWER_SPEED = 375;
    private final static Integer SLOWEST_SPEED = 300;
    private final static Integer[] SPEEDS = {SLOWEST_SPEED, SLOWER_SPEED, NORMAL_SPEED, FASTER_SPEED, FASTEST_SPEED};
    private final static Integer ZERO_SPEED = 20;

    private final static Long NORMAL_DEPART_INTERVAL = 5000L;
    private final static Long SHORTER_DEPART_INTERVAL = 4000L;
    private final static Long SHORTEST_DEPART_INTERVAL = 3000L;
    private final static Long LONGER_DEPART_INTERVAL = 6000L;
    private final static Long LONGEST_DEPART_INTERVAL = 7000L;
    private final static Long[] DEPART_INTERVALS = {LONGEST_DEPART_INTERVAL, LONGER_DEPART_INTERVAL, NORMAL_DEPART_INTERVAL, SHORTER_DEPART_INTERVAL, SHORTEST_DEPART_INTERVAL};

    private static Long lastDepartTime = 0L;
    private static Long departInterval = 0L;
    private static Integer initialSpeed = NORMAL_SPEED;
    private static Integer lastDepartTrainId = -1;

    private static class SchedResult {
        Integer oldSpeed;
        Integer speed;
        Integer train_id;
        Boolean send;
        Integer position;

        public SchedResult(Integer speed, Integer train_id, Boolean send, Integer oldSpeed, Integer position) {
            this.speed = speed;
            this.train_id = train_id;
            this.send = send;
            this.oldSpeed = oldSpeed;
            this.position = position;
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

        public Integer getPosition() {
            return position;
        }

        public Integer getOldSpeed() {
            return oldSpeed;
        }

        public void setSpeed(Integer speed) {
            this.speed = speed;
        }

        public void Schedule() {
            this.send = true;
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

    public static void schedule(CustomTrainReceiver receiver, List<Tuple2<Integer, Train>> trainList) {
        ArrayList<Station> waitingPeople = new ArrayList<>(STATIONS);
        ArrayList<SchedResult> schedResult = new ArrayList<>();
        Iterator<Tuple2<Integer, Train>> trainItr = trainList.iterator();
        Integer lastTrainPosition = 0;
        boolean firstTrain = true;
        boolean lastTrainStopMore = true;

        while(trainItr.hasNext()) {
            Tuple2<Integer, Train> trainEntry = trainItr.next();
            Integer position = trainEntry._1();
            Train trainStatus = trainEntry._2();

            if(position <= 0 || trainStatus.getSpeed() == 0) continue;

            int reduceSpeed = 0;
            if(firstTrain) {
                firstTrain = false;

                Integer passengerNumOnTrain = trainStatus.getPassenger_num();
                boolean nextStation = true;
                int arrivalTime = 0;
                int lastStationPosition = 0;
                int fullLoadTimes = 0, emptyLoadTimes = 0;
                int stationNum = waitingPeople.size();
                int stationPassed = 0;

                for(int i = 0; i < stationNum; ++i) {
                    Station stationStatus = waitingPeople.get(i);
                    if(stationStatus == null)      continue;
                    if(stationStatus.getPosition() < position)   continue;
                    stationPassed++;

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
                        nextStation = false;
                        arrivalTime = (stationStatus.getPosition() - position) / trainStatus.getSpeed();
                        if(stationStatus.getPosition() == position)
                            arrivalTime += (STOP_TIME / 2);
                        lastStationPosition = stationStatus.getPosition();
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
                System.out.println("train" + trainStatus.getTrain_id() + ":" + "stationPassed: " + stationPassed + "fullLoadTimes: " + fullLoadTimes + "emptyLoadTimes: " + emptyLoadTimes);

                int stillWaitingPeople = 0;
                for(Station status : waitingPeople) {
                    System.out.println("station" + status.getId() + ":" + "peopleNum: " + status.getPeopleNum());
                    stillWaitingPeople += status.getPeopleNum();
                }

                if(fullLoadTimes > (int)Math.round(stationPassed * FULL_LOAD_LIMIT) || stillWaitingPeople > TRAIN_CAPACITY) {
                    int speedIndex = getSpeedIndex(trainStatus.getSpeed());
                    if(speedIndex >= SPEEDS.length - 1) {
                        schedResult.add(new SchedResult(SPEEDS[SPEEDS.length - 1], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed(), trainStatus.getPosition()));
                    }
                    else {
                        schedResult.add(new SchedResult(SPEEDS[speedIndex + 1], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed(), trainStatus.getPosition()));
                    }
                }
                else if(emptyLoadTimes > (int)Math.round(stationPassed * EMPTY_LOAD_LIMIT) || stillWaitingPeople < TRAIN_CAPACITY / 10) {
                    int speedIndex = getSpeedIndex(trainStatus.getSpeed());
                    if(speedIndex <= 0) {
                        schedResult.add(new SchedResult(SPEEDS[0], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed(), trainStatus.getPosition()));
                    }
                    else {
                        schedResult.add(new SchedResult(SPEEDS[speedIndex - 1], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed(), trainStatus.getPosition()));
                    }
                }
                else {
                    schedResult.add(new SchedResult(trainStatus.getSpeed(), trainStatus.getTrain_id(),
                            false, trainStatus.getSpeed(), trainStatus.getPosition()));
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
                boolean nextStation = true;
                int fullLoadTimes = 0, emptyLoadTimes = 0;
                int stationNum = waitingPeople.size();
                int stationPassed = 0;

                for(int i = 0; i < stationNum; i++) {
                    Station stationStatus = waitingPeople.get(i);
                    if(stationStatus == null)   continue;
                    if(stationStatus.getPosition() > position) continue;
                    stationPassed++;

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
                        nextStation = false;
                        arrivalTime = (stationStatus.getPosition() - position) / trainStatus.getSpeed();
                        if(position == stationStatus.getPosition())
                            arrivalTime += (STOP_TIME / 2);
                        lastStationPosition = stationStatus.getPosition();
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

                System.out.println("train" + trainStatus.getTrain_id() + ":" + "stationPassed: " + stationPassed + "fullLoadTimes: " + fullLoadTimes + "emptyLoadTimes: " + emptyLoadTimes);

                int stillWaitingPeople = 0;
                for(Station status : waitingPeople) {
                    System.out.println("station" + status.getId() + ":" + "peopleNum: " + status.getPeopleNum());
                    stillWaitingPeople += status.getPeopleNum();
                }

                // schedule result
                if(fullLoadTimes > (int)Math.round(stationPassed * FULL_LOAD_LIMIT) || stillWaitingPeople > TRAIN_CAPACITY) {
                    int speedIndex = getSpeedIndex(trainStatus.getSpeed());
                    if(speedIndex + 1 - reduceSpeed >= SPEEDS.length) {
                        schedResult.add(new SchedResult(SPEEDS[SPEEDS.length - 1], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed(), trainStatus.getPosition()));
                    }
                    else if(speedIndex + 1 - reduceSpeed < 0) {
                        schedResult.add(new SchedResult(SPEEDS[0], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed(), trainStatus.getPosition()));
                    }
                    else {
                        schedResult.add(new SchedResult(SPEEDS[speedIndex + 1 - reduceSpeed], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed(), trainStatus.getPosition()));
                    }
                }
                else if(emptyLoadTimes > (int)Math.round(stationPassed * EMPTY_LOAD_LIMIT) || stillWaitingPeople < TRAIN_CAPACITY / 10) {
                    int speedIndex = getSpeedIndex(trainStatus.getSpeed());
                    if(speedIndex - 1 - reduceSpeed >= SPEEDS.length) {
                        schedResult.add(new SchedResult(SPEEDS[SPEEDS.length - 1], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed(), trainStatus.getPosition()));
                    }
                    else if(speedIndex - 1 - reduceSpeed < 0) {
                        schedResult.add(new SchedResult(SPEEDS[0], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed(), trainStatus.getPosition()));
                    }
                    else {
                        schedResult.add(new SchedResult(SPEEDS[speedIndex - 1 - reduceSpeed], trainStatus.getTrain_id(),
                                true, trainStatus.getSpeed(), trainStatus.getPosition()));
                    }
                }
                else {
                    schedResult.add(new SchedResult(trainStatus.getSpeed(), trainStatus.getTrain_id(),
                            false, trainStatus.getSpeed(), trainStatus.getPosition()));
                }

                lastTrainPosition = trainStatus.getPosition();
            }
        }

        int schedResultSize = schedResult.size();
        boolean isDanger = false;
        for(int i = 0; i < schedResultSize; i++) {
            SchedResult schedResultItem = schedResult.get(i);
            if(i > 0) {
                if(schedResultItem.getPosition() - schedResult.get(i - 1).getPosition() < STATION_DISTANCE) {
                    isDanger = true;
                }
            }
            if(isDanger) {
                schedResultItem.setSpeed(ZERO_SPEED);
                schedResultItem.Schedule();
            }
            if (schedResultItem.isScheduled()) {
                System.out.println("train" + schedResultItem.getTrain_id() + ": " +
                       schedResultItem.getOldSpeed() + "->"  + schedResultItem.getSpeed());
                if(receiver != null) {
                    receiver.send(schedResultItem.getTrain_id(), String.valueOf(schedResultItem.getSpeed()));
                }
                if(i == schedResultSize - 1) {
                    if(schedResultItem.getOldSpeed() > schedResultItem.getSpeed()) {
                        Integer index = getSpeedIndex(schedResultItem.getSpeed());
                        departInterval = DEPART_INTERVALS[index];
                        initialSpeed = schedResultItem.getSpeed();
                    } else {
                        Integer index = getSpeedIndex(schedResultItem.getSpeed());
                        departInterval = DEPART_INTERVALS[index];
                        initialSpeed = schedResultItem.getSpeed();
                    }
                }
            } else {
                System.out.println("train" + schedResultItem.getTrain_id() + ": " +
                        schedResultItem.getOldSpeed() + "->"  + schedResultItem.getSpeed() + " (not scheduled)");
            }
        }

        if(System.currentTimeMillis() > departInterval + lastDepartTime
                && schedResult.get(schedResultSize - 1).getPosition() > MIN_DISTANCE) {
            if(receiver != null) {
                receiver.send(lastDepartTrainId + 1, String.valueOf(initialSpeed));
            }
            System.out.println("train" + (lastDepartTrainId + 1) + ": depart, speed " + initialSpeed);
            lastDepartTrainId++;
            lastDepartTime = System.currentTimeMillis();
        }
    }
}
