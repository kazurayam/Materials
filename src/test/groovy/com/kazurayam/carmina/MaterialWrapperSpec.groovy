package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class MaterialWrapperSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")
    private static TestResultsRepositoryImpl trri
    private static TargetURL tu

    // fixture methods
    def setup() {
        TSuiteTimestamp tstamp = new TSuiteTimestamp('20180530_130419')
        TSuiteResult tsr = trri.getTsResult(new TSuiteName('TS1'), tstamp)
        TCaseResult tcr = tsr.findOrNewTcResult(new TCaseName('TC1'))
        assert tcr != null
        tu = tcr.findOrNewTargetURL(new URL('http://demoaut.katalon.com/'))
    }
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(MaterialWrapperSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
        trri = TestResultsRepositoryFactory.createInstance(workdir, new TSuiteName('TS1'))
    }

    // feature methods
    def testSetParent_GetParent() {
        when:
        MaterialWrapper mw = tu.findOrNewMaterialWrapper('1', FileType.PNG)
        MaterialWrapper modified = mw.setParent(tu)
        then:
        modified.getParent() == tu

    }

    def testToJson() {
        when:
        MaterialWrapper mw = tu.findOrNewMaterialWrapper('1', FileType.PNG)
        def str = mw.toString()
        System.out.println("#testToJson:\n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"MaterialWrapper":{"materialFilePath":"')
        str.contains(Helpers.escapeAsJsonText(mw.getMaterialFilePath().toString()))
        str.endsWith('"}}')
    }

    def testGetRelativePathToTsTimestampDir() {
        when:
        MaterialWrapper mw = tu.findOrNewMaterialWrapper('1', FileType.PNG)
        Path p = mw.getRelativePathToTsTimestampDir()
        then:
        p.toString().replace('\\','/') == 'TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.1.png'
    }

    def testGetRelativePathAsString() {
        when:
        MaterialWrapper mw = tu.findOrNewMaterialWrapper('1', FileType.PNG)
        String s = mw.getRelativePathAsString()
        then:
        s.toString().replace('\\', '/') == 'TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.1.png'
    }

    def testGetRelativeUrlAsString() {
        when:
        MaterialWrapper mw = tu.findOrNewMaterialWrapper('1', FileType.PNG)
        String s = mw.getRelativeUrlAsString()
        then:
        s == 'TC1/http%253A%252F%252Fdemoaut.katalon.com%252F.1.png'

    }

}
