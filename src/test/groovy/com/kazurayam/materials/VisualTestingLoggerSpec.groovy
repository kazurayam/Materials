package com.kazurayam.materials

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl

import spock.lang.Specification

class VisualTestingLoggerSpec extends Specification {
    
    // fields
    static Logger logger_ = LoggerFactory.getLogger(VisualTestingLoggerSpec.class)
    
    // fixture methods
    def setupSpec() {}
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    def testDefaultImpl() {
        setup:
            VisualTestingLogger vtLogger = new VisualTestingLoggerDefaultImpl()
        when:
            vtLogger.info("This is a INFO message")
            vtLogger.failed("This is a FAILED message")
            vtLogger.fatal("This is a FATAL message")
        then:
            true
    }

}
