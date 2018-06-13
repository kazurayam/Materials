package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

/**
 *
 */
final class TSuiteResult {

    static Logger logger = LoggerFactory.getLogger(TSuiteResult.class)

    private TSuiteName tSuiteName
    private TSuiteTimestamp tSuiteTimestamp
    private Path baseDir
    private Path tSuiteTimestampDir
    private List<TCaseResult> tCaseResults


    // ------------------ constructors & initializer -------------------------------
    TSuiteResult(TSuiteName testSuiteName, TSuiteTimestamp testSuiteTimestamp) {
        assert testSuiteName != null
        assert testSuiteTimestamp != null
        this.tSuiteName = testSuiteName
        this.tSuiteTimestamp = testSuiteTimestamp
        this.tCaseResults = new ArrayList<TCaseResult>()
    }

    // ------------------ attribute setter & getter -------------------------------
    TSuiteResult setParent(Path baseDir) {
        this.baseDir = baseDir
        this.tSuiteTimestampDir = baseDir.resolve(tSuiteName.toString()).resolve(tSuiteTimestamp.format())
        return this
    }

    Path getParent() {
        return this.getBaseDir()
    }

    Path getBaseDir() {
        return this.baseDir
    }

    Path getTSuiteTimestampDir() {
        return this.tSuiteTimestampDir
    }

    TSuiteName getTSuiteName() {
        return tSuiteName
    }

    TSuiteTimestamp getTSuiteTimestamp() {
        return tSuiteTimestamp
    }

    // ------------------ create/add/get child nodes ------------------------------
    TCaseResult getTCaseResult(TCaseName tCaseName) {
        for (TCaseResult tcr : this.tCaseResults) {
            if (tcr.getTCaseName() == tCaseName) {
                return tcr
            }
        }
        return null
    }

    List<TCaseResult> getTCaseResults() {
        return tCaseResults
    }

    /*
    TCaseResult findOrNewTCaseResult(TCaseName tCaseName) {
        TCaseResult tcr = this.getTCaseResult(tCaseName)
        if (tcr == null) {
            tcr = new TCaseResult(tCaseName).setParent(this)
            this.tCaseResults.add(tcr)
        }
        return tcr
    }
     */


    void addTCaseResult(TCaseResult tCaseResult) {
        boolean found = false
        for (TCaseResult tcr : this.tCaseResults) {
            if (tcr == tCaseResult) {
                found = true
            }
        }
        if (!found) {
            this.tCaseResults.add(tCaseResult)
        }
    }


    // ------------------- helpers -----------------------------------------------
    List<MaterialWrapper> getMaterialWrappers() {
        List<MaterialWrapper> materialWrappers = new ArrayList<MaterialWrapper>()
        for (TCaseResult tcr : this.getTCaseResults()) {
            for (TargetURL targetURL : tcr.getTargetURLs()) {
                for (MaterialWrapper mw : targetURL.getMaterialWrappers()) {
                    materialWrappers.add(mw)
                }
            }
        }
        return materialWrappers
    }

    // -------------------- overriding Object properties ----------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof TSuiteResult)) { return false }
        TSuiteResult other = (TSuiteResult)obj
        if (this.tSuiteName == other.getTSuiteName() && this.tSuiteTimestamp == other.getTSuiteTimestamp()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + this.getTSuiteName().hashCode()
        result = prime * result + this.getTSuiteTimestamp().hashCode()
        return result
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TSuiteResult":{')
        sb.append('"baseDir": "' + Helpers.escapeAsJsonText(this.baseDir.toString()) + '",')
        sb.append('"tSuiteName": "' + Helpers.escapeAsJsonText(this.tSuiteName.toString()) + '",')
        sb.append('"tSuiteTimestamp": ' + this.tSuiteTimestamp.toString() + ',')
        sb.append('"tSuiteTimestampDir": "' + Helpers.escapeAsJsonText(this.tSuiteTimestampDir.toString()) + '",')
        sb.append('"tCaseResults": [')
        def count = 0
        for (TCaseResult tcr : this.tCaseResults) {
            if (count > 0) { sb.append(',') }
            count += 1
            sb.append(tcr.toJson())
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }

}

