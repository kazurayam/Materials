package com.kazurayam.carmina

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAccessor

/**
 * Timestamp of a Test Suite
 *
 * @author kazurayam
 *
 */
class TSuiteTimestamp {

    static final String TIMELESS_DIRNAME = '_'

    static final TSuiteTimestamp TIMELESS = new TSuiteTimestamp(LocalDateTime.MIN)

    static final String DATE_TIME_PATTERN = 'yyyyMMdd_HHmmss'

    private LocalDateTime timestamp

    /**
     * create a Timestamp object based on the LocalDateTime of now
     */
    TSuiteTimestamp() {
        this(LocalDateTime.now())
    }

    TSuiteTimestamp(String timestamp) {
        LocalDateTime ldt = parse(timestamp)
        if (ldt != null) {
            this.timestamp = ignoreMilliseconds(ldt)
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
        this.timestamp = ignoreMilliseconds(ts)
    }

    private LocalDateTime ignoreMilliseconds(LocalDateTime ts) {
        return LocalDateTime.of(ts.getYear(), ts.getMonth(), ts.getDayOfMonth(),
                ts.getHour(), ts.getMinute(), ts.getSecond())
    }

    LocalDateTime getValue() {
        return this.timestamp
    }

    /**
     *
     * @return
     */
    String format() {
        if (timestamp == LocalDateTime.MIN) {
            return TIMELESS_DIRNAME
        } else {
            return DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).format(timestamp)
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
            System.err.println("unable to parse '${str}' as LocalDateTime")
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
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TsTimestamp":')
        sb.append('{"timestamp":"' + this.format()+ '"}' )
        sb.append('}')
        return sb.toString()
    }

}
