package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TargetURLSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")
    private static TCaseResult tcr
    
    // fixture methods
    def setup() {
        TestResultsRepositoryImpl strri = new TestResultsRepositoryImpl(workdir, new TSuiteName('TS1'))
        TSuiteResult tsr = strri.getCurrentTsResult()
        tcr = tsr.findOrNewTcResult(new TCaseName('TC1'))
    }
    def cleanup() {}
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TargetURLSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
    }
    def cleanupSpec() {}

    // feature methods
    def testSetParent_GetParent() {
        when:
        TargetURL tu = new TargetURL(new URL('http://demoaut.katalon.com/'))
        TargetURL modified = tu.setParent(tcr)
        then:
        modified == tu
        modified.getParent() == tcr
    }

    def testToJson() {
        setup:
        TargetURL tp = tcr.findOrNewTargetURL(new URL('http://demoaut.katalon.com/'))
        MaterialWrapper mw = tp.findOrNewMaterialWrapper('', FileType.PNG)
        when:
        def str = tp.toString()
        def pretty = JsonOutput.prettyPrint(str)
        System.out.println("#testToJson: ${pretty}")
        then:
        str.startsWith('{"TargetURL":{')
        str.contains(Helpers.escapeAsJsonText('http://demoaut.katalon.com/'))
        str.contains(Helpers.escapeAsJsonText('http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
        str.endsWith('}}')
    }


    // helper methods

}
