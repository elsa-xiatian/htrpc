package com.htrpc;

import javax.swing.plaf.PanelUI;
import java.util.Date;
import java.util.concurrent.atomic.LongAdder;

public class IdGenerator {
    private static LongAdder longAdder = new LongAdder();
    //起始时间戳
    public static final long START_STAMP = DateUtil.get("2025-1-1").getTime();

    public static final long DATA_CENTER_BIT = 5L;
    public static final long MACHINE_BIT = 5L;
    public static final long SEQUENCE_BIT = 12L;

    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);
    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT+ MACHINE_BIT + SEQUENCE_BIT;
    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;

    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId = new LongAdder();
    private long lastTimeStamp = -1L;

    public IdGenerator(long dataCenterId, long machineId) {
        if(dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX){
            throw new IllegalArgumentException("传入的编号不合法");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public  long getId(){
        //1.处理时间戳
        long currentTime = System.currentTimeMillis();

        long timeStamp = currentTime - START_STAMP;

        if(timeStamp < lastTimeStamp){
            throw new RuntimeException("您的服务器进行了时钟回调");
        }

        if(timeStamp == lastTimeStamp){
            sequenceId.increment();
            if(sequenceId.sum() >= SEQUENCE_MAX){
                timeStamp = getNextTimeStamp();
                sequenceId.reset();
            }
        }else{
            sequenceId.reset();
        }

        lastTimeStamp = timeStamp;
        long sequence = sequenceId.sum();
        return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_CENTER_LEFT |
                machineId << MACHINE_LEFT | sequence;
    }

    private long getNextTimeStamp() {
        long current = System.currentTimeMillis() - START_STAMP;
        while(current == lastTimeStamp){
         current = System.currentTimeMillis() - START_STAMP;
        }

        return current;
    }

    public static void main(String[] args) {

    }
}
