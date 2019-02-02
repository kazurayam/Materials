package com.kazurayam.materials.impl

import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp

public class TSuiteResultIdImpl implements TSuiteResultId {

    private TSuiteName tSuiteName_
    private TSuiteTimestamp tSuiteTimestamp_
    
    private TSuiteResultIdImpl(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        this.tSuiteName_ = tSuiteName
        this.tSuiteTimestamp_ = tSuiteTimestamp
    }
    
    TSuiteResultIdImpl newInstance(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        return new TSuiteResultIdImpl(tSuiteName, tSuiteTimestamp)
    }
    
    void setTSuiteName(TSuiteName tSuiteName) {
        tSuiteName_ = tSuiteName
    }
    
    void setTSuiteTimestamp(TSuiteTimestamp tSuiteTimestamp) {
        tSuiteTimestamp_ = tSuiteTimestamp
    }
    
    @Override
    TSuiteName getTSuiteName() {
        return tSuiteName_
    }
    
    @Override
    TSuiteTimestamp getTSuiteTimestamp() {
        return tSuiteTimestamp_
    }
}
