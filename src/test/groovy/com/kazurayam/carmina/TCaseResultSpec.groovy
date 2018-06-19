package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TCaseResultSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(TCaseResultSpec.class);

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")
    private static RepositoryScanner scanner

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(TCaseResultSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }
    def setup() {
        scanner = new RepositoryScanner(workdir_)
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
        TargetURL tu = tcr.getTargetURL(new URL('http://demoaut.katalon.com/'))
        Material mate = tu.getMaterials().get(0)
        when:
        def str = tcr.toString()
        logger_.debug("#testToString: \n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"TCaseResult":{')
        str.contains('tCaseName')
        str.contains('TC1')
        str.contains('tCaseDir')
        str.contains(Helpers.escapeAsJsonText( mate.getMaterialFilePath().toString()))
        str.endsWith('}}')
    }

    def testToBootstrapTreeviewData() {
        setup:
        TSuiteResult tsr = scanner.getTSuiteResult(new TSuiteName('TS1'),
                new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        when:
        def str = tcr.toBootstrapTreeviewData()
        logger_.debug("#testToBootstrapTreeviewData: \n${JsonOutput.prettyPrint(str)}")
        then:
        str.contains('text')
        str.contains('nodes')
    }



    // helper methods
}
