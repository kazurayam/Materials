package com.kazurayam.webtestingresultstorage

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.webtestingresultstorage.Helpers
import com.kazurayam.webtestingresultstorage.ScreenshotRepositoryImpl
import com.kazurayam.webtestingresultstorage.TestCaseName
import com.kazurayam.webtestingresultstorage.TestCaseResult
import com.kazurayam.webtestingresultstorage.TestSuiteName
import com.kazurayam.webtestingresultstorage.TestSuiteResult

import spock.lang.Specification

//@Ignore
class TestSuiteResultSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Screenshots")

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
        String dirName = 'testGetTestCaseResult'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
        ScreenshotRepositoryImpl sr = new ScreenshotRepositoryImpl(baseDir, new TestSuiteName('TS1'))
        when:
        TestSuiteResult tsr = sr.getCurrentTestSuiteResult()
        TestCaseResult tcr = tsr.findOrNewTestCaseResult(new TestCaseName('TC1'))
        then:
        tcr != null
        tcr.getTestCaseName() == new TestCaseName('TC1')

    }

    // helper methods
}
