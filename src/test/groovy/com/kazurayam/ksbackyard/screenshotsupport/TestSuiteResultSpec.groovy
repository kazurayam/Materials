package com.kazurayam.ksbackyard.screenshotsupport

import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

@Ignore
class TestSuiteResultSpec extends Specification {

    // fields
    private static Path workdir

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TestSuiteResultSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testFindOrNewTestCaseResult() {
        setup:
        Path fixture = Paths.get("./src/test/fixture/Screenshots")
        String dirName = 'testGetTestCaseResult'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
        ScreenshotRepository sr = new ScreenshotRepositoryImpl(baseDir, new TestSuiteName('TS1'))
        when:
        TestSuiteResult tsr = sr.getCurrentTestSuiteResult()
        TestCaseResult tcr = tsr.findOrNewTestCaseResult(new TestCaseName('TC1'))
        then:
        tcr != null
        tcr.getTestCaseName() == new TestCaseName('TC1')

    }

    // helper methods
}
