package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TCaseResultSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")
    private static TSuiteResult tsr

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TCaseResultSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
        TestResultsRepositoryImpl trri = new TestResultsRepositoryImpl(workdir, new TSuiteName('TS1'))
        tsr = trri.getCurrentTSuiteResult()
    }
    def cleanupSpec() {}

    // feature methods
    def testSetParent_GetParent() {
        when:
        TCaseResult tcr = new TCaseResult(new TCaseName('TC1'))
        TCaseResult modified = tcr.setParent(tsr)
        then:
        modified.getParent() == tsr
    }

    def testToJson() {
        setup:
        TCaseResult tcr = tsr.findOrNewTCaseResult(new TCaseName('TC1'))
        TargetURL tp = tcr.findOrNewTargetURL(new URL('http://demoaut.katalon.com/'))
        MaterialWrapper sw = tp.findOrNewMaterialWrapper('', FileType.PNG)
        when:
        def str = tcr.toString()
        def pretty = JsonOutput.prettyPrint(str)
        System.out.println("#testToString: \n${pretty}")
        then:
        str.startsWith('{"TCaseResult":{')
        str.contains('tCaseName')
        str.contains('TC1')
        str.contains('tCaseDir')
        str.contains(Helpers.escapeAsJsonText( sw.getMaterialFilePath().toString()))
        str.contains('tCaseStatus')
        str.contains(TestCaseStatus.TO_BE_EXECUTED.toString())
        str.endsWith('}}')
    }



    // helper methods
}
