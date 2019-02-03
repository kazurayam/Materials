package com.kazurayam.materials

import java.nio.file.Path
import java.time.LocalDateTime

import com.kazurayam.materials.impl.TSuiteResultImpl
import com.kazurayam.materials.model.TCaseResult
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.view.ExecutionPropertiesWrapper
import com.kazurayam.materials.view.JUnitReportWrapper

/**
 *
 */
abstract class TSuiteResult implements Comparable<TSuiteResult> {

    static final TSuiteResult NULL = new TSuiteResultImpl(TSuiteName.NULL, TSuiteTimestamp.NULL)
    
    static TSuiteResult newInstance(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        return new TSuiteResultImpl(tSuiteName, tSuiteTimestamp)
    }
    // ------------------ attribute setter & getter -------------------------------
    abstract TSuiteResultId getId()

    abstract TSuiteResult setParent(RepositoryRoot repoRoot)

    abstract RepositoryRoot getParent()

    abstract RepositoryRoot getRepositoryRoot()

    abstract Path getTSuiteTimestampDirectory()

    abstract TSuiteResult setLastModified(LocalDateTime lastModified)

    abstract LocalDateTime getLastModified()

    abstract boolean isLatestModified()

    abstract TSuiteResult setLatestModified(Boolean isLatest)

    abstract JUnitReportWrapper getJUnitReportWrapper()

    abstract ExecutionPropertiesWrapper getExecutionPropertiesWrapper()

    /**
     *
     * @return DOM of ./Reports/xxx/xxx/yyyyMMdd_hhmmss/JUnit_Report.xml
     */
    abstract JUnitReportWrapper createJUnitReportWrapper()

    abstract ExecutionPropertiesWrapper createExecutionPropertiesWrapper()

    // ------------------ add/get child nodes ------------------------------
    
    abstract TCaseResult getTCaseResult(TCaseName tCaseName)

    abstract List<TCaseResult> getTCaseResultList()

    abstract void addTCaseResult(TCaseResult tCaseResult)

    abstract String treeviewTitle()

    // ------------------- helpers -----------------------------------------------
    abstract List<Material> getMaterialList()

    // -------------------- overriding Object properties ----------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof TSuiteResult)) { return false }
        TSuiteResult other = (TSuiteResult)obj
        if (this.getId().getTSuiteName().equals(other.getId().getTSuiteName()) && 
            this.getId().getTSuiteTimestamp().equals(other.getId().getTSuiteTimestamp())) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + this.getId().getTSuiteName().hashCode()
        result = prime * result + this.getId().getTSuiteTimestamp().hashCode()
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
        int v = this.getId().getTSuiteName().compareTo(other.getId().getTSuiteName())
        if (v < 0) {
            return v
        } else if (v == 0) {
            v = this.getId().getTSuiteTimestamp().compareTo(other.getId().getTSuiteTimestamp())
            return v
        } else {
            return v
        }
    }

    @Override
    String toString() {
        return this.getId().getTSuiteName().getValue() + '/' + this.getId().getTSuiteTimestamp().format()
    }

}

