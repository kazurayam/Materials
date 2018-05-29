package com.kazurayam.ksbackyard.screenshotsupport

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

class Timestamp {

    private LocalDateTime timestamp

    static String DATE_TIME_PATTERN = 'yyyyMMdd_HHmmss'

    /**
     * create a Timestamp object based on the LocalDateTime of now
     */
    Timestamp() {
        this(LocalDateTime.now())
    }

    /**
     * instanciate a Timestamp object while ignoring milliseconds
     *
     * @param ts
     */
    Timestamp(LocalDateTime ts) {
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
        if (!(obj instanceof Timestamp))
            return false
        Timestamp other = (Timestamp)obj
        return this.getValue() == other.getValue()
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode()
    }
}
