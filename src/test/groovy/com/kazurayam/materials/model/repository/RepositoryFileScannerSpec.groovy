package com.kazurayam.materials.model.repository

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.model.Suffix
import com.kazurayam.materials.model.TCaseResult
import com.kazurayam.materials.model.TSuiteResult
import com.kazurayam.materials.model.TSuiteTimestamp
import com.kazurayam.materials.model.repository.RepositoryFileScanner
import com.kazurayam.materials.model.repository.RepositoryRoot

import groovy.json.JsonOutput
import spock.lang.IgnoreRest
import spock.lang.Specification

class RepositoryFileScannerSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryFileScannerSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(RepositoryFileScannerSpec.class)}")
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
        RepositoryFileScanner scanner = new RepositoryFileScanner(casedir)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        List<TSuiteResult> tSuiteResults = repoRoot.getTSuiteResults()
        logger_.debug("#testScan() tSuiteResults.size()=${tSuiteResults.size()}")
        logger_.debug(prettyPrint(tSuiteResults))
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
            new TSuiteName("Test Suites/main/TS1"), new TSuiteTimestamp('20180530_130419'))
        then:
        tSuiteResult != null
        tSuiteResult.getRepositoryRoot() == repoRoot
        tSuiteResult.getParent() == repoRoot
        tSuiteResult.getTSuiteName() == new TSuiteName('Test Suites/main/TS1')
        tSuiteResult.getTSuiteTimestamp() != null

        //
        when:
        List<TCaseResult> tCaseResults = tSuiteResult.getTCaseResults()
        then:
        tCaseResults.size() == 1
        //
        when:
        TCaseResult tCaseResult = tSuiteResult.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        then:
        tCaseResult.getParent() == tSuiteResult
        tCaseResult.getTCaseName() == new TCaseName('Test Cases/main/TC1')
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
            '/testScan/main.TS1/20180530_130419' +
            '/main.TC1/' + 'http%3A%2F%2Fdemoaut.katalon.com%2F(1).png'
        then:
        mate0.getParent() == tCaseResult
        mate0.getPath().toString().replace('\\', '/') == p0
        mate0.getFileType() == FileType.PNG

        //
        when:
        Material mate1 = materials[1]
        String p1 = 'build/tmp/' + Helpers.getClassShortName(this.class) +
                '/testScan/main.TS1/20180530_130419' +
                '/main.TC1/' + 'http%3A%2F%2Fdemoaut.katalon.com%2F.png'
        then:
        mate1.getParent() == tCaseResult
        mate1.getPath().toString().replace('\\', '/') == p1
        mate1.getFileType() == FileType.PNG

    }

    /**
     * execute RepositoryScanner.scan() then check the result
     * if a TCaseResult object has a lastModified property with appropriate value which
     * must be equal to the maximum value of contained Materials.
     *
     */
    def testScan_lastModifiedOfTCaseResult() {
        setup:
        Path casedir = workdir_.resolve("test_lastModifiedOfTCaseResult")
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryFileScanner scanner = new RepositoryFileScanner(casedir)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        logger_.debug("#testScan_lastModifiedOfTCaseResult repoRoot: ${JsonOutput.prettyPrint(repoRoot.toJson())}")
        when:
        TSuiteResult ts1_20180530_130604 = repoRoot.getTSuiteResult(new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180530_130604'))
        TCaseResult tcr = ts1_20180530_130604.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        LocalDateTime lastModifiedOfTCaseResult = tcr.getLastModified()
        LocalDateTime lastModifiedOfMaterials = LocalDateTime.MIN
        List<Material> materials = tcr.getMaterials()
        for (Material mate : materials) {
            if (mate.getLastModified() > lastModifiedOfMaterials) {
                lastModifiedOfMaterials = mate.getLastModified()
            }
        }
        then:
        lastModifiedOfTCaseResult == lastModifiedOfMaterials
    }

    /**
     * execuite RepositoryScanner.scan() then check the result
     * if a TSuiteResult object has a lastModified property with appropriate value which
     * must be equal to the maximum value of contained TCaseResults.
     *
     */
    def testScan_lastModifiedOfTSuiteResult() {
        setup:
        Path casedir = workdir_.resolve("testScan_lastModifiedOfTCaseResult")
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryFileScanner scanner = new RepositoryFileScanner(casedir)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        logger_.debug("#testScan_lastModifiedOfTCaseResult repoRoot: ${JsonOutput.prettyPrint(repoRoot.toJson())}")
        when:
        TSuiteResult ts1_20180530_130604 = repoRoot.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180530_130604'))
        LocalDateTime lastModifiedOfTSuiteResult = ts1_20180530_130604.getLastModified()
        LocalDateTime lastModifiedOfTCaseResults = LocalDateTime.MIN
        List<TCaseResult> tCaseResults = ts1_20180530_130604.getTCaseResults()
        for (TCaseResult tcr : tCaseResults) {
            if (tcr.getLastModified() > lastModifiedOfTCaseResults) {
                lastModifiedOfTCaseResults = tcr.getLastModified()
            }
        }
        then:
        lastModifiedOfTSuiteResult == lastModifiedOfTCaseResults

    }

    /**
     * Test if the following files are recognized as Materials:
     * "main.TS1/20180718_142832/main.TC4/foo/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
     * "main.TS1/20180718_142832/main.TC4/foo/bar/smilechart.xls"
     *
     * These files resides under a TCaseResult directory with a subpath foo or foo/bar
     * This test is designed to confirm the subpath is appropriately recognized
     * by the RepositoryFileScanner
     *
     */
    @IgnoreRest
    def testScan_MaterialsUnderSubpath() {
        setup:
        Path casedir = workdir_.resolve("testScan_MaterialsUnderSubpath")
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryFileScanner scanner = new RepositoryFileScanner(casedir)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        when:
        TSuiteResult tsr = repoRoot.getTSuiteResult(new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180718_142832'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC4'))
        then:
        tcr != null
        when:
        logger_.debug("testScan_MaterialsUnderSubpath tcr.toJson()=${JsonOutput.prettyPrint(tcr.toJson())}")
        then:
        tcr.getMaterials().size() == 2
        tcr.getMaterial(Paths.get('foo/bar/smilechart.xls')) != null
        tcr.getMaterial(Paths.get('foo/http%3A%2F%2Fdemoaut.katalon.com%2F.png')) != null
    }

    def testScanForMiscellaneousImages() {
        setup:
        Path casedir = workdir_.resolve("testScanForMiscellaneousImages")
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryFileScanner scanner = new RepositoryFileScanner(casedir)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(
            new TSuiteName("Test Suites/main/TS1"), new TSuiteTimestamp('20180530_130604'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        List<Material> materials = tcr.getMaterials()
        then:
        materials.size() == 5
    }




    def testScanForExcel() {
        setup:
        Path casedir = workdir_.resolve("testScanForExcel")
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryFileScanner scanner = new RepositoryFileScanner(casedir)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(
            new TSuiteName("Test Suites/main/TS3"), new TSuiteTimestamp("20180627_140853"))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName("Test Cases/main/TC3"))
        // .xlsx
        when:
        Material mate = tcr.getMaterial(
            Paths.get('.'),
            new URL("http://www.kazurayam.com/carmina/example/Book1.xlsx"),
            Suffix.NULL,
            FileType.XLSX)
        then:
        assert mate != null
        // .xlsm
        when:
        mate = tcr.getMaterial(
            Paths.get('.'),
            new URL("http://www.kazurayam.com/carmina/example/Book1.xlsm"),
            Suffix.NULL,
            FileType.XLSM)
        then:
        assert mate != null
        //
    }

    def testScanForPDF() {
        setup:
        Path casedir = workdir_.resolve("testScanForPDF")
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryFileScanner scanner = new RepositoryFileScanner(casedir)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(
            new TSuiteName("Test Suites/main/TS3"), new TSuiteTimestamp("20180627_140853"))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName("Test Cases/main/TC3"))
        when:
        Material mate = tcr.getMaterial(
            Paths.get('.'),
            new URL("http://files.shareholder.com/downloads/AAPL/6323171818x0xS320193-17-70/320193/filing.pdf"),
            Suffix.NULL,
            FileType.PDF)
        then:
        assert mate != null
    }

    def testPrettyPrint() {
        setup:
        Path casedir = workdir_.resolve('testPrettyPrint')
        Helpers.copyDirectory(fixture_, casedir)
        RepositoryFileScanner scanner = new RepositoryFileScanner(casedir)
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
