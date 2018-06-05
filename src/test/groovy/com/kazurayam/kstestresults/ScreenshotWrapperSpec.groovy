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
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(ScreenshotWrapperSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }

    // feature methods
    def testToJson() {
        setup:
        String dirName = 'testToString'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
        TsName tsn = new TsName('TS1')
        TcName tcn = new TcName('TC1')
        TsTimestamp tstamp = new TsTimestamp('20180530_130419')
        TestResultsImpl wtrs = new TestResultsImpl(baseDir, tsn)
        TsResult tsr = wtrs.getTestSuiteResult(tsn, tstamp)
        TcResult tcr = tsr.findOrNewTestCaseResult(tcn)
        assert tcr != null
        TargetURL tp = tcr.findOrNewTargetPage(new URL('http://demoaut.katalon.com/'))
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
