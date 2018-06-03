package com.kazurayam.ksbackyard.screenshotsupport

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import java.nio.file.Path

/**
 *
 */
final class TestSuiteResult {

    private Path baseDir
    private TestSuiteName testSuiteName
    private TestSuiteTimestamp testSuiteTimestamp
    private Path testSuiteTimestampDir
    private List<TestCaseResult> testCaseResults


    // ------------------ constructors & initializer -------------------------------
    TestSuiteResult(Path baseDir, TestSuiteName testSuiteName, TestSuiteTimestamp testSuiteTimestamp) {
        assert baseDir != null
        assert testSuiteName != null
        assert testSuiteTimestamp != null
        this.baseDir = baseDir
        this.testSuiteName = testSuiteName
        this.testSuiteTimestamp = testSuiteTimestamp
        this.testSuiteTimestampDir = baseDir.resolve(testSuiteName.toString()).resolve(testSuiteTimestamp.format())
        this.testCaseResults = new ArrayList<TestCaseResult>()
    }

    // ------------------ attribute setter & getter -------------------------------
    protected Path getBaseDir() {
        return this.baseDir
    }

    protected Path getTestSuiteTimestampDir() {
        return this.testSuiteTimestampDir
    }

    TestSuiteName getTestSuiteName() {
        return testSuiteName
    }

    TestSuiteTimestamp getTestSuiteTimestamp() {
        return testSuiteTimestamp
    }

    // ------------------ create/add/get child nodes ------------------------------
    TestCaseResult getTestCaseResult(TestCaseName testCaseName) {
        for (TestCaseResult tcr : this.testCaseResults) {
            if (tcr.getTestCaseName() == testCaseName) {
                return tcr
            }
        }
        return null
    }

    TestCaseResult findOrNewTestCaseResult(TestCaseName testCaseName) {
        TestCaseResult tcr = this.getTestCaseResult(testCaseName)
        if (tcr == null) {
            tcr = new TestCaseResult(this, testCaseName)
            this.testCaseResults.add(tcr)
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

    // ------------------- helpers -----------------------------------------------
    /*
    Path resolveTestSuiteDirPath() {
        def ts = URLEncoder.encode(testSuiteId.replaceFirst('^Test Suites/', ''), 'UTF-8')
        Path tsOutputDir = this.baseDir.resolve("${ts}/${this.timestamp}")
        return tsOutputDir
    }
     */

    // -------------------- overriding Object properties ----------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof TestSuiteResult)) { return false }
        TestSuiteResult other = (TestSuiteResult)obj
        if (this.testSuiteName == other.getTestSuiteName() && this.testSuiteTimestamp == other.getTestSuiteTimestamp()) {
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
        result = prime * result + this.getTestSuiteTimestamp().hashCode()
        return result
    }

    @Override
    String toString() {
        def json = new JsonBuilder()
        json (
                ["baseDir": this.baseDir.toString()],
                ["testSuiteName": this.testSuiteName.toString()],
                ["testSuiteTimestamp": this.testSuiteTimestamp.toString()],
                ["testSuiteTimestampDir": this.testSuiteTimestampDir.toString()],
                //["testCaseResults": this.testCaseResults]
        )
        return json.toString()
    }

}

