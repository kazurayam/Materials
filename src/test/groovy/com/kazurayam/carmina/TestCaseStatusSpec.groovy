package com.kazurayam.carmina

import spock.lang.Specification

class TestCaseStatusSpec extends Specification {

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
