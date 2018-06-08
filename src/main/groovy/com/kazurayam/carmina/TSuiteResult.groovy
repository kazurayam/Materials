package com.kazurayam.carmina

import java.nio.file.Path

/**
 *
 */
final class TSuiteResult {

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

    Path getTsTimestampDir() {
        return this.tSuiteTimestampDir
    }

    TSuiteName getTsName() {
        return tSuiteName
    }

    TSuiteTimestamp getTsTimestamp() {
        return tSuiteTimestamp
    }

    // ------------------ create/add/get child nodes ------------------------------
    TCaseResult getTcResult(TCaseName tcName) {
        for (TCaseResult tcr : this.tCaseResults) {
            if (tcr.getTcName() == tcName) {
                return tcr
            }
        }
        return null
    }

    List<TCaseResult> getTcResults() {
        return tCaseResults
    }

    TCaseResult findOrNewTcResult(TCaseName tcName) {
        TCaseResult tcr = this.getTcResult(tcName)
        if (tcr == null) {
            tcr = new TCaseResult(tcName).setParent(this)
            this.tCaseResults.add(tcr)
        }
        return tcr
    }

    void addTcResult(TCaseResult tcResult) {
        boolean found = false
        for (TCaseResult tcr : this.tCaseResults) {
            if (tcr == tcResult) {
                found = true
            }
        }
        if (!found) {
            this.tCaseResults.add(tcResult)
        }
    }


    // ------------------- helpers -----------------------------------------------
    List<MaterialWrapper> getMaterialWrappers() {
        List<MaterialWrapper> materialWrappers = new ArrayList<MaterialWrapper>()
        for (TCaseResult tcr : this.getTcResults()) {
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
        if (this.tSuiteName == other.getTsName() && this.tSuiteTimestamp == other.getTsTimestamp()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + this.getTsName().hashCode()
        result = prime * result + this.getTsTimestamp().hashCode()
        return result
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TsResult":{')
        sb.append('"baseDir": "' + Helpers.escapeAsJsonText(this.baseDir.toString()) + '",')
        sb.append('"tsName": "' + Helpers.escapeAsJsonText(this.tSuiteName.toString()) + '",')
        sb.append('"tsTimestamp": ' + this.tSuiteTimestamp.toString() + ',')
        sb.append('"tsTimestampDir": "' + Helpers.escapeAsJsonText(this.tSuiteTimestampDir.toString()) + '",')
        sb.append('"tcResults": [')
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

