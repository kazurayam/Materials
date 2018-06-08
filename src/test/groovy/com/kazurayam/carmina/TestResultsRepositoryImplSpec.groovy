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
    def testIdentifyFileType() {
        expect:
        TestResultsRepositoryImpl.identifyFileType(Paths.get('/temp/a.png')) == FileType.PNG
        TestResultsRepositoryImpl.identifyFileType(Paths.get('/temp/a.pdf')) == FileType.PDF
        TestResultsRepositoryImpl.identifyFileType(Paths.get('/temp/a.csv')) == FileType.CSV
        TestResultsRepositoryImpl.identifyFileType(Paths.get('/temp/a')) == FileType.OCTET
    }

    def testIdentifySuffix_noDot() {
        expect:
        TestResultsRepositoryImpl.identifySuffix(Paths.get('/temp/a')) == ''
    }

    def testIdentifySuffix_1dot() {
        expect:
        TestResultsRepositoryImpl.identifySuffix(Paths.get('/temp/a.png')) == ''
    }

    def testIdentifySuffix_2dots() {
        expect:
        TestResultsRepositoryImpl.identifySuffix(Paths.get('/temp/a.1.png')) == '1'
    }

    def testIdentifySuffix_3dots() {
        expect:
        TestResultsRepositoryImpl.identifySuffix(Paths.get('/temp/a.b.c.png')) == 'c'
    }

    def testIdentifyURLpart_noDot() {
        expect:
        TestResultsRepositoryImpl.identifyURLpart(Paths.get('/temp/a')) == 'a'
    }

    def testIdentifyURLpart_1Dot() {
        expect:
        TestResultsRepositoryImpl.identifyURLpart(Paths.get('/temp/a.png')) == 'a'
    }

    def testIdentifyURLpart_2Dots() {
        expect:
        TestResultsRepositoryImpl.identifyURLpart(Paths.get('/temp/a.1.png')) == 'a'
    }

    def testIdentifyURLpart_3Dots() {
        expect:
        TestResultsRepositoryImpl.identifyURLpart(Paths.get('/temp/a.b.c.png')) == 'a.b'
    }

    @Ignore
    def testIdentifyURLpart_realistic() {
        expect:
        TestResultsRepositoryImpl.identifyURLpart(Paths.get('/temp/http%3A%2F%2Fdemoaut.katalon.com%2F.png')) == 'http%3A%2F%2Fdemoaut.katalon.com%2F'
    }

    def testResolveMaterialFilePath() {
        setup:
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, new TsName('TS1'), new TsTimestamp('20180530_130604'))
        when:
        Path p = tri.resolveMaterialFilePath('TC1', 'http://demoaut.katalon.com/', FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
    }

    def testResolveMaterialFilePathWithSuffix() {
        setup:
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, new TsName('TS1'), new TsTimestamp('20180530_130604'))
        when:
        Path p = tri.resolveMaterialFilePath('TC1', 'http://demoaut.katalon.com/', '1', FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.1.png"
    }

    def testResolvePngFilePath() {
        setup:
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, new TsName('TS1'), new TsTimestamp('20180530_130604'))
        when:
        Path p = tri.resolvePngFilePath('TC1', 'http://demoaut.katalon.com/')
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
    }

    def testResolvePngFilePathWithSuffix() {
        setup:
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, new TsName('TS1'), new TsTimestamp('20180530_130604'))
        when:
        Path p = tri.resolvePngFilePath('TC1', 'http://demoaut.katalon.com/', '1')
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.1.png"
    }

    def testResolvePngFilePathBySuitelessTimeless() {
        setup:
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, TsName.SUITELESS, TsTimestamp.TIMELESS)
        when:
        Path p = tri.resolvePngFilePath('TC1', 'http://demoaut.katalon.com/', '1')
        then:
        p != null
        p.toString().replace('\\', '/') == "./build/tmp/${classShortName}/_/_/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.1.png"
    }

    def testScanBaseDir() {
        when:
        List<TsResult> tsrList = TestResultsRepositoryImpl.scanBaseDir(workdir)
        then:
        tsrList != null
        tsrList.size() == 3
        when:
        TsResult tsr =
                lookupTestSuiteResult(tsrList, new TsName('TS1'),
                        new TsTimestamp('20180530_130419'))
        then:
        tsr != null
        tsr.getBaseDir() == workdir
        tsr.getTsName() == new TsName('TS1')
        tsr.getTsTimestamp() == new TsTimestamp('20180530_130419')
        tsr.getTsTimestampDir() == workdir.resolve('TS1/20180530_130419')
        when:
        TcName tcn = new TcName('TC1')
        TcResult tcr = tsr.getTcResult(tcn)
        then:
        tcr != null
        System.out.println("tcr=\n${JsonOutput.prettyPrint(tcr.toString())}")
        tcr.getTsResult() == tsr
        tcr.getTcName() == tcn
        tcr.getTcDir() == tsr.getTsTimestampDir().resolve('TC1')
        tcr.getTcStatus() == TcStatus.TO_BE_EXECUTED
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
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, new TsName('TS1'))
        when:
        def str = tri.toJson()
        //System.err.println("str=\n${str}")
        //System.out.println("str=\n${JsonOutput.prettyPrint(str)}")
        then:
        str != null
        str.contains('{"TestResultsImpl":{')
        str.contains(Helpers.escapeAsJsonText(workdir.toString()))
        // TODO
        str.contains('}}')
    }


    def testReport() {
        setup:
        TestResultsRepositoryImpl tri = new TestResultsRepositoryImpl(workdir, new TsName('TS1'), new TsTimestamp('20180530_130604'))
        when:
        Path html = tri.report()
        then:
        html.toFile().exists()
    }

    // helper methods
    TsResult lookupTestSuiteResult(List<TsResult> tsrList, TsName tsn, TsTimestamp tst) {
        for (TsResult tsr : tsrList ) {
            if (tsr.getTsName() == tsn && tsr.getTsTimestamp() == tst) {
                return tsr
            }
        }
        return null
    }
}
