package com.kazurayam.materials

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.impl.MaterialImpl
import com.kazurayam.materials.model.Suffix
import com.kazurayam.materials.repository.RepositoryFileScanner
import com.kazurayam.materials.repository.RepositoryRoot

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
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(TCaseResultSpec.class)}")
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
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = TCaseResult.newInstance(new TCaseName('Test Cases/main/TC2'))
        TCaseResult modified = tcr.setParent(tsr)
        then:
        modified.getParent() == tsr
    }


    def testGetMaterial() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
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


    def testGetMaterialListOfDifferentSuffixes() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        List<Material> mateList = tcr.getMaterialList(Paths.get('.'), new URL('http://demoaut.katalon.com/'), FileType.PNG)
        then:
        mateList.size() == 2
    }

    def testGetMaterialByPath() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS4'), TSuiteTimestamp.newInstance('20180712_142755'))
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
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        URL url = new URL('http://demoaut.katalon.com/')
        Suffix suffix = new Suffix(1)
        Material mate = MaterialImpl.newInstance(Paths.get('.'), url, suffix, FileType.PNG).setParent(tcr)
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
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        URL url = new URL('http://demoaut.katalon.com/')
        Suffix suffix = new Suffix(2)
        //Material mate = new Material(url, suffix, FileType.PNG).setParent(tcr)
        Material mate = MaterialImpl.newInstance(Paths.get('.'), url, suffix, FileType.PNG)
        tcr.addMaterial(mate)
        then:
        thrown(IllegalStateException)
    }

    def testGetMaterialList() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        List<Material> materials = tcr.getMaterialList()
        then:
        materials.size() == 2
    }


    def testToJson() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        Material mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        when:
        def str = tcr.toString()
        println ">>>>>>>>>>>>>>" + str
        logger_.debug("#testToString: \n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"TCaseResult":{')
        str.contains('tCaseName')
        str.contains('TC1')
        str.contains('tCaseDir')
        str.contains(Helpers.escapeAsJsonText( mate.getPath().toString()))
        str.endsWith('}}')
    }


    def testEquals() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        expect:
        tcr != "string"
        when:
        TCaseResult other = TCaseResult.newInstance(new TCaseName('Test Cases/main/TC1')).setParent(tsr)
        then:
        tcr == other
    }

    def testHashCode() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        TCaseResult other = TCaseResult.newInstance(new TCaseName('Test Cases/main/TC1')).setParent(tsr)
        then:
        tcr.hashCode() == other.hashCode()
    }



    // helper methods
}
