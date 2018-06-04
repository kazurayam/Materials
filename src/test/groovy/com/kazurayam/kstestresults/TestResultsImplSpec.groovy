package com.kazurayam.kstestresults

import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.Specification

//@Ignore
class TestResultsImplSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Screenshots")

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TestResultsImplSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }

    def cleanup() {}

    def setupSpec() {}

    def cleanupSpec() {}

    // feature methods
    def testScan() {
        setup:
        String dirName = 'testScan'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
        when:
        List<TestSuiteResult> tsrList = TestResultsImpl.scan(baseDir)
        then:
        tsrList != null
        tsrList.size() == 2
        when:
        TestSuiteResult tsr0 =
                lookup(tsrList, new TestSuiteName('TS1'),
                        new TestSuiteTimestamp('20180530_130419'))
        then:
        tsr0 != null
        tsr0.getBaseDir() == baseDir
        tsr0.getTestSuiteName() == new TestSuiteName('TS1')
        tsr0.getTestSuiteTimestamp() == new TestSuiteTimestamp('20180530_130419')
        tsr0.getTestSuiteTimestampDir() == baseDir.resolve('TS1/20180530_130419')
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
        //System.out.println(prettyPrint("${sw}"))
        then:
        sw.getScreenshotFilePath() == imageFilePath
    }

    // helper methods
    TestSuiteResult lookup(List<TestSuiteResult> tsrList, TestSuiteName tsn, TestSuiteTimestamp tst) {
        for (TestSuiteResult tsr : tsrList ) {
            if (tsr.getTestSuiteName() == tsn && tsr.getTestSuiteTimestamp() == tst) {
                return tsr
            }
        }
        return null
    }
}
