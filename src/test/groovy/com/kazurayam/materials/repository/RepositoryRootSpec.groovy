package com.kazurayam.materials.repository

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.repository.RepositoryFileScanner
import com.kazurayam.materials.repository.RepositoryRoot

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
        RepositoryFileScanner scanner = new RepositoryFileScanner(workdir_)
        scanner.scan()
        repoRoot_ = scanner.getRepositoryRoot()
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testAddGetTSuiteResults() {
        when:
        TSuiteName tsn = new TSuiteName('Test Suites/main/TS1')
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance('20180530_130419')
        TSuiteResult tsr = TSuiteResult.newInstance(tsn, tst)
        repoRoot_.addTSuiteResult(tsr)
        TSuiteResult returned = repoRoot_.getTSuiteResult(tsn, tst)
        then:
        tsr == returned
    }
    
    def testGetMaterials_noArgs() {
        when:
        List<Material> mates = repoRoot_.getMaterials()
        then:
        mates.size() > 2
    }
    
    def testGetMaterials_withArgs() {
        when:
        TSuiteName tsn = new TSuiteName('Test Suites/main/TS1')
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance('20180530_130419')
        List<Material> mates = repoRoot_.getMaterials(tsn, tst)
        then:
        mates.size() == 2
    }

    def testGetTSuiteResults() {
        when:
        TSuiteResult tsr = TSuiteResult.newInstance(
                new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        repoRoot_.addTSuiteResult(tsr)
        List<TSuiteResult> tSuiteResults = repoRoot_.getTSuiteResults()
        then:
        tSuiteResults != null
        tSuiteResults.size() > 0
    }


    def testGetTSuiteResults_byTSuiteName() {
        when:
        List<TSuiteResult> tsrList = repoRoot_.getTSuiteResults(new TSuiteName('TS1'))
        then:
        tsrList.size() == 2
        // tsList is not sorted
        //tsrList[0].getTSuiteName() == new TSuiteName('TS1')
        //tsrList[0].getTSuiteTimestamp() == TSuiteTimestampImpl.newInstance('20180810_140105')
        //tsrList[1].getTSuiteName() == new TSuiteName('TS1')
        //tsrList[1].getTSuiteTimestamp() == TSuiteTimestampImpl.newInstance('20180810_140106')
    }
    
    def testGetTSuiteResultsBeforeExclusive() {
        when:
        TSuiteName tsn = new TSuiteName('main/TS1')
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance('20180805_081908')
        List<TSuiteResult> tsrList = repoRoot_.getTSuiteResultsBeforeExclusive(tsn, tst)
        then:
        tsrList.size() == 3
        when:
        TSuiteTimestamp expectedTst = TSuiteTimestamp.newInstance('20180718_142832')
        then:
        tsrList[0].getId().getTSuiteTimestamp().equals(expectedTst)
    }
    
    def testGetTSuiteResultsBeforeExclusive_47News() {
        when:
        TSuiteName tsn = new TSuiteName('Montor47News')
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance('20190123_153854')
        List<TSuiteResult> tsrList = repoRoot_.getTSuiteResultsBeforeExclusive(tsn, tst)
        then:
        tsrList.size() == 0
    }
    
    def testGetSortedTSuiteResults() {
        when:
        RepositoryFileScanner scanner = new RepositoryFileScanner(workdir_)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        List<TSuiteResult> tSuiteResults = repoRoot.getSortedTSuiteResults()
        then:
        //tSuiteResults.size() == 9
        true
        when:
        def count = 0
        for (TSuiteResult tsr : tSuiteResults) {
            TSuiteTimestamp tst = tsr.getId().getTSuiteTimestamp()
            logger_.debug("#testGetTSuiteResultsSortedByTSuiteTimestampReverseOrder tst${count}=${tst}")
            count += 1
        }
        then:
        true
        when:
        logger_.debug("#testGetSortedTSuiteResults tSuiteResults[0] : ${tSuiteResults[0]}") // _/_
        logger_.debug("#testGetSortedTSuiteResults tSuiteResults[1] : ${tSuiteResults[1]}") // main.TS1 20180805_081908
        logger_.debug("#testGetSortedTSuiteResults tSuiteResults[2] : ${tSuiteResults[2]}") // main.TS1 20180718_142832
        TSuiteName tsn0 = tSuiteResults[0].getId().getTSuiteName()
        TSuiteName tsn1 = tSuiteResults[1].getId().getTSuiteName()
        TSuiteName tsn2 = tSuiteResults[2].getId().getTSuiteName()
        LocalDateTime ldt0 = tSuiteResults[0].getId().getTSuiteTimestamp().getValue()
        LocalDateTime ldt1 = tSuiteResults[1].getId().getTSuiteTimestamp().getValue()
        LocalDateTime ldt2 = tSuiteResults[2].getId().getTSuiteTimestamp().getValue()
        then:
        tsn0 <= tsn1  // _ == main.TS1
        tsn1 <= tsn2  // main.TS1 <= main.TS2
        ldt1 >= ldt2  // 20180805_081908 >= 20180718_142832
    }

    def testEquals() {
        when:
        RepositoryRoot thisRoot = new RepositoryRoot(workdir_)
        TSuiteResult tsr = TSuiteResult.newInstance(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        thisRoot.addTSuiteResult(tsr)
        //
        RepositoryRoot otherRoot = new RepositoryRoot(workdir_)
        TSuiteResult otherTsr = TSuiteResult.newInstance(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        otherRoot.addTSuiteResult(otherTsr)
        then:
        thisRoot == otherRoot
    }

    def testHashCode() {
        when:
        TSuiteResult tsr = TSuiteResult.newInstance(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        repoRoot_.addTSuiteResult(tsr)
        RepositoryRoot otherRoot = new RepositoryRoot(workdir_)
        TSuiteResult otherTsr = TSuiteResult.newInstance(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        otherRoot.addTSuiteResult(otherTsr)
        then:
        repoRoot_.hashCode() == otherRoot.hashCode()
    }

    def testToJson() {
        when:
        TSuiteResult tsr = TSuiteResult.newInstance(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        repoRoot_.addTSuiteResult(tsr)
        logger_.debug("#testToJson ${JsonOutput.prettyPrint(repoRoot_.toJson())}")
        then:
        true
    }

    def testToString() {
        when:
        TSuiteResult tsr = TSuiteResult.newInstance(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        repoRoot_.addTSuiteResult(tsr)
        logger_.debug("#testToString ${JsonOutput.prettyPrint(repoRoot_.toString())}")
        then:
        true
    }

    /*
    def testToBootstrapTreeviewData() {
        when:
        RepositoryFileScanner scanner = new RepositoryFileScanner(workdir_)
        scanner.scan()
        RepositoryRoot rr = scanner.getRepositoryRoot()
        def s = rr.toBootstrapTreeviewData()
        logger_.debug("#testToBootstrapTreeviewData ${JsonOutput.prettyPrint(s)}")
        then:
        s.contains('text')
        s.contains('nodes')
    }
    */

    /*
    def testHtmlFragmensOfMaterialsAsModal() {
        when:
        RepositoryFileScanner scanner = new RepositoryFileScanner(workdir_)
        scanner.scan()
        RepositoryRoot rr = scanner.getRepositoryRoot()
        def html = rr.htmlFragmensOfMaterialsAsModal()
        logger_.debug("#testHtmlFragmentsOfMaterialsAsModal html=\n${html}")
        then:
        html.contains('<div')
    }
    */

}
