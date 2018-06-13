package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TSuiteName {

    static Logger logger = LoggerFactory.getLogger(TSuiteName.class)

    static final String SUITELESS_DIRNAME = '_'

    static final TSuiteName SUITELESS = new TSuiteName(SUITELESS_DIRNAME)

    private String value

    TSuiteName(String testSuiteId) {
        String[] arr = testSuiteId.split('[/\\\\]')
        this.value = arr[arr.size() - 1]
    }

    String getValue() {
        return value
    }

    // -------------------- overriding Object properties ----------------------
    @Override
    String toString() {
        return value
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
