package com.kazurayam.kstestresults

import java.nio.file.Path

/**
 *
 */
final class TsResult {

    private Path baseDir
    private TsName tsName
    private TsTimestamp tsTimestamp
    private Path tsTimestampDir
    private List<TcResult> tcResults


    // ------------------ constructors & initializer -------------------------------
    TsResult(Path baseDir, TsName testSuiteName, TsTimestamp testSuiteTimestamp) {
        assert baseDir != null
        assert testSuiteName != null
        assert testSuiteTimestamp != null
        this.baseDir = baseDir
        this.tsName = testSuiteName
        this.tsTimestamp = testSuiteTimestamp
        this.tsTimestampDir = baseDir.resolve(testSuiteName.toString()).resolve(testSuiteTimestamp.format())
        this.tcResults = new ArrayList<TcResult>()
    }

    // ------------------ attribute setter & getter -------------------------------
    protected Path getBaseDir() {
        return this.baseDir
    }

    protected Path getTsTimestampDir() {
        return this.tsTimestampDir
    }

    TsName getTsName() {
        return tsName
    }

    TsTimestamp getTsTimestamp() {
        return tsTimestamp
    }

    // ------------------ create/add/get child nodes ------------------------------
    TcResult getTcResult(TcName tcName) {
        for (TcResult tcr : this.tcResults) {
            if (tcr.getTcName() == tcName) {
                return tcr
            }
        }
        return null
    }

    List<TcResult> getTcResults() {
        return tcResults
    }

    TcResult findOrNewTcResult(TcName tcName) {
        TcResult tcr = this.getTcResult(tcName)
        if (tcr == null) {
            tcr = new TcResult(tcName).setParent(this)
            this.tcResults.add(tcr)
        }
        return tcr
    }

    void addTcResult(TcResult tcResult) {
        boolean found = false
        for (TcResult tcr : this.tcResults) {
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
        for (TcResult tcr : this.getTcResults()) {
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
        if (!(obj instanceof TsResult)) { return false }
        TsResult other = (TsResult)obj
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
        for (TcResult tcr : this.tcResults) {
            if (count > 0) { sb.append(',') }
            count += 1
            sb.append(tcr.toJson())
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }

}

