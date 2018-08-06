package com.kazurayam.material

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.xml.sax.SAXException

/**
 *
 */
final class TSuiteResult implements Comparable<TSuiteResult> {

    static Logger logger_ = LoggerFactory.getLogger(TSuiteResult.class)

    static DocumentBuilderFactory dbFactory_

    static {
        dbFactory_ = DocumentBuilderFactory.newInstance()
        dbFactory_.setNamespaceAware(true)
    }

    private TSuiteName tSuiteName_
    private TSuiteTimestamp tSuiteTimestamp_
    private RepositoryRoot repoRoot_
    private Path tSuiteTimestampDirectory_
    private List<TCaseResult> tCaseResults_
    private LocalDateTime lastModified_
    private Boolean latestModified_

    /*
     * ./Reports/xxx/xxx/yyyyMMdd_hhmmss/JUnit_Report.xml
     */
    private JUnitReport junitReport_


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
        Document doc = createReportDocument()
        if (doc != null) {
            junitReport_ = new JUnitReport(doc)
        }
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

    TSuiteResult setLatestModified(Boolean isLatest) {
        latestModified_ = isLatest
        return this
    }

    JUnitReport getJUnitReport() {
        return junitReport_
    }

    /**
     *
     * @return DOM of ./Reports/xxx/xxx/yyyyMMdd_hhmmss/JUnit_Report.xml
     */
    Document createReportDocument() {
        if (this.getRepositoryRoot() != null) {
            Path reportsDirPath = this.getRepositoryRoot().getBaseDir().resolve('../Reports')
            Path reportFilePath = reportsDirPath.
                    resolve(this.getTSuiteName().getValue().replace('.', '/')).
                    resolve(this.getTSuiteTimestamp().format()).
                    resolve('JUnit_Report.xml')
            if (Files.exists(reportFilePath)) {
                try {
                    DocumentBuilder db = dbFactory_.newDocumentBuilder()
                    Document doc = db.parse(reportFilePath.toFile())
                    return doc
                } catch (IOException e) {
                    logger_.warn("#createReportDocument raised IOException for ${reportFilePath}")
                    return null
                } catch (SAXException e) {
                    logger_.warn("#createReportDocument raised SAXException for ${reportFilePath}")
                    e.printStackTrace()
                    return null
                }
            } else {
                logger_.debug("#createReportDocument ${reportFilePath} does not exist")
                return null
            }
        } else {
            logger_.debug("#createReportDocument this.getRepositoryRoot() returned null")
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



    /**
     * @return
     */
    String toBootstrapTreeviewData() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"text":"' + Helpers.escapeAsJsonText(this.treeviewTitle()) + '",')
        sb.append('"backColor":"#CCDDFF",')
        sb.append('"selectable":false,')
        sb.append('"state":{')
        sb.append('    "expanded":' + latestModified_ + ',')
        sb.append('},')
        sb.append('"nodes":[')
        def count = 0
        for (TCaseResult tcr : tCaseResults_) {
            if (count > 0) { sb.append(',') }
            count += 1
            sb.append(tcr.toBootstrapTreeviewData())
        }
        sb.append(']')
        if (this.getJUnitReport() != null) {
            sb.append(',')
            sb.append('"tags": ["')
            logger_.debug("#toBootstrapTreeviewData this.getTSuiteName() is '${this.getTSuiteName()}'")
            sb.append(this.getJUnitReport().getTestSuiteSummary(this.getTSuiteName().getId()))
            sb.append('"]')
        }
        sb.append('}')
        return sb.toString()
    }

    String treeviewTitle() {
        return tSuiteName_.getValue() + '/' + tSuiteTimestamp_.format()
    }
}

