package com.kazurayam.materials;

import com.kazurayam.materials.impl.TSuiteResultImpl;
import com.kazurayam.materials.repository.RepositoryRoot;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public abstract class TSuiteResult implements Comparable<TSuiteResult> {

    public static TSuiteResult newInstance(TSuiteName tSuiteName,
                                           TExecutionProfile tExecutionProfile,
                                           TSuiteTimestamp tSuiteTimestamp) {
        return new TSuiteResultImpl(tSuiteName, tExecutionProfile, tSuiteTimestamp);
    }

    public abstract TSuiteResultId getId();

    public abstract TSuiteResult setParent(RepositoryRoot repoRoot);

    public abstract RepositoryRoot getParent();

    public abstract RepositoryRoot getRepositoryRoot();

    public abstract Path getTSuiteNameDirectory();

    public abstract TSuiteName getTSuiteName();

    public abstract Path getTExecutionProfileDirectory();

    public abstract TExecutionProfile getTExecutionProfile();

    public abstract TSuiteTimestamp getTSuiteTimestamp();

    public abstract Path getTSuiteTimestampDirectory();

    public abstract Path createDirectories();

    public abstract TSuiteResult setLastModified(LocalDateTime lastModified);

    public abstract LocalDateTime getLastModified();

    public abstract TSuiteResult setSize(long size);

    public abstract long getSize();

    public abstract boolean isLatestModified();

    public abstract TSuiteResult setLatestModified(Boolean isLatest);

    public abstract TCaseResult getTCaseResult(TCaseName tCaseName);

    public abstract List<TCaseResult> getTCaseResultList();

    public abstract void addTCaseResult(TCaseResult tCaseResult);

    public abstract String treeviewTitle();

    public abstract List<Material> getMaterialList();

    public abstract List<Material> getMaterialList(Path pathRelativeToTSuiteTimestamp);

    @Override
    public boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof TSuiteResult)) {
            return false;
        }

        TSuiteResult other = (TSuiteResult) obj;
        if (this.getId().getTSuiteName().equals(other.getId().getTSuiteName()) && this.getId().getTSuiteTimestamp().equals(other.getId().getTSuiteTimestamp())) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.getId().getTSuiteName().hashCode();
        result = prime * result + this.getId().getTExecutionProfile().hashCode();
        result = prime * result + this.getId().getTSuiteTimestamp().hashCode();
        return result;
    }

    /**
     * TSuitResult is comparable.
     * Primarily sorted by the ascending order of TSuiteName, and
     * secondly sorted by the ascending order of TExecutionProfile, and
     * thirdly sorted by the ascending order of TSuiteTimestamp.
     *
     * This means:
     * 1. TS0/default/20181023_140000
     * 2. TS1/default/20181023_132618
     * 3. TS1/default/20181023_132619
     * 4. TS2/another/20180930_000000
     * 5. TS2/default/20180923_000000
     */
    @Override
    public int compareTo(TSuiteResult other) {
        int v = this.getId().getTSuiteName().compareTo(other.getId().getTSuiteName());
        if (v < 0) {
            return v;
        } else if (v == 0) {
            v = this.getId().getTExecutionProfile().compareTo(other.getId().getTExecutionProfile());
            if (v < 0) {
                return v;
            } else if (v == 0) {
                return this.getId().getTSuiteTimestamp().compareTo(
                        other.getId().getTSuiteTimestamp());
            } else {
                return v;
            }
        } else {
            return v;
        }

    }

    @Override
    public String toString() {
        return toJsonText();
    }

    public String toJsonText() {
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

    public static TSuiteResult getNULL() {
        return TSuiteResult.NULL;
    }

    private static final TSuiteResult NULL =
            new TSuiteResultImpl(TSuiteName.getNULL(),
                    TExecutionProfile.getBLANK(),
                    TSuiteTimestamp.getNULL());

    /**
     * sort a list of TSuiteResult by
     * 1. Descending order of TSuiteTimestamp
     * 2. Ascending order of TExecutionProfile
     * 3. Ascending order of TSuiteName
     *
     * This method is required to find out the TSuiteResult which was created last
     */
    public static class TimestampFirstTSuiteResultComparator implements Comparator<TSuiteResult> {
        @Override
        public int compare(TSuiteResult a, TSuiteResult b) {
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
