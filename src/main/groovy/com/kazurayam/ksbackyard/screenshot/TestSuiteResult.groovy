package com.kazurayam.ksbackyard.screenshot

import java.nio.file.Path
import java.time.LocalDateTime

/**
 *
 */
final class TestSuiteResult {

    private ScreenshotRepository parent
    private String testSuiteId
    private LocalDateTime timestamp
    private Map<String, TestCaseResult> testCaseResultMap

    protected TestSuiteResult(ScreenshotRepository parent, String testSuiteId, LocalDateTime timestamp) {
        this.parent = parent
        this.testSuiteId = testSuiteId
        this.timestamp = timestamp
        this.testCaseResultMap = new HashMap<String, TestCaseResult>()
    }

    List<String> getTestCaseIdList() {
        List<String> keys = new ArrayList<String>(testCaseResultMap.keySet())
        Collections.sort(keys)
        return keys
    }

    String getTestSuiteId() {
        return testSuiteId
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
        def tstamp = Helpers.getTimestampAsString(timestamp)
        Path tsOutputDir = parent.getBaseDirPath().resolve("${ts}/${tstamp}")
        Helpers.ensureDirs(tsOutputDir)
        return tsOutputDir
    }

}

