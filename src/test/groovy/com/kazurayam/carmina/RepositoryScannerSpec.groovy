package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

class RepositoryScannerSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryScannerSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(RepositoryScannerSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}


    // feature methods

    /**
     * test RepositoryScanner#scan() method ; an ordinal case
     *
     * @return
     */
    def testScan() {
        setup:
        Path casedir = workdir_.resolve("testScan")
        Helpers.copyDirectory(fixture_, casedir)

        when:
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        scanner.scan()
        List<TSuiteResult> tSuiteResults = scanner.getTSuiteResults()
        logger_.debug("#testScan() tSuiteResults.size()=${tSuiteResults.size()}")
        logger_.debug(prettyPrint(tSuiteResults))
        then:
        tSuiteResults != null
        tSuiteResults.size() == 5 // _/_, TS1/20180530_130419, TS1/20180530_130604, TS2/20180612_111256, §A/20180616_170941

        //
        when:
        TSuiteResult tSuiteResult = scanner.getTSuiteResult(
            new TSuiteName("TS1"), new TSuiteTimestamp('20180530_130419'))
        then:
        tSuiteResult.getBaseDir() == casedir
        tSuiteResult.getParent() == casedir
        tSuiteResult.getTSuiteName() == new TSuiteName('TS1')
        tSuiteResult.getTSuiteTimestamp() != null

        //
        when:
        List<TCaseResult> tCaseResults = tSuiteResult.getTCaseResults()
        then:
        tCaseResults.size() == 1
        //
        when:
        TCaseResult tCaseResult = tSuiteResult.getTCaseResult(new TCaseName('TC1'))
        then:
        tCaseResult.getParent() == tSuiteResult
        tCaseResult.getTCaseName() == new TCaseName('TC1')
        tCaseResult.getTCaseDirectory() != null
        
        //
        when:
        List<Material> materials = tCaseResult.getMaterials()
        then:
        materials.size() == 2
        //
        when:
        Material mate0 = materials[0]
        String p0 = 'build/tmp/' + Helpers.getClassShortName(this.class) +
            '/testScan/TS1/20180530_130419' +
            '/TC1/' + 'http%3A%2F%2Fdemoaut.katalon.com%2F.png'
        then:
        mate0.getParent() == tCaseResult
        mate0.getMaterialFilePath().toString().replace('\\', '/') == p0
        mate0.getFileType() == FileType.PNG

        //
        when:
        Material mate1 = materials[1]
        String p1 = 'build/tmp/' + Helpers.getClassShortName(this.class) +
                '/testScan/TS1/20180530_130419' +
                '/TC1/' + 'http%3A%2F%2Fdemoaut.katalon.com%2F§1.png'
        then:
        mate1.getParent() == tCaseResult
        mate1.getMaterialFilePath().toString().replace('\\', '/') == p1
        mate1.getFileType() == FileType.PNG

    }

    /**
     *
     * @return
     */
    def testGetTSuiteResults_noArg() {
        setup:
        Path casedir = workdir_.resolve('testGetTSuiteResults_noArg')
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        when:
        scanner.scan()
        then:
        scanner.getTSuiteResults().size() == 5
        // _/_
        // TS1/20180530_130419
        // TS1/20180530_130604
        // TS2/20180612_111256
        // §A/20180616_170941
    }


    def testGetTSuiteResults_byTSuiteName() {
        setup:
        Path casedir = workdir_.resolve('testGetTSuiteResults_byTSuiteName')
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        when:
        scanner.scan()
        then:
        scanner.getTSuiteResults(new TSuiteName('TS1')).size() == 2
        // TS1/20180530_130419
        // TS1/20180530_130604
    }


    def testGetTSuiteResults_byTSuiteTimestamp() {
        setup:
        Path casedir = workdir_.resolve('testGetTSuiteResults_byTSuiteTimestamp')
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        when:
        scanner.scan()
        then:
        scanner.getTSuiteResults(new TSuiteTimestamp('20180530_130419')).size() == 1
        // TS1/20180530_130419
    }


    def testGetTSuiteResult() {
        setup:
        Path casedir = workdir_.resolve('testGetTSuiteResult')
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        when:
        scanner.scan()
        TSuiteResult tSuiteResult = scanner.getTSuiteResult(
            new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        then:
        tSuiteResult != null
    }

    def testFind2MaterialsIn1TestCase() {
        setup:
        Path casedir = workdir_.resolve('testGetTSuiteResult')
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        scanner.scan()
        when:
        TSuiteResult tSuiteResult = scanner.getTSuiteResult(
            new TSuiteName('TS1'), new TSuiteTimestamp('20180530_130419'))
        TCaseResult tCaseResult = tSuiteResult.getTCaseResult(new TCaseName('TC1'))
        List<Material> materials = tCaseResult.getMaterials()
        then:
        materials.size() == 2
        // TS1/20180530_130419/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png
        // TS1/20180530_130419/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F§%C2%A71.png
    }

    def testPrettyPrint() {
        setup:
        Path casedir = workdir_.resolve('testPrettyPrint')
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        scanner.scan()
        when:
        logger_.debug(JsonOutput.prettyPrint(scanner.toJson()))
        then:
        true
    }

    // helper methods
    private static String prettyPrint(List<TSuiteResult> tSuiteResults) {
        StringBuilder sb = new StringBuilder()
        sb.append("[")
        def count = 0
        for (TSuiteResult tSuiteResult: tSuiteResults) {
            if (count > 0) {
                sb.append(",")
            }
            sb.append(tSuiteResult.toJson())
            count += 1
        }
        sb.append("]")
        return JsonOutput.prettyPrint(sb.toString())
    }

}
