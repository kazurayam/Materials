package com.kazurayam.carmina.material

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TSuiteName {

    static Logger logger_ = LoggerFactory.getLogger(TSuiteName.class)

    static final String SUITELESS_DIRNAME = '_'

    static final TSuiteName SUITELESS = new TSuiteName(SUITELESS_DIRNAME)

    private String value_

    TSuiteName(String testSuiteId) {
        String[] arr = testSuiteId.split('[/\\\\]')
        value_ = arr[arr.size() - 1].trim()
    }

    String getValue() {
        return value_
    }

    // -------------------- overriding Object properties ----------------------
    @Override
    String toString() {
        return value_
    }

    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof TSuiteName))
            return false
        TSuiteName other = (TSuiteName)obj
        return this.getValue() == other.getValue()
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode()
    }
}
