package com.kazurayam.carmina.material

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

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
    
    def testGetTSuiteResultsSortedByTSuiteTimestampReverseOrder() {
        when:
        RepositoryScanner scanner = new RepositoryScanner(workdir_)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        List<TSuiteResult> tSuiteResults = repoRoot.getTSuiteResultsSortedByTSuiteTimestampReverseOrder()
        then:
        tSuiteResults.size() > 3
        when:
        def count = 0
        for (TSuiteResult tsr : tSuiteResults) {
            TSuiteTimestamp tst = tsr.getTSuiteTimestamp()
            logger_.debug("#testGetTSuiteResultsSortedByTSuiteTimestampReverseOrder tst${count}=${tst}")
            count += 1
        }
        then:
        true
        when:
        LocalDateTime ldt0 = tSuiteResults.get(0).getTSuiteTimestamp().getValue()
        LocalDateTime ldt1 = tSuiteResults.get(1).getTSuiteTimestamp().getValue()
        LocalDateTime ldt2 = tSuiteResults.get(2).getTSuiteTimestamp().getValue()
        then:
        ldt0 > ldt1
        ldt1 > ldt2
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
