package com.kazurayam.kstestresults

import java.nio.file.Path

/**
 *
 */
final class TsResult {

    private Path baseDir
    private TsName testSuiteName
    private TsTimestamp testSuiteTimestamp
    private Path testSuiteTimestampDir
    private List<TcResult> testCaseResults


    // ------------------ constructors & initializer -------------------------------
    TsResult(Path baseDir, TsName testSuiteName, TsTimestamp testSuiteTimestamp) {
        assert baseDir != null
        assert testSuiteName != null
        assert testSuiteTimestamp != null
        this.baseDir = baseDir
        this.testSuiteName = testSuiteName
        this.testSuiteTimestamp = testSuiteTimestamp
        this.testSuiteTimestampDir = baseDir.resolve(testSuiteName.toString()).resolve(testSuiteTimestamp.format())
        this.testCaseResults = new ArrayList<TcResult>()
    }

    // ------------------ attribute setter & getter -------------------------------
    protected Path getBaseDir() {
        return this.baseDir
    }

    protected Path getTestSuiteTimestampDir() {
        return this.testSuiteTimestampDir
    }

    TsName getTestSuiteName() {
        return testSuiteName
    }

    TsTimestamp getTestSuiteTimestamp() {
        return testSuiteTimestamp
    }

    // ------------------ create/add/get child nodes ------------------------------
    TcResult getTestCaseResult(TcName testCaseName) {
        for (TcResult tcr : this.testCaseResults) {
            if (tcr.getTestCaseName() == testCaseName) {
                return tcr
            }
        }
        return null
    }

    TcResult findOrNewTestCaseResult(TcName testCaseName) {
        TcResult tcr = this.getTestCaseResult(testCaseName)
        if (tcr == null) {
            tcr = new TcResult(this, testCaseName)
            this.testCaseResults.add(tcr)
        }
        return tcr
    }

    void addTestCaseResult(TcResult testCaseResult) {
        boolean found = false
        for (TcResult tcr : this.testCaseResults) {
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
        if (!(obj instanceof TsResult)) { return false }
        TsResult other = (TsResult)obj
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
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TestSuiteResult":{')
        sb.append('"baseDir": "' + Helpers.escapeAsJsonText(this.baseDir.toString()) + '",')
        sb.append('"testSuiteName": "' + Helpers.escapeAsJsonText(this.testSuiteName.toString()) + '",')
        sb.append('"testSuiteTimestamp": ' + this.testSuiteTimestamp.toString() + ',')
        sb.append('"testSuiteTimestampDir": "' + Helpers.escapeAsJsonText(this.testSuiteTimestampDir.toString()) + '",')
        sb.append('"testCaseResults": [')
        def count = 0
        for (TcResult tcr : this.testCaseResults) {
            if (count > 0) { sb.append(',') }
            count += 1
            sb.append(tcr.toJson())
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }

}

