package com.kazurayam.kstestresults

import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TsResultSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Screenshots")
    private TestResultsImpl wtrs

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TsResultSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
        wtrs = new TestResultsImpl(workdir, new TsName('TS1'))

    }
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testFindOrNewTestCaseResult() {
        when:
        TsResult tsr = wtrs.getCurrentTestSuiteResult()
        TcResult tcr = tsr.findOrNewTestCaseResult(new TcName('TC1'))
        then:
        tcr != null
        tcr.getTestCaseName() == new TcName('TC1')
        when:
        TargetURL tp = tcr.findOrNewTargetPage(new URL('http://demoaut.katalon.com/'))
        then:
        tp != null
        when:
        ScreenshotWrapper sw = tp.findOrNewScreenshotWrapper('')
        then:
        sw != null
    }

    def testToJson() {
        setup:
        TsResult tsr = wtrs.getCurrentTestSuiteResult()
        when:
        TcResult tcr = tsr.findOrNewTestCaseResult(new TcName('TC1'))
        TargetURL tp = tcr.findOrNewTargetPage(new URL('http://demoaut.katalon.com/'))
        ScreenshotWrapper sw = tp.findOrNewScreenshotWrapper('')
        def str = tsr.toString()
        System.err.println("${str}")
        System.out.println("${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"TestSuiteResult":{')
        str.contains('testSuiteName')
        str.contains('TS1')
        str.contains('testCaseName')
        str.contains('TC1')
        str.contains(Helpers.escapeAsJsonText('http://demoaut.katalon.com/'))
        str.endsWith('}}')
    }

    // helper methods
}
