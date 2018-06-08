package com.kazurayam.carmina

import static groovy.json.JsonOutput.*

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.carmina.FileType
import com.kazurayam.carmina.Helpers
import com.kazurayam.carmina.TestResultsRepository
import com.kazurayam.carmina.TestResultsRepositoryFactory

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TestResultsRepositorySpec extends Specification {

    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")

    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TestResultsRepositorySpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
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

    def testToJson() {
        when:
        TestResultsRepository sr = TestResultsRepositoryFactory.createInstance(workdir, 'Test Suites/TS1')
        def str = JsonOutput.prettyPrint(sr.toString())
        System.out.println(JsonOutput.prettyPrint(str))
        then:
        str.contains('TS1')
    }

    def testConstructor_Path_tsn() {
        when:
        TestResultsRepository sr = TestResultsRepositoryFactory.createInstance(workdir, 'Test Suites/TS1')
        String str = sr.toString()
        then:
        str.contains('TS1')
    }

    def testResolveScreenshotFilePath() {
        when:
        TestResultsRepository sr = TestResultsRepositoryFactory.createInstance(workdir, 'Test Suites/TS1')
        Path scfp = sr.resolveMaterialFilePath('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.PNG)
        then:
        scfp != null
        scfp.toString().contains('TC1')
        scfp.toString().contains('demoaut.katalon.com')
    }

}

