package com.kazurayam.carmina.material

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
class MaterialRepositorySpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositorySpec.class);

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")
    private static MaterialRepository mr_

    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(MaterialRepositorySpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }

    def setup() {
        mr_ = MaterialRepositoryFactory.createInstance(workdir_)
    }

    def testPutCurrentTestSuite_oneStringArg() {
        when:
        mr_.putCurrentTestSuite('oneStringArg')
        Path timestampdir = mr_.getCurrentTestSuiteDirectory()
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
        mr_.putCurrentTestSuite('oneStringArg', '20180616_160000')
        Path timestampdir = mr_.getCurrentTestSuiteDirectory()
        logger_.debug("#testSetCurrentTestSuite_oneStringArg timestampdir=${timestampdir}")
        then:
        timestampdir.toString().contains('oneStringArg')
        timestampdir.getFileName().toString().contains('20180616_160000')
    }

    def testPutCurrentTestSuite_tSuiteName_tSuiteTimestamp() {
        when:
        mr_.putCurrentTestSuite(
                new TSuiteName('oneStringArg'),
                new TSuiteTimestamp('20180616_160000'))
        Path timestampdir = mr_.getCurrentTestSuiteDirectory()
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
        mr_.putCurrentTestSuite('Test Suites/TS1')
        def str = JsonOutput.prettyPrint(mr_.toString())
        //logger_.debug(JsonOutput.prettyPrint(str))
        then:
        str.contains('TS1')
    }

    def testConstructor_Path_tsn() {
        when:
        mr_.putCurrentTestSuite('Test Suites/TS1')
        String str = mr_.toString()
        then:
        str.contains('TS1')
    }


    def testGetCurrentTestSuiteDirectory() {
        when:
        mr_.putCurrentTestSuite('Test Suites/TS1','20180530_130419')
        Path testSuiteDir = mr_.getCurrentTestSuiteDirectory()
        then:
        testSuiteDir == workdir_.resolve('TS1/20180530_130419').normalize()
    }

    def testGetTestCaseDirectory() {
        when:
        mr_.putCurrentTestSuite('Test Suites/TS1','20180530_130419')
        Path testCaseDir = mr_.getTestCaseDirectory('Test Cases/TC1')
        then:
        testCaseDir == workdir_.resolve('TS1/20180530_130419/TC1').normalize()
    }

    def testResolveScreenshotMaterialPath() {
        when:
        mr_.putCurrentTestSuite('Test Suites/TS1','20180530_130419')
        Path path = mr_.resolveScreenshotMaterialPath('Test Cases/TC1', 'http://demoaut.katalon.com/')
        then:
        path.getFileName().toString() == 'http%3A%2F%2Fdemoaut.katalon.com%2F(2).png'
    }

    def testDeleteFilesInDownloadsDir() {
        when:
        Path downloadsDir = Paths.get(System.getProperty('user.home'), 'Downloads')
        Path sourceFile   = downloadsDir.resolve('myFunnyDays.txt')
        Helpers.touch(sourceFile)
        List<Path> list = DownloadsDirectoryHelper.listSuffixedFiles('myFunnyDays.txt')
        then:
        list.size() >= 1
        when:
        int count = mr_.deleteFilesInDownloadsDir('myFunnyDays.txt')
        then:
        count > 0
        when:
        list = DownloadsDirectoryHelper.listSuffixedFiles('myFunnyDays.txt')
        then:
        list.size() == 0
    }

    def testImportFileFromDownloadsDir() {
        when:
        mr_.putCurrentTestSuite('Test Suites/TS1','20180530_130419')
        Path downloadsDir = Paths.get(System.getProperty('user.home'), 'Downloads')
        Path sourceFile   = downloadsDir.resolve('downloaded.pdf')
        Helpers.touch(sourceFile)
        List<Path> list = DownloadsDirectoryHelper.listSuffixedFiles('downloaded.pdf')
        then:
        list.size() >= 1
        when:
        Path path = mr_.importFileFromDownloadsDir('Test Cases/TC1', 'downloaded.pdf')
        then:
        path != null
        path.toString().contains('TS1')
        path.toString().contains('20180530_130419')
        path.toString().contains('TC1')
        path.toString().contains('downloaded.pdf')
    }

    def testMakeIndex() {
        when:
        mr_.putCurrentTestSuite('Test Suites/TS1','20180530_130419')
        Path index = mr_.makeIndex()
        then:
        Files.exists(index)
    }


}

