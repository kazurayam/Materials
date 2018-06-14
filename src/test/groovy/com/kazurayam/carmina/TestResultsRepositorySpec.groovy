package com.kazurayam.carmina

import static groovy.json.JsonOutput.*

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TestResultsRepositorySpec extends Specification {

    static Logger logger = LoggerFactory.getLogger(TestResultsRepositorySpec.class);

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
        logger.debug(JsonOutput.prettyPrint(str))
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

    def testResolvePngFilePath() {
        when:
        TestResultsRepository sr = TestResultsRepositoryFactory.createInstance(
                workdir,
                new TSuiteName('TS1'),
                new TSuiteTimestamp('20180530_130419')
        )
        Path scfp = sr.resolvePngFilePath('Test Cases/TC1', 'http://demoaut.katalon.com/')
        then:
        scfp != null
        scfp.toString().contains('TC1')
        scfp.toString().contains('demoaut.katalon.com')
    }
    
    def testResolveJsonFilePath() {
        when:
        TestResultsRepository sr = TestResultsRepositoryFactory.createInstance(
                workdir,
                new TSuiteName('TS1'),
                new TSuiteTimestamp('20180530_130419')
        )
        Path scfp = sr.resolveJsonFilePath('Test Cases/TC1', 'http://demoaut.katalon.com/')
        then:
        scfp != null
        scfp.toString().contains('TC1')
        scfp.toString().contains('demoaut.katalon.com')
    }
    
    def testResolveXmlFilePath() {
        when:
        TestResultsRepository sr = TestResultsRepositoryFactory.createInstance(
                workdir,
                new TSuiteName('TS1'),
                new TSuiteTimestamp('20180530_130419')
        )
        Path scfp = sr.resolveXmlFilePath('Test Cases/TC1', 'http://demoaut.katalon.com/')
        then:
        scfp != null
        scfp.toString().contains('TC1')
        scfp.toString().contains('demoaut.katalon.com')
    }
    
    def testResolvePdfFilePath() {
        when:
        TestResultsRepository sr = TestResultsRepositoryFactory.createInstance(
                workdir,
                new TSuiteName('TS1'),
                new TSuiteTimestamp('20180530_130419')
        )
        Path scfp = sr.resolvePdfFilePath('Test Cases/TC1', 'http://demoaut.katalon.com/')
        then:
        scfp != null
        scfp.toString().contains('TC1')
        scfp.toString().contains('demoaut.katalon.com')
    }
}

