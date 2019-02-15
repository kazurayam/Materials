package com.kazurayam.materials

import java.time.LocalDateTime

import com.kazurayam.materials.impl.TSuiteTimestampImpl

/**
 * Wraps a time stamp when a Test Suite was executed.
 * The time stamp value is formatted in 'yyyyMMdd_HHmmss'
 *
 * @author kazurayam
 *
 */
abstract class TSuiteTimestamp implements Comparable<TSuiteTimestamp> {
    /**
     * Builder method with String argument
     * @param timestamp
     * @return
     */
    static TSuiteTimestamp newInstance(String timestamp) {
        Objects.requireNonNull(timestamp, "timestamp must not be null")
        return TSuiteTimestampImpl.newInstance(timestamp)
    }
    
    /**
     * Builder method with LocalDateTime argument
     * @param localDateTime
     * @return
     */
    static TSuiteTimestamp newInstance(LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime, "localDateTime must not be null")
        return TSuiteTimestampImpl.newInstance(localDateTime)
    }
    
    /**
     * NULL Object
     */
    static final TSuiteTimestamp NULL = new TSuiteTimestampImpl()
    
    
    /**
     * The format of standard String representation of TSuiteTimestamp object
     */
    static final String DATE_TIME_PATTERN = 'yyyyMMdd_HHmmss'
    
    
    // ------------------ interface ---------------------------------
    
    /**
     * 
     * @return LocalDateTime object which represents 'yyyyMMdd_HHmmss'
     */
    abstract LocalDateTime getValue()

    /**
     *
     * @return String in the 'yyyyMMdd_HHmmss' format
     */
    abstract String format()

    abstract String toJson()

}
