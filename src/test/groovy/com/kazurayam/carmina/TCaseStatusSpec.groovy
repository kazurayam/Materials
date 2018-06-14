package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

class TCaseStatusSpec extends Specification {

    static Logger logger = LoggerFactory.getLogger(TCaseStatusSpec.class)

    def testValueOf() {
        expect:
        TCaseStatus.valueOf('FAILED') == TCaseStatus.FAILED
        TCaseStatus.valueOf('PASSED') == TCaseStatus.PASSED
        TCaseStatus.valueOf('TO_BE_EXECUTED') == TCaseStatus.TO_BE_EXECUTED

    }

    def testName() {
        expect:
        TCaseStatus.FAILED.name() == 'FAILED'
        TCaseStatus.PASSED.name() == 'PASSED'
        TCaseStatus.TO_BE_EXECUTED.name() == 'TO_BE_EXECUTED'
    }
}
