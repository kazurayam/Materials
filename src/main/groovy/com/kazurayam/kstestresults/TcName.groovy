package com.kazurayam.kstestresults

class TcName {

    private String value

    TcName(String testCaseId) {
        this.value = testCaseId.replaceFirst('^Test Cases/', '')
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
        if (!(obj instanceof TcName))
            return false
        TcName other = (TcName)obj
        return this.getValue() == other.getValue()
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode()
    }
}
