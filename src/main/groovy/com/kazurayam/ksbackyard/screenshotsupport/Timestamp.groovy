package com.kazurayam.ksbackyard.screenshotsupport

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

class Timestamp {

    // yyyyhhMMdd_hhmmssを記録するLocalDateTime。ミリ秒以下はゼロにしてある。
    private LocalDateTime timestamp

    static String DATE_TIME_PATTERN = 'yyyyMMdd_HHmmss'

    Timestamp() {
        this(LocalDateTime.now())
    }

    /**
     * tsのミリ秒をゼロにしてから記憶する。
     *
     * @param ts
     */
    Timestamp(LocalDateTime ts) {
        this.timestamp = LocalDateTime.of(ts.getYear(), ts.getMonth(), ts.getDayOfMonth(),
            ts.getHour(), ts.getMinute(), ts.getSecond())
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
