package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.Specification

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

    def testConstructor_Path_tsId() {
        setup:
        String dirName = 'testConstructor_Path_tsId'
        String tsId = 'TS0'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        ScreenshotRepository sr = new ScreenshotRepository(baseDir, tsId)
        when:
        Path actual = sr.getBaseDir()
        then:
        actual == baseDir
        sr.getCurrentTestSuiteId() == tsId
    }

    def testConstructor_PathString_tsId() {
        setup:
        String dirName = 'testConstructor_PathString_tsId'
        String tsId = 'TS1'
        Path baseDir = Paths.get(System.getProperty('user.dir')).resolve(dirName)
        Helpers.ensureDirs(baseDir)
        ScreenshotRepository sr = new ScreenshotRepository(dirName, tsId)
        when:
        Path actual = sr.getBaseDir()
        then:
        actual == baseDir
        sr.getCurrentTestSuiteId() == tsId
        cleanup:
        Helpers.deleteDirectory(baseDir)
    }

    def testResolveScreenshotFilePath() {
        setup:
        String dirName = 'testResolveScreenshotFilePath'
        String tsId = 'TS5'
        String tcId = 'TC5'
        String url = 'http://demoauto.katalon.com/'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        ScreenshotRepository sr = new ScreenshotRepository(baseDir, tsId)
        sr.setCurrentTestCaseId(tcId)
        when:
        Path p = sr.resolveScreenshotFilePath(url)
        then:
        p != null
        assert p.endsWith("${URLEncoder.encode(url, 'UTF-8')}.png")
    }

    def testLoadTree() {
        setup:
        Path fixture = Paths.get("./src/test/fixture/Screenshots")
        String dirName = 'testResolveScreenshotFilePath'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
        when:
        Map<String, Map<TSTimestamp, TestSuiteResult>> tree = ScreenshotRepository.loadTree(baseDir)
        then:
        tree.size() == 2
    }

}

