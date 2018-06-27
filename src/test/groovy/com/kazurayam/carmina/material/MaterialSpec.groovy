package com.kazurayam.carmina.material

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import groovy.xml.*
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

    def testGetHrefRelativeToRepositoryRoot() {
        when:
        Material mate = tcr_.getMaterial(new URL('http://demoaut.katalon.com/'), new Suffix('1'), FileType.PNG)
        String href = mate.getHrefRelativeToRepositoryRoot()
        then:
        href != null
        href == 'TS1/20180530_130419/TC1/http%253A%252F%252Fdemoaut.katalon.com%252F§1.png'
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

    def testToHtmlAsModalWindow_PNG() {
        when:
        Material mate = tcr_.getMaterial(new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        String str = mate.toHtmlAsModalWindow()
        logger_.debug("#testToHtmlAsModalWindow str=${str}")
        //Node node = new XmlParser().parseText(str)
        //logger_.debug("#testToHtmlAsModalWindow str parsed as XML =${XmlUtil.serialize(node)}")
        then:
        str.startsWith('<div')
        str.contains('<img')
        str.contains(mate.getHrefRelativeToRepositoryRoot())
    }

    def testToHtmlAsModalWindows_miscellaneousImages() {
        setup:
        RepositoryRoot repoRoot = rs_.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130604'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        assert tcr != null
         //
        expect:
        tcr.getMaterials().size() == 5
        when:
        Material mate = tcr.getMaterial(new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        String str = mate.toHtmlAsModalWindow()
        then:
        str.contains('<img')
        str.contains('.png')
        //
        when:
        mate = tcr.getMaterial(new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.BMP)
        str = mate.toHtmlAsModalWindow()
        then:
        str.contains('<img')
        str.contains('.bmp')
        //
        when:
        mate = tcr.getMaterial(new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.GIF)
        str = mate.toHtmlAsModalWindow()
        then:
        str.contains('<img')
        str.contains('.gif')
        //
        when:
        mate = tcr.getMaterial(new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.JPEG)
        str = mate.toHtmlAsModalWindow()
        then:
        str.contains('<img')
        str.contains('.jpeg')
        //
        when:
        mate = tcr.getMaterial(new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.JPG)
        str = mate.toHtmlAsModalWindow()
        then:
        str.contains('<img')
        str.contains('.jpg')
        //
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

    def testHashCodeWithAncestors() {
        setup:
        RepositoryRoot repoRoot = rs_.getRepositoryRoot()
        TSuiteResult tsr1 = repoRoot.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        TSuiteResult tsr2 = repoRoot.getTSuiteResult(new TSuiteName('TS2'), new TSuiteTimestamp('20180612_111256'))
        TCaseResult tcr1 = tsr1.getTCaseResult(new TCaseName('TC1'))
        TCaseResult tcr2 = tsr2.getTCaseResult(new TCaseName('TC1'))
        when:
        Material mate1 = new Material(new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr1)
        Material mate2 = new Material(new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr2)
        logger_.debug("#testHashCodeWithAncestors mate1.hashCode()=${mate1.hashCode()}")
        logger_.debug("#testHashCodeWithAncestors mate2.hashCode()=${mate2.hashCode()}")
        then:
        mate1.hashCode() != mate2.hashCode()
    }

    def testMarkupInModalWindow_PNG() {
        setup:
        Material mate = new Material(new URL('http://demoaut.katalon.com'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        when:
        String markup = mate.markupInModalWindow()
        logger_.debug("#testMarkupInModalWindow_png markup=\n${markup}")
        then:
        markup.contains('<img')
        markup.contains('class="img-fluid"')
        markup.contains(FileType.PNG.getExtension())
    }

}
