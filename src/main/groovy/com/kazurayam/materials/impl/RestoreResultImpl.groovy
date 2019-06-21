package com.kazurayam.materials.impl

import com.kazurayam.materials.RestoreResult
import com.kazurayam.materials.TSuiteResult

/**
 * MaterialStorage#restore*() returns a RestoreResult object which encloses a TSuiteResult object
 * and number of Material files copied from the Storage dir into the Materials dir
 */
class RestoreResultImpl implements RestoreResult {
    
    private TSuiteResult tSuiteResult_
    private Integer count_
    
    RestoreResultImpl(TSuiteResult tSuiteResult, Integer count) {
        this.tSuiteResult_ = tSuiteResult
        this.count_ = count
    }
    
    @Override
    TSuiteResult getTSuiteResult() {
        return tSuiteResult_
    }
    
    @Override
    Integer getCount() {
        return count_
    }
}

