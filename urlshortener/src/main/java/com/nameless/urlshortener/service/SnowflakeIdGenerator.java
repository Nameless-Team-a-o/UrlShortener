package com.nameless.urlshortener.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SnowflakeIdGenerator {
    private final long workerId; // 10 bits for worker ID
    private long sequence = 0L; // 12 bits for sequence number
    private long lastTimestamp = -1L; // Last timestamp

    // Constants for bit lengths
    private static final long TWEPOCH = 1288834974657L; // Custom epoch
    private static final long SEQUENCE_BITS = 12L; // 12 bits for sequence number
    private static final long WORKER_ID_BITS = 10L; // 10 bits for worker ID

    // Maximum values for the IDs
    private final long maxWorkerId = ~(-1L << WORKER_ID_BITS); // Max worker ID (1023)
    private final long sequenceMask = ~(-1L << SEQUENCE_BITS); // Max sequence number (4095)

    public SnowflakeIdGenerator(@Value("${snowflake.workerId}") long workerId) {
        validateWorkerId(workerId);
        this.workerId = workerId;
    }

    // Generate the next Snowflake ID
    public synchronized long nextId() {
        long timestamp = currentTimestamp();

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = waitForNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L; // Reset sequence for a new millisecond
        }

        lastTimestamp = timestamp; // Update last timestamp
        return createId(timestamp);
    }

    private long createId(long timestamp) {
        return ((timestamp - TWEPOCH) << (WORKER_ID_BITS + SEQUENCE_BITS)) // Shift timestamp
                | (workerId << SEQUENCE_BITS) // Shift worker ID
                | sequence; // Add sequence number
    }

    private long currentTimestamp() {
        return Instant.now().toEpochMilli(); // Current timestamp in milliseconds
    }

    private long waitForNextMillis(long lastTimestamp) {
        long timestamp = currentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimestamp();
        }
        return timestamp;
    }

    private void validateWorkerId(long workerId) {
        if (workerId < 0 || workerId > maxWorkerId) {
            throw new IllegalArgumentException("Worker ID must be between 0 and " + maxWorkerId);
        }
    }
}
