package com.kazurayam.kstestresults

import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TestResultsImplSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TestResultsImplSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
    }
    def cleanupSpec() {}

    // feature methods
    def testScan() {
        setup:
        Helpers.copyDirectory(fixture, workdir)
        when:
        List<TsResult> tsrList = TestResultsImpl.scan(workdir)
        then:
        tsrList != null
        tsrList.size() == 2
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
        tcr.getParentTsResult() == tsr
        tcr.getTcName() == tcn
        tcr.getTcDir() == tsr.getTsTimestampDir().resolve('TC1')
        tcr.getTcStatus() == TcStatus.TO_BE_EXECUTED
        when:
        TargetURL tp = tcr.getTargetURL(new URL('http://demoaut.katalon.com/'))
        then:
        tp != null
        when:
        Path imageFilePath = tcr.getTcDir().resolve('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
        MaterialWrapper sw = tp.getMaterialWrapper(imageFilePath)
        //System.out.println(prettyPrint("${sw}"))
        then:
        sw.getMaterialFilePath() == imageFilePath
    }

    def testToJson() {
        setup:
        TestResultsImpl tri = new TestResultsImpl(workdir, new TsName('TS1'))
        when:
        def str = tri.toJson()
        System.err.println("str=\n${str}")
        System.out.println("str=\n${JsonOutput.prettyPrint(str)}")
        then:
        str != null
        str.contains('{"TestResultsImpl":{')
        str.contains(Helpers.escapeAsJsonText(workdir.toString()))
        // TODO
        str.contains('}}')
    }

    def testReport() {
        setup:
        TestResultsImpl tri = new TestResultsImpl(workdir, new TsName('TS1'))
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
