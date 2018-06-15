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

    static Logger logger_ = LoggerFactory.getLogger(TSuiteResultSpec.class);

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Results")
    private RepositoryScanner scanner_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(TSuiteResultSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }
    def setup() {
        scanner_ = new RepositoryScanner(workdir_)
        scanner_.scan()
    }
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

    def testToJson() {
        setup:
        TSuiteResult tsr = scanner_.getTSuiteResult(new TSuiteName('TS1'),
                new TSuiteTimestamp('20180530_130419'))
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

    // helper methods
}
