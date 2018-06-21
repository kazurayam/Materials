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
    private static TestMaterialsRepository tmr_

    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(TestMaterialsRepositorySpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }

    def setup() {
        tmr_ = TestMaterialsRepositoryFactory.createInstance(workdir_)
    }

    def testPutCurrentTestSuite_oneStringArg() {
        when:
        tmr_.putCurrentTestSuite('oneStringArg')
        Path timestampdir = tmr_.getCurrentTestSuiteDirectory()
        logger_.debug("#testSetCurrentTestSuite_oneStringArg timestampdir=${timestampdir}")
        then:
        timestampdir.toString().contains('oneStringArg')
        when:
        String dirName = timestampdir.getFileName()
        LocalDateTime ldt = TSuiteTimestamp.parse(dirName)
        then:
        true
    }

    def testPutCurrentTestSuite_twoStringArg() {
        when:
        tmr_.putCurrentTestSuite('oneStringArg', '20180616_160000')
        Path timestampdir = tmr_.getCurrentTestSuiteDirectory()
        logger_.debug("#testSetCurrentTestSuite_oneStringArg timestampdir=${timestampdir}")
        then:
        timestampdir.toString().contains('oneStringArg')
        timestampdir.getFileName().toString().contains('20180616_160000')
    }

    def testPutCurrentTestSuite_tSuiteName_tSuiteTimestamp() {
        when:
        tmr_.putCurrentTestSuite(
                new TSuiteName('oneStringArg'),
                new TSuiteTimestamp('20180616_160000'))
        Path timestampdir = tmr_.getCurrentTestSuiteDirectory()
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
        tmr_.putCurrentTestSuite('Test Suites/TS1')
        def str = JsonOutput.prettyPrint(tmr_.toString())
        //logger_.debug(JsonOutput.prettyPrint(str))
        then:
        str.contains('TS1')
    }

    def testConstructor_Path_tsn() {
        when:
        tmr_.putCurrentTestSuite('Test Suites/TS1')
        String str = tmr_.toString()
        then:
        str.contains('TS1')
    }

    def testResolveMaterial_png() {
        when:
        tmr_.putCurrentTestSuite('TS1','20180530_130419')
        Path png = tmr_.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.PNG)
        then:
        png != null
        png.toString().contains('TC1')
        png.toString().contains('demoaut.katalon.com')
        png.toString().contains('png')
    }

    def testResolveMaterial_json() {
        when:
        tmr_.putCurrentTestSuite('TS1','20180530_130419')
        Path json = tmr_.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.JSON)
        then:
        json != null
        json.toString().contains('TC1')
        json.toString().contains('demoaut.katalon.com')
        json.toString().contains('json')
    }

    def testResolveMaterial_xml() {
        when:
        tmr_.putCurrentTestSuite('TS1','20180530_130419')
        Path xml = tmr_.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', '1', FileType.XML)
        then:
        xml != null
        xml.toString().contains('TC1')
        xml.toString().contains('demoaut.katalon.com')
        xml.toString().contains('xml')
    }

    def testResolveMaterial_pdf() {
        when:
        tmr_.putCurrentTestSuite('TS1','20180530_130419')
        Path pdf = tmr_.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.PDF)
        then:
        pdf != null
        pdf.toString().contains('TC1')
        pdf.toString().contains('demoaut.katalon.com')
        pdf.toString().contains('pdf')
    }

    def testResolveMaterial_txt() {
        when:
        tmr_.putCurrentTestSuite('TS1','20180530_130419')
        Path txt = tmr_.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', 'x', FileType.TXT)
        then:
        txt != null
        txt.toString().contains('TC1')
        txt.toString().contains('demoaut.katalon.com')
        txt.toString().contains('txt')
    }

    def testResolveMaterial_xls() {
        when:
        tmr_.putCurrentTestSuite('TS1','20180530_130419')
        Path xls = tmr_.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.XLS)
        then:
        xls != null
        xls.toString().contains('TC1')
        xls.toString().contains('demoaut.katalon.com')
        xls.toString().contains('xls')
    }

    def testResolveMaterial_xlsx() {
        when:
        tmr_.putCurrentTestSuite('TS1','20180530_130419')
        Path xls = tmr_.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.XLSX)
        then:
        xls != null
        xls.toString().contains('TC1')
        xls.toString().contains('demoaut.katalon.com')
        xls.toString().contains('xlsx')
    }

    def testResolveMaterial_xlsm() {
        when:
        tmr_.putCurrentTestSuite('TS1','20180530_130419')
        Path xls = tmr_.resolveMaterial('Test Cases/TC1', 'http://demoaut.katalon.com/', FileType.XLSM)
        then:
        xls != null
        xls.toString().contains('TC1')
        xls.toString().contains('demoaut.katalon.com')
        xls.toString().contains('xlsm')
    }


    def testGetCurrentTestSuiteDirectory() {
        when:
        tmr_.putCurrentTestSuite('Test Suites/TS1','20180530_130419')
        Path testSuiteDir = tmr_.getCurrentTestSuiteDirectory()
        then:
        testSuiteDir == workdir_.resolve('TS1/20180530_130419')
    }

    def testGetTestCaseDirectory() {
        when:
        tmr_.putCurrentTestSuite('Test Suites/TS1','20180530_130419')
        Path testCaseDir = tmr_.getTestCaseDirectory('Test Cases/TC1')
        then:
        testCaseDir == workdir_.resolve('TS1/20180530_130419/TC1')
    }

    def testMakeIndex() {
        when:
        tmr_.putCurrentTestSuite('Test Suites/TS1','20180530_130419')
        tmr_.makeIndex()
        then:
        Files.exists(tmr_.getCurrentTestSuiteDirectory().resolve('Materials.html'))
    }
}

