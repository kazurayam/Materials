package com.kazurayam.kstestresults

import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TestCaseResultSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Screenshots")
    private static TestResultsImpl tri
    private static TestSuiteResult tsr

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TestCaseResultSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
        tri = new TestResultsImpl(workdir, new TestSuiteName('TS1'))
        tsr = tri.getCurrentTestSuiteResult()
    }
    def cleanupSpec() {}

    // feature methods
    def testToJson() {
        setup:
        TestCaseResult tcr = tsr.findOrNewTestCaseResult(new TestCaseName('TC1'))
        TargetPage tp = tcr.findOrNewTargetPage(new URL('http://demoaut.katalon.com/'))
        ScreenshotWrapper sw = tp.findOrNewScreenshotWrapper('')
        when:
        def str = tcr.toString()
        def pretty = JsonOutput.prettyPrint(str)
        System.out.println("#testToString: \n${pretty}")
        then:
        str.startsWith('{"TestCaseResult":{')
        str.contains('testCaseName')
        str.contains('TC1')
        str.contains('testCaseDir')
        str.contains(Helpers.escapeAsJsonText( sw.getScreenshotFilePath().toString()))
        str.contains('testCaseStatus')
        str.contains(TestCaseStatus.TO_BE_EXECUTED.toString())
        str.endsWith('}}')
    }



    // helper methods
}
