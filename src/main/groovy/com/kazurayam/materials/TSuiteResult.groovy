package com.kazurayam.materials

import java.nio.file.Path
import java.time.LocalDateTime

import com.kazurayam.materials.impl.TSuiteResultImpl
import com.kazurayam.materials.repository.RepositoryRoot

/**
 *
 */
abstract class TSuiteResult implements Comparable<TSuiteResult> {

    static TSuiteResult newInstance(TSuiteName tSuiteName,
                                    TExecutionProfile tExecutionProfile,
                                    TSuiteTimestamp tSuiteTimestamp) {
        return new TSuiteResultImpl(tSuiteName, tExecutionProfile, tSuiteTimestamp)
    }

    // ------------------ attribute setter & getter -------------------------------
    abstract TSuiteResultId getId()

    abstract TSuiteResult setParent(RepositoryRoot repoRoot)

    abstract RepositoryRoot getParent()

    abstract RepositoryRoot getRepositoryRoot()
    
    abstract Path getTSuiteNameDirectory()

    abstract TSuiteName getTSuiteName()

    abstract Path getTExecutionProfileDirectory()

    abstract TExecutionProfile getTExecutionProfile()
    
    abstract TSuiteTimestamp getTSuiteTimestamp()
    
    abstract Path getTSuiteTimestampDirectory()
    
    abstract Path createDirectories()

    abstract LocalDateTime getLastModified()

    abstract boolean isLatestModified()

    abstract TSuiteResult setLatestModified(Boolean isLatest)

    /**
     * get the sum of length of files belonging to this TSuiteResult
     *
     * @return
     */
    abstract long getSize()

    // ------------------ add/get child nodes ------------------------------
    
    abstract TCaseResult getTCaseResult(TCaseName tCaseName)

    abstract void addTCaseResult(TCaseResult tCaseResult)

    /**
     * This method makes sure we have a TCaseResult that is liked to the TSuiteResult object.
     * If the TCaseResult object is already there then we will reuse it.
     * If the TCaseResult object is NOT there then we will newly create it.
     *
     * This method drives
     * 1. TSuiteResult.getTCaseResult(TCaseName) +
     * 2. TCaseResult.setParent(TSuiteResult) +
     * 3. TSuiteResult.addTCaseResult(TCaseResult)
     * in one sequence.
     *
     * This sequence is frequently called up by
     * - MaterialRepositoryImpl
     * - RepositoryFileVisitor
     * - TreeBranchVisitor
     *
     * @param tCaseName
     * @return
     */
    TCaseResult ensureTCaseResultPresent(TCaseName tCaseName) {
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
        TCaseResult tCaseResult = this.getTCaseResult(tCaseName)
        if (tCaseResult == null) {
            tCaseResult = TCaseResult.newInstance(tCaseName).setParent(this)
            this.addTCaseResult(tCaseResult)
        }
        return tCaseResult
    }

    abstract List<TCaseResult> getTCaseResultList()

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
        if (this.getId().getTSuiteName() == other.getId().getTSuiteName() &&
                this.getId().getTExecutionProfile() == other.getId().getTExecutionProfile() &&
                this.getId().getTSuiteTimestamp() == other.getId().getTSuiteTimestamp()) {
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
        result = prime * result + this.getId().getTExecutionProfile().hashCode()
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
        return this.getId().compareTo(other.getId())
        /*
        int v = this.getId().getTSuiteName().compareTo(
                other.getId().getTSuiteName())
        if (v < 0) {
            return v
        } else if (v == 0) {
            v = this.getId().getTExecutionProfile().compareTo(
                    other.getId().getTExecutionProfile())
            if (v == 0) {
                return this.getId().getTSuiteTimestamp().compareTo(
                        other.getId().getTSuiteTimestamp())
            } else {
                return v
            }
        } else {
            return v
        }
         */
    }

    
    @Override
    String toString() {
        return toJsonText()
    }
    
    String toJsonText() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"value\":\"");
        sb.append(this.getId().getTSuiteName().getValue());
        sb.append("\"");
        sb.append(",\"profile\":\"");
        sb.append(this.getId().getTExecutionProfile().getName());
        sb.append("\"");
        sb.append("\",\"format\":\"");
        sb.append(this.getId().getTSuiteTimestamp().format());
        sb.append("\"");
        sb.append("}");
        return sb.toString();
    }

    static final TSuiteResult NULL =
            new TSuiteResultImpl(TSuiteName.getNULL(),
                    TExecutionProfile.getBLANK(),
                    TSuiteTimestamp.getNULL())

    /**
     * sort a list of TSuiteResult by
     * 1. Descending order of TSuiteTimestamp
     * 2. Ascending order of TExecutionProfile
     * 3. Ascending order of TSuiteName
     *
     * This method is required to find out the TSuiteResult which was created last
     */
    static class TimestampFirstTSuiteResultComparator implements Comparator<TSuiteResult> {
        @Override
        int compare(TSuiteResult a, TSuiteResult b) {
            int v = a.getId().getTSuiteTimestamp().compareTo(b.getId().getTSuiteTimestamp());
            if (v < 0) {
                return v * -1;
            } else if (v == 0) {
                v = a.getId().getTExecutionProfile().compareTo(b.getId().getTExecutionProfile());
                if (v < 0) {
                    return v;
                } else if (v == 0) {
                    return a.getId().getTSuiteName().compareTo(b.getId().getTSuiteName());
                } else {
                    return v;
                }
            } else {
                return v * -1;
            }

        }
    }
}

