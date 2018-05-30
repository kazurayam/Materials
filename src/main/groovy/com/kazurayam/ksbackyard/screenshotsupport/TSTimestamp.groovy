package com.kazurayam.ksbackyard.screenshotsupport

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

/**
 * Timestamp of a Test Suite
 *
 * @author kazurayam
 *
 */
class TSTimestamp {

    private LocalDateTime timestamp

    static String DATE_TIME_PATTERN = 'yyyyMMdd_HHmmss'

    /**
     * create a Timestamp object based on the LocalDateTime of now
     */
    TSTimestamp() {
        this(LocalDateTime.now())
    }

    /**
     * instanciate a Timestamp object while ignoring milliseconds
     *
     * @param ts
     */
    TSTimestamp(LocalDateTime ts) {
        this.timestamp = LocalDateTime.of(ts.getYear(), ts.getMonth(), ts.getDayOfMonth(),
            ts.getHour(), ts.getMinute(), ts.getSecond())  // ignore milliseconds
    }

    LocalDateTime getValue() {
        return this.timestamp
    }

    String toString() {
        return DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).format(timestamp)
    }

    static LocalDateTime parse(String str) {
        TemporalAccessor parsed = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).parse(str)
        return LocalDateTime.from(parsed)
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true
        if (!(obj instanceof TSTimestamp))
            return false
        TSTimestamp other = (TSTimestamp)obj
        return this.getValue() == other.getValue()
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode()
    }
}
