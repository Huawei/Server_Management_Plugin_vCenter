package com.huawei.vcenterpluginui.utils;

/**
 * Created by Rays on 2018/4/16.
 */
public class IdWorker {
    private final long workerId;
    private final long datacenterId;
    private final long idepoch;
    private final long workerIdBits;
    private final long datacenterIdBits;
    private final long maxWorkerId;
    private final long maxDatacenterId;
    private final long sequenceBits;
    private final long workerIdShift;
    private final long datacenterIdShift;
    private final long timestampLeftShift;
    private final long sequenceMask;
    private long lastTimestamp;
    private long sequence;

    public IdWorker(long workerId, long datacenterId, long sequence) {
        this(workerId, datacenterId, sequence, 1344322705519L);
    }

    public IdWorker(long workerId, long datacenterId, long sequence, long idepoch) {
        this.workerIdBits = 5L;
        this.datacenterIdBits = 5L;
        this.maxWorkerId = 31L;
        this.maxDatacenterId = 31L;
        this.sequenceBits = 12L;
        this.workerIdShift = 12L;
        this.datacenterIdShift = 17L;
        this.timestampLeftShift = 22L;
        this.sequenceMask = 4095L;
        this.lastTimestamp = -1L;
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.sequence = sequence;
        this.idepoch = idepoch;
        if (workerId >= 0L && workerId <= 31L) {
            if (datacenterId < 0L || datacenterId > 31L) {
                throw new IllegalArgumentException("datacenterId is illegal: " + workerId);
            }
        } else {
            throw new IllegalArgumentException("workerId is illegal: " + workerId);
        }
    }

    public long getDatacenterId() {
        return this.datacenterId;
    }

    public long getWorkerId() {
        return this.workerId;
    }

    public long time() {
        return System.currentTimeMillis();
    }

    public long nextId() {
        return this.random();
    }

    public synchronized long random() {
        long timestamp = this.time();
        if (timestamp < this.lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards.");
        } else {
            if (this.lastTimestamp == timestamp) {
                this.sequence = this.sequence + 1L & 4095L;
                if (this.sequence == 0L) {
                    timestamp = this.nextTime(this.lastTimestamp);
                }
            } else {
                this.sequence = 0L;
            }

            this.lastTimestamp = timestamp;
            return timestamp - this.idepoch << 22 | this.datacenterId << 17 | this.workerId << 12 | this.sequence;
        }
    }

    private long nextTime(long lastTimestamp) {
        long timestamp = this.time();
        while (timestamp <= lastTimestamp) {
            timestamp = this.time();
        }
        return timestamp;
    }
}
