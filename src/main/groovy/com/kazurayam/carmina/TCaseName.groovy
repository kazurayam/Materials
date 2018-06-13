package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TCaseName {

    static Logger logger = LoggerFactory.getLogger(TCaseName.class)

    private String value

    TCaseName(String testCaseId) {
        String[] arr = testCaseId.split('/')
        this.value = URLEncoder.encode(arr[arr.size() - 1], 'UTF-8')
    }

    String getValue() {
        return value
    }

    // ---------------- overriding Object properties --------------------------
    @Override
    String toString() {
        return value
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
