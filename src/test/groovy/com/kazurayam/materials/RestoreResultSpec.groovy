package com.kazurayam.materials

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.impl.RestoreResultImpl
import com.kazurayam.materials.impl.TSuiteResultImpl

import spock.lang.Specification

class RestoreResultSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(RestoreResultSpec)

    def setupSpec() {}
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    def test_smoke() {
        setup:
        TSuiteName tsn = new TSuiteName('Test Suites/TS1')
        TExecutionProfile tep = new TExecutionProfile("default")
        TSuiteTimestamp tst = new TSuiteTimestamp('20190621_150102')
        TSuiteResult tSuiteResult = new TSuiteResultImpl(tsn, tep, tst)
        Integer count = 10
        when:
        RestoreResult restoreResult = new RestoreResultImpl(tSuiteResult, count)
        then:
        restoreResult.getTSuiteResult().getTSuiteName() == tsn
        restoreResult.getTSuiteResult().getTSuiteTimestamp() == tst
        restoreResult.getCount() == count
    }
}
