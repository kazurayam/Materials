package com.kazurayam.carmina

import java.nio.file.Path

/**
 *
 */
final class TSuiteResult {

    private Path baseDir
    private TSuiteName tsName
    private TSuiteTimestamp tsTimestamp
    private Path tsTimestampDir
    private List<TCaseResult> tcResults


    // ------------------ constructors & initializer -------------------------------
    TSuiteResult(Path baseDir, TSuiteName testSuiteName, TSuiteTimestamp testSuiteTimestamp) {
        assert baseDir != null
        assert testSuiteName != null
        assert testSuiteTimestamp != null
        this.baseDir = baseDir
        this.tsName = testSuiteName
        this.tsTimestamp = testSuiteTimestamp
        this.tsTimestampDir = baseDir.resolve(testSuiteName.toString()).resolve(testSuiteTimestamp.format())
        this.tcResults = new ArrayList<TCaseResult>()
    }

    // ------------------ attribute setter & getter -------------------------------
    Path getBaseDir() {
        return this.baseDir
    }

    Path getTsTimestampDir() {
        return this.tsTimestampDir
    }

    TSuiteName getTsName() {
        return tsName
    }

    TSuiteTimestamp getTsTimestamp() {
        return tsTimestamp
    }

    // ------------------ create/add/get child nodes ------------------------------
    TCaseResult getTcResult(TCaseName tcName) {
        for (TCaseResult tcr : this.tcResults) {
            if (tcr.getTcName() == tcName) {
                return tcr
            }
        }
        return null
    }

    List<TCaseResult> getTcResults() {
        return tcResults
    }

    TCaseResult findOrNewTcResult(TCaseName tcName) {
        TCaseResult tcr = this.getTcResult(tcName)
        if (tcr == null) {
            tcr = new TCaseResult(tcName).setParent(this)
            this.tcResults.add(tcr)
        }
        return tcr
    }

    void addTcResult(TCaseResult tcResult) {
        boolean found = false
        for (TCaseResult tcr : this.tcResults) {
            if (tcr == tcResult) {
                found = true
            }
        }
        if (!found) {
            this.tcResults.add(tcResult)
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
        if (this.tsName == other.getTsName() && this.tsTimestamp == other.getTsTimestamp()) {
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
        sb.append('"tsName": "' + Helpers.escapeAsJsonText(this.tsName.toString()) + '",')
        sb.append('"tsTimestamp": ' + this.tsTimestamp.toString() + ',')
        sb.append('"tsTimestampDir": "' + Helpers.escapeAsJsonText(this.tsTimestampDir.toString()) + '",')
        sb.append('"tcResults": [')
        def count = 0
        for (TCaseResult tcr : this.tcResults) {
            if (count > 0) { sb.append(',') }
            count += 1
            sb.append(tcr.toJson())
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }

}

