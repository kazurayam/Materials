package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class ScreenshotRepositorySpec extends Specification {

    private static Path workdir

    def setupSpec() {
        workdir = Paths.get("./build/tmp/${ScreenshotRepositorySpec.getName()}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }

    def testConstructor_Path() {
        setup:
        String dirName = 'testConstructor_Path'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        ScreenshotRepository sr = new ScreenshotRepository(baseDir)
        when:
        Path actual = sr.getBaseDir()
        then:
        actual == baseDir
    }

    def testConstructor_PathString() {
        setup:
        String dirName = 'testConstructor_PathString'
        Path baseDir = Paths.get(System.getProperty('user.dir')).resolve(dirName)
        Helpers.ensureDirs(baseDir)
        ScreenshotRepository sr = new ScreenshotRepository(dirName)
        when:
        Path actual = sr.getBaseDir()
        then:
        actual == baseDir
        cleanup:
        Helpers.deleteDirectory(baseDir)
    }

    def testConstructor_Path_tsn() {
        setup:
        String dirName = 'testConstructor_Path_tsId'
        TestSuiteName tsn = new TestSuiteName('TS0')
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        ScreenshotRepository sr = new ScreenshotRepository(baseDir, tsn)
        when:
        Path actual = sr.getBaseDir()
        then:
        actual == baseDir
        sr.getCurrentTestSuiteName() == tsn
    }

    def testConstructor_PathString_tsn() {
        setup:
        String dirName = 'testConstructor_PathString_tsId'
        TestSuiteName tsn = new TestSuiteName('TS1')
        Path baseDir = Paths.get(System.getProperty('user.dir')).resolve(dirName)
        Helpers.ensureDirs(baseDir)
        ScreenshotRepository sr = new ScreenshotRepository(baseDir, tsn)
        when:
        Path actual = sr.getBaseDir()
        then:
        actual == baseDir
        sr.getCurrentTestSuiteName() == tsn
        cleanup:
        Helpers.deleteDirectory(baseDir)
    }

    def testResolveScreenshotFilePath() {
        setup:
        String dirName = 'testResolveScreenshotFilePath'
        Path baseDir = workdir.resolve(dirName)
        TestSuiteName tsn = new TestSuiteName('TS5')
        TestCaseName tcn = new TestCaseName('TC5')
        URL url = new URL('http://demoauto.katalon.com/')
        Helpers.ensureDirs(baseDir)
        when:
        ScreenshotRepository sr = new ScreenshotRepository(baseDir, tsn)
        TestCaseResult tcr = sr.getCurrentTestSuiteResult().findOrNewTestCaseResult(tcn)
        then:
        tcr != null
        tcr.getTestCaseName() == tcn
        when:
        TargetPage tp = tcr.findOrNewTargetPage(url)
        then:
        tp.getUrl() == url
        when:
        ScreenshotWrapper sw = tp.uniqueScreenshotWrapper()
        then:
        sw != null
    }

    @Ignore
    def testLoadTree() {
        setup:
        Path fixture = Paths.get("./src/test/fixture/Screenshots")
        String dirName = 'testResolveScreenshotFilePath'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
        when:
        Map<String, Map<TestSuiteTimestamp, TestSuiteResult>> tree = ScreenshotRepository.loadTree(baseDir)
        then:
        tree.size() == 2
    }

}

