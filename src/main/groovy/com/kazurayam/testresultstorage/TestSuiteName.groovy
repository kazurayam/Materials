package com.kazurayam.testresultstorage

class TestSuiteName {

    private String value

    TestSuiteName(String testSuiteId) {
        this.value = testSuiteId.replaceFirst('^Test Suites/', '')
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
        if (!(obj instanceof TestSuiteName))
            return false
        TestSuiteName other = (TestSuiteName)obj
        return this.getValue() == other.getValue()
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode()
    }
}
