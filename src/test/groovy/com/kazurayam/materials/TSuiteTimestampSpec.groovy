package com.kazurayam.materials

import java.time.LocalDateTime
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.TSuiteName

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
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance(str)
        then:
        tst != null
        tst.format().equals(str)
    }
    
    def testNewInstance_LocalDateTime() {
        when:
        def now = LocalDateTime.now()
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance(now)
        then:
        tst != null
        tst.getValue().equals(now.withNano(0))
    }
}
