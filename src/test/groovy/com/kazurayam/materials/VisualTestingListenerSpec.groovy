package com.kazurayam.materials

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.impl.VisualTestingListenerDefaultImpl

import spock.lang.Specification

class VisualTestingListenerSpec extends Specification {
    
    // fields
    static Logger logger_ = LoggerFactory.getLogger(VisualTestingListenerSpec.class)
    
    // fixture methods
    def setupSpec() {}
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    def testDefaultImpl() {
        setup:
            VisualTestingListener listener = new VisualTestingListenerDefaultImpl()
        when:
            listener.info("This is a INFO message")
            listener.failed("This is a FAILED message")
            listener.fatal("This is a FATAL message")
        then:
            true
    }

}
