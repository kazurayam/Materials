package com.kazurayam.materials

import com.kazurayam.materials.impl.TSuiteResultIdImpl

abstract class TSuiteResultId {

    static TSuiteResultId newInstance(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        return new TSuiteResultIdImpl(tSuiteName, tSuiteTimestamp)
    }
    
    abstract TSuiteName getTSuiteName()

    abstract TSuiteTimestamp getTSuiteTimestamp()


}