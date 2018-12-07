package com.kazurayam.materials.model

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.RepositoryFileScanner
import com.kazurayam.materials.RepositoryRoot
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TCaseResultSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(TCaseResultSpec.class);

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")
    private static RepositoryRoot repoRoot_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(TCaseResultSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        RepositoryFileScanner scanner = new RepositoryFileScanner(workdir_)
        scanner.scan()
        repoRoot_ = scanner.getRepositoryRoot()
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testSetParent_GetParent() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = new TCaseResult(new TCaseName('Test Cases/main/TC2'))
        TCaseResult modified = tcr.setParent(tsr)
        then:
        modified.getParent() == tsr
    }


    def testGetMaterial() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        logger_.debug("#testGetMaterial tcr=${tcr.toString()}")
        URL url = new URL('http://demoaut.katalon.com/')
        Material mate = tcr.getMaterial(Paths.get('.'), url, Suffix.NULL, FileType.PNG)
        then:
        mate != null
        mate.getURL().toString() == url.toString()
        mate.getSuffix() == Suffix.NULL
        mate.getFileType() == FileType.PNG
    }


    def testGetMaterialsOfDifferentSuffixes() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        List<Material> mateList = tcr.getMaterials(Paths.get('.'), new URL('http://demoaut.katalon.com/'), FileType.PNG)
        then:
        mateList.size() == 2
    }

    def testGetMaterialByPath() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS4'),
            new TSuiteTimestamp('20180712_142755'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        logger_.debug("#testGetMaterialByPath tcr=${JsonOutput.prettyPrint(tcr.toJson())}")
        when:
        Material mate = tcr.getMaterial(Paths.get('smilechart.xls'))
        then:
        mate != null
        mate.getHrefRelativeToRepositoryRoot() == 'main.TS4/20180712_142755/main.TC1/smilechart.xls'
    }

    def testAddMaterial() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS1'),
            new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        URL url = new URL('http://demoaut.katalon.com/')
        Suffix suffix = new Suffix(1)
        Material mate = new Material(Paths.get('.'), url, suffix, FileType.PNG).setParent(tcr)
        tcr.addMaterial(mate)
        mate = tcr.getMaterial(Paths.get('.'), url, suffix, FileType.PNG)
        then:
        mate != null
        mate.getParent() == tcr
        mate.getURL().toString() == url.toString()
        mate.getSuffix() == suffix
        mate.getFileType() == FileType.PNG
    }

    def testAddMaterial_parentIsNotSet() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS1'),
            new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        URL url = new URL('http://demoaut.katalon.com/')
        Suffix suffix = new Suffix(2)
        //Material mate = new Material(url, suffix, FileType.PNG).setParent(tcr)
        Material mate = new Material(Paths.get('.'), url, suffix, FileType.PNG)
        tcr.addMaterial(mate)
        then:
        thrown(IllegalStateException)
    }

    def testGetMaterials() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS1'),
            new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        List<Material> materials = tcr.getMaterials()
        then:
        materials.size() == 2
    }

    /*
    def testGetMaterials_reproducingProblem() {
        when:
        RepositoryRoot repoRoot = scanner_.getRepositoryRoot()
        logger_.debug("#testGetMaterials_reproducingProblem repoRoot is ${JsonOutput.prettyPrint(repoRoot.toJson())}")
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('Test Suites/AllCorps'),
            new TSuiteTimestamp('20180810_095325'))
        assert tsr != null
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/fnhp/ecza/visitAllFunds_ecza_pc'))
        List<Material> materials = tcr.getMaterials()
        then:
        materials.size() == 4
    }
     */

    def testToJson() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS1'),
                new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        Material mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        when:
        def str = tcr.toString()
        logger_.debug("#testToString: \n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"TCaseResult":{')
        str.contains('tCaseName')
        str.contains('TC1')
        str.contains('tCaseDir')
        str.contains(Helpers.escapeAsJsonText( mate.getPath().toString()))
        str.endsWith('}}')
    }

    /*
    def testToBootstrapTreeviewData() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        def str = tcr.toBootstrapTreeviewData()
        logger_.debug("#testToBootstrapTreeviewData: \n${JsonOutput.prettyPrint(str)}")
        then:
        str.contains('text')
        str.contains('nodes')
    }
    */

    def testEquals() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        expect:
        tcr != "string"
        when:
        TCaseResult other = new TCaseResult(new TCaseName('Test Cases/main/TC1')).setParent(tsr)
        then:
        tcr == other
    }

    def testHashCode() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        TCaseResult other = new TCaseResult(new TCaseName('Test Cases/main/TC1')).setParent(tsr)
        then:
        tcr.hashCode() == other.hashCode()
    }



    // helper methods
}
