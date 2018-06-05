package com.kazurayam.kstestresults

import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TestSuiteResultSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Screenshots")
    private TestResultsImpl wtrs

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TestSuiteResultSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
        wtrs = new TestResultsImpl(workdir, new TestSuiteName('TS1'))

    }
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testFindOrNewTestCaseResult() {
        when:
        TestSuiteResult tsr = wtrs.getCurrentTestSuiteResult()
        TestCaseResult tcr = tsr.findOrNewTestCaseResult(new TestCaseName('TC1'))
        then:
        tcr != null
        tcr.getTestCaseName() == new TestCaseName('TC1')
        when:
        TargetPage tp = tcr.findOrNewTargetPage(new URL('http://demoaut.katalon.com/'))
        then:
        tp != null
        when:
        ScreenshotWrapper sw = tp.findOrNewScreenshotWrapper('')
        then:
        sw != null
    }

    def testToJson() {
        setup:
        TestSuiteResult tsr = wtrs.getCurrentTestSuiteResult()
        when:
        TestCaseResult tcr = tsr.findOrNewTestCaseResult(new TestCaseName('TC1'))
        TargetPage tp = tcr.findOrNewTargetPage(new URL('http://demoaut.katalon.com/'))
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
