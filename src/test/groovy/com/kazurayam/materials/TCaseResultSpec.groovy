package com.kazurayam.materials

import com.kazurayam.materials.repository.TreeTrunkScanner

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.impl.MaterialImpl
import com.kazurayam.materials.model.Suffix

import com.kazurayam.materials.repository.RepositoryRoot

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TCaseResultSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(TCaseResultSpec.class);

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static RepositoryRoot repoRoot_
    private static Path materialsDir

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(TCaseResultSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        materialsDir = workdir_.resolve('Materials')
    }
    def setup() {
        TreeTrunkScanner scanner = new TreeTrunkScanner(materialsDir)
        scanner.scan()
        repoRoot_ = scanner.getRepositoryRoot()
    }
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testSetParent_GetParent() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = TCaseResult.newInstance(new TCaseName('Test Cases/main/TC2'))
        TCaseResult modified = tcr.setParent(tsr)
        then:
        modified.getParent() == tsr
    }


    def testGetMaterial() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile('CURA_ProductionEnv'),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        logger_.debug("#testGetMaterial tcr=${tcr.toString()}")
        URL url = new URL('http://demoaut.katalon.com/')
        Material mate = tcr.getMaterial('', url, Suffix.NULL, FileType.PNG)
        then:
        mate != null
        mate.getURL().toString() == url.toString()
        mate.getSuffix() == Suffix.NULL
        mate.getFileType() == FileType.PNG
    }


    def testGetMaterialListOfDifferentSuffixes() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        List<Material> mateList = tcr.getMaterialList('', new URL('http://demoaut.katalon.com/'), FileType.PNG)
        then:
        mateList.size() == 2
    }

    def testGetMaterialByPath() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS4'),
                new TExecutionProfile("default"),
                TSuiteTimestamp.newInstance('20180712_142755'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        logger_.debug("#testGetMaterialByPath tcr=${JsonOutput.prettyPrint(tcr.toJsonText())}")
        when:
        Material mate = tcr.getMaterial(Paths.get('smilechart.xls'))
        then:
        mate != null
        mate.getHrefRelativeToRepositoryRoot() == 'main.TS4/default/20180712_142755/main.TC1/smilechart.xls'
    }

    def testAddMaterial() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        URL url = new URL('http://demoaut.katalon.com/')
        Suffix suffix = new Suffix(1)
        Material mate = new MaterialImpl(tcr, '', url, suffix, FileType.PNG)
        tcr.addMaterial(mate)
        mate = tcr.getMaterial('', url, suffix, FileType.PNG)
        then:
        mate != null
        mate.getParent() == tcr
        mate.getURL().toString() == url.toString()
        mate.getSuffix() == suffix
        mate.getFileType() == FileType.PNG
    }

    def testGetMaterialList() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        List<Material> materials = tcr.getMaterialList()
        then:
        materials.size() == 2
    }
    
    def testGetMaterialList_withPattern() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile('CURA_ProductionEnv'),
                TSuiteTimestamp.newInstance('20180530_130604'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        List<Material> m1 = tcr.getMaterialList('http%3A%2F%2Fdemoaut.katalon.com%2F.bmp')
        then:
        m1.size() == 1
        when:
        List<Material> m2 = tcr.getMaterialList('http%3A%2F%2Fdemoaut.katalon.com%2F.bmp', false)
        then:
        m2.size() == 1
        when:
        List<Material> m3 = tcr.getMaterialList('^http', true)
        then:
        m3.size() == 5
        when:
        List<Material> m4 = tcr.getMaterialList('demoaut', true)
        then:
        m4.size() == 5
        when:
        List<Material> m5 = tcr.getMaterialList('\\.png$', true)
        then:
        m5.size() == 1
        when:
        List<Material> m6 = tcr.getMaterialList('\\.PNG$', true)
        then:
        m6.size() == 1
        when:
        List<Material> m7 = tcr.getMaterialList('\\.jpe?g', true)
        then:
        m7.size() == 2
        when:
        List<Material> m8 = tcr.getMaterialList('\\.(bmp|gif|png)', true)
        then:
        m8.size() == 3
    }


    def testToString() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile('CURA_ProductionEnv'),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        Material mate = tcr.getMaterial('', new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
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


    def testEquals_sameTSuiteResult() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        expect:
        tcr != "string"

        when:
        TCaseResult other = TCaseResult.newInstance(new TCaseName('Test Cases/main/TC1')).setParent(tsr)
        then:
        tcr == other
    }

    def testEquals_differentTSuiteResult() {
        setup:
        TSuiteResult tsr1 = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TSuiteResult tsr2 = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130604'))   // different!
        when:
        TCaseResult tcr1 = tsr1.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        TCaseResult tcr2 = tsr2.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        then:
        ! tcr1.equals(tcr2)
        tcr1 != tcr2
    }


    def testHashCode_sameTSuiteResult() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        TCaseResult other = TCaseResult.newInstance(new TCaseName('Test Cases/main/TC1')).setParent(tsr)
        then:
        tcr.hashCode() == other.hashCode()
    }

    def testHashCode_differentTSuiteResult() {
        setup:
        TSuiteResult tsr1 = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TSuiteResult tsr2 = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130604'))   // different!
        when:
        TCaseResult tcr1 = tsr1.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        TCaseResult tcr2 = tsr2.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        then:
        tcr1.hashCode() != tcr2.hashCode()
    }

    def testCompareTo_sameTSuiteResult() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        TCaseResult other = TCaseResult.newInstance(new TCaseName('Test Cases/main/TC1')).setParent(tsr)
        then:
        tcr.compareTo(other) == 0
    }

    def testCompareTo_differentTSuiteResult() {
        setup:
        TSuiteResult tsr1 = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TSuiteResult tsr2 = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130604'))   // different!
        when:
        TCaseResult tcr1 = tsr1.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        TCaseResult tcr2 = tsr2.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        then:
        tcr1.compareTo(tcr2) != 0
    }
}
