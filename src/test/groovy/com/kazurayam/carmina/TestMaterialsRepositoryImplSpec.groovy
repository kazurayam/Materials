package com.kazurayam.carmina

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification

//@Ignore
class TestMaterialsRepositoryImplSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(TestMaterialsRepositoryImplSpec.class)

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")
    private static String classShortName_ = Helpers.getClassShortName(TestMaterialsRepositoryImplSpec.class)

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${classShortName_}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testGetBaseDir() {
        setup:
        Path casedir = workdir_.resolve('testGetBaseDir')
        Helpers.copyDirectory(fixture_, casedir)
        when:
        TestMaterialsRepositoryImpl tmri = new TestMaterialsRepositoryImpl(casedir)
        then:
        tmri.getBaseDir() == casedir
    }


    def testResolveMaterialFilePath() {
        setup:
        Path casedir = workdir_.resolve('testResolveMaterialFilePath')
        Helpers.copyDirectory(fixture_, casedir)
        TestMaterialsRepositoryImpl tmri = new TestMaterialsRepositoryImpl(casedir)
        tmri.setCurrentTestSuite('TS1', '20180530_130604')
        when:
        Path p = tmri.resolveMaterial(
            new TCaseName('TC1'),
            new URL('http://demoaut.katalon.com/'),
            Suffix.NULL,
            FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "./build/tmp/${classShortName_}/testResolveMaterialFilePath/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
    }

    def testResolveMaterialFilePathWithSuffix() {
        setup:
        Path casedir = workdir_.resolve('testResolveMaterialFilePathWithSuffix')
        Helpers.copyDirectory(fixture_, casedir)
        TestMaterialsRepositoryImpl tmri = new TestMaterialsRepositoryImpl(casedir)
        tmri.setCurrentTestSuite('TS1', '20180530_130604')
        when:
        Path p = tmri.resolveMaterial(
            new TCaseName('TC1'),
            new URL('http://demoaut.katalon.com/'),
            new Suffix('1'),
            FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "./build/tmp/${classShortName_}/testResolveMaterialFilePathWithSuffix/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F§1.png"
    }

    def testResolveMaterialFilePath_new() {
        setup:
        Path casedir = workdir_.resolve('testResolveMaterialFilePath_new')
        Helpers.copyDirectory(fixture_, casedir)
        TestMaterialsRepositoryImpl tmri = new TestMaterialsRepositoryImpl(casedir)
        tmri.setCurrentTestSuite('TS3', '20180614_152000')
        when:
        Path p = tmri.resolveMaterial(
            new TCaseName('TC1'),
            new URL('http://demoaut.katalon.com/'),
            Suffix.NULL,
            FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "./build/tmp/${classShortName_}/testResolveMaterialFilePath_new/TS3/20180614_152000/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
        Files.exists(p.getParent())
    }

    def testResolveMaterialFilePathWithSuffix_new() {
        setup:
        Path casedir = workdir_.resolve('testResolveMaterialFilePathWithSuffix_new')
        Helpers.copyDirectory(fixture_, casedir)
        TestMaterialsRepositoryImpl tmri = new TestMaterialsRepositoryImpl(casedir)
        tmri.setCurrentTestSuite('TS3', '20180614_152000')
        when:
        Path p = tmri.resolveMaterial(
            new TCaseName('TC1'),
            new URL('http://demoaut.katalon.com/'),
            new Suffix('1'),
            FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "./build/tmp/${classShortName_}/testResolveMaterialFilePathWithSuffix_new/TS3/20180614_152000/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F§1.png"
        Files.exists(p.getParent())
    }

    def testResolvePngFilePath() {
        setup:
        Path casedir = workdir_.resolve('testResolvePngFilePath')
        Helpers.copyDirectory(fixture_, casedir)
        TestMaterialsRepositoryImpl tmri = new TestMaterialsRepositoryImpl(casedir)
        tmri.setCurrentTestSuite('TS1', '20180530_130604')
        when:
        Path p = tmri.resolveMaterial('TC1', 'http://demoaut.katalon.com/', FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName_}/testResolvePngFilePath/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
    }

    def testResolvePngFilePathWithSuffix() {
        setup:
        Path casedir = workdir_.resolve('testResolveMaterialFilePathWithSuffix')
        Helpers.copyDirectory(fixture_, casedir)
        TestMaterialsRepositoryImpl tmri = new TestMaterialsRepositoryImpl(casedir)
        tmri.setCurrentTestSuite('TS1', '20180530_130604')
        when:
        Path p = tmri.resolveMaterial('TC1', 'http://demoaut.katalon.com/', '1', FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName_}/testResolveMaterialFilePathWithSuffix/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F§1.png"
    }

    def testResolvePngFilePathBySuitelessTimeless() {
        setup:
        Path casedir = workdir_.resolve('testResolvePngFilePathBySuitelessTimeless')
        Helpers.copyDirectory(fixture_, casedir)
        TestMaterialsRepositoryImpl tmri = new TestMaterialsRepositoryImpl(casedir)
        tmri.setCurrentTestSuite(TSuiteName.SUITELESS, TSuiteTimestamp.TIMELESS)
        when:
        Path p = tmri.resolveMaterial('TC1', 'http://demoaut.katalon.com/', '1', FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName_}/testResolvePngFilePathBySuitelessTimeless/_/_/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F§1.png"
    }

    def testToJson() {
        setup:
        Path casedir = workdir_.resolve('testToJson')
        Helpers.copyDirectory(fixture_, casedir)
        TestMaterialsRepositoryImpl tmri = new TestMaterialsRepositoryImpl(casedir)
        tmri.setCurrentTestSuite('TS1')
        when:
        def str = tmri.toJson()
        then:
        str != null
        str.contains('{"TestResultsImpl":{')
        str.contains(Helpers.escapeAsJsonText(casedir.toString()))
        str.contains('}}')
    }


    def testMakeIndex() {
        setup:
        Path casedir = workdir_.resolve('testReport')
        Helpers.copyDirectory(fixture_, casedir)
        TestMaterialsRepositoryImpl tmri = new TestMaterialsRepositoryImpl(casedir)
        tmri.setCurrentTestSuite('TS1', '20180530_130604')
        when:
        Path html = tmri.makeIndex()
        then:
        html.toFile().exists()
    }

    // helper methods
    TSuiteResult lookupTestSuiteResult(List<TSuiteResult> tsrList, TSuiteName tsn, TSuiteTimestamp tst) {
        for (TSuiteResult tsr : tsrList ) {
            if (tsr.getTSuiteName() == tsn && tsr.getTSuiteTimestamp() == tst) {
                return tsr
            }
        }
        return null
    }
}
