package com.kazurayam.materials.impl

import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp

public class TSuiteResultIdImpl extends TSuiteResultId {

    private TSuiteName tSuiteName_
    private TSuiteTimestamp tSuiteTimestamp_
    
    TSuiteResultIdImpl(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        this.tSuiteName_ = tSuiteName
        this.tSuiteTimestamp_ = tSuiteTimestamp
    }
    
    /*
    void setTSuiteName(TSuiteName tSuiteName) {
        tSuiteName_ = tSuiteName
    }
    
    void setTSuiteTimestamp(TSuiteTimestamp tSuiteTimestamp) {
        tSuiteTimestamp_ = tSuiteTimestamp
    }
    */
    
    @Override
    TSuiteName getTSuiteName() {
        return tSuiteName_
    }
    
    @Override
    TSuiteTimestamp getTSuiteTimestamp() {
        return tSuiteTimestamp_
    }
    
    @Override
    String toString() {
        return "\'" + tSuiteName_.getValue() + '/' + tSuiteTimestamp_.format() + "\'"
    }
    
    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof TSuiteResultIdImpl))
            return false
        TSuiteResultIdImpl other = (TSuiteResultIdImpl)obj
        return this.getTSuiteName().equals(other.getTSuiteName()) &&
                this.getTSuiteTimestamp().equals(other.getTSuiteTimestamp())
    }
    
    @Override
    int hashCode() {
        return Objects.hash(tSuiteName_, tSuiteTimestamp_)
    }
}
