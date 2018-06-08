package com.kazurayam.carmina

import spock.lang.Specification

class TCaseStatusSpec extends Specification {

    def testValueOf() {
        expect:
        TCaseStatus.valueOf('FAILED') == TCaseStatus.FAILED
        TCaseStatus.valueOf('PASSED') == TCaseStatus.PASSED
        TCaseStatus.valueOf('TO_BE_EXECUTED') == TCaseStatus.TO_BE_EXECUTED

    }
}
