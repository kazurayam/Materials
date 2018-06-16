package com.kazurayam.carmina

import static groovy.json.JsonOutput.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

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

    def testSetCurrentTestSuite_oneStringArg() {
        setup:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        when:
        trr.setCurrentTestSuite('oneStringArg')
        Path timestampdir = trr.getCurrentTestSuiteDirectory()
        logger_.debug("#testSetCurrentTestSuite_oneStringArg timestampdir=${timestampdir}")
        then:
        timestampdir.toString().contains('oneStringArg')
        when:
        String dirName = timestampdir.getFileName()
        LocalDateTime ldt = TSuiteTimestamp.parse(dirName)
        then:
        true
    }

    def testSetCurrentTestSuite_twoStringArg() {
        setup:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        when:
        trr.setCurrentTestSuite('oneStringArg', '20180616_160000')
        Path timestampdir = trr.getCurrentTestSuiteDirectory()
        logger_.debug("#testSetCurrentTestSuite_oneStringArg timestampdir=${timestampdir}")
        then:
        timestampdir.toString().contains('oneStringArg')
        timestampdir.getFileName().toString().contains('20180616_160000')
    }

    def testSetCurrentTestSuite_tSuiteName_tSuiteTimestamp() {
        setup:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        when:
        trr.setCurrentTestSuite(
                new TSuiteName('oneStringArg'),
                new TSuiteTimestamp('20180616_160000'))
        Path timestampdir = trr.getCurrentTestSuiteDirectory()
        logger_.debug("#testSetCurrentTestSuite_oneStringArg timestampdir=${timestampdir}")
        then:
        timestampdir.toString().contains('oneStringArg')
        timestampdir.getFileName().toString().contains('20180616_160000')
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
        //logger_.debug(JsonOutput.prettyPrint(str))
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

    def testResolveMaterial_png() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('TS1','20180530_130419')
        Path png = trr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.PNG)
        then:
        png != null
        png.toString().contains('TC1')
        png.toString().contains('demoaut.katalon.com')
        png.toString().contains('png')
    }

    def testResolveMaterial_json() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('TS1','20180530_130419')
        Path json = trr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.JSON)
        then:
        json != null
        json.toString().contains('TC1')
        json.toString().contains('demoaut.katalon.com')
        json.toString().contains('json')
    }

    def testResolveMaterial_xml() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('TS1','20180530_130419')
        Path xml = trr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', '1', FileType.XML)
        then:
        xml != null
        xml.toString().contains('TC1')
        xml.toString().contains('demoaut.katalon.com')
        xml.toString().contains('xml')
    }

    def testResolveMaterial_pdf() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('TS1','20180530_130419')
        Path pdf = trr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.PDF)
        then:
        pdf != null
        pdf.toString().contains('TC1')
        pdf.toString().contains('demoaut.katalon.com')
        pdf.toString().contains('pdf')
    }

    def testResolveMaterial_txt() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('TS1','20180530_130419')
        Path txt = trr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', 'x', FileType.TXT)
        then:
        txt != null
        txt.toString().contains('TC1')
        txt.toString().contains('demoaut.katalon.com')
        txt.toString().contains('txt')
    }
    
    def testResolveMaterial_xls() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('TS1','20180530_130419')
        Path xls = trr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.XLS)
        then:
        xls != null
        xls.toString().contains('TC1')
        xls.toString().contains('demoaut.katalon.com')
        xls.toString().contains('xls')
    }

    def testResolveMaterial_xlsx() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('TS1','20180530_130419')
        Path xls = trr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.XLSX)
        then:
        xls != null
        xls.toString().contains('TC1')
        xls.toString().contains('demoaut.katalon.com')
        xls.toString().contains('xlsx')
    }

    def testResolveMaterial_xlsm() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('TS1','20180530_130419')
        Path xls = trr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.XLSM)
        then:
        xls != null
        xls.toString().contains('TC1')
        xls.toString().contains('demoaut.katalon.com')
        xls.toString().contains('xlsm')
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

