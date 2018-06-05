package com.kazurayam.kstestresults

import java.time.LocalDateTime

import groovy.json.JsonOutput
import spock.lang.Specification

/**
 * TestSuite Timestamp
 *
 * @author kazurayam
 *
 */
//@Ignore
class TestSuiteTimestampSpec extends Specification {

    // fields

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testParse() {
        setup:
        String fixture = '20180529_143459'
        LocalDateTime expected = LocalDateTime.of(2018, 5, 29, 14, 34, 59)
        when:
        LocalDateTime actual = TestSuiteTimestamp.parse(fixture)
        then:
        actual == expected
    }

    def testEquals() {
        setup:
        LocalDateTime source = LocalDateTime.of(2018, 5, 29, 11, 22, 33, 44)
        LocalDateTime expected1 = LocalDateTime.of(2018, 5, 29, 11, 22, 33)
        LocalDateTime expected2 = LocalDateTime.of(2018, 5, 29, 11, 22, 33, 00)
        when:
        TestSuiteTimestamp ts = new TestSuiteTimestamp(source)
        then:
        ts.getValue() == expected1
        ts.getValue() == expected2
        //cleanup:
    }

    def testToJson() {
        setup:
        LocalDateTime source = LocalDateTime.of(2018, 6, 5, 9, 2, 13)
        when:
        TestSuiteTimestamp ts = new TestSuiteTimestamp(source)
        def str = ts.toString()
        System.out.println("${JsonOutput.prettyPrint(str)}")
        then:
        str.contains('{"TestSuiteTimestamp":')
        str.contains('{"timestamp":')
        str.contains('20180605_090213')
        str.contains('}}')
    }

    // helper methods
}
