package com.kazurayam.carmina.material

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class MaterialSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(MaterialSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")
    private static RepositoryScanner rs_
    private static TCaseResult tcr_


    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(MaterialSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        rs_ = new RepositoryScanner(workdir_)
        rs_.scan()
        RepositoryRoot repoRoot = rs_.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        tcr_ = tsr.getTCaseResult(new TCaseName('TC1'))
        logger_.debug("#setupSpec tcr_:\n${JsonOutput.prettyPrint(tcr_.toJson())}")
    }
    def setup() {}


    // feature methods

    def testSetParent_GetParent() {
        when:
        Material mate = new Material(new URL('http://demoaut.katalon.com/'), new Suffix('2'), FileType.PNG)
        Material modified = mate.setParent(tcr_)
        then:
        modified.getParent() == tcr_

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
        suffix == Suffix.NULL
        //
        when:
        suffix = Material.parseFileNameForSuffix('foo')
        then:
        suffix == Suffix.NULL
        //
        when:
        suffix = Material.parseFileNameForSuffix('a§b§c.png')
        then:
        suffix == new Suffix('c')
    }

    def testParseFileNameForURL_http() {
        when:
        URL url = Material.parseFileNameForURL('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
        then:
        url.toString() == new URL('http://demoaut.katalon.com/').toString()
    }

    def testParseFileNameForURL_https() {
        when:
        URL url = Material.parseFileNameForURL('https%3A%2F%2Fwww.google.com%2F.png')
        then:
        url.toString() == new URL('https://www.google.com/').toString()
    }

    def testParseFileNameForURL_Malformed() {
        when:
        URL url = Material.parseFileNameForURL('this_is_unexpected_file_name.png')
        then:
        url == null
    }

    def testResolveMaterialFileName() {
        when:
        String fileName = Material.resolveMaterialFileName(
            new URL('http://demoaut.katalon.com/'),
            new Suffix('foo'),
            FileType.PNG)
        then:
        fileName.toString().contains('http%3A%2F%2Fdemoaut.katalon.com%2F§foo.png')
    }

    def testGetPathRelativeToTSuiteTimestamp() {
        when:
        Material mate = tcr_.getMaterial(new URL('http://demoaut.katalon.com/'), new Suffix('1'), FileType.PNG)
        Path relative = mate.getPathRelativeToTSuiteTimestamp()
        then:
        relative != null
        relative.toString().replace('\\', '/') == 'TC1/http%3A%2F%2Fdemoaut.katalon.com%2F§1.png'
    }
    
    def testGetHrefRelativeToTSuiteTimestamp() {
        when:
        Material mate = tcr_.getMaterial(new URL('http://demoaut.katalon.com/'), new Suffix('1'), FileType.PNG)
        String href = mate.getHrefRelativeToTSuiteTimestamp()
        then:
        href != null
        href == 'TC1/http%253A%252F%252Fdemoaut.katalon.com%252F§1.png'
    }
    
    def testToJson() {
        when:
        Material mate = tcr_.getMaterial(new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        def str = mate.toString()
        //System.out.println("#testToJson:\n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"Material":{"url":"')
        str.contains('"suffix":')
        str.contains('"materialFilePath":')
        str.contains(Helpers.escapeAsJsonText(mate.getMaterialFilePath().toString()))
        str.contains('"fileType":')
        str.endsWith('"}}')
    }

    def testToBootstrapTreeviewData() {
        when:
        Material mate = tcr_.getMaterial(new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        def str = mate.toBootstrapTreeviewData()
        //System.out.println("#testToJson:\n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"text":"')
        str.contains(Helpers.escapeAsJsonText(mate.getMaterialFilePath().getFileName().toString()))
        str.endsWith('"}')
    }

    def testEquals() {
        when:
        Material mate1 = new Material(new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG)
        Material mate2 = new Material(new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG)
        then:
        mate1 != null
        mate2 != null
        mate1 == mate2
    }

    def testEquals_differentURL() {
        Material mate1 = new Material(new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG)
        when:
        Material mate3 = new Material(new URL('https://www.yahoo.com/'), Suffix.NULL, FileType.PNG)
        then:
        mate3 != null
        mate1 != mate3
    }

    def testEquals_differentSuffix() {
        Material mate1 = new Material(new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG)
        when:
        Material mate3 = new Material(new URL('https://www.google.com/'), new Suffix("foo"), FileType.PNG)
        then:
        mate3 != null
        mate1 != mate3
    }

    def testEquals_differentFileType() {
        Material mate1 = new Material(new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG)
        when:
        Material mate3 = new Material(new URL('https://www.google.com/'), Suffix.NULL, FileType.JPEG)
        then:
        mate3 != null
        mate1 != mate3
    }

    def testHashCode() {
        when:
        Material mate1 = new Material(new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG)
        Material mate2 = new Material(new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG)
        then:
        mate1.hashCode() == mate2.hashCode()
        when:
        Material mate3 = new Material(new URL('https://www.google.com/'), new Suffix("foo"), FileType.PNG)
        then:
        mate1.hashCode() != mate3.hashCode()
    }
}
