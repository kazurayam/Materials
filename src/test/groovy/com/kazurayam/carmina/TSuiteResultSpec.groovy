package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TSuiteResultSpec extends Specification {

    static Logger logger = LoggerFactory.getLogger(TSuiteResultSpec.class);

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")
    private TestResultsRepositoryImpl trri

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TSuiteResultSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
        trri = new TestResultsRepositoryImpl(workdir, new TSuiteName('TS1'))

    }
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testSetParent_getParent() {
        when:
        TSuiteResult tsr = new TSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp(LocalDateTime.now()))
        TSuiteResult modified = tsr.setParent(workdir)
        then:
        modified.getParent() == workdir

    }

    def testFindOrNewTcResult() {
        when:
        TSuiteResult tsr = trri.getCurrentTSuiteResult()
        TCaseResult tcr = tsr.findOrNewTCaseResult(new TCaseName('TC1'))
        then:
        tcr != null
        tcr.getTCaseName() == new TCaseName('TC1')
        when:
        TargetURL tu = tcr.findOrNewTargetURL(new URL('http://demoaut.katalon.com/'))
        then:
        tu != null
        when:
        MaterialWrapper mw = tu.findOrNewMaterialWrapper('', FileType.PNG)
        then:
        mw != null
    }

    def testToJson() {
        setup:
        TSuiteResult tsr = trri.getCurrentTSuiteResult()
        when:
        TCaseResult tcr = tsr.findOrNewTCaseResult(new TCaseName('TC1'))
        TargetURL tu = tcr.findOrNewTargetURL(new URL('http://demoaut.katalon.com/'))
        MaterialWrapper sw = tu.findOrNewMaterialWrapper('', FileType.PNG)
        def s = tsr.toString()
        logger.debug("#testToJson ${s}")
        logger.debug("#testToJson ${JsonOutput.prettyPrint(s)}")
        then:
        s.startsWith('{"TSuiteResult":{')
        s.contains('tSuiteName')
        s.contains('TS1')
        s.contains('tCaseName')
        s.contains('TC1')
        s.contains(Helpers.escapeAsJsonText('http://demoaut.katalon.com/'))
        s.endsWith('}}')
    }

    // helper methods
}
