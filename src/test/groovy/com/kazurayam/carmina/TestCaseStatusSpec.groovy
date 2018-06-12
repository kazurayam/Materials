package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

class TestCaseStatusSpec extends Specification {

    static Logger logger = LoggerFactory.getLogger(TestCaseStatusSpec.class)

    def testValueOf() {
        expect:
        TestCaseStatus.valueOf('FAILED') == TestCaseStatus.FAILED
        TestCaseStatus.valueOf('PASSED') == TestCaseStatus.PASSED
        TestCaseStatus.valueOf('TO_BE_EXECUTED') == TestCaseStatus.TO_BE_EXECUTED

    }

    def testName() {
        expect:
        TestCaseStatus.FAILED.name() == 'FAILED'
        TestCaseStatus.PASSED.name() == 'PASSED'
        TestCaseStatus.TO_BE_EXECUTED.name() == 'TO_BE_EXECUTED'
    }
}
