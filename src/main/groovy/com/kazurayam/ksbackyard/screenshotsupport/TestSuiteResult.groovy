package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path

/**
 *
 */
final class TestSuiteResult {

    private Path baseDir
    private TestSuiteName testSuiteName
    private TSTimestamp timestamp
    private List<TestCaseResult> testCaseResults

    // ------------------ constructors & initializer -------------------------------
    TestSuiteResult(Path baseDir, TestSuiteName testSuiteName, TSTimestamp timestamp) {
        assert baseDir != null
        assert testSuiteName != null
        assert timestamp != null
        this.baseDir = baseDir
        this.testSuiteName = testSuiteName
        this.timestamp = timestamp
        this.testCaseResults = new ArrayList<TestCaseResult>()
    }

    // ------------------ attribute setter & getter -------------------------------
    protected Path getBaseDir() {
        return this.baseDir
    }

    TestSuiteName getTestSuiteName() {
        return testSuiteName
    }

    TSTimestamp getTSTimestamp() {
        return timestamp
    }

    // ------------------ create/add/get child nodes ------------------------------
    TestCaseResult findOrNewTestCaseResult(TestCaseName testCaseName) {
        TestCaseResult tcr = this.getTestCaseResult(testCaseName)
        if (tcr == null) {
            tcr = new TestCaseResult(this, testCaseName)
        }
        return tcr
    }

    void addTestCaseResult(TestCaseResult testCaseResult) {
        boolean found = false
        for (TestCaseResult tcr : this.testCaseResults) {
            if (tcr == testCaseResult) {
                found = true
            }
        }
        if (!found) {
            this.testCaseResults.add(testCaseResult)
        }
    }

    TestCaseResult getTestCaseResult(TestCaseName testCaseName) {
        for (TestCaseResult tcr : this.testCaseResults) {
            if (tcr.getTestCaseName() == testCaseName) {
                return tcr
            }
        }
        return null
    }

    // ------------------- helpers -----------------------------------------------
    /*
    Path resolveTestSuiteDirPath() {
        def ts = URLEncoder.encode(testSuiteId.replaceFirst('^Test Suites/', ''), 'UTF-8')
        Path tsOutputDir = this.baseDir.resolve("${ts}/${this.timestamp}")
        Helpers.ensureDirs(tsOutputDir)
        return tsOutputDir
    }
     */

    // -------------------- equals, hashCode ------------------------------------
    @Override
    boolean equals(Object obj) {
        if (this == obj) { return true }
        if (!(obj instanceof TestSuiteResult)) { return false }
        TestSuiteResult other = (TestSuiteResult)obj
        if (this.testSuiteName == other.getTestSuiteName() && this.timestamp == other.getTSTimestamp()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + this.getTestSuiteName().hashCode()
        result = prime * result + this.getTSTimestamp().hashCode()
        return result
    }
}

