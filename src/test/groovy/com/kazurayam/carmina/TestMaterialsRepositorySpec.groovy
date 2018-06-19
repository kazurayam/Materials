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
class TestMaterialsRepositorySpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(TestMaterialsRepositorySpec.class);

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")

    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(TestMaterialsRepositorySpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }

    def testSetCurrentTestSuite_oneStringArg() {
        setup:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        when:
        tmr.setCurrentTestSuite('oneStringArg')
        Path timestampdir = tmr.getCurrentTestSuiteDirectory()
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
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        when:
        tmr.setCurrentTestSuite('oneStringArg', '20180616_160000')
        Path timestampdir = tmr.getCurrentTestSuiteDirectory()
        logger_.debug("#testSetCurrentTestSuite_oneStringArg timestampdir=${timestampdir}")
        then:
        timestampdir.toString().contains('oneStringArg')
        timestampdir.getFileName().toString().contains('20180616_160000')
    }

    def testSetCurrentTestSuite_tSuiteName_tSuiteTimestamp() {
        setup:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        when:
        tmr.setCurrentTestSuite(
                new TSuiteName('oneStringArg'),
                new TSuiteTimestamp('20180616_160000'))
        Path timestampdir = tmr.getCurrentTestSuiteDirectory()
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
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.setCurrentTestSuite('Test Suites/TS1')
        def str = JsonOutput.prettyPrint(tmr.toString())
        //logger_.debug(JsonOutput.prettyPrint(str))
        then:
        str.contains('TS1')
    }

    def testConstructor_Path_tsn() {
        when:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.setCurrentTestSuite('Test Suites/TS1')
        String str = tmr.toString()
        then:
        str.contains('TS1')
    }

    def testResolveMaterial_png() {
        when:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.setCurrentTestSuite('TS1','20180530_130419')
        Path png = tmr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.PNG)
        then:
        png != null
        png.toString().contains('TC1')
        png.toString().contains('demoaut.katalon.com')
        png.toString().contains('png')
    }

    def testResolveMaterial_json() {
        when:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.setCurrentTestSuite('TS1','20180530_130419')
        Path json = tmr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.JSON)
        then:
        json != null
        json.toString().contains('TC1')
        json.toString().contains('demoaut.katalon.com')
        json.toString().contains('json')
    }

    def testResolveMaterial_xml() {
        when:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.setCurrentTestSuite('TS1','20180530_130419')
        Path xml = tmr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', '1', FileType.XML)
        then:
        xml != null
        xml.toString().contains('TC1')
        xml.toString().contains('demoaut.katalon.com')
        xml.toString().contains('xml')
    }

    def testResolveMaterial_pdf() {
        when:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.setCurrentTestSuite('TS1','20180530_130419')
        Path pdf = tmr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.PDF)
        then:
        pdf != null
        pdf.toString().contains('TC1')
        pdf.toString().contains('demoaut.katalon.com')
        pdf.toString().contains('pdf')
    }

    def testResolveMaterial_txt() {
        when:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.setCurrentTestSuite('TS1','20180530_130419')
        Path txt = tmr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', 'x', FileType.TXT)
        then:
        txt != null
        txt.toString().contains('TC1')
        txt.toString().contains('demoaut.katalon.com')
        txt.toString().contains('txt')
    }

    def testResolveMaterial_xls() {
        when:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.setCurrentTestSuite('TS1','20180530_130419')
        Path xls = tmr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.XLS)
        then:
        xls != null
        xls.toString().contains('TC1')
        xls.toString().contains('demoaut.katalon.com')
        xls.toString().contains('xls')
    }

    def testResolveMaterial_xlsx() {
        when:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.setCurrentTestSuite('TS1','20180530_130419')
        Path xls = tmr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.XLSX)
        then:
        xls != null
        xls.toString().contains('TC1')
        xls.toString().contains('demoaut.katalon.com')
        xls.toString().contains('xlsx')
    }

    def testResolveMaterial_xlsm() {
        when:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.setCurrentTestSuite('TS1','20180530_130419')
        Path xls = tmr.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.XLSM)
        then:
        xls != null
        xls.toString().contains('TC1')
        xls.toString().contains('demoaut.katalon.com')
        xls.toString().contains('xlsm')
    }


    def testGetCurrentTestSuiteDirectory() {
        when:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.setCurrentTestSuite('Test Suites/TS1','20180530_130419')
        Path testSuiteDir = tmr.getCurrentTestSuiteDirectory()
        then:
        testSuiteDir == workdir_.resolve('TS1/20180530_130419')
    }

    def testGetTestCaseDirectory() {
        when:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.setCurrentTestSuite('Test Suites/TS1','20180530_130419')
        Path testCaseDir = tmr.getTestCaseDirectory('Test Cases/TC1')
        then:
        testCaseDir == workdir_.resolve('TS1/20180530_130419/TC1')
    }

    def testSetTestCaseStatus() {
        when:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.setCurrentTestSuite('Test Suites/TS1','20180530_130419')
        tmr.setTestCaseStatus('Test Cases/TC1','PASSED')
        TestMaterialsRepositoryImpl tmri = (TestMaterialsRepositoryImpl)tmr
        TCaseResult tcr = tmri.getTCaseResult(new TCaseName('Test Cases/TC1'))
        then:
        tcr.getTestCaseStatus() == TCaseStatus.PASSED
    }

    def testMakeIndex() {
        setup:
        Path dir = workdir_.resolve('testReport')
        Helpers.ensureDirs(dir)
        Helpers.copyDirectory(fixture_, dir)
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(dir)
        when:
        tmr.setCurrentTestSuite('Test Suites/TS1','20180530_130419')
        tmr.makeIndex()
        then:
        Files.exists(tmr.getCurrentTestSuiteDirectory().resolve('Result.html'))
    }
}

