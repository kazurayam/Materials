package com.kazurayam.materials.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.TSuiteExecutionRecord
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp

class TSuiteExecutionRecordImpl implements TSuiteExecutionRecord {
    
    static Logger logger_ = LoggerFactory.getLogger(TSuiteExecutionRecordImpl.class)
    
    private TSuiteName tSuiteName_
    private TSuiteTimestamp tSuiteTimestamp_
    
    private TSuiteExecutionRecordImpl(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        tSuiteName_ = tSuiteName
        tSuiteTimestamp_ = tSuiteTimestamp    
    }
    
    @Override
    public TSuiteName getTSuiteName() {
        return tSuiteName_
    }

    @Override
    public TSuiteTimestamp getTSuiteTimestamp() {
        return tSuiteTimestamp_
    }
    
    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('\"tSuiteName\":\"')
        sb.append(this.getTSuiteName().getValue())
        sb.append('\",')
        sb.append('\"tSuiteTimestamp\":\"')
        sb.append(this.getTSuiteTimestamp().format())
        sb.append('\"}')
        return sb.toString()
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof TSuiteExecutionRecordImpl) return false
        TSuiteExecutionRecordImpl other = (TSuiteExecutionRecordImpl)obj
        return this.getTSuiteName().equals(other.getTSuiteName()) &&
            this.getTSuiteTimestamp().equals(other.getTSuiteTimestamp())
    }
    
    @Override
    int hashCode() {
        int result = tSuiteName_.hashCode()
        result = 31 * result * tSuiteTimestamp_.hashCode()
        return result
    }
    
    @Override
    public int compareTo(TSuiteExecutionRecord other) {
        int result = tSuiteName_.compareTo(other.getTSuiteName())
        if (result == 0) {
            result = tSuiteTimestamp_.compareTo(other.getTSuiteTimestamp())
        }
        return result
    }

}
