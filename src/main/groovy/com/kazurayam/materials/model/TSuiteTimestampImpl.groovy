package com.kazurayam.materials.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAccessor

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.TSuiteTimestamp

class TSuiteTimestampImpl implements TSuiteTimestamp {

    static Logger logger_ = LoggerFactory.getLogger(TSuiteTimestampImpl.class);

    static final String TIMELESS_DIRNAME = '_'

    static final TSuiteTimestamp TIMELESS = TSuiteTimestampImpl.newInstance(LocalDateTime.MIN)

    private LocalDateTime timestamp_

    /**
     * private constructor by LocalDateTime of now
     */
    private TSuiteTimestampImpl() {
        this(LocalDateTime.now())
    }

    /**
     * private constructor by String in 'yyyyMMdd_HHmmss' format
     * 
     * @param timestamp
     */
    private TSuiteTimestampImpl(String timestamp) {
        Objects.requireNonNull(timestamp)
        LocalDateTime ldt = parse(timestamp)
        if (ldt != null) {
            timestamp_ = ignoreMilliseconds(ldt)
        } else {
            throw new IllegalArgumentException("unable to parse '${timestamp}' as TestSuiteTimestamp")
        }
    }

    /**
     * private constructor instanciating a Timestamp object while ignoring milliseconds
     *
     * @param ts
     */
    private TSuiteTimestampImpl(LocalDateTime ts) {
        Objects.requireNonNull(ts)
        timestamp_ = ignoreMilliseconds(ts)
    }


    /**
     * public static factory method with now
     * 
     * @return
     */
    static TSuiteTimestamp newInstance() {
        TSuiteTimestamp tst = new TSuiteTimestampImpl()
        return tst
    }

    /**
     * public static factory method with given string in 'yyyyMMdd_HHmmss' format
     * 
     * @param timestamp
     * @return
     */
    static TSuiteTimestamp newInstance(String timestamp) {
        Objects.requireNonNull(timestamp)
        TSuiteTimestamp tst = new TSuiteTimestampImpl(timestamp)
        return tst
    }

    /**
     * public static factory method with given LocalDateTim.
     * The millisecond value is ingnored.
     * 
     * @param ts
     * @return
     */
    static TSuiteTimestamp newInstance(LocalDateTime ts) {
        Objects.requireNonNull(ts)
        TSuiteTimestamp tst = new TSuiteTimestampImpl(ts)
        return tst
    }



    @Override
    LocalDateTime getValue() {
        return timestamp_
    }

    /**
     *
     * @return
     */
    @Override
    String format() {
        if (timestamp_ == LocalDateTime.MIN) {
            return TIMELESS_DIRNAME
        } else {
            return DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).format(timestamp_)
        }
    }

    @Override
    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TSuiteTimestamp":')
        sb.append('{"timestamp":"' + this.format()+ '"}' )
        sb.append('}')
        return sb.toString()
    }

    private LocalDateTime ignoreMilliseconds(LocalDateTime ts) {
        return LocalDateTime.of(ts.getYear(), ts.getMonth(), ts.getDayOfMonth(),
                ts.getHour(), ts.getMinute(), ts.getSecond())
    }

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

    @Override
    String toString() {
        return this.toJson()
    }


}
