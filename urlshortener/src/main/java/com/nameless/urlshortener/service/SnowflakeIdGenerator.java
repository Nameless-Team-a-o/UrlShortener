package com.nameless.urlshortener.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * SnowflakeIdGenerator is a class that implements the Snowflake algorithm for generating unique IDs.
 * This algorithm is well-suited for distributed systems, providing unique IDs that are time-ordered.
 * Each generated ID consists of three main parts:
 *   - 41 bits represent the timestamp (in milliseconds) from a custom epoch.
 *   - 10 bits represent the worker ID (identifying the machine or process generating the ID).
 *   - 12 bits represent a sequence number (used to differentiate IDs generated in the same millisecond).
 *
 * The ID is composed such that it is always unique and sortable based on the time it was generated.
 */
@Service
public class SnowflakeIdGenerator {

    // The workerId identifies the machine generating the IDs.
    // It's a 10-bit value, which means it supports 1024 unique workers (2^10 = 1024).
    private final long workerId;

    // The sequence number is a 12-bit value, allowing up to 4096 IDs per millisecond per worker.
    // The sequence is reset every millisecond.
    private long sequence = 0L;

    // The timestamp of the last generated ID, to check if we're still in the same millisecond.
    private long lastTimestamp = -1L;

    // Constants related to the Snowflake algorithm

    // The custom epoch: The base timestamp (in milliseconds) from which the ID's timestamp is derived.
    // This is the equivalent of January 1, 2024, in millisecond.
    private static final long SERVER_START_EPOCH = 1704067200000L; // s

    // Number of bits allocated for the sequence.
    // This is a 12-bit value, meaning we can generate 4096 unique IDs in a single millisecond (2^12 = 4096).
    private static final long SEQUENCE_BITS = 12L;

    // Number of bits allocated for the worker ID.
    // This is a 10-bit value, allowing for 1024 unique workers (2^10 = 1024).
    private static final long WORKER_ID_BITS = 10L;

    // The maximum value for the sequence within a single millisecond.
    // This is calculated as 2^12 - 1, which equals 4095.
    private static final long SEQUENCE_MASK = (1 << SEQUENCE_BITS) - 1; // 4095 = 2^12 - 1

    // The maximum value for the worker ID, calculated as 2^10 - 1 = 1023.
    private static final long MAX_WORKER_ID = (1 << WORKER_ID_BITS) - 1; // 1023 = 2^10 - 1

    // The worker ID needs to be shifted to the left by the number of sequence bits (12),
    // so the worker ID can occupy the correct part of the final 64-bit ID.
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    // The timestamp needs to be shifted left by the sum of the worker ID bits and the sequence bits (12 + 10 = 22),
    // so it occupies the top 41 bits of the final ID.
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * Constructor to initialize the workerId.
     * The workerId identifies the machine or instance generating the ID, and it must be in the range [0, 1023].
     *
     * @param workerId The worker ID for this machine or process. Must be between 0 and 1023.
     */
    public SnowflakeIdGenerator(@Value("${snowflake.workerId}") long workerId) {
        validateWorkerId(workerId);
        this.workerId = workerId; // Set the workerId for this instance.
    }

    /**
     * Generates the next unique ID using the Snowflake algorithm.
     * The method is synchronized to ensure that no two threads can generate the same ID at the same time.
     *
     * @return A unique 64-bit ID.
     */
    public synchronized long nextId() {
        long timestamp = currentTimestamp(); // Get the current timestamp in milliseconds.

        // If the current timestamp is the same as the last timestamp (same millisecond),
        // increment the sequence number.
        if (timestamp == lastTimestamp) {
            // Increment the sequence and use bitwise AND to ensure it stays within the 12-bit range (0-4095).
            sequence = (sequence + 1) & SEQUENCE_MASK;

            // If the sequence overflows (more than 4096 IDs generated in one millisecond), wait for the next millisecond.
            if (sequence == 0) {
                timestamp = waitForNextMillis(lastTimestamp);
            }
        } else {
            // If we're in a new millisecond, reset the sequence number.
            sequence = 0L;
        }

        // Update the last timestamp with the current one.
        lastTimestamp = timestamp;

        // Generate the final 64-bit ID by combining the timestamp, workerId, and sequence number.
        return createId(timestamp);
    }

    /**
     * Combines the timestamp, worker ID, and sequence number into a 64-bit Snowflake ID.
     *
     * @param timestamp The current timestamp in milliseconds.
     * @return A unique 64-bit Snowflake ID.
     */
    private long createId(long timestamp) {
        // Subtract the custom epoch (SERVER_START_EPOCH) from the current timestamp, and shift the result left by 22 bits.
        long timePart = (timestamp - SERVER_START_EPOCH) << TIMESTAMP_SHIFT;

        // Shift the worker ID to the left by 12 bits to make room for the sequence.
        long workerPart = workerId << WORKER_ID_SHIFT;

        // Combine the time part, worker ID part, and sequence number into a single 64-bit value using bitwise OR.
        return timePart | workerPart | sequence;
    }

    /**
     * Returns the current timestamp in milliseconds.
     *
     * @return The current time in milliseconds since the Unix epoch.
     */
    private long currentTimestamp() {
        return Instant.now().toEpochMilli(); // Get the current timestamp.
    }

    /**
     * Busy-waits until the next millisecond if the current timestamp is the same as the last one.
     * This ensures that sequence numbers don't overflow within a single millisecond.
     *
     * @param lastTimestamp The timestamp of the last generated ID.
     * @return The next millisecond's timestamp.
     */
    private long waitForNextMillis(long lastTimestamp) {
        long timestamp = currentTimestamp();
        // Loop until the current timestamp is greater than the last timestamp.
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimestamp();
        }
        return timestamp;
    }

    /**
     * Validates that the workerId is within the allowed range (0 to 1023).
     *
     * @param workerId The worker ID to validate.
     * @throws IllegalArgumentException If the workerId is out of range.
     */
    private void validateWorkerId(long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("Worker ID must be between 0 and " + MAX_WORKER_ID);
        }
    }

}
