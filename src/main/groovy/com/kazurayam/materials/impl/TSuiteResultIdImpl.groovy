package com.kazurayam.materials.impl

import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp

public class TSuiteResultIdImpl implements TSuiteResultId {

    private TSuiteName tSuiteName_
    private TSuiteTimestamp tSuiteTimestamp_
    
    private TSuiteResultIdImpl() {}
    
    TSuiteResultId newInstance(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        TSuiteResultId instance = new TSuiteResultIdImpl(tSuiteName, tSuiteTimestamp)
        instance.setTSuiteName(tSuiteName)
        instance.setTSuiteTimestamp(tSuiteTimestamp)
        return instance
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
