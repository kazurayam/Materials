package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TSuiteResultSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(TSuiteResultSpec.class);

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")
    private static TestMaterialsRepositoryImpl tmri_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(TSuiteResultSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        tmri_ = new TestMaterialsRepositoryImpl(workdir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testSetParent_getParent() {
        when:
        TSuiteResult tsr = new TSuiteResult(new TSuiteName('TS3'),
                new TSuiteTimestamp(LocalDateTime.now()))
        TSuiteResult modified = tsr.setParent(workdir_)
        then:
        modified.getParent() == workdir_
    }

    def testGetTSuiteName() {
        when:
        TSuiteResult tsr = tmri_.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        then:
        tsr.getTSuiteName() == new TSuiteName('TS1')
    }

    def testGetTSuiteTimestamp() {
        when:
        TSuiteResult tsr = tmri_.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        then:
        tsr.getTSuiteTimestamp() == new TSuiteTimestamp('20180530_130419')
    }

    def testGetTCaseResult() {
        when:
        TSuiteResult tsr = tmri_.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        then:
        tcr != null
        tcr.getTCaseName() == new TCaseName('TC1')
        tcr.getParent() == tsr
    }

    def testGetTCaseResults() {
        when:
        TSuiteResult tsr = tmri_.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
        List<TCaseResult> tCaseResults = tsr.getTCaseResults()
        then:
        tCaseResults != null
        tCaseResults.size() == 2
    }

    def testAddTCaseResult() {
        when:
        TSuiteResult tsr = tmri_.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
        TCaseResult tcr = new TCaseResult(new TCaseName('TSX')).setParent(tsr)
        tsr.addTCaseResult(tcr)
        TCaseResult tcr2 = tsr.getTCaseResult(new TCaseName('TSX'))
        then:
        tcr2 != null
        tcr2.getParent() == tsr
    }

    def testAddTCaseResult_parentIsNotSet() {
        when:
        TSuiteResult tsr = tmri_.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
        //TCaseResult tcr = new TCaseResult(new TCaseName('TSX')).setParent(tsr)
        TCaseResult tcr = new TCaseResult(new TCaseName('TSX'))
        tsr.addTCaseResult(tcr)
        then:
        thrown(IllegalArgumentException)
    }

    def testGetTSuiteTimestampDirectory() {
        when:
        TSuiteResult tsr = tmri_.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        Path dir = tsr.getTSuiteTimestampDirectory()
        then:
        dir.getFileName().toString() == '20180530_130419'
    }

    def testEquals() {
        when:
        TSuiteResult tsr = tmri_.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        TSuiteResult other = new TSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419')).setParent(workdir_)
        then:
        tsr == other
        when:
        TSuiteResult more = new TSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604')).setParent(workdir_)
        then:
        tsr != more
    }

    def testHashCode() {
        when:
        TSuiteResult tsr = tmri_.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        TSuiteResult other = new TSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419')).setParent(workdir_)
        then:
        tsr.hashCode() == other.hashCode()
    }

    def testToJson() {
        setup:
        TSuiteResult tsr = tmri_.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        when:
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        def s = tsr.toString()
        logger_.debug("#testToJson ${s}")
        logger_.debug("#testToJson ${JsonOutput.prettyPrint(s)}")
        then:
        s.startsWith('{"TSuiteResult":{')
        s.contains('tSuiteName')
        s.contains('TS1')
        s.contains('tCaseName')
        s.contains('TC1')
        s.contains(Helpers.escapeAsJsonText('http://demoaut.katalon.com/'))
        s.endsWith('}}')
    }

    def testToBootstrapTreeviewData() {
        setup:
        TSuiteResult tsr = tmri_.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        when:
        def s = tsr.toBootstrapTreeviewData()
        logger_.debug("#testToBootstrapTreeviewData ${JsonOutput.prettyPrint(s)}")
        then:
        s.contains('text')
        s.contains('nodes')
    }


    // helper methods
}
