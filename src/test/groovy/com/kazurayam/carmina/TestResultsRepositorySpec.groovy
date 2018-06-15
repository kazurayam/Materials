package com.kazurayam.carmina

import static groovy.json.JsonOutput.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TestResultsRepositorySpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(TestResultsRepositorySpec.class);

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Results")

    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(TestResultsRepositorySpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
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
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('Test Suites/TS1')
        def str = JsonOutput.prettyPrint(trr.toString())
        logger_.debug(JsonOutput.prettyPrint(str))
        then:
        str.contains('TS1')
    }

    def testConstructor_Path_tsn() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('Test Suites/TS1')
        String str = trr.toString()
        then:
        str.contains('TS1')
    }

    def testResolvePngFilePath() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('TS1','20180530_130419')
        Path png = trr.resolvePngFilePath('Test Cases/TC1', 'http://demoaut.katalon.com/')
        then:
        png != null
        png.toString().contains('TC1')
        png.toString().contains('demoaut.katalon.com')
    }

    def testResolveJsonFilePath() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('TS1','20180530_130419')
        Path json = trr.resolveJsonFilePath('Test Cases/TC1', 'http://demoaut.katalon.com/')
        then:
        json != null
        json.toString().contains('TC1')
        json.toString().contains('demoaut.katalon.com')
    }

    def testResolveXmlFilePath() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('TS1','20180530_130419')
        Path xml = trr.resolveXmlFilePath('Test Cases/TC1', 'http://demoaut.katalon.com/')
        then:
        xml != null
        xml.toString().contains('TC1')
        xml.toString().contains('demoaut.katalon.com')
    }

    def testResolvePdfFilePath() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('TS1','20180530_130419')
        Path pdf = trr.resolvePdfFilePath('Test Cases/TC1', 'http://demoaut.katalon.com/')
        then:
        pdf != null
        pdf.toString().contains('TC1')
        pdf.toString().contains('demoaut.katalon.com')
    }

    def testResolveTxtFilePath() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('TS1','20180530_130419')
        Path txt = trr.resolvePdfFilePath('Test Cases/TC1', 'http://demoaut.katalon.com/')
        then:
        txt != null
        txt.toString().contains('TC1')
        txt.toString().contains('demoaut.katalon.com')
    }

    def testGetCurrentTestSuiteDirectory() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('Test Suites/TS1','20180530_130419')
        Path testSuiteDir = trr.getCurrentTestSuiteDirectory()
        then:
        testSuiteDir == workdir_.resolve('TS1/20180530_130419')
    }

    def testGetTestCaseDirectory() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('Test Suites/TS1','20180530_130419')
        Path testCaseDir = trr.getTestCaseDirectory('Test Cases/TC1')
        then:
        testCaseDir == workdir_.resolve('TS1/20180530_130419/TC1')
    }

    def testSetTestCaseStatus() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('Test Suites/TS1','20180530_130419')
        trr.setTestCaseStatus('Test Cases/TC1','PASSED')
        TestResultsRepositoryImpl trri = (TestResultsRepositoryImpl)trr
        TCaseResult tcr = trri.getTCaseResult(new TCaseName('Test Cases/TC1'))
        then:
        tcr.getTestCaseStatus() == TCaseStatus.PASSED
    }

    def testReport() {
        setup:
        Path dir = workdir_.resolve('testReport')
        Helpers.ensureDirs(dir)
        Helpers.copyDirectory(fixture_, dir)
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(dir)
        when:
        trr.setCurrentTestSuite('Test Suites/TS1','20180530_130419')
        trr.report()
        then:
        Files.exists(trr.getCurrentTestSuiteDirectory().resolve('Result.html'))
    }
}

