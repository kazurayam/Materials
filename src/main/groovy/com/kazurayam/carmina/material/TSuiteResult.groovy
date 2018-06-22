package com.kazurayam.carmina.material

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 */
final class TSuiteResult {

    static Logger logger_ = LoggerFactory.getLogger(TSuiteResult.class)

    private TSuiteName tSuiteName_
    private TSuiteTimestamp tSuiteTimestamp_
    private Path baseDir_
    private Path tSuiteTimestampDirectory_
    private List<TCaseResult> tCaseResults_


    // ------------------ constructors & initializer -------------------------------
    TSuiteResult(TSuiteName testSuiteName, TSuiteTimestamp testSuiteTimestamp) {
        assert testSuiteName != null
        assert testSuiteTimestamp != null
        tSuiteName_ = testSuiteName
        tSuiteTimestamp_ = testSuiteTimestamp
        tCaseResults_ = new ArrayList<TCaseResult>()
    }

    // ------------------ attribute setter & getter -------------------------------
    TSuiteResult setParent(Path baseDir) {
        baseDir_ = baseDir
        tSuiteTimestampDirectory_ = baseDir.resolve(tSuiteName_.toString()).resolve(tSuiteTimestamp_.format())
        return this
    }

    Path getParent() {
        return this.getBaseDir()
    }

    Path getBaseDir() {
        return baseDir_
    }

    Path getTSuiteTimestampDirectory() {
        return tSuiteTimestampDirectory_
    }

    TSuiteName getTSuiteName() {
        return tSuiteName_
    }

    TSuiteTimestamp getTSuiteTimestamp() {
        return tSuiteTimestamp_
    }

    // ------------------ create/add/get child nodes ------------------------------
    TCaseResult getTCaseResult(TCaseName tCaseName) {
        for (TCaseResult tcr : tCaseResults_) {
            if (tcr.getTCaseName() == tCaseName) {
                return tcr
            }
        }
        return null
    }

    List<TCaseResult> getTCaseResults() {
        return tCaseResults_
    }

    void addTCaseResult(TCaseResult tCaseResult) {
        if (tCaseResult.getParent() != this) {
            def msg = "tCaseResult ${tCaseResult.toString()} does not have appropriate parent"
            logger_.error("#addTCaseResult ${msg}")
            throw new IllegalArgumentException(msg)
        }
        boolean found = false
        for (TCaseResult tcr : tCaseResults_) {
            if (tcr == tCaseResult) {
                found = true
            }
        }
        if (!found) {
            tCaseResults_.add(tCaseResult)
        }
    }


    // ------------------- helpers -----------------------------------------------
    List<Material> getMaterials() {
        List<Material> materials = new ArrayList<Material>()
        for (TCaseResult tcr : this.getTCaseResults()) {
            for (Material mate : tcr.getMaterials()) {
                materials.add(mate)
            }
        }
        return materials
    }


    // -------------------- overriding Object properties ----------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof TSuiteResult)) { return false }
        TSuiteResult other = (TSuiteResult)obj
        if (tSuiteName_ == other.getTSuiteName() && tSuiteTimestamp_ == other.getTSuiteTimestamp()) {
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
        sb.append('"baseDir": "' + Helpers.escapeAsJsonText(baseDir_.toString()) + '",')
        sb.append('"tSuiteName": "' + Helpers.escapeAsJsonText(tSuiteName_.toString()) + '",')
        sb.append('"tSuiteTimestamp": "' + tSuiteTimestamp_.format() + '",')
        sb.append('"tSuiteTimestampDir": "' + Helpers.escapeAsJsonText(tSuiteTimestampDirectory_.toString()) + '",')
        sb.append('"tCaseResults": [')
        def count = 0
        for (TCaseResult tcr : tCaseResults_) {
            if (count > 0) { sb.append(',') }
            count += 1
            sb.append(tcr.toJson())
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }



    /**
     * generate a JSON text for bootstrap-treeview
     *
     * <pre>
     * {@code
     * [
     *   {
     *     text: "TC1",
     *     nodes: [
     *       {
     *         text: 'http%3A%2F%2Fdemoaut.katalon.com%2F.png'
     *       },
     *       {
     *         text: 'http%3A%2F%2Fdemoaut.katalon.com§1.png'
     *       }
     *     ]
     *   },
     *   {
     *     text: "TC2,
     *     nodes: [
     *       {
     *         text: 'http%3A%2F%2Fdemoaut.katalon.com§atoz.png'
     *       }
     *     ]
     *   }
     * ]
     * }
     * </pre>
     * @return
     */
    String toBootstrapTreeviewData() {
        StringBuilder sb = new StringBuilder()
        sb.append('[{')
        sb.append('"text":"' + Helpers.escapeAsJsonText(tSuiteName_.toString() +
            '/' + tSuiteTimestamp_.format()) + '",')
        sb.append('"nodes":[')
        def count = 0
        for (TCaseResult tcr : tCaseResults_) {
            if (count > 0) { sb.append(',') }
            count += 1
            sb.append(tcr.toBootstrapTreeviewData())
        }
        sb.append(']')
        sb.append('}]')
        return sb.toString()
    }

}

