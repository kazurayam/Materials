package com.kazurayam.carmina

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.nio.file.Paths

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
        Helpers.copyDirectory(fixture, workdir)
    }
    def cleanupSpec() {}


    // feature methods
    def testScan() {
        setup:
        Helpers.ensureDirs(workdir.resolve("TS1/timestamp"))
        when:
        RepositoryScanner scanner = new RepositoryScanner(workdir)
        List<TSuiteResult> tSuiteResults = scanner.scan()
        logger.debug("#testScan() tSuiteResults.size()=${tSuiteResults.size()}")
        logger.debug(prettyPrint(tSuiteResults))
        then:
        tSuiteResults != null
        tSuiteResults.size() == 2
        //
        when:
        TSuiteResult tSuiteResult = tSuiteResults.get(0)
        then:
        tSuiteResult.getBaseDir() == workdir
        tSuiteResult.getParent() == workdir
        tSuiteResult.getTSuiteName() == new TSuiteName('TS1')
        tSuiteResult.getTSuiteTimestamp() != null

        //
        when:
        List<TCaseResult> tCaseResults = tSuiteResult.getTCaseResults()
        then:
        tCaseResults.size() == 1
        //
        when:
        TCaseResult tCaseResult = tCaseResults[0]
        then:
        tCaseResult.getParent() == tSuiteResult
        tCaseResult.getTCaseName() == new TCaseName('TC1')
        tCaseResult.getTCaseDir() != null
        //

        when:
        List<TargetURL> targetURLs = tCaseResult.getTargetURLs()
        then:
        targetURLs.size() == 1
        //
        /*
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
        String p = './' + Helpers.getClassShortName(this.class) + '/TS1/20180530_130419' +
                '/TC1/' + 'http%3A%2F%2Fdemoaut.katalon.com%2F.png'
        then:
        mw.getParent() == tu
        mw.getMaterialFilePath().toString().replace('\\', '/') == p
        mw.getFileType() == FileType.PNG
        */

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
