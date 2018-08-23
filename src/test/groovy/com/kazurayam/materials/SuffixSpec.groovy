package com.kazurayam.materials

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Suffix

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
        Suffix suffix = new Suffix(1)
        then:
        suffix.getValue() == 1
        suffix.toString() == '(1)'
    }

    def testNullObject() {
        when:
        Suffix suffix = Suffix.NULL
        then:
        suffix.getValue() == 0
        suffix.toString() == ''
    }

    def testIllegalArgument_minusInt() {
        when:
        Suffix suffix = new Suffix(-1)

        then:
        IllegalArgumentException e = thrown()
        e.cause ==null
    }

    def testIllegalArugment_StringArgument() {
        when:
        Suffix suffix = new Suffix('abc')

        then:
        groovy.lang.GroovyRuntimeException e = thrown()
        e.cause ==null
    }

}
