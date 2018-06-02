package com.kazurayam.ksbackyard.screenshotsupport

import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class ScreenshotRepositoryImplSpec extends Specification {

    // fields
    private static Path workdir

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${ScreenshotRepositoryImplSpec.getName()}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testLoad() {
        setup:
        Path fixture = Paths.get("./src/test/fixture/Screenshots")
        String dirName = 'testLoad'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
        when:
        List<TestSuiteResult> tsrList = ScreenshotRepositoryImpl.load(baseDir)
        then:
        tsrList != null
        tsrList.size() == 2
        when:
        // Screenshots/TS1/20180530_130419/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png
        TestSuiteResult tsr0 = tsrList.get(0)
        then:
        tsr0.getBaseDir() == baseDir
        tsr0.getTestSuiteName() == new TestSuiteName('TS1')
        tsr0.getTestSuiteTimestamp() == new TestSuiteTimestamp(TestSuiteTimestamp.parse('20180530_130419'))
        tsr0.getTestSuiteTimestampDir() == baseDir.get('TS1/20180530_130419')
        when:
        TestCaseName tcn = new TestCaseName('TC1')
        TestCaseResult tcr = tsr0.getTestCaseResult(tcn)
        then:
        tcr != null
        tcr.getParentTestSuiteResult() == tsr0
        tcr.getTestCaseName() == tcn
        tcr.getTestCaseDir() == tsr0.getTestSuiteTimestampDir().resolve('TC1')
        tcr.getTestCaseStatus() == TestCaseStatus.TO_BE_EXECUTED
        when:
        TargetPage tp = tcr.getTargetPage(new URL('http://demoaut.katalon.com/'))
        then:
        tp != null
        when:
        Path imageFilePath = tcr.getTestCaseDir().resolve('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
        ScreenshotWrapper sw = tp.getScreenshotWrapper(imageFilePath)
        then:
        sw.getScreenshotFilePath() == imageFilePath
    }

    // helper methods
}
