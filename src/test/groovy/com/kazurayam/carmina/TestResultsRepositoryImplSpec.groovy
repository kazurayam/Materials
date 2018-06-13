package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.Specification

//@Ignore
class TestResultsRepositoryImplSpec extends Specification {

    // fields
    static Logger logger = LoggerFactory.getLogger(TestResultsRepositoryImplSpec.class)

    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")
    private static String classShortName = Helpers.getClassShortName(TestResultsRepositoryImplSpec.class)

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${classShortName}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }
    def cleanupSpec() {}

    // feature methods
    def testGetBaseDir() {
        setup:
        Path casedir = workdir.resolve('testGetBaseDir')
        Helpers.copyDirectory(fixture, casedir)
        when:
        TestResultsRepositoryImpl trri = new TestResultsRepositoryImpl(casedir)
        then:
        trri.getBaseDir() == casedir
    }

    def testResolveMaterialFilePath() {
        setup:
        Path casedir = workdir.resolve('testResolveMaterialFilePath')
        Helpers.copyDirectory(fixture, casedir)
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(casedir, new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
        when:
        Path p = tri.resolveMaterialFilePath('TC1', 'http://demoaut.katalon.com/', FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/testResolveMaterialFilePath/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
    }

    def testResolveMaterialFilePathWithSuffix() {
        setup:
        Path casedir = workdir.resolve('testResolveMaterialFilePathWithSuffix')
        Helpers.copyDirectory(fixture, casedir)
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(casedir, new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
        when:
        Path p = tri.resolveMaterialFilePath('TC1', 'http://demoaut.katalon.com/', '1', FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/testResolveMaterialFilePathWithSuffix/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F§1.png"
    }

    def testResolvePngFilePath() {
        setup:
        Path casedir = workdir.resolve('testResolvePngFilePath')
        Helpers.copyDirectory(fixture, casedir)
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(casedir, new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
        when:
        Path p = tri.resolvePngFilePath('TC1', 'http://demoaut.katalon.com/')
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/testResolvePngFilePath/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
    }

    def testResolvePngFilePathWithSuffix() {
        setup:
        Path casedir = workdir.resolve('testResolveMaterialFilePathWithSuffix')
        Helpers.copyDirectory(fixture, casedir)
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(casedir, new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
        when:
        Path p = tri.resolvePngFilePath('TC1', 'http://demoaut.katalon.com/', '1')
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/testResolveMaterialFilePathWithSuffix/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F§1.png"
    }

    def testResolvePngFilePathBySuitelessTimeless() {
        setup:
        Path casedir = workdir.resolve('testResolvePngFilePathBySuitelessTimeless')
        Helpers.copyDirectory(fixture, casedir)
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(casedir, TSuiteName.SUITELESS, TSuiteTimestamp.TIMELESS)
        when:
        Path p = tri.resolvePngFilePath('TC1', 'http://demoaut.katalon.com/', '1')
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/testResolvePngFilePathBySuitelessTimeless/_/_/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F§1.png"
    }

    /*
    def testScanBaseDir() {
        when:
        Path casedir = workdir.resolve('testScanBaseDir')
        Helpers.copyDirectory(fixture, casedir)
        List<TSuiteResult> tsrList = TestResultsRepositoryImpl.scanBaseDir(casedir)
        then:
        tsrList != null
        tsrList.size() == 3
        when:
        TSuiteResult tsr =
                lookupTestSuiteResult(tsrList, new TSuiteName('TS1'),
                        new TSuiteTimestamp('20180530_130419'))
        then:
        tsr != null
        tsr.getBaseDir() == casedir
        tsr.getTSuiteName() == new TSuiteName('TS1')
        tsr.getTSuiteTimestamp() == new TSuiteTimestamp('20180530_130419')
        tsr.getTSuiteTimestampDir() == casedir.resolve('TS1/20180530_130419')
        when:
        TCaseName tcn = new TCaseName('TC1')
        TCaseResult tcr = tsr.getTCaseResult(tcn)
        then:
        tcr != null
        tcr.getTSuiteResult() == tsr
        tcr.getTCaseName() == tcn
        tcr.getTCaseDir() == tsr.getTSuiteTimestampDir().resolve('TC1')
        tcr.getTestCaseStatus() == TestCaseStatus.TO_BE_EXECUTED

        when:
        TargetURL tp = tcr.getTargetURL(new URL('http://demoaut.katalon.com/'))
        then:
        tp != null
        when:
        Path imageFilePath = tcr.getTcDir().resolve('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
        MaterialWrapper sw = tp.getMaterialWrapper(imageFilePath)
        then:
        sw.getMaterialFilePath() == imageFilePath

    }
*/

    def testToJson() {
        setup:
        Path casedir = workdir.resolve('testToJson')
        Helpers.copyDirectory(fixture, casedir)
        TestResultsRepositoryImpl trri = new TestResultsRepositoryImpl(casedir, new TSuiteName('TS1'))
        when:
        def str = trri.toJson()
        then:
        str != null
        str.contains('{"TestResultsImpl":{')
        str.contains(Helpers.escapeAsJsonText(casedir.toString()))
        str.contains('}}')
    }


    def testReport() {
        setup:
        Path casedir = workdir.resolve('testReport')
        Helpers.copyDirectory(fixture, casedir)
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(casedir, new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
        when:
        Path html = tri.report()
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
