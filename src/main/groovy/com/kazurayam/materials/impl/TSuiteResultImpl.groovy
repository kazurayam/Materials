package com.kazurayam.materials.impl

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.repository.RepositoryRoot
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime

class TSuiteResultImpl extends TSuiteResult implements Comparable<TSuiteResult>{
    
    static final Logger logger_ = LoggerFactory.getLogger(TSuiteResultImpl.class)

    private final TSuiteResultId tSuiteResultId_
    
    private RepositoryRoot repoRoot_
    private Path tSuiteNameDirectory_
    private Path tExecutionProfileDirectory_
    private Path tSuiteTimestampDirectory_

    private Set<TCaseResult> tCaseResults_

    private boolean latestModified_

    // ------------------ constructors & initializer -------------------------------
	TSuiteResultImpl(TSuiteResultId tSuiteResultId) {
		this(tSuiteResultId.getTSuiteName(),
                tSuiteResultId.getTExecutionProfile(),
                tSuiteResultId.getTSuiteTimestamp())
	}
	
    TSuiteResultImpl(TSuiteName tSuiteName,
                     TExecutionProfile tExecutionProfile,
                     TSuiteTimestamp tSuiteTimestamp) {
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(tExecutionProfile, "tExecutionProfile must not be null")
        Objects.requireNonNull(tSuiteTimestamp, "tSuiteTimestamp must not be null")

        tSuiteResultId_ = TSuiteResultIdImpl.newInstance(tSuiteName,
                tExecutionProfile,
                tSuiteTimestamp)

        tCaseResults_   = new HashSet<TCaseResult>()
        latestModified_ = false
    }

    // ================================================================
    //
    //
    // ----------------------------------------------------------------
    @Override
    List<TCaseResult> getTCaseResultList() {
        List<TCaseResult> list = new ArrayList(tCaseResults_)
        Collections.sort(list)
        return list
    }

    @Override
    TCaseResult getTCaseResult(TCaseName tCaseName) {
        Objects.requireNonNull(tCaseName)
        for (tcr in this.getTCaseResultList()) {
            if (tcr.getTCaseName() == tCaseName) {
                return tcr
            }
        }
        return null
    }


    @Override
    void addTCaseResult(TCaseResult tCaseResult) {
        Objects.requireNonNull(tCaseResult)
        if (tCaseResult.getParent() != this) {
            def msg = "tCaseResult ${tCaseResult.toString()} does not have appropriate parent"
            logger_.error("#addTCaseResult ${msg}")
            throw new IllegalArgumentException(msg)
        }
        boolean found = false
        for (tcr in tCaseResults_) {
            if (tcr == tCaseResult) {
                found = true
            }
        }
        if (!found) {
            tCaseResults_.add(tCaseResult)
        }
    }


    @Override
    TSuiteResultId getId() {
        return TSuiteResultIdImpl.newInstance(
                tSuiteResultId_.getTSuiteName(),
                tSuiteResultId_.getTExecutionProfile(),
                tSuiteResultId_.getTSuiteTimestamp()
        )
    }

    /**
     * Here we create the directory tree of
     *
     * Materials/<Test Suite name>/<Execution Profile name>/<Test Suite timestamp>
     *
     * @param repoRoot
     * @return
     */
    @Override
    TSuiteResult setParent(RepositoryRoot repoRoot) {
        Objects.requireNonNull(repoRoot)
        repoRoot_ = repoRoot

        tSuiteNameDirectory_ = repoRoot_.getBaseDir()
                .resolve(this.getId().getTSuiteName().getValue())

        tExecutionProfileDirectory_ = tSuiteNameDirectory_
                .resolve(this.getId().getTExecutionProfile().getNameInPathSafeChars())

        tSuiteTimestampDirectory_ = tExecutionProfileDirectory_
                .resolve(this.getId().getTSuiteTimestamp().format())

        return this
    }

    @Override
    RepositoryRoot getParent() {
        return this.getRepositoryRoot()
    }

    @Override
    RepositoryRoot getRepositoryRoot() {
        return repoRoot_
    }
    
    @Override
    Path getTSuiteNameDirectory() {
        return tSuiteNameDirectory_
    }

    @Override
    Path getTExecutionProfileDirectory() {
        return tExecutionProfileDirectory_
    }
    
