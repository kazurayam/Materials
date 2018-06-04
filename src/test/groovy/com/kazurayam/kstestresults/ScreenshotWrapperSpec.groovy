package com.kazurayam.kstestresults

import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class ScreenshotWrapperSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Screenshots")

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(ScreenshotWrapperSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }

    }

    // feature methods
    def testToString() {
        setup:
        String dirName = 'testToString'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
        TestSuiteName tsn = new TestSuiteName('TS1')
        TestCaseName tcn = new TestCaseName('TC1')
        TestSuiteTimestamp tstamp = new TestSuiteTimestamp('20180530_130419')
        TestResultsImpl wtrs = new TestResultsImpl(baseDir, tsn)
        TestSuiteResult tsr = wtrs.getTestSuiteResult(tsn, tstamp)
        TestCaseResult tcr = tsr.findOrNewTestCaseResult(tcn)
        assert tcr != null
        TargetPage tp = tcr.findOrNewTargetPage(new URL('http://demoaut.katalon.com/'))
        when:
        ScreenshotWrapper sw = tp.findOrNewScreenshotWrapper('')
        def str = sw.toString()
        System.out.println("#testToString:\n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"ScreenshotWrapper":{"screenshotFilePath":"')
        str.contains(Helpers.escapeAsJsonText(sw.getScreenshotFilePath().toString()))
        str.endsWith('"}}')
    }
}
