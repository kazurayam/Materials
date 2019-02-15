package com.kazurayam.materials.impl

import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.impl.TSuiteTimestampImpl

import groovy.json.JsonOutput
import spock.lang.Specification

/**
 * TestSuite Timestamp
 *
 * @author kazurayam
 *
 */
//@Ignore
class TSuiteTimestampImplSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(TSuiteTimestampImplSpec);

    // fixture methods
    def setupSpec() {}
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testParse() {
        setup:
        String fixture = '20180529_143459'
        LocalDateTime expected = LocalDateTime.of(2018, 5, 29, 14, 34, 59)
        when:
        LocalDateTime actual = TSuiteTimestampImpl.parse(fixture)
        then:
        actual == expected
    }

    def testEquals() {
        setup:
        LocalDateTime source = LocalDateTime.of(2018, 5, 29, 11, 22, 33, 44)
        LocalDateTime expected1 = LocalDateTime.of(2018, 5, 29, 11, 22, 33)
        LocalDateTime expected2 = LocalDateTime.of(2018, 5, 29, 11, 22, 33, 00)
        when:
        TSuiteTimestamp ts = TSuiteTimestampImpl.newInstance(source)
        then:
        ts.getValue() == expected1
        ts.getValue() == expected2
        //cleanup:
    }

    def testToJson() {
        setup:
        LocalDateTime source = LocalDateTime.of(2018, 6, 5, 9, 2, 13)
        when:
        TSuiteTimestamp ts = TSuiteTimestampImpl.newInstance(source)
        def str = ts.toString()
        logger_.debug("#testToJson ${JsonOutput.prettyPrint(str)}")
        then:
        str.contains('{"TSuiteTimestamp":')
        str.contains('{"timestamp":')
        str.contains('20180605_090213')
        str.contains('}}')
    }

    def testFormatOfTimeless() {
        expect:
        TSuiteTimestampImpl.TIMELESS.format() == TSuiteTimestampImpl.TIMELESS_DIRNAME
    }

    def testParseDirnameOfTimeless() {
        expect:
        TSuiteTimestampImpl.parse(TSuiteTimestampImpl.TIMELESS_DIRNAME) == LocalDateTime.MIN
    }

    // helper methods
}
