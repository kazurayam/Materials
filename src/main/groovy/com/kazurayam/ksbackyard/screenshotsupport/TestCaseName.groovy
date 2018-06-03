package com.kazurayam.ksbackyard.screenshotsupport

class TestCaseName {

    private String value

    TestCaseName(String testCaseId) {
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
        if (!(obj instanceof TestCaseName))
            return false
        TestCaseName other = (TestCaseName)obj
        return this.getValue() == other.getValue()
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode()
    }
}
