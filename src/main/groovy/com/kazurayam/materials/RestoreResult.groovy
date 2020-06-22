package com.kazurayam.materials

import com.kazurayam.materials.impl.RestoreResultImpl
import com.kazurayam.materials.impl.TSuiteResultImpl

/**
 * An instance of MaterialStorage.RestoreResult object is returned by
 * MaterialStrage#restoreUnary(...) method.
 *
 * An instance of MaterialStorage.RestoreResult object contains information
 * what TSuiteReult was restored by the method invokation and
 * how many Materials ware copied from the Stroage dir to the Materials dir.
 */
interface RestoreResult {
    
    static RestoreResult NULL =
        new RestoreResultImpl(
            new TSuiteResultImpl(TSuiteName.NULL, TExecutionProfile.BLANK, TSuiteTimestamp.NULL),
            0)
    
    TSuiteResult getTSuiteResult()
    
    Integer getCount()
}

