package com.kazurayam.ksbackyard.screenshotsupport

import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

@Ignore
class ScreenshotWrapperSpec extends Specification {

    // fields
    private static Path workdir

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${ScreenshotWrapperSpec.getName()}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }

    }

    // feature methods
    def testToString() {
        setup:
        Path fixture = Paths.get("./src/test/fixture/Screenshots")
        ScreenshotRepository sr = new ScreenshotRepositoryImpl(fixture, new TestSuiteName('TS1'))
        when:
        TestSuiteResult tsr = sr.getCurrentTestSuiteResult()
        TestCaseResult tcr = tsr.getTestCaseResult(new TestCaseName('TC1'))
        assert tcr != null
        TargetPage tp = tcr.getTargetPage(new URL('http://demoaut.katalon.com/'))
        ScreenshotWrapper sw = tp.getScreenshotWrapper('')
        then:
        sw.toString().startsWith('{¥"ScreenshotWrapper¥":[{¥"screenshotFilePath¥":¥"')
        sw.toString().contains(sw.getScreenshotFilePath())
        sw.toString().endsWith('¥"}]}')

    }
}
