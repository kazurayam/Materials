package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.carmina.FileType
import com.kazurayam.carmina.Helpers
import com.kazurayam.carmina.MaterialWrapper
import com.kazurayam.carmina.TargetURL
import com.kazurayam.carmina.TcName
import com.kazurayam.carmina.TcResult
import com.kazurayam.carmina.TestResultsRepositoryImpl
import com.kazurayam.carmina.TsName
import com.kazurayam.carmina.TsResult

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TsResultSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")
    private TestResultsRepositoryImpl trsi

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TsResultSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
        trsi = new TestResultsRepositoryImpl(workdir, new TsName('TS1'))

    }
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testFindOrNewTcResult() {
        when:
        TsResult tsr = trsi.getCurrentTsResult()
        TcResult tcr = tsr.findOrNewTcResult(new TcName('TC1'))
        then:
        tcr != null
        tcr.getTcName() == new TcName('TC1')
        when:
        TargetURL tp = tcr.findOrNewTargetURL(new URL('http://demoaut.katalon.com/'))
        then:
        tp != null
        when:
        MaterialWrapper sw = tp.findOrNewMaterialWrapper('', FileType.PNG)
        then:
        sw != null
    }

    def testToJson() {
        setup:
        TsResult tsr = trsi.getCurrentTsResult()
        when:
        TcResult tcr = tsr.findOrNewTcResult(new TcName('TC1'))
        TargetURL tp = tcr.findOrNewTargetURL(new URL('http://demoaut.katalon.com/'))
        MaterialWrapper sw = tp.findOrNewMaterialWrapper('', FileType.PNG)
        def str = tsr.toString()
        System.err.println("${str}")
        System.out.println("${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"TsResult":{')
        str.contains('tsName')
        str.contains('TS1')
        str.contains('tcName')
        str.contains('TC1')
        str.contains(Helpers.escapeAsJsonText('http://demoaut.katalon.com/'))
        str.endsWith('}}')
    }

    // helper methods
}