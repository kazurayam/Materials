package com.kazurayam.kstestresults

import java.nio.file.Path

/**
 *
 */
class TcResult {

    private TsResult parentTestSuiteResult
    private TcName testCaseName
    private Path testCaseDir
    private List<TargetURL> targetPages
    private TcStatus testCaseStatus

    // --------------------- constructors and initializer ---------------------
    TcResult(TsResult parentTestSuiteResult, TcName testCaseName) {
        assert parentTestSuiteResult != null
        assert testCaseName != null
        this.parentTestSuiteResult = parentTestSuiteResult
        this.testCaseName = testCaseName
        this.testCaseDir = parentTestSuiteResult.getTestSuiteTimestampDir().resolve(this.testCaseName.toString())
        this.targetPages = new ArrayList<TargetURL>()
        this.testCaseStatus = TcStatus.TO_BE_EXECUTED
    }

    // --------------------- properties getter & setters ----------------------
    TsResult getParentTestSuiteResult() {
        return this.parentTestSuiteResult
    }

    TcName getTestCaseName() {
        return testCaseName
    }

    Path getTestCaseDir() {
        return testCaseDir
    }

    void setTestCaseStatus(String testCaseStatus) {
        assert testCaseStatus != null
        TcStatus tcs = TcStatus.valueOf(testCaseStatus)  // this may throw IllegalArgumentException
        this.setTestCaseStatus(tcs)
    }

    void setTestCaseStatus(TcStatus testCaseStatus) {
        assert testCaseStatus != null
        this.testCaseStatus = testCaseStatus
    }

    TcStatus getTestCaseStatus() {
        return this.testCaseStatus
    }

    // --------------------- create/add/get child nodes ----------------------
    TargetURL findOrNewTargetPage(URL url) {
        TargetURL ntp = this.getTargetPage(url)
        if (ntp == null) {
            ntp = new TargetURL(this, url)
            this.targetPages.add(ntp)
        }
        return ntp
    }

    TargetURL getTargetPage(URL url) {
        for (TargetURL tp : this.targetPages) {
            // you MUST NOT evaluate 'tp.getUrl() == url'
            // because it will take more than 10 seconds for DNS Hostname resolution
            if (tp.getUrl().toString() == url.toString()) {
                return tp
            }
        }
        return null
    }

    void addTargetPage(TargetURL targetPage) {
        boolean found = false
        for (TargetURL tp : this.targetPages) {
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
        if (!(obj instanceof TcResult)) {
            return false
        }
        TcResult other = (TcResult) obj
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
        for (TargetURL tp : this.targetPages) {
            if (count > 0) { sb.append(',') }
            sb.append(tp.toJson())
            count += 1
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }
}


