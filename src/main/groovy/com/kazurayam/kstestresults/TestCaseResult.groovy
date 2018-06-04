package com.kazurayam.kstestresults

import java.nio.file.Path

/**
 *
 */
class TestCaseResult {

    private TestSuiteResult parentTestSuiteResult
    private TestCaseName testCaseName
    private Path testCaseDir
    private List<TargetPage> targetPages
    private TestCaseStatus testCaseStatus

    // --------------------- constructors and initializer ---------------------
    TestCaseResult(TestSuiteResult parentTestSuiteResult, TestCaseName testCaseName) {
        assert parentTestSuiteResult != null
        assert testCaseName != null
        this.parentTestSuiteResult = parentTestSuiteResult
        this.testCaseName = testCaseName
        this.testCaseDir = parentTestSuiteResult.getTestSuiteTimestampDir().resolve(this.testCaseName.toString())
        this.targetPages = new ArrayList<TargetPage>()
        this.testCaseStatus = TestCaseStatus.TO_BE_EXECUTED
    }

    // --------------------- properties getter & setters ----------------------
    TestSuiteResult getParentTestSuiteResult() {
        return this.parentTestSuiteResult
    }

    TestCaseName getTestCaseName() {
        return testCaseName
    }

    Path getTestCaseDir() {
        return testCaseDir
    }

    void setTestCaseStatus(String testCaseStatus) {
        assert testCaseStatus != null
        TestCaseStatus tcs = TestCaseStatus.valueOf(testCaseStatus)  // this may throw IllegalArgumentException
        this.setTestCaseStatus(tcs)
    }

    void setTestCaseStatus(TestCaseStatus testCaseStatus) {
        assert testCaseStatus != null
        this.testCaseStatus = testCaseStatus
    }

    TestCaseStatus getTestCaseStatus() {
        return this.testCaseStatus
    }

    // --------------------- create/add/get child nodes ----------------------
    TargetPage findOrNewTargetPage(URL url) {
        TargetPage ntp = this.getTargetPage(url)
        if (ntp == null) {
            ntp = new TargetPage(this, url)
            this.targetPages.add(ntp)
        }
        return ntp
    }

    TargetPage getTargetPage(URL url) {
        for (TargetPage tp : this.targetPages) {
            // you MUST NOT evaluate 'tp.getUrl() == url'
            // because it will take more than 10 seconds for DNS Hostname resolution
            if (tp.getUrl().toString() == url.toString()) {
                return tp
            }
        }
        return null
    }

    void addTargetPage(TargetPage targetPage) {
        boolean found = false
        for (TargetPage tp : this.targetPages) {
            if (tp == targetPage) {
                found = true
            }
        }
        if (!found) {
            this.targetPages.add(targetPage)
        }
    }

    // -------------------------- helpers -------------------------------------

    // ------------------ overriding Object properties ------------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) {
        //    return true
        //}
        if (!(obj instanceof TestCaseResult)) {
            return false
        }
        TestCaseResult other = (TestCaseResult) obj
        if (this.testCaseName == other.getTestCaseName()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        return this.testCaseName.hashCode()
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TestCaseResult":{')
        sb.append('"testCaseName":"'   + Helpers.escapeAsJsonText(this.testCaseName.toString())   + '",')
        sb.append('"testCaseDir":"'    + Helpers.escapeAsJsonText(this.testCaseDir.toString())    + '",')
        sb.append('"testCaseStatus":"' + this.testCaseStatus.toString() + '",')
        sb.append('"targetPages":[')
        def count = 0
        for (TargetPage tp : this.targetPages) {
            if (count > 0) { sb.append(',') }
            sb.append(tp.toJson())
            count += 1
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }
}


