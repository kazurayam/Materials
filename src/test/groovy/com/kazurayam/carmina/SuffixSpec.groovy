package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Ignore
import spock.lang.Specification

//@Ignore
class SuffixSpec extends Specification {
    
    static Logger logger_ = LoggerFactory.getLogger(SuffixSpec.class)

    // fields
    
    // fixture methods
    def setupSpec() {}
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testUsualCase() {
        when:
        Suffix suffix = new Suffix("abc")
        then:
        suffix.getValue() == 'abc'
    }
    
    def testCaseWithSection() {
        when:
        Suffix suffix = new Suffix("§A")
        then:
        suffix.getValue() == 'A'
    }
    
    def testCaseWithSlash() {
        when:
        Suffix suffix = new Suffix("a/b")
        then:
        suffix.getValue() == 'ab'
    }
    
    def testCaseWithBackSlash() {
        when:
        Suffix suffix = new Suffix("x\\y")
        then:
        suffix.getValue() == 'xy'
    }
    
    def testCaseWithNihongo() {
        when:
        Suffix suffix = new Suffix("あ")
        then:
        suffix.getValue() == 'あ'
    }

    
    @Ignore
    def testIgnoring() {}

    // helper methods
    def void anything() {}
}
