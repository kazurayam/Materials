package com.kazurayam.materials

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAccessor

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Wraps a time stamp when a Test Suite was executed.
 * The time stamp value is formatted in 'yyyyMMdd_HHmmss'
 *
 * @author kazurayam
 *
 */
class TSuiteTimestamp implements Comparable<TSuiteTimestamp> {
    
    static Logger logger_ = LoggerFactory.getLogger(TSuiteTimestamp.class);
    
    static final String TIMELESS_DIRNAME = '_'
    
    static final TSuiteTimestamp TIMELESS = new TSuiteTimestamp(LocalDateTime.MIN)
    
    /**
     * NULL Object
     * '19990101_000000'
     */
    static final TSuiteTimestamp NULL = new TSuiteTimestamp('19990101_000000')
    
    /**
     * The format of standard String representation of TSuiteTimestamp object
     */
    static final String DATE_TIME_PATTERN = 'yyyyMMdd_HHmmss'
    
    private LocalDateTime timestamp_
    
    /**
     * 
     */
    TSuiteTimestamp() {
        this(LocalDateTime.now())
    }
    
    /**
     * constructor by String in 'yyyyMMdd_HHmmss' format
     */
    TSuiteTimestamp(String timestamp) {
        Objects.requireNonNull(timestamp)
        LocalDateTime ldt = parse(timestamp)
        if (ldt != null) {
            timestamp_ = ignoreMilliseconds(ldt)
        } else {
            try {
                throw new IllegalArgumentException("unable to parse '${timestamp}' as TestSuiteTimestamp")
            } catch (Exception e) {
                e.printStackTrace()
                throw e
            }
        }
    }
    
    /**
     * 
     * @param ts
     */
    TSuiteTimestamp(LocalDateTime ts) {
        Objects.requireNonNull(ts)
        timestamp_ = ignoreMilliseconds(ts)
    }

    //------------------ class methods ---------------------------
    /**
     *
     * @param str
     * @return
     */
    static LocalDateTime parse(String str) {
        Objects.requireNonNull(str)
        try {
            if (str == TIMELESS_DIRNAME) {
                return LocalDateTime.MIN
            }else {
                TemporalAccessor parsed = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).parse(str)
                return LocalDateTime.from(parsed)
            }
        } catch (DateTimeParseException ex) {
            logger_.info("unable to parse '${str}' as LocalDateTime")
            return null
        }
    }

    // ------------- instance methods ---------------------------------
    /**
     * 
     * @return LocalDateTime object which represents 'yyyyMMdd_HHmmss'
     */
    LocalDateTime getValue() {
        return timestamp_
    }

    private LocalDateTime ignoreMilliseconds(LocalDateTime ts) {
        return LocalDateTime.of(ts.getYear(), ts.getMonth(), ts.getDayOfMonth(),
                ts.getHour(), ts.getMinute(), ts.getSecond())
    }

    /**
     *
     * @return String in the 'yyyyMMdd_HHmmss' format
     */
    String format() {
        if (timestamp_ == LocalDateTime.MIN) {
            return TIMELESS_DIRNAME
        } else {
            return DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).format(timestamp_)
        }
    }

    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TSuiteTimestamp":')
        sb.append('{"timestamp":"' + this.format()+ '"}' )
        sb.append('}')
        return sb.toString()
    }
    
    String toString() {
        return this.toJsonText()
    }

    // ---------------- overriding Object properties --------------------------
    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof TSuiteTimestamp))
            return false
        TSuiteTimestamp other = (TSuiteTimestamp)obj
        return this.getValue().equals(other.getValue())
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode()
    }

    @Override
    int compareTo(TSuiteTimestamp other) {
        return this.getValue().compareTo(other.getValue())
    }

}
