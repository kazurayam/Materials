package com.kazurayam.materials.impl

import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp

public class TSuiteResultIdImpl extends TSuiteResultId {

    private TSuiteName tSuiteName_
    private TExecutionProfile tExecutionProfile_
    private TSuiteTimestamp tSuiteTimestamp_
    
    TSuiteResultIdImpl(TSuiteName tSuiteName,
                       TExecutionProfile tExecutionProfile,
                       TSuiteTimestamp tSuiteTimestamp) {
        this.tSuiteName_ = tSuiteName
        this.tExecutionProfile_ = tExecutionProfile
        this.tSuiteTimestamp_ = tSuiteTimestamp
    }

    @Override
    TSuiteName getTSuiteName() {
        return tSuiteName_
    }

    @Override
    TExecutionProfile getTExecutionProfile() {
        return tExecutionProfile_
    }

    @Override
    TSuiteTimestamp getTSuiteTimestamp() {
        return tSuiteTimestamp_
    }
    
    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append("\'")
        sb.append(tSuiteName_.getValue())
        sb.append('/')
        sb.append(tExecutionProfile_.getName())
        sb.append('/')
        sb.append(tSuiteTimestamp_.format())
        sb.append("\'")
        return sb.toString()
    }
    
    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof TSuiteResultIdImpl))
            return false
        TSuiteResultIdImpl other = (TSuiteResultIdImpl)obj
        return this.getTSuiteName() == other.getTSuiteName() &&
                this.getTExecutionProfile() == other.getTExecutionProfile() &&
                this.getTSuiteTimestamp() == other.getTSuiteTimestamp()
    }
    
    @Override
    int hashCode() {
        return Objects.hash(tSuiteName_, tExecutionProfile_, tSuiteTimestamp_)
    }

}
