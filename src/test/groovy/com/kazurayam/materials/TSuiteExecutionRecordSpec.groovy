package com.kazurayam.materials

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.model.TSuiteExecutionRecordImpl
import com.kazurayam.materials.model.TSuiteTimestampImpl

import spock.lang.Specification

class TSuiteExecutionRecordSpec extends Specification {
    
    static Logger logger_ = LoggerFactory.getLogger(TSuiteExecutionRecordSpec.class)

    private TSuiteName tSuiteName_
    private TSuiteTimestamp tSuiteTimestamp_
    
    def setup() {
        tSuiteName_ = new TSuiteName('Test Suites/TS1')
        tSuiteTimestamp_ = TSuiteTimestampImpl.newInstance('20181211_152348')
    }
    
    def testGetTSuiteName() {
        setup:
        TSuiteExecutionRecord tser = TSuiteExecutionRecordImpl.newInstance(tSuiteName_, tSuiteTimestamp_)
        when:
        TSuiteName tsn = tser.getTSuiteName()
        then:
        tsn.equals(tSuiteName_)
    }
    
    def testGetTSuiteTimestamp() {
        setup:
        TSuiteExecutionRecord tser = TSuiteExecutionRecordImpl.newInstance(tSuiteName_, tSuiteTimestamp_)
        when:
        TSuiteTimestamp tsn = tser.getTSuiteTimestamp()
        then:
        tsn.equals(tSuiteTimestamp_)
    }
    
    def testEquals() {
        setup:
        TSuiteExecutionRecord tser1 = TSuiteExecutionRecordImpl.newInstance(tSuiteName_, tSuiteTimestamp_)
        TSuiteExecutionRecord tser2 = TSuiteExecutionRecordImpl.newInstance(tSuiteName_, tSuiteTimestamp_)
        expect:
        tser1.equals(tser2)
    }
    
    def testToString() {
        setup:
        TSuiteExecutionRecord tser = TSuiteExecutionRecordImpl.newInstance(tSuiteName_, tSuiteTimestamp_)
        when:
        String s = tser.toString()
        System.out.println(TSuiteExecutionRecordSpec.class.getName() + "#testToString s=${s}")
        then:
        s.contains('tSuiteName')
        s.contains('TS1')
        s.contains('tSuiteTimestamp')
        s.contains('20181211_152348')
    }

}
