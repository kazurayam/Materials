package com.kazurayam.carmina.material

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Ignore
import spock.lang.Specification
import groovy.json.JsonOutput
import java.nio.file.Path
import java.nio.file.Paths

class RepositoryRootSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryRootSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")
    private static RepositoryRoot repoRoot_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(RepositoryRootSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }
    def setup() {
        repoRoot_ = new RepositoryRoot(workdir_)
    }
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testAddGetTSuiteResults() {
        when:
        TSuiteName tsn = new TSuiteName('TS1')
        TSuiteTimestamp tst = new TSuiteTimestamp('20180530_130419')
        TSuiteResult tsr = new TSuiteResult(tsn, tst)
        repoRoot_.addTSuiteResult(tsr)
        TSuiteResult returned = repoRoot_.getTSuiteResult(tsn, tst)
        then:
        tsr == returned
    }

    def testGetTSuiteResults() {
        when:
        TSuiteResult tsr = new TSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        repoRoot_.addTSuiteResult(tsr)
        List<TSuiteResult> tSuiteResults = repoRoot_.getTSuiteResults()
        then:
        tSuiteResults != null
        tSuiteResults.size() > 0
    }

    def testEquals() {
        when:
        TSuiteResult tsr = new TSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        repoRoot_.addTSuiteResult(tsr)
        RepositoryRoot otherRoot = new RepositoryRoot(workdir_)
        TSuiteResult otherTsr = new TSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        otherRoot.addTSuiteResult(otherTsr)
        then:
        repoRoot_ == otherRoot
    }

    def testHashCode() {
        when:
        TSuiteResult tsr = new TSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        repoRoot_.addTSuiteResult(tsr)
        RepositoryRoot otherRoot = new RepositoryRoot(workdir_)
        TSuiteResult otherTsr = new TSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        otherRoot.addTSuiteResult(otherTsr)
        then:
        repoRoot_.hashCode() == otherRoot.hashCode()
    }

    def testToJson() {
        when:
        TSuiteResult tsr = new TSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        repoRoot_.addTSuiteResult(tsr)
        logger_.debug("#testToJson ${JsonOutput.prettyPrint(repoRoot_.toJson())}")
        then:
        true
    }

    def testToString() {
        when:
        TSuiteResult tsr = new TSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        repoRoot_.addTSuiteResult(tsr)
        logger_.debug("#testToString ${JsonOutput.prettyPrint(repoRoot_.toString())}")
        then:
        true
    }

    def testToBootstrapTreeviewData() {
        when:
        RepositoryScanner scanner = new RepositoryScanner(workdir_)
        scanner.scan()
        RepositoryRoot rr = scanner.getRepositoryRoot()
        def s = rr.toBootstrapTreeviewData()
        logger_.debug("#testToBootstrapTreeviewData ${JsonOutput.prettyPrint(s)}")
        then:
        s.contains('text')
        s.contains('nodes')
    }

    def testHtmlFragmensOfMaterialsAsModal() {
        when:
        RepositoryScanner scanner = new RepositoryScanner(workdir_)
        scanner.scan()
        RepositoryRoot rr = scanner.getRepositoryRoot()
        def html = rr.htmlFragmensOfMaterialsAsModal()
        logger_.debug("#testHtmlFragmentsOfMaterialsAsModal html=\n${html}")
        then:
        html.contains('<div')
    }

}
