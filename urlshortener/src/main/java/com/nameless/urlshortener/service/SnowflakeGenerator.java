package com.nameless.urlshortener.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * SnowflakeGenerator is a class that implements the Snowflake algorithm for generating unique IDs.
 * This algorithm is well-suited for distributed systems, providing unique IDs that are time-ordered.
 * Each generated ID consists of three main parts:
 *   - 41 bits represent the timestamp (in milliseconds) from a custom epoch.
 *   - 10 bits represent the machine ID (identifying the machine or process generating the ID).
 *   - 12 bits represent a sequence number (used to differentiate IDs generated in the same millisecond).
 *
 * The ID is composed such that it is always unique and sortable based on the time it was generated.
 */
@Service
public class SnowflakeGenerator {

    private final long machineId; // Identifies the machine generating the IDs.
    private long sequenceNumber = 0L; // Allows up to 4096 IDs per millisecond per machine.
    private long lastGeneratedTimestamp = -1L; // Timestamp of the last generated ID.

    private static final long CUSTOM_EPOCH = 1704067200000L; // Equivalent of January 1, 2024.
    private static final long SEQUENCE_BITS = 12L; // 12 bits for the sequence.
    private static final long MACHINE_ID_BITS = 10L; // 10 bits for the machine ID.
    private static final long MAX_SEQUENCE_VALUE = (1 << SEQUENCE_BITS) - 1; // 4095.
    private static final long MAX_MACHINE_ID = (1 << MACHINE_ID_BITS) - 1; // 1023.
    private static final long INSTANCE_ID_SHIFT = SEQUENCE_BITS; // Shift for machine ID.
    private static final long TIMESTAMP_BITS_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS; // Shift for timestamp.

    public SnowflakeGenerator(@Value("${snowflake.machineId}") long machineId) {
        validateMachineId(machineId);
        this.machineId = machineId;
    }

    /**
     * Generates the next unique ID using the Snowflake algorithm.
     * The method is synchronized to ensure thread safety in a multi-threaded environment.
     *
     * @return A unique ID as a long value.
     */
    public synchronized long generateNextId() {
        long currentTimestamp = getCurrentTimestamp();

        if (currentTimestamp == lastGeneratedTimestamp) {
            sequenceNumber = (sequenceNumber + 1) & MAX_SEQUENCE_VALUE;

            if (sequenceNumber == 0) {
                currentTimestamp = waitUntilNextMillisecond(lastGeneratedTimestamp);
            }
        } else {
            sequenceNumber = 0L; // Reset the sequence for the new millisecond.
        }

        lastGeneratedTimestamp = currentTimestamp;

        return buildId(currentTimestamp);
    }

    /**
     * Builds the unique ID by combining the timestamp, machine ID, and sequence number.
     *
     * @param currentTimestamp The current timestamp.
     * @return A unique ID as a long value.
     */
    private long buildId(long currentTimestamp) {
        long timePart = (currentTimestamp - CUSTOM_EPOCH) << TIMESTAMP_BITS_SHIFT;
        long machinePart = machineId << INSTANCE_ID_SHIFT;
        return timePart | machinePart | sequenceNumber; // Combine parts into a single 64-bit value.
    }

    /**
     * Retrieves the current timestamp in milliseconds.
     *
     * @return The current timestamp as a long value.
     */
    private long getCurrentTimestamp() {
        return Instant.now().toEpochMilli();
    }

    /**
     * Waits until the next millisecond if IDs are being generated at the same timestamp.
     *
     * @param lastTimestamp The last generated timestamp.
     * @return The next valid timestamp as a long value.
     */
    private long waitUntilNextMillisecond(long lastTimestamp) {
        long currentTimestamp = getCurrentTimestamp();
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = getCurrentTimestamp();
        }
        return currentTimestamp;
    }

    /**
     * Validates the provided machine ID to ensure it is within the allowed range.
     *
     * @param machineId The machine ID to validate.
     */
    private void validateMachineId(long machineId) {
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("Machine ID must be between 0 and " + MAX_MACHINE_ID);
        }
    }
}
