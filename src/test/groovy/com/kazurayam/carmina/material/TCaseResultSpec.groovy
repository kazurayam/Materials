package com.kazurayam.carmina.material

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TCaseResultSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(TCaseResultSpec.class);

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")
    private static RepositoryScanner scanner_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(TCaseResultSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }
    def setup() {
        scanner_ = new RepositoryScanner(workdir_)
        scanner_.scan()
    }
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testSetParent_GetParent() {
        when:
        RepositoryRoot repoRoot = scanner_.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('TS1'),
                new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = new TCaseResult(new TCaseName('TC2'))
        TCaseResult modified = tcr.setParent(tsr)
        then:
        modified.getParent() == tsr
    }

    def testGetMaterial() {
        when:
        RepositoryRoot repoRoot = scanner_.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('TS1'),
            new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        URL url = new URL('http://demoaut.katalon.com/')
        Material mate = tcr.getMaterial(url, Suffix.NULL, FileType.PNG)
        then:
        mate != null
        mate.getURL().toString() == url.toString()
        mate.getSuffix() == Suffix.NULL
        mate.getFileType() == FileType.PNG
    }

    def testGetMaterial_fileName() {
        when:
        RepositoryRoot repoRoot = scanner_.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('TS1'),
            new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        Material mate = tcr.getMaterial('http%3A%2F%2Fdemoaut.katalon.com%2F(1).png')
        then:
        mate != null
        mate.getURL().toString() == new URL('http://demoaut.katalon.com/').toString()
        mate.getSuffix() == new Suffix(1)
        mate.getFileType() == FileType.PNG
    }

    def testAddMaterial() {
        when:
        RepositoryRoot repoRoot = scanner_.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('TS1'),
            new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        URL url = new URL('http://demoaut.katalon.com/')
        Suffix suffix = new Suffix(1)
        Material mate = new Material(url, suffix, FileType.PNG).setParent(tcr)
        tcr.addMaterial(mate)
        mate = tcr.getMaterial(url, suffix, FileType.PNG)
        then:
        mate != null
        mate.getParent() == tcr
        mate.getURL().toString() == url.toString()
        mate.getSuffix() == suffix
        mate.getFileType() == FileType.PNG
    }

    def testAddMaterial_parentIsNotSet() {
        when:
        RepositoryRoot repoRoot = scanner_.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('TS1'),
            new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        URL url = new URL('http://demoaut.katalon.com/')
        Suffix suffix = new Suffix(2)
        //Material mate = new Material(url, suffix, FileType.PNG).setParent(tcr)
        Material mate = new Material(url, suffix, FileType.PNG)
        tcr.addMaterial(mate)
        then:
        thrown(IllegalArgumentException)
    }

    def testGetMaterials() {
        when:
        RepositoryRoot repoRoot = scanner_.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('TS1'),
            new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        List<Material> materials = tcr.getMaterials()
        then:
        materials.size() == 2
    }

    def testToJson() {
        setup:
        RepositoryRoot repoRoot = scanner_.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('TS1'),
                new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        Material mate = tcr.getMaterial(new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        when:
        def str = tcr.toString()
        logger_.debug("#testToString: \n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"TCaseResult":{')
        str.contains('tCaseName')
        str.contains('TC1')
        str.contains('tCaseDir')
        str.contains(Helpers.escapeAsJsonText( mate.getMaterialFilePath().toString()))
        str.endsWith('}}')
    }

    def testToBootstrapTreeviewData() {
        setup:
        RepositoryRoot repoRoot = scanner_.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        when:
        def str = tcr.toBootstrapTreeviewData()
        logger_.debug("#testToBootstrapTreeviewData: \n${JsonOutput.prettyPrint(str)}")
        then:
        str.contains('text')
        str.contains('nodes')
    }

    def testEquals() {
        setup:
        RepositoryRoot repoRoot = scanner_.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        expect:
        tcr != "string"
        when:
        TCaseResult other = new TCaseResult(new TCaseName('TC1')).setParent(tsr)
        then:
        tcr == other
    }

    def testHashCode() {
        setup:
        RepositoryRoot repoRoot = scanner_.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        when:
        TCaseResult other = new TCaseResult(new TCaseName('TC1')).setParent(tsr)
        then:
        tcr.hashCode() == other.hashCode()
    }



    // helper methods
}
