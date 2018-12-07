package com.kazurayam.materials.model

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.RepositoryRoot
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.view.ExecutionPropertiesWrapper
import com.kazurayam.materials.view.JUnitReportWrapper

/**
 *
 */
final class TSuiteResult implements Comparable<TSuiteResult> {

    static Logger logger_ = LoggerFactory.getLogger(TSuiteResult.class)

    private TSuiteName tSuiteName_
    private TSuiteTimestamp tSuiteTimestamp_
    private RepositoryRoot repoRoot_
    private Path tSuiteTimestampDirectory_
    private List<TCaseResult> tCaseResults_
    private LocalDateTime lastModified_
    private Boolean latestModified_

    /*
     * wraps ./Reports/xxx/xxx/yyyyMMdd_hhmmss/JUnit_Report.xml
     */
    private JUnitReportWrapper junitReportWrapper_

    /*
     *  wraps ./Reports/xxx/xxx/yyyMMdd_hhmmss/execution.properties
     */
    private ExecutionPropertiesWrapper executionPropertiesWrapper_

    // ------------------ constructors & initializer -------------------------------
    TSuiteResult(TSuiteName testSuiteName, TSuiteTimestamp testSuiteTimestamp) {
        assert testSuiteName != null
        assert testSuiteTimestamp != null
        tSuiteName_ = testSuiteName
        tSuiteTimestamp_ = testSuiteTimestamp
        tCaseResults_ = new ArrayList<TCaseResult>()
        lastModified_ = LocalDateTime.MIN
        latestModified_ = false
    }

    // ------------------ attribute setter & getter -------------------------------
    TSuiteResult setParent(RepositoryRoot repoRoot) {
        repoRoot_ = repoRoot
        tSuiteTimestampDirectory_ =
                repoRoot_.getBaseDir().resolve(tSuiteName_.getValue()).resolve(tSuiteTimestamp_.format())
        junitReportWrapper_ = createJUnitReportWrapper()
        executionPropertiesWrapper_ = createExecutionPropertiesWrapper()
        return this
    }


    RepositoryRoot getParent() {
        return this.getRepositoryRoot()
    }

    RepositoryRoot getRepositoryRoot() {
        return repoRoot_
    }

    Path getTSuiteTimestampDirectory() {
        return tSuiteTimestampDirectory_.normalize()
    }

    TSuiteName getTSuiteName() {
        return tSuiteName_
    }

    TSuiteTimestamp getTSuiteTimestamp() {
        return tSuiteTimestamp_
    }

    TSuiteResult setLastModified(LocalDateTime lastModified) {
        lastModified_ = lastModified
        return this
    }

    LocalDateTime getLastModified() {
        return lastModified_
    }

    Boolean isLatestModified() {
        return latestModified_
    }

    TSuiteResult setLatestModified(Boolean isLatest) {
        latestModified_ = isLatest
        return this
    }

    JUnitReportWrapper getJUnitReportWrapper() {
        return this.junitReportWrapper_
    }

    ExecutionPropertiesWrapper getExecutionPropertiesWrapper() {
        return this.executionPropertiesWrapper_
    }

    /**
     *
     * @return DOM of ./Reports/xxx/xxx/yyyyMMdd_hhmmss/JUnit_Report.xml
     */
    JUnitReportWrapper createJUnitReportWrapper() {
        if (this.getRepositoryRoot() != null) {
            Path reportsDirPath = this.getRepositoryRoot().getBaseDir().resolve('../Reports')
            Path reportFilePath = reportsDirPath.
                    resolve(this.getTSuiteName().getValue().replace('.', '/')).
                    resolve(this.getTSuiteTimestamp().format()).
                    resolve('JUnit_Report.xml')
            if (Files.exists(reportFilePath)) {
                return new JUnitReportWrapper(reportFilePath)
            } else {
                logger_.info("#createJUnitReportWrapper ${reportFilePath} does not exist")
                return null
            }
        } else {
            logger_.debug("#createJUnitReportWrapper this.getRepositoryRoot() returned null")
            return null
        }
    }

    ExecutionPropertiesWrapper createExecutionPropertiesWrapper() {
        if (this.getRepositoryRoot() != null) {
            Path reportsDirPath = this.getRepositoryRoot().getBaseDir().resolve('../Reports')
            Path expropFilePath = reportsDirPath.
                    resolve(this.getTSuiteName().getValue().replace('.', '/')).
                    resolve(this.getTSuiteTimestamp().format()).
                    resolve('execution.properties')
            if (Files.exists(expropFilePath)) {
                return new ExecutionPropertiesWrapper(expropFilePath)
            } else {
                logger_.info("#createExecutionPropertiesWrapper ${expropFilePath} does not exist")
                return null
            }
        } else {
            logger_.debug("#createExecutionPropertiesWrapper this.getRepositoryRoot() returned null")
            return null
        }
    }

    // ------------------ add/get child nodes ------------------------------
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
            Collections.sort(tCaseResults_)
        }
    }


    String treeviewTitle() {
        return tSuiteName_.getValue() + '/' + tSuiteTimestamp_.format()
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
        if (tSuiteName_.equals(other.getTSuiteName()) && tSuiteTimestamp_.equals(other.getTSuiteTimestamp())) {
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

    /**
     * TSuitResult is comparable.
     * Primarily sorted by the ascending order of TSuiteName, and
     * secondarily sorted by the ascending order of TSuiteTimestamp.
     * 
     * This means:
     * 1. TS0/20181023_140000
     * 2. TS1/20181023_132618
     * 3. TS1/20181023_132619
     * 4. TS2/20180923_000000 
     *
     */
    @Override
    int compareTo(TSuiteResult other) {
        int v = this.getTSuiteName().compareTo(other.getTSuiteName())
        if (v < 0) {
            return v
        } else if (v == 0) {
            v = this.getTSuiteTimestamp().compareTo(other.getTSuiteTimestamp())
            return v
        } else {
            return v
        }
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TSuiteResult":{')
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
        sb.append('],')
        sb.append('"lastModified":"' + lastModified_.toString() + '"')
        sb.append('}}')
        return sb.toString()
    }


}

