package com.kazurayam.material

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

    static Logger logger_ = LoggerFactory.getLogger(RepositoryScanner.class);

    static final String TIMELESS_DIRNAME = '_'

    static final TSuiteTimestamp TIMELESS = new TSuiteTimestamp(LocalDateTime.MIN)

    static final String DATE_TIME_PATTERN = 'yyyyMMdd_HHmmss'

    private LocalDateTime timestamp_

    /**
     * create a Timestamp object based on the LocalDateTime of now
     */
    TSuiteTimestamp() {
        this(LocalDateTime.now())
    }

    TSuiteTimestamp(String timestamp) {
        LocalDateTime ldt = parse(timestamp)
        if (ldt != null) {
            timestamp_ = ignoreMilliseconds(ldt)
        } else {
            throw new IllegalArgumentException("unable to parse '${timestamp}' as TestSuiteTimestamp")
        }
    }

    /**
     * instanciate a Timestamp object while ignoring milliseconds
     *
     * @param ts
     */
    TSuiteTimestamp(LocalDateTime ts) {
        timestamp_ = ignoreMilliseconds(ts)
    }

    private LocalDateTime ignoreMilliseconds(LocalDateTime ts) {
        return LocalDateTime.of(ts.getYear(), ts.getMonth(), ts.getDayOfMonth(),
                ts.getHour(), ts.getMinute(), ts.getSecond())
    }

    LocalDateTime getValue() {
        return timestamp_
    }

    /**
     *
     * @return
     */
    String format() {
        if (timestamp_ == LocalDateTime.MIN) {
            return TIMELESS_DIRNAME
        } else {
            return DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).format(timestamp_)
        }
    }

    /**
     *
     * @param str
     * @return
     */
    static LocalDateTime parse(String str) {
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



    // ---------------- overriding Object properties --------------------------
    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof TSuiteTimestamp))
            return false
        TSuiteTimestamp other = (TSuiteTimestamp)obj
        return this.getValue() == other.getValue()
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode()
    }

    @Override
    int compareTo(TSuiteTimestamp other) {
        return this.getValue().compareTo(other.getValue())
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TSuiteTimestamp":')
        sb.append('{"timestamp":"' + this.format()+ '"}' )
        sb.append('}')
        return sb.toString()
    }

}
