package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path

/**
 *
 */
final class TestSuiteResult {

    private Path baseDir
    private String testSuiteId
    private TSTimestamp timestamp
    private Map<String, TestCaseResult> testCaseResultMap

    protected TestSuiteResult(Path baseDir, String testSuiteId, TSTimestamp timestamp) {
        assert baseDir != null
        assert testSuiteId != null
        assert timestamp != null
        this.baseDir = baseDir
        this.testSuiteId = testSuiteId
        this.timestamp = timestamp
        this.testCaseResultMap = new HashMap<String, TestCaseResult>()
    }

    protected ScreenshotRepository getParent() {
        return this.parent
    }

    List<String> getTestCaseIdList() {
        List<String> keys = new ArrayList<String>(testCaseResultMap.keySet())
        Collections.sort(keys)
        return keys
    }

    String getTestSuiteId() {
        return testSuiteId
    }

    TSTimestamp getTimestamp() {
        return timestamp
    }

    TestCaseResult getTestCaseResult(String testCaseId) {
        return testCaseResultMap.get(testCaseId)
    }

    protected TestCaseResult findOrNewTestCaseResult(String testCaseId) {
        if (testCaseResultMap.containsKey(testCaseId)) {
            return testCaseResultMap.get(testCaseId)
        }
        else {
            TestCaseResult tcr = new TestCaseResult(this, testCaseId)
            this.testCaseResultMap.put(testCaseId, tcr)
            return tcr
        }
    }

    protected Path resolveTestSuiteOutputDirPath() {
        def ts = URLEncoder.encode(testSuiteId.replaceFirst('^Test Suites/', ''), 'UTF-8')
        Path tsOutputDir = this.baseDir.resolve("${ts}/${this.timestamp}")
        Helpers.ensureDirs(tsOutputDir)
        return tsOutputDir
    }

    @Override
    boolean equals(Object obj) {
        if (this == obj) { return true }
        if (!(obj instanceof TestSuiteResult)) { return false }
        TestSuiteResult other = (TestSuiteResult)obj
        if (this.testSuiteId == other.getTestSuiteId() && this.timestamp == other.getTimestamp()) {
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
        result = prime * result + this.getTimestamp().hashCode()
        return result
    }
}

