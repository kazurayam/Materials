package com.kazurayam.ksbackyard.screenshotsupport

import groovy.json.JsonOutput
import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.Ignore
import spock.lang.Specification


class ScreenshotRepositorySpec extends Specification {

    private static Path workdir

    def setupSpec() {
        workdir = Paths.get("./build/tmp/${ScreenshotRepositorySpec.getName()}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }

    def testToString() {
        setup:
        String dirName = 'testToString'
        Path baseDir = workdir.resolve(dirName)
        when:
        ScreenshotRepository sr = ScreenshotRepositoryFactory.createInstance(baseDir, 'Test Suites/TS1')
        def prettyJson = JsonOutput.prettyPrint(sr.toString())
        System.out.println(prettyJson)
        then:
        prettyJson.contains(dirName)
        prettyJson.contains('TS1')
    }

    def testConstructor_Path_tsn() {
        setup:
        String dirName = 'testConstructor_Path_tsId'
        Path baseDir = workdir.resolve(dirName)
        when:
        ScreenshotRepository sr = ScreenshotRepositoryFactory.createInstance(baseDir, 'Test Suites/TS1')
        String str = sr.toString()
        then:
        str.contains(dirName)
        str.contains('TS1')
    }


    def testResolveScreenshotFilePath() {
        setup:
        String dirName = 'testResolveScreenshotFilePath'
        Path baseDir = workdir.resolve(dirName)
        when:
        ScreenshotRepository sr = ScreenshotRepositoryFactory.createInstance(baseDir, 'Test Suites/TS1')
        Path scfp = sr.resolveScreenshotFilePath('Test Cases/TC1', 'http://demoaut.katalon.com/')
        then:
        scfp != null
        scfp.toString().contains('http://demoaut.katalon.com/')
        scfp.toString().contains('TS1')
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
        Map<String, Map<TestSuiteTimestamp, TestSuiteResult>> tree = ScreenshotRepositoryImpl.loadTree(baseDir)
        then:
        tree.size() == 2
    }

}

