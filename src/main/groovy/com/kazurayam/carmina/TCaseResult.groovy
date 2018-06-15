package com.kazurayam.carmina

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 */
class TCaseResult {

    static Logger logger_ = LoggerFactory.getLogger(TCaseResult.class)

    private TSuiteResult parent_
    private TCaseName tCaseName_
    private Path tCaseDir_
    private List<TargetURL> targetURLs_
    private TCaseStatus tCaseStatus_

    // --------------------- constructors and initializer ---------------------
    /**
     *
     * @param tCaseName
     */
    TCaseResult(TCaseName tCaseName) {
        tCaseName_ = tCaseName
        targetURLs_ = new ArrayList<TargetURL>()
        tCaseStatus_ = TCaseStatus.TO_BE_EXECUTED
    }

    // --------------------- properties getter & setters ----------------------
    TCaseResult setParent(TSuiteResult parent) {
        parent_ = parent
        tCaseDir_ = parent.getTSuiteTimestampDir().resolve(tCaseName_.toString())
        return this
    }

    TSuiteResult getParent() {
        return parent_
    }

    TSuiteResult getTSuiteResult() {
        return this.getParent()
    }

    TCaseName getTCaseName() {
        return tCaseName_
    }

    Path getTCaseDir() {
        return tCaseDir_
    }

    void setTestCaseStatus(String str) {
        TCaseStatus tcs = TCaseStatus.valueOf(str)  // this may throw IllegalArgumentException
        this.setTestCaseStatus(tcs)
    }

    void setTestCaseStatus(TCaseStatus tCaseStatus) {
        assert tCaseStatus != null
        tCaseStatus_ = tCaseStatus
    }

    TCaseStatus getTestCaseStatus() {
        return tCaseStatus_
    }

    // --------------------- create/add/get child nodes ----------------------

    TargetURL getTargetURL(URL url) {
        for (TargetURL tp : targetURLs_) {
            // you MUST NOT evaluate 'tp.getUrl() == url'
            // because it will take more than 10 seconds for DNS Hostname resolution
            if (tp.getUrl().toString() == url.toString()) {
                return tp
            }
        }
        return null
    }

    List<TargetURL> getTargetURLs() {
        return targetURLs_
    }

    void addTargetURL(TargetURL targetPage) {
        boolean found = false
        for (TargetURL tp : targetURLs_) {
            if (tp == targetPage) {
                found = true
            }
        }
        if (!found) {
            targetURLs_.add(targetPage)
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
        return tCaseName_ == other.getTCaseName()
    }

    @Override
    int hashCode() {
        return tCaseName_.hashCode()
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TCaseResult":{')
        sb.append('"tCaseName":"'   + Helpers.escapeAsJsonText(tCaseName_.toString())   + '",')
        sb.append('"tCaseDir":"'    + Helpers.escapeAsJsonText(tCaseDir_.toString())    + '",')
        sb.append('"tCaseStatus":"' + tCaseStatus_.toString() + '",')
        sb.append('"targetURLs":[')
        def count = 0
        for (TargetURL tp : targetURLs_) {
            if (count > 0) { sb.append(',') }
            sb.append(tp.toJson())
            count += 1
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }
}


