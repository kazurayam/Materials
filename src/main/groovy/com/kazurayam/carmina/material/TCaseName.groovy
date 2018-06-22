package com.kazurayam.carmina.material

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TCaseName {

    static Logger logger_ = LoggerFactory.getLogger(TCaseName.class)

    private String value_

    TCaseName(String testCaseId) {
        String[] arr = testCaseId.split('[/\\\\]')
        value_ = arr[arr.size() - 1].trim()
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
}
