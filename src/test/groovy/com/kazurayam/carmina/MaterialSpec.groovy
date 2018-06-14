package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification

//@Ignore
class MaterialSpec extends Specification {

    static Logger logger = LoggerFactory.getLogger(MaterialSpec.class)

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")
    private static TestResultsRepositoryImpl trri
    private static TargetURL tu

    // fixture methods
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(MaterialSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
        trri = TestResultsRepositoryFactory.createInstance(workdir, new TSuiteName('TS1'))
    }
    def setup() {
        TSuiteTimestamp tstamp = new TSuiteTimestamp('20180530_130419')
        TSuiteResult tsr = trri.getTsResult(new TSuiteName('TS1'), tstamp)
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        assert tcr != null
        //tu = tcr.findOrNewTargetURL(new URL('http://demoaut.katalon.com/'))
        tu = tcr.getTargetURL(new URL('http://demoaut.katalon.com/'))
    }


    // feature methods

    def testSetParent_GetParent() {
        when:
        Material mw = tu.getMaterial(new Suffix('1'), FileType.PNG)
        Material modified = mw.setParent(tu)
        then:
        modified.getParent() == tu

    }

    def testParseFileNameForFileType_png() {
        when:
        FileType ft = Material.parseFileNameForFileType('a.png')
        then:
        ft == FileType.PNG
    }

    def testParseFileNameForFileType_none() {
        when:
        FileType ft = Material.parseFileNameForFileType('a')
        then:
        ft == FileType.NULL
    }

    def testParseFileNameForFileType_unknown() {
        when:
        FileType ft = Material.parseFileNameForFileType('a.foo')
        then:
        ft == FileType.NULL
    }

    def testParseFileNameForSuffix_atoz() {
        when:
        Suffix suffix = Material.parseFileNameForSuffix('a§atoz.png')
        then:
        suffix == new Suffix('atoz')
    }

    def testParseFileNameForSuffix_Nihonngo() {
        when:
        Suffix suffix = Material.parseFileNameForSuffix('a§あ.png')
        then:
        suffix == new Suffix('あ')
    }

    def testParseFileNameForSuffix_none() {
        when:
        Suffix suffix = Material.parseFileNameForSuffix('a.png')
        then:
        suffix == null
    }

    def testParseFileNameForURL_http() {
        when:
        URL url = Material.parseFileNameForURL('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
        then:
        url == new URL('http://demoaut.katalon.com/')
    }

    def testParseFileNameForURL_https() {
        when:
        URL url = Material.parseFileNameForURL('https%3A%2F%2Fwww.google.com%2F.png')
        then:
        url == new URL('https://www.google.com/')
    }

    def testParseFileNameForURL_Malformed() {
        when:
        URL url = Material.parseFileNameForURL('demoaut.katalon.com.png')
        then:
        url == null
    }

    /**
     * @return
     */
    def testResolveMaterialFileName() {
        when:
        String fileName = Material.resolveMaterialFileName(
            new URL('http://demoaut.katalon.com/'),
            new Suffix('foo'),
            FileType.PNG)
        then:
        fileName.toString().contains('http%3A%2F%2Fdemoaut.katalon.com%2F§foo.png')
    }


    def testToJson() {
        when:
        Material mw = tu.getMaterial(new Suffix('1'), FileType.PNG)
        def str = mw.toString()
        //System.out.println("#testToJson:\n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"Material":{"materialFilePath":"')
        str.contains(Helpers.escapeAsJsonText(mw.getMaterialFilePath().toString()))
        str.endsWith('"}}')
    }

    def testGetRelativePathToTsTimestampDir() {
        when:
        Material mw = tu.getMaterial(new Suffix('1'), FileType.PNG)
        Path p = mw.getRelativePathToTsTimestampDir()
        then:
        p.toString().replace('\\','/') == 'TC1/http%3A%2F%2Fdemoaut.katalon.com%2F§1.png'
    }

    def testGetRelativePathAsString() {
        when:
        Material mw = tu.getMaterial(new Suffix('1'), FileType.PNG)
        String s = mw.getRelativePathAsString()
        then:
        s.toString().replace('\\', '/') == 'TC1/http%3A%2F%2Fdemoaut.katalon.com%2F§1.png'
    }

    def testGetRelativeUrlAsString() {
        when:
        Material mw = tu.getMaterial(new Suffix('1'), FileType.PNG)
        String s = mw.getRelativeUrlAsString()
        then:
        s == 'TC1/http%253A%252F%252Fdemoaut.katalon.com%252F§1.png'

    }

}
