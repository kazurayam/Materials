package com.kazurayam.kstestresults

import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TestResultsImplSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Screenshots")

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TestResultsImplSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
    }
    def cleanupSpec() {}

    // feature methods
    def testScan() {
        setup:
        Helpers.copyDirectory(fixture, workdir)
        when:
        List<TestSuiteResult> tsrList = TestResultsImpl.scan(workdir)
        then:
        tsrList != null
        tsrList.size() == 2
        when:
        TestSuiteResult tsr =
                lookupTestSuiteResult(tsrList, new TestSuiteName('TS1'),
                        new TestSuiteTimestamp('20180530_130419'))
        then:
        tsr != null
        tsr.getBaseDir() == workdir
        tsr.getTestSuiteName() == new TestSuiteName('TS1')
        tsr.getTestSuiteTimestamp() == new TestSuiteTimestamp('20180530_130419')
        tsr.getTestSuiteTimestampDir() == workdir.resolve('TS1/20180530_130419')
        when:
        TestCaseName tcn = new TestCaseName('TC1')
        TestCaseResult tcr = tsr.getTestCaseResult(tcn)
        then:
        tcr != null
        tcr.getParentTestSuiteResult() == tsr
        tcr.getTestCaseName() == tcn
        tcr.getTestCaseDir() == tsr.getTestSuiteTimestampDir().resolve('TC1')
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

    def testToJson() {
        setup:
        TestResultsImpl tri = new TestResultsImpl(workdir, new TestSuiteName('TS1'))
        when:
        def str = tri.toJson()
        System.err.println("str=\n${str}")
        System.out.println("str=\n${JsonOutput.prettyPrint(str)}")
        then:
        str != null
        str.contains('{"TestResultsImpl":{')
        str.contains(Helpers.escapeAsJsonText(workdir.toString()))
        // TODO
        str.contains('}}')
    }

    // helper methods
    TestSuiteResult lookupTestSuiteResult(List<TestSuiteResult> tsrList, TestSuiteName tsn, TestSuiteTimestamp tst) {
        for (TestSuiteResult tsr : tsrList ) {
            if (tsr.getTestSuiteName() == tsn && tsr.getTestSuiteTimestamp() == tst) {
                return tsr
            }
        }
        return null
    }
}
