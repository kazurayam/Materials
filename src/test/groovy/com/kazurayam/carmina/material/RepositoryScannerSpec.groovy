package com.kazurayam.carmina.material

import com.sun.xml.internal.bind.v2.TODO

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
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        List<TSuiteResult> tSuiteResults = repoRoot.getTSuiteResults()
        logger_.debug("#testScan() tSuiteResults.size()=${tSuiteResults.size()}")
        logger_.debug(prettyPrint(tSuiteResults))
        then:
        tSuiteResults != null
        tSuiteResults.size() == 6 // _/_, TS1/20180530_130419, TS1/20180530_130604, TS2/20180612_111256, TS3/20180627_140853, §A/20180616_170941

        //
        when:
        TSuiteResult tSuiteResult = repoRoot.getTSuiteResult(
            new TSuiteName("TS1"), new TSuiteTimestamp('20180530_130419'))
        then:
        tSuiteResult.getRepositoryRoot() == repoRoot
        tSuiteResult.getParent() == repoRoot
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

    def testScan_resolveLastModifiedOfTCaseResult() {
        expect:
        false
    }

    def testScanForMiscellaneousImages() {
        setup:
        Path casedir = workdir_.resolve("testScanForMiscellaneousImages")
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(
            new TSuiteName("TS1"), new TSuiteTimestamp('20180530_130604'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('TC1'))
        when:
        List<Material> materials = tcr.getMaterials()
        then:
        materials.size() == 5
    }

    def testScanForPDF() {
        setup:
        Path casedir = workdir_.resolve("testScanForPDF")
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(
            new TSuiteName("TS3"), new TSuiteTimestamp("20180627_140853"))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName("TC3"))
        when:
        Material mate = tcr.getMaterial(
            new URL("http://files.shareholder.com/downloads/AAPL/6323171818x0xS320193-17-70/320193/filing.pdf"),
            Suffix.NULL,
            FileType.PDF)
        then:
        assert mate != null
    }

    def testScanForExcel() {
        setup:
        Path casedir = workdir_.resolve("testScanForExcel")
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryScanner scanner = new RepositoryScanner(casedir)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(
            new TSuiteName("TS3"), new TSuiteTimestamp("20180627_140853"))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName("TC3"))
        // .xlsx
        when:
        Material mate = tcr.getMaterial(
            new URL("http://www.kazurayam.com/carmina/example/Book1.xlsx"),
            Suffix.NULL,
            FileType.XLSX)
        then:
        assert mate != null
        // .xlsm
        when:
        mate = tcr.getMaterial(
            new URL("http://www.kazurayam.com/carmina/example/Book1.xlsm"),
            Suffix.NULL,
            FileType.XLSM)
        then:
        assert mate != null
        //
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
