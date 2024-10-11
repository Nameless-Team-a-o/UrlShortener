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
    private final long twepoch = 1288834974657L; // Custom epoch
    private final long sequenceBits = 12L; // 12 bits for sequence number
    private final long workerIdBits = 10L; // 10 bits for worker ID

    // Maximum values for the IDs
    private final long maxWorkerId = ~(-1L << workerIdBits); // Max worker ID (1023)
    private final long sequenceMask = ~(-1L << sequenceBits); // Max sequence number (4095)

    // Constructor to set the worker ID
    public SnowflakeIdGenerator(@Value("${snowflake.workerId}") long workerId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException("Worker ID can't be greater than " + maxWorkerId + " or less than 0");
        }
        this.workerId = workerId;
    }

    // Generate the next Snowflake ID
    public synchronized long nextId() {
        long timestamp = timestamp();

        // If the current timestamp is the same as the last timestamp, increment the sequence
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask; // Wrap the sequence
            if (sequence == 0) {
                // Wait for the next millisecond if the sequence is exhausted
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L; // Reset sequence for a new millisecond
        }

        lastTimestamp = timestamp; // Update the last timestamp

        // Shift and combine all parts to create the ID
        return ((timestamp - twepoch) << (workerIdBits + sequenceBits)) // Shift timestamp
                | (workerId << sequenceBits) // Shift worker ID
                | sequence; // Add sequence number
    }

    // Get the current timestamp in milliseconds
    private long timestamp() {
        return Instant.now().toEpochMilli(); // Get current timestamp in milliseconds
    }

    // Wait for the next millisecond
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = timestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = timestamp();
        }
        return timestamp;
    }
}
