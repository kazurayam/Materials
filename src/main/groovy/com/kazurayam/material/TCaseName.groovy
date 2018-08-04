package com.kazurayam.material

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TCaseName implements Comparable<TCaseName> {

    static Logger logger_ = LoggerFactory.getLogger(TCaseName.class)

    private String value_

    /**
     *
     * @param testCaseId
     */
    TCaseName(String testCaseId) {
        def s = testCaseId
        if (s.startsWith('Test Cases/')) {
            s = s.substring('Test Cases/'.length())
        }
        s = s.replace('/', '.')
        s = s.replace(' ', '')
        value_ = s.trim()
    }

    String getValue() {
        return value_
    }

    // ---------------- overriding Object properties --------------------------
    @Override
    String toString() {
        return value_
    }

    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof TCaseName))
            return false
        TCaseName other = (TCaseName)obj
        return this.getValue() == other.getValue()
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode()
    }

    @Override
    int compareTo(TCaseName other) {
        return this.getValue().compareTo(other.getValue())
    }
}
