package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path

/**
 *
 */
final class TestSuiteResult {

    private Path baseDir
    private String testSuiteId
    private TSTimestamp timestamp
    private List<TestCaseResult> testCaseResults

    // ------------------ constructors & initializer -------------------------------
    TestSuiteResult(Path baseDir, String testSuiteId, TSTimestamp timestamp) {
        assert baseDir != null
        assert testSuiteId != null
        assert timestamp != null
        this.baseDir = baseDir
        this.testSuiteId = testSuiteId
        this.timestamp = timestamp
        this.testCaseResults = new ArrayList<TestCaseResult>()
    }

    // ------------------ attribute setter & getter -------------------------------
    protected Path getBaseDir() {
        return this.baseDir
    }

    String getTestSuiteId() {
        return testSuiteId
    }

    TSTimestamp getTSTimestamp() {
        return timestamp
    }

    // ------------------ create/add/get child nodes ------------------------------
    TestCaseResult findOrNewTestCaseResult(String testCaseId) {
        TestCaseResult tcr = this.getTestCaseResult(testCaseId)
        if (tcr == null) {
            tcr = new TestCaseResult(this, testCaseId)
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

    TestCaseResult getTestCaseResult(String testCaseId) {
        for (TestCaseResult tcr : this.testCaseResults) {
            if (tcr.getTestCaseId() == testCaseId) {
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
        if (this.testSuiteId == other.getTestSuiteId() && this.timestamp == other.getTSTimestamp()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + this.getTestSuiteId().hashCode()
        result = prime * result + this.getTSTimestamp().hashCode()
        return result
    }
}

