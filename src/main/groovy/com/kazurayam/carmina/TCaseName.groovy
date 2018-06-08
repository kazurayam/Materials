package com.kazurayam.carmina

class TCaseName {

    private String value

    TCaseName(String testCaseId) {
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
