package com.kazurayam.materials.repository

import com.kazurayam.materials.TExecutionProfile

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp
import groovy.json.JsonOutput
import spock.lang.Specification

class TreeTrunkScannerSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(TreeTrunkScannerSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(TreeTrunkScannerSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    /**
     * test TreeTrunkScanner#scan() method ; an ordinal case
     *
     * @return
     */
    def testScan() {
        setup:
        Path casedir = workdir_.resolve("testScan")
        Helpers.copyDirectory(fixture_, casedir)
        Path materialsDir = casedir.resolve('Materials')
        // now we touch it
        TreeTrunkScanner scanner = new TreeTrunkScanner(materialsDir)
        when:
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        logger_.debug("repoRoot=${JsonOutput.prettyPrint(repoRoot.toJsonText())}")

        List<TSuiteResult> tSuiteResults = repoRoot.getTSuiteResults()
        logger_.debug("#testScan() tSuiteResults.size()=${tSuiteResults.size()}")
        //logger_.debug(prettyPrint(tSuiteResults))
        then:
        tSuiteResults != null

        //tSuiteResults.size() == 9

        // _/_
        // main.§A/20180616_170941
        // main.TS1/20180530_130419
        // main.TS1/20180530_130604
        // main.TS1/20180717_142832
        // main.TS1/20180805_081908
        // main.TS2/20180612_111256
        // main.TS3/20180627_140853
        // main.TS4/20180712_142755

        //
        when:
        TSuiteResult tSuiteResult = repoRoot.getTSuiteResult(
                new TSuiteName("Test Suites/main/TS1"),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        then:
        tSuiteResult != null
        tSuiteResult.getRepositoryRoot() == repoRoot
        tSuiteResult.getParent() == repoRoot
        tSuiteResult.getTSuiteName() == new TSuiteName('Test Suites/main/TS1')
        tSuiteResult.getTSuiteTimestamp() != null


        //
        //when:
        //List<TCaseResult> tCaseResults = tSuiteResult.getTCaseResultList()
        //then:
        //tCaseResults.size() == 1

        //when:
        //TCaseResult tCaseResult = tSuiteResult.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        //then:
        //tCaseResult.getParent() == tSuiteResult
        //tCaseResult.getTCaseName() == new TCaseName('Test Cases/main/TC1')
        //tCaseResult.getTCaseDirectory() != null

        //
        //when:
        //List<Material> materials = tCaseResult.getMaterialList()
        //then:
        //materials.size() == 2
        //
        //when:
        //Material mate0 = materials[0]
        //String p0 = 'build/tmp/testOutput/' + Helpers.getClassShortName(this.class) +
        //        '/testScan/Materials/main.TS1/CURA_ProductionEnv/20180530_130419' +
        //        '/main.TC1/' + 'http%3A%2F%2Fdemoaut.katalon.com%2F(1).png'
        //then:
        //mate0.getParent() == tCaseResult
        //mate0.getPath().toString().replace('\\', '/') == p0
        //mate0.getFileType() == FileType.PNG

        //
        //when:
        //Material mate1 = materials[1]
        //String p1 = 'build/tmp/testOutput/' + Helpers.getClassShortName(this.class) +
        //        '/testScan/Materials/main.TS1/CURA_ProductionEnv/20180530_130419' +
        //        '/main.TC1/' + 'http%3A%2F%2Fdemoaut.katalon.com%2F.png'
        //then:
        //mate1.getParent() == tCaseResult
        //mate1.getPath().toString().replace('\\', '/') == p1
        //mate1.getFileType() == FileType.PNG
    }

}
