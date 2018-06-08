package com.kazurayam.carmina

import java.nio.file.Path

/**
 *
 */
class TCaseResult {

    private TSuiteResult parent
    private TCaseName tCaseName
    private Path tCaseDir
    private List<TargetURL> targetURLs
    private TestCaseStatus tCaseStatus

    // --------------------- constructors and initializer ---------------------
    /**
     *
     * @param tCaseName
     */
    TCaseResult(TCaseName tCaseName) {
        this.tCaseName = tCaseName
        this.targetURLs = new ArrayList<TargetURL>()
        this.tCaseStatus = TestCaseStatus.TO_BE_EXECUTED
    }

    // --------------------- properties getter & setters ----------------------
    TCaseResult setParent(TSuiteResult parent) {
        this.parent = parent
        this.tCaseDir = parent.getTsTimestampDir().resolve(this.tCaseName.toString())
        return this
    }

    TSuiteResult getParent() {
        return this.parent
    }

    TSuiteResult getTSuiteResult() {
        return this.getParent()
    }

    TCaseName getTCaseName() {
        return tCaseName
    }

    Path getTCaseDir() {
        return tCaseDir
    }

    void setTestCaseStatus(String str) {
        TestCaseStatus tcs = TCaseStatus.valueOf(str)  // this may throw IllegalArgumentException
        this.setTestCaseStatus(tcs)
    }

    void setTestCaseStatus(TestCaseStatus tCaseStatus) {
        assert tCaseStatus != null
        this.tCaseStatus = tCaseStatus
    }

    TestCaseStatus getTestCaseStatus() {
        return this.tCaseStatus
    }

    // --------------------- create/add/get child nodes ----------------------
    TargetURL findOrNewTargetURL(URL url) {
        TargetURL ntp = this.getTargetURL(url)
        if (ntp == null) {
            ntp = new TargetURL(url).setParent(this)
            this.targetURLs.add(ntp)
        }
        return ntp
    }

    TargetURL getTargetURL(URL url) {
        for (TargetURL tp : this.targetURLs) {
            // you MUST NOT evaluate 'tp.getUrl() == url'
            // because it will take more than 10 seconds for DNS Hostname resolution
            if (tp.getUrl().toString() == url.toString()) {
                return tp
            }
        }
        return null
    }

    List<TargetURL> getTargetURLs() {
        return this.targetURLs
    }

    void addTargetURL(TargetURL targetPage) {
        boolean found = false
        for (TargetURL tp : this.targetURLs) {
            if (tp == targetPage) {
                found = true
            }
        }
        if (!found) {
            this.targetURLs.add(targetPage)
        }
    }

    // -------------------------- helpers -------------------------------------

    // ------------------ overriding Object properties ------------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) {
        //    return true
        //}
        if (!(obj instanceof TCaseResult)) {
            return false
        }
        TCaseResult other = (TCaseResult) obj
        if (this.tCaseName == other.getTCaseName()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        return this.tCaseName.hashCode()
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TCaseResult":{')
        sb.append('"tCaseName":"'   + Helpers.escapeAsJsonText(this.tCaseName.toString())   + '",')
        sb.append('"tCaseDir":"'    + Helpers.escapeAsJsonText(this.tCaseDir.toString())    + '",')
        sb.append('"tCaseStatus":"' + this.tCaseStatus.toString() + '",')
        sb.append('"targetURLs":[')
        def count = 0
        for (TargetURL tp : this.targetURLs) {
            if (count > 0) { sb.append(',') }
            sb.append(tp.toJson())
            count += 1
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }
}


