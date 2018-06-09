package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput
import spock.lang.Ignore
import spock.lang.Specification

//@Ignore
class TestResultsRepositoryImplSpec extends Specification {

    // fields
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
        Helpers.copyDirectory(fixture, workdir)
    }
    def cleanupSpec() {}

    // feature methods
    def testGetBaseDir() {
        when:
        TestResultsRepositoryImpl trri = new TestResultsRepositoryImpl(workdir)
        then:
        trri.getBaseDir() == workdir
    }

    def testResolveMaterialFilePath() {
        setup:
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
        when:
        Path p = tri.resolveMaterialFilePath('TC1', 'http://demoaut.katalon.com/', FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
    }

    def testResolveMaterialFilePathWithSuffix() {
        setup:
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
        when:
        Path p = tri.resolveMaterialFilePath('TC1', 'http://demoaut.katalon.com/', '1', FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.1.png"
    }

    def testResolvePngFilePath() {
        setup:
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
        when:
        Path p = tri.resolvePngFilePath('TC1', 'http://demoaut.katalon.com/')
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
    }

    def testResolvePngFilePathWithSuffix() {
        setup:
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
        when:
        Path p = tri.resolvePngFilePath('TC1', 'http://demoaut.katalon.com/', '1')
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.1.png"
    }

    def testResolvePngFilePathBySuitelessTimeless() {
        setup:
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, TSuiteName.SUITELESS, TSuiteTimestamp.TIMELESS)
        when:
        Path p = tri.resolvePngFilePath('TC1', 'http://demoaut.katalon.com/', '1')
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/_/_/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.1.png"
    }

    def testScanBaseDir() {
        when:
        List<TSuiteResult> tsrList = TestResultsRepositoryImpl.scanBaseDir(workdir)
        then:
        tsrList != null
        tsrList.size() == 3
        when:
        TSuiteResult tsr =
                lookupTestSuiteResult(tsrList, new TSuiteName('TS1'),
                        new TSuiteTimestamp('20180530_130419'))
        then:
        tsr != null
        tsr.getBaseDir() == workdir
        tsr.getTSuiteName() == new TSuiteName('TS1')
        tsr.getTSuiteTimestamp() == new TSuiteTimestamp('20180530_130419')
        tsr.getTsTimestampDir() == workdir.resolve('TS1/20180530_130419')
        when:
        TCaseName tcn = new TCaseName('TC1')
        TCaseResult tcr = tsr.getTCaseResult(tcn)
        then:
        tcr != null
        tcr.getTSuiteResult() == tsr
        tcr.getTCaseName() == tcn
        tcr.getTCaseDir() == tsr.getTsTimestampDir().resolve('TC1')
        tcr.getTestCaseStatus() == TestCaseStatus.TO_BE_EXECUTED
        /*
        when:
        TargetURL tp = tcr.getTargetURL(new URL('http://demoaut.katalon.com/'))
        then:
        tp != null
        when:
        Path imageFilePath = tcr.getTcDir().resolve('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
        MaterialWrapper sw = tp.getMaterialWrapper(imageFilePath)
        then:
        sw.getMaterialFilePath() == imageFilePath
        */
    }


    def testToJson() {
        setup:
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, new TSuiteName('TS1'))
        when:
        def str = tri.toJson()
        then:
        str != null
        str.contains('{"TestResultsImpl":{')
        str.contains(Helpers.escapeAsJsonText(workdir.toString()))
        str.contains('}}')
    }


    def testReport() {
        setup:
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
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
