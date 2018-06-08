package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.carmina.FileType
import com.kazurayam.carmina.Helpers
import com.kazurayam.carmina.MaterialWrapper
import com.kazurayam.carmina.TargetURL
import com.kazurayam.carmina.TCaseName
import com.kazurayam.carmina.TCaseResult
import com.kazurayam.carmina.TestResultsRepositoryImpl
import com.kazurayam.carmina.TSuiteName
import com.kazurayam.carmina.TSuiteResult

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TargetURLSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")

    // fixture methods
    def setup() {}
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

    def testToJson() {
        setup:
        TestResultsRepositoryImpl sr = new TestResultsRepositoryImpl(workdir, new TSuiteName('TS1'))
        TSuiteResult tsr = sr.getCurrentTsResult()
        TCaseResult tcr = tsr.findOrNewTcResult(new TCaseName('TC1'))
        TargetURL tp = tcr.findOrNewTargetURL(new URL('http://demoaut.katalon.com/'))
        MaterialWrapper sw = tp.findOrNewMaterialWrapper('', FileType.PNG)
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
