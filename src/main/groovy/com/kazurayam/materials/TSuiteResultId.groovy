package com.kazurayam.materials

import com.kazurayam.materials.impl.TSuiteResultIdImpl

abstract class TSuiteResultId {

    static TSuiteResultId newInstance(TSuiteName tSuiteName,
                                      TExecutionProfile tExecutionProfile,
                                      TSuiteTimestamp tSuiteTimestamp) {

        return new TSuiteResultIdImpl(tSuiteName, tExecutionProfile, tSuiteTimestamp)
    }
    
    abstract TSuiteName getTSuiteName()

    abstract TExecutionProfile getTExecutionProfile()

    abstract TSuiteTimestamp getTSuiteTimestamp()


}