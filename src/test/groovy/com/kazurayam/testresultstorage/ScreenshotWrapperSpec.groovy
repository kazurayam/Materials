package com.kazurayam.testresultstorage

import java.nio.file.Path
import java.nio.file.Paths

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
        when:
        ScreenshotRepositoryImpl sr = new ScreenshotRepositoryImpl(baseDir, new TestSuiteName('TS1'))
        TestSuiteResult tsr = sr.getTestSuiteResult(
                new TestSuiteName('TS1'), new TestSuiteTimestamp('20180530_130419'))
        TestCaseResult tcr = tsr.getTestCaseResult(new TestCaseName('TC1'))
        assert tcr != null
        TargetPage tp = tcr.getTargetPage(new URL('http://demoaut.katalon.com/'))
        ScreenshotWrapper sw = tp.getScreenshotWrapper('')
        then:
        sw.toString().startsWith('{"ScreenshotWrapper":{"screenshotFilePath":"')
        sw.toString().contains(sw.getScreenshotFilePath().toString())
        sw.toString().endsWith('"}}')
    }
}
