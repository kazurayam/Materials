package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.Specification

class ScreenshotRepositorySpec extends Specification {

    private static Path workdir

    def setupSpec() {
        workdir = Paths.get("./build/tmp/${ScreenshotRepository.getName()}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }


    def testConstructor_Path() {
        setup:
        String dirName = 'testConstructor_Path'
        Path expected = workdir.resolve(dirName)
        ScreenshotRepository sr = new ScreenshotRepository(expected)
        when:
        Path actual = sr.getBaseDir()
        then:
        actual == expected
    }

    def testConstructor_PathString() {
        setup:
        String dirName = 'testConstructor_PathString'
        Path expected = Paths.get(System.getProperty('user.dir')).resolve(dirName)
        ScreenshotRepository sr = new ScreenshotRepository(dirName)
        when:
        Path actual = sr.getBaseDir()
        then:
        actual == expected
    }

    def testConstructor_Path_tsId() {
        setup:
        String dirName = 'testConstructor_Path_tsId'
        String tsId = 'TS0'
        Path expected = workdir.resolve(dirName)
        ScreenshotRepository sr = new ScreenshotRepository(expected, tsId)
        when:
        Path actual = sr.getBaseDir()
        then:
        actual == expected
        sr.getCurrentTestSuiteId() == tsId
    }

    def testConstructor_PathString_tsId() {
        setup:
        String dirName = 'testConstructor_PathString_tsId'
        String tsId = 'TS1'
        Path expected = Paths.get(System.getProperty('user.dir')).resolve(dirName)
        ScreenshotRepository sr = new ScreenshotRepository(dirName, tsId)
        when:
        Path actual = sr.getBaseDir()
        then:
        actual == expected
        sr.getCurrentTestSuiteId() == tsId
    }
}