    @Override
    TSuiteName getTSuiteName() {
        return tSuiteResultId_.getTSuiteName()
    }

    @Override
    TExecutionProfile getTExecutionProfile() {
        return tSuiteResultId_.getTExecutionProfile()
    }

    @Override
    TSuiteTimestamp getTSuiteTimestamp() {
        return tSuiteResultId_.getTSuiteTimestamp()
    }

    @Override
    Path getTSuiteTimestampDirectory() {
        if (tSuiteTimestampDirectory_ != null) {
            return tSuiteTimestampDirectory_.normalize()
        } else {
            return null
        }
    }

    /**
     * create the directory of this TSuiteResult = the dir of TSuiteTimestamp
     *
     * <baseDir>/<TSuiteName>/<TExecutionProfile>/<TSuiteTimestamp>
     *
     * @return
     */
    @Override
    Path createDirectories() {
        Path d = this.getTSuiteTimestampDirectory()
        if (d != null) {
            Files.createDirectories(d)
        } else {
            return null
        }
    }

    @Override
    LocalDateTime getLastModified() {
        LocalDateTime lastModified = LocalDateTime.MIN
        List<TCaseResult> tCaseResults = this.getTCaseResultList()
        for (TCaseResult tcr : tCaseResults) {
            if (tcr.getLastModified() > lastModified) {
                lastModified = tcr.getLastModified()
            }
        }
        return lastModified
    }

    @Override
    long getSize() {
        long length = 0
        List<TCaseResult> tCaseResults = this.getTCaseResultList()
        for (TCaseResult tcr :  tCaseResults) {
            length += tcr.getSize()
        }
        return length
    }
    
    @Override
    boolean isLatestModified() {
        return latestModified_
    }

    @Override
    TSuiteResult setLatestModified(Boolean isLatest) {
        latestModified_ = isLatest
        return this
    }



    @Override
    String treeviewTitle() {
        StringBuilder sb = new StringBuilder()
        sb.append(this.getId().getTSuiteName().getValue())
        sb.append('/')
        sb.append(this.getId().getTExecutionProfile().getName())
        sb.append('/')
        sb.append(this.getId().getTSuiteTimestamp().format())
        return sb.toString()
    }

    // ------------------- helpers -----------------------------------------------
    @Override
    List<Material> getMaterialList() {
        List<Material> materials = new ArrayList<Material>()
        for (TCaseResult tcr : this.getTCaseResultList()) {
            for (Material mate : tcr.getMaterialList()) {
                materials.add(mate)
            }
        }
        return Collections.unmodifiableList(materials)
    }
    
    @Override
    List<Material> getMaterialList(Path pathRelativeToTSuiteTimestamp) {
        List<Material> materials = new ArrayList<Material>()
        for (TCaseResult tcr : this.getTCaseResultList()) {
            for (Material mate : tcr.getMaterialList()) {
                if (mate.getPathRelativeToTSuiteTimestamp().equals(pathRelativeToTSuiteTimestamp)) {
                    materials.add(mate)
                }
            }
        }
        return Collections.unmodifiableList(materials)
    }

    // -------------------- overriding Object properties ----------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof TSuiteResultImpl)) { return false }
        TSuiteResultImpl other = (TSuiteResultImpl)obj
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
        return this.toJsonText()
    }

    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TSuiteResult":{')
        sb.append('"tSuiteName": "'
                + Helpers.escapeAsJsonText(this.getId().getTSuiteName().toString()) + '",')
        sb.append('"tExecutionProfile": "'
                + Helpers.escapeAsJsonText(this.getId().getTExecutionProfile().getName()) + '",')
        sb.append('"tSuiteTimestamp": "'
                + Helpers.escapeAsJsonText(this.getId().getTSuiteTimestamp().format()) + '",')
        sb.append('"tSuiteTimestampDir": "'
                + Helpers.escapeAsJsonText(this.getTSuiteTimestampDirectory().toString()) + '",')
        sb.append('"lastModified": "'
                + this.getLastModified().toString() + '",')
        sb.append('"length":' + this.getSize()+ ',')
        sb.append('"tCaseResults": [')
        def count = 0
        for (TCaseResult tcr : this.getTCaseResultList()) {
            if (count > 0) { sb.append(',') }
            count += 1
            sb.append(tcr.toJsonText())
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }


}
