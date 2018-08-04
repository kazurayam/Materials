package com.kazurayam.material

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TSuiteName implements Comparable<TSuiteName> {

    static Logger logger_ = LoggerFactory.getLogger(TSuiteName.class)

    static final String SUITELESS_DIRNAME = '_'

    static final TSuiteName SUITELESS = new TSuiteName(SUITELESS_DIRNAME)

    private String value_

    TSuiteName(String testSuiteId) {
        def s = testSuiteId
        def prefix = 'Test Suites/'
        if (s.startsWith(prefix)) {
            s = s.substring(prefix.length())
        }
        s = s.replace('/', '.')
        s = s.replace(' ', '')
        value_ = s
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

    @Override
    int compareTo(TSuiteName other) {
        return this.getValue().compareTo(other.getValue())
    }
}
