package com.kazurayam.webtestingresultstorage

import static groovy.json.JsonOutput.*

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.webtestingresultstorage.Helpers
import com.kazurayam.webtestingresultstorage.ScreenshotRepository
import com.kazurayam.webtestingresultstorage.ScreenshotRepositoryFactory

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class ScreenshotRepositorySpec extends Specification {

    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Screenshots")

    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(ScreenshotRepositorySpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }

    def testJsonOutput() {
        setup:
        List<String> errors = []
        errors << 'Your password was bad.'
        errors << 'Your feet smell.'
        errors << 'I am having a bad day.'
        when:
        def jsonString = toJson(errors)
        then:
        jsonString == '["Your password was bad.","Your feet smell.","I am having a bad day."]'
    }

    def testToString() {
        setup:
        String dirName = 'testToString'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
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
        String dirName = 'testConstructor_Path_tsn'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
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
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
        when:
        ScreenshotRepository sr = ScreenshotRepositoryFactory.createInstance(baseDir, 'Test Suites/TS1')
        Path scfp = sr.resolveScreenshotFilePath('Test Cases/TC1', 'http://demoaut.katalon.com/')
        then:
        scfp != null
        scfp.toString().contains('TC1')
        scfp.toString().contains('demoaut.katalon.com')
    }

}

