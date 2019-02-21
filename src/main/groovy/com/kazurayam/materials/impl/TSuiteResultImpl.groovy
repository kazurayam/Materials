package com.kazurayam.materials.impl

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.view.ExecutionPropertiesWrapper
import com.kazurayam.materials.view.JUnitReportWrapper

class TSuiteResultImpl extends TSuiteResult implements Comparable<TSuiteResultImpl>{
    
    static final Logger logger_ = LoggerFactory.getLogger(TSuiteResultImpl.class)

    private final TSuiteResultId tSuiteResultId_
    
    private RepositoryRoot repoRoot_
    private Path tSuiteTimestampDirectory_
    private List<TCaseResult> tCaseResults_
    private LocalDateTime lastModified_
    private boolean latestModified_
    private long size_

    /*
     * wraps ./Reports/xxx/xxx/yyyyMMdd_hhmmss/JUnit_Report.xml
     */
    private JUnitReportWrapper junitReportWrapper_

    /*
     *  wraps ./Reports/xxx/xxx/yyyMMdd_hhmmss/execution.properties
     */
    private ExecutionPropertiesWrapper executionPropertiesWrapper_

    // ------------------ constructors & initializer -------------------------------
    TSuiteResultImpl(TSuiteName testSuiteName, TSuiteTimestamp testSuiteTimestamp) {
        Objects.requireNonNull(testSuiteName)
        Objects.requireNonNull(testSuiteTimestamp)
        tSuiteResultId_ = TSuiteResultIdImpl.newInstance(testSuiteName, testSuiteTimestamp)
        tCaseResults_   = new ArrayList<TCaseResult>()
        lastModified_   = LocalDateTime.MIN
        latestModified_ = false
    }

    // ------------------ attribute setter & getter -------------------------------
    @Override
    TSuiteResultId getId() {
        TSuiteResultId tsri = TSuiteResultIdImpl.newInstance(
                                    tSuiteResultId_.getTSuiteName(),
                                    tSuiteResultId_.getTSuiteTimestamp())
        return tsri
    }

    @Override
    TSuiteResult setParent(RepositoryRoot repoRoot) {
        Objects.requireNonNull(repoRoot)
        repoRoot_ = repoRoot
        tSuiteTimestampDirectory_ =
                repoRoot_.getBaseDir()
                    .resolve(this.getId().getTSuiteName().getValue())
                    .resolve(this.getId().getTSuiteTimestamp().format())
        junitReportWrapper_ = createJUnitReportWrapper()
        executionPropertiesWrapper_ = createExecutionPropertiesWrapper()
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
    TSuiteName getTSuiteName() {
        return tSuiteResultId_.getTSuiteName()
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

    @Override
    TSuiteResult setLastModified(LocalDateTime lastModified) {
        lastModified_ = lastModified
        return this
    }

    @Override
    LocalDateTime getLastModified() {
        return lastModified_
    }

    @Override
    TSuiteResult setSize(long length) {
        size_ = length
        return this
    }
    
    @Override
    long getSize() {
        return size_
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
    JUnitReportWrapper getJUnitReportWrapper() {
        return this.junitReportWrapper_
    }

    @Override
    ExecutionPropertiesWrapper getExecutionPropertiesWrapper() {
        return this.executionPropertiesWrapper_
    }

    /**
     *
     * @return DOM of ./Reports/xxx/xxx/yyyyMMdd_hhmmss/JUnit_Report.xml
     */
    @Override
    JUnitReportWrapper createJUnitReportWrapper() {
        if (this.getRepositoryRoot() != null) {
            Path reportsDirPath = this.getRepositoryRoot().getBaseDir().resolve('../Reports')
            Path reportFilePath = reportsDirPath.
                    resolve(this.getId().getTSuiteName().getValue().replace('.', '/')).
                    resolve(this.getId().getTSuiteTimestamp().format()).
                    resolve('JUnit_Report.xml')
            if (Files.exists(reportFilePath)) {
                return new JUnitReportWrapper(reportFilePath)
            } else {
                logger_.debug("#createJUnitReportWrapper ${reportFilePath} does not exist")
                return null
            }
        } else {
            logger_.debug("#createJUnitReportWrapper this.getRepositoryRoot() returned null")
            return null
        }
    }

    @Override
    ExecutionPropertiesWrapper createExecutionPropertiesWrapper() {
        if (this.getRepositoryRoot() != null) {
            Path reportsDirPath = this.getRepositoryRoot().getBaseDir().resolve('../Reports')
            Path expropFilePath = reportsDirPath.
                    resolve(this.getId().getTSuiteName().getValue().replace('.', '/')).
                    resolve(this.getId().getTSuiteTimestamp().format()).
                    resolve('execution.properties')
            if (Files.exists(expropFilePath)) {
                return new ExecutionPropertiesWrapper(expropFilePath)
            } else {
                logger_.debug("#createExecutionPropertiesWrapper ${expropFilePath} does not exist")
                return null
            }
        } else {
            logger_.debug("#createExecutionPropertiesWrapper this.getRepositoryRoot() returned null")
            return null
        }
    }

    // ------------------ add/get child nodes ------------------------------
    @Override
    TCaseResult getTCaseResult(TCaseName tCaseName) {
        Objects.requireNonNull(tCaseName)
        for (TCaseResult tcr : tCaseResults_) {
            if (tcr.getTCaseName() == tCaseName) {
                return tcr
            }
        }
        return null
    }

    @Override
    List<TCaseResult> getTCaseResultList() {
        return Collections.unmodifiableList(tCaseResults_)
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

    @Override
    String treeviewTitle() {
        StringBuilder sb = new StringBuilder()
        sb.append(this.getId().getTSuiteName().getValue())
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
    int compareTo(TSuiteResultImpl other) {
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
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TSuiteResult":{')
        sb.append('"tSuiteName":' + this.getId().getTSuiteName().toString() + ',')
        sb.append('"tSuiteTimestamp": "' + this.getId().getTSuiteTimestamp().format() + '",')
        sb.append('"tSuiteTimestampDir": "' + Helpers.escapeAsJsonText(this.getTSuiteTimestampDirectory().toString()) + '",')
        sb.append('"lastModified":"' + this.getLastModified().toString() + '",')
        sb.append('"length":' + this.getSize()+ ',')
        sb.append('"tCaseResults": [')
        def count = 0
        for (TCaseResult tcr : this.getTCaseResultList()) {
            if (count > 0) { sb.append(',') }
            count += 1
            sb.append(tcr.toJson())
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }


}
