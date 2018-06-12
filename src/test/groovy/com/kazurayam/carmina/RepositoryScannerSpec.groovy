package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

class RepositoryScannerSpec extends Specification {

    static Logger logger = LoggerFactory.getLogger(RepositoryScannerSpec.class);

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")

    // fixture methods
    def setup() {
    }
    def cleanup() {}
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(RepositoryScannerSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }

    }
    def cleanupSpec() {}


    // feature methods
    def testScan() {
        setup:
        Path casedir = workdir.resolve("testScan")
        Helpers.copyDirectory(fixture, casedir)

        when:
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        scanner.scan()
        List<TSuiteResult> tSuiteResults = scanner.getTSuiteResults()
        logger.debug("#testScan() tSuiteResults.size()=${tSuiteResults.size()}")
        logger.debug(prettyPrint(tSuiteResults))
        then:
        tSuiteResults != null
        tSuiteResults.size() == 3 // TS1/20180530_130419, TS1/20180530_130604, TS1/ timestamp

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
        tCaseResult.getTCaseDir() != null
        //

        when:
        List<TargetURL> targetURLs = tCaseResult.getTargetURLs()
        then:
        targetURLs.size() == 2   //
        //

        when:
        TargetURL tu = targetURLs[0]
        then:
        tu.getParent() == tCaseResult
        tu.getUrl() == new URL('http://demoaut.katalon.com/')
        //
        when:
        List<MaterialWrapper> materialWrappers = tu.getMaterialWrappers()
        then:
        materialWrappers.size() == 1
        //
        when:
        MaterialWrapper mw = materialWrappers[0]
        String p = './build/tmp/' + Helpers.getClassShortName(this.class) +
            '/testScan/TS1/20180530_130419' +
            '/TC1/' + 'http%3A%2F%2Fdemoaut.katalon.com%2F.png'
        then:
        mw.getParent() == tu
        mw.getMaterialFilePath().toString().replace('\\', '/') == p
        mw.getFileType() == FileType.PNG
    }

    def testGetTSuiteResults_noArg() {
        setup:
        Path casedir = workdir.resolve('testGetTSuiteResults_noArg')
        Helpers.copyDirectory(fixture, casedir)
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        when:
        scanner.scan()
        then:
        scanner.getTSuiteResults().size() == 3
        // TS1/20180530_130419
        // TS1/20180530_130604
        // TS2/20180612_111256
    }

    def testGetTSuiteResults_byTSuiteName() {
        setup:
        Path casedir = workdir.resolve('testGetTSuiteResults_byTSuiteName')
        Helpers.copyDirectory(fixture, casedir)
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
        Path casedir = workdir.resolve('testGetTSuiteResults_byTSuiteTimestamp')
        Helpers.copyDirectory(fixture, casedir)
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        when:
        scanner.scan()
        then:
        scanner.getTSuiteResults(new TSuiteTimestamp('20180530_130419')).size() == 1
        // TS1/20180530_130419
    }

    def testGetTSuiteResult() {
        setup:
        Path casedir = workdir.resolve('testGetTSuiteResult')
        Helpers.copyDirectory(fixture, casedir)
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        when:
        scanner.scan()
        then:
        TSuiteResult tSuiteResult = scanner.getTSuiteResult(
            new TSuiteName('TS1'),new TSuiteTimestamp('20180530_130419'))
    }

    // helper methods
    private def String prettyPrint(List<TSuiteResult> tSuiteResults) {
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
