package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TCaseResultSpec extends Specification {

    static Logger logger = LoggerFactory.getLogger(TCaseResultSpec.class);

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")
    private static RepositoryScanner scanner

    // fixture methods
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TCaseResultSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
    }
    def setup() {
        scanner = new RepositoryScanner(workdir)
        scanner.scan()
    }
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testSetParent_GetParent() {
        when:
        TSuiteResult tsr = scanner.getTSuiteResult(new TSuiteName('TS1'),
                new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = new TCaseResult(new TCaseName('TC2'))
        TCaseResult modified = tcr.setParent(tsr)
        then:
        modified.getParent() == tsr
    }

    def testToJson() {
        setup:
        TSuiteResult tsr = scanner.getTSuiteResult(new TSuiteName('TS1'),
                new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        TargetURL targetURL = tcr.getTargetURL(new URL('http://demoaut.katalon.com/'))
        Material mw = targetURL.getMaterials().get(0)
        when:
        def str = tcr.toString()
        logger.debug("#testToString: \n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"TCaseResult":{')
        str.contains('tCaseName')
        str.contains('TC1')
        str.contains('tCaseDir')
        str.contains(Helpers.escapeAsJsonText( mw.getMaterialFilePath().toString()))
        str.contains('tCaseStatus')
        str.contains(TestCaseStatus.TO_BE_EXECUTED.toString())
        str.endsWith('}}')
    }



    // helper methods
}
