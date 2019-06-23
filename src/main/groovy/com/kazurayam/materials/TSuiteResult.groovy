package com.kazurayam.materials

import java.nio.file.Path
import java.time.LocalDateTime

import com.kazurayam.materials.impl.TSuiteResultImpl
import com.kazurayam.materials.repository.RepositoryRoot

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
    
    abstract Path getTSuiteNameDirectory()

    abstract TSuiteName getTSuiteName()
    
    abstract TSuiteTimestamp getTSuiteTimestamp()
    
    abstract Path getTSuiteTimestampDirectory()
    
    abstract Path createDirectories()

    abstract TSuiteResult setLastModified(LocalDateTime lastModified)

    abstract LocalDateTime getLastModified()
    
    abstract TSuiteResult setSize(long size)
    
    abstract long getSize()

    abstract boolean isLatestModified()

    abstract TSuiteResult setLatestModified(Boolean isLatest)
        
    // ------------------ add/get child nodes ------------------------------
    
    abstract TCaseResult getTCaseResult(TCaseName tCaseName)

    abstract List<TCaseResult> getTCaseResultList()

    abstract void addTCaseResult(TCaseResult tCaseResult)

    abstract String treeviewTitle()

    // ------------------- helpers -----------------------------------------------
    abstract List<Material> getMaterialList()
    
    abstract List<Material> getMaterialList(Path pathRelativeToTSuiteTimestamp)

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
        return toJsonText()
    }
    
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"value\":\"")
        sb.append(this.getId().getTSuiteName().getValue())
        sb.append("\",\"format\":\"")
        sb.append(this.getId().getTSuiteTimestamp().format())
        sb.append("\"")
        sb.append("}")
        return sb.toString()
    }

    /**
     * sort a list of TSuiteResult by
     * 1. Descending order of TSuiteTimestamp
     * 2. Ascending order of TSuiteName
     */
    public static class TimestampFirstTSuiteResultComparator implements Comparator<TSuiteResult> {
        @Override
        int compare(TSuiteResult a, TSuiteResult b) {
            int v = a.getId().getTSuiteTimestamp().compareTo(b.getId().getTSuiteTimestamp())
            if (v < 0) {
                return v * -1
            } else if (v == 0) {
                v = a.getId().getTSuiteName().compareTo(b.getId().getTSuiteName())
                return v
            } else {
                return v * -1
            }
        }
    }

}

