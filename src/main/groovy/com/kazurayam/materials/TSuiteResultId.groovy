package com.kazurayam.materials

import com.kazurayam.materials.impl.TSuiteResultIdImpl

import java.awt.print.Book

abstract class TSuiteResultId implements Comparable<TSuiteResultId> {

    static TSuiteResultId newInstance(TSuiteName tSuiteName,
                                      TExecutionProfile tExecutionProfile,
                                      TSuiteTimestamp tSuiteTimestamp) {

        return new TSuiteResultIdImpl(tSuiteName, tExecutionProfile, tSuiteTimestamp)
    }
    
    abstract TSuiteName getTSuiteName()

    abstract TExecutionProfile getTExecutionProfile()

    abstract TSuiteTimestamp getTSuiteTimestamp()

    @Override
    int compareTo(TSuiteResultId other){
        int v = this.getTSuiteName().compareTo(
                other.getTSuiteName())
        if (v < 0) {
            return v
        } else if (v == 0) {
            v = this.getTExecutionProfile().compareTo(
                    other.getTExecutionProfile())
            if (v == 0) {
                return this.getTSuiteTimestamp().compareTo(
                        other.getTSuiteTimestamp())
            } else {
                return v
            }
        } else {
            return v
        }
    }
}