package com.kazurayam.materials

import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

class TSuiteTimestampSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(TSuiteTimestampSpec.class)
    
    // fixture methods
    def setupSpec() {}
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    def testNewInstance_String() {
        when:
        def str = '20190130_131024'
        TSuiteTimestamp tst = new TSuiteTimestamp(str)
        then:
        tst != null
        tst.format().equals(str)
    }
    
    def testNewInstance_LocalDateTime() {
        when:
        def now = LocalDateTime.now()
        TSuiteTimestamp tst = new TSuiteTimestamp(now)
        then:
        tst != null
        tst.getValue().equals(now.withNano(0))
    }
    
    def testParse() {
        setup:
        String fixture = '20180529_143459'
        LocalDateTime expected = LocalDateTime.of(2018, 5, 29, 14, 34, 59)
        when:
        LocalDateTime actual = TSuiteTimestamp.parse(fixture)
        then:
        actual == expected
    }

    def testEquals() {
        setup:
        LocalDateTime source = LocalDateTime.of(2018, 5, 29, 11, 22, 33, 44)
        LocalDateTime expected1 = LocalDateTime.of(2018, 5, 29, 11, 22, 33)
        LocalDateTime expected2 = LocalDateTime.of(2018, 5, 29, 11, 22, 33, 00)
        when:
        TSuiteTimestamp ts = new TSuiteTimestamp(source)
        then:
        ts.getValue() == expected1
        ts.getValue() == expected2
        //cleanup:
    }

    def testToString() {
        setup:
        LocalDateTime source = LocalDateTime.of(2018, 6, 5, 9, 2, 13)
        when:
        TSuiteTimestamp ts = new TSuiteTimestamp(source)
        def str = ts.toString()
        logger_.debug("#testToJson str=${str}")
        then:
        str.contains('{"TSuiteTimestamp":')
        str.contains('{"timestamp":')
        str.contains('20180605_090213')
        str.contains('}}')
        when:
        logger_.debug("#testToJson str=${JsonOutput.prettyPrint(str)}")
        then:
        true
    }

    def testFormatOfTimeless() {
        expect:
        TSuiteTimestamp.TIMELESS.format() == TSuiteTimestamp.TIMELESS_DIRNAME
    }

    def testParseDirnameOfTimeless() {
        expect:
        TSuiteTimestamp.parse(TSuiteTimestamp.TIMELESS_DIRNAME) == LocalDateTime.MIN
    }

}
