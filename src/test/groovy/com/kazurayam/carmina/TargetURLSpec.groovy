package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.carmina.FileType
import com.kazurayam.carmina.Helpers
import com.kazurayam.carmina.MaterialWrapper
import com.kazurayam.carmina.TargetURL
import com.kazurayam.carmina.TcName
import com.kazurayam.carmina.TcResult
import com.kazurayam.carmina.TestResultsImpl
import com.kazurayam.carmina.TsName
import com.kazurayam.carmina.TsResult

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
        TestResultsImpl sr = new TestResultsImpl(workdir, new TsName('TS1'))
        TsResult tsr = sr.getCurrentTsResult()
        TcResult tcr = tsr.findOrNewTcResult(new TcName('TC1'))
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
