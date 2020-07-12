package com.kazurayam.materials

import com.kazurayam.materials.repository.TreeTrunkScanner

import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.impl.MaterialImpl
import com.kazurayam.materials.model.Suffix

import com.kazurayam.materials.repository.RepositoryRoot

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class MaterialSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(MaterialSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static RepositoryRoot repoRoot_
    private static TCaseResult tcr_



    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(MaterialSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        Path materials = workdir_.resolve('Materials')
        TreeTrunkScanner scanner = new TreeTrunkScanner(materials)
        scanner.scan()
        repoRoot_ = scanner.getRepositoryRoot()
    }
    def setup() {
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile('CURA_ProductionEnv'),
                TSuiteTimestamp.newInstance('20180530_130419'))
        tcr_ = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
    }

    // helper
    private static List<Material> getMaterialList(RepositoryRoot repoRoot,
                                   TSuiteName tSuiteName,
                                   TExecutionProfile tExecutionProfile,
                                   TSuiteTimestamp tSuiteTimestamp) {
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(tExecutionProfile, "tExecutionProfile must not be null")
        Objects.requireNonNull(tSuiteTimestamp, "tSuiteTimestamp must not be null")
        List<Material> list = new ArrayList<Material>()
        for (TSuiteResult tsr : repoRoot.getSortedTSuiteResults()) {
            if (tsr.getId().getTSuiteName().equals(tSuiteName) &&
                    tsr.getId().getTExecutionProfile().equals(tExecutionProfile) &&
                    tsr.getId().getTSuiteTimestamp().equals(tSuiteTimestamp)
            ) {
                List<Material> mates = tsr.getMaterialList()
                for (Material mate : mates) {
                    list.add(mate)
                }
            }
        }
        return Collections.unmodifiableList(list)
    }

    // feature methods
    def testCompareTo_byFileType() {
        when:
        Material mate1 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.JPG)
        Material mate2 = new MaterialImpl(tcr_, '.', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        then:
        mate1.compareTo(mate2) < 0
        mate2.compareTo(mate1) > 0
    }

    def testCompareTo_bySuffix() {
        when:
        Material mate1 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        Material mate2 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            new Suffix(1), FileType.PNG)
        then:
        mate1.compareTo(mate2) > 0
        mate2.compareTo(mate1) < 0
    }

    def testCompareTo_byURL() {
        when:
        Material mate1 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        Material mate2 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/abc'),
                                            Suffix.NULL, FileType.PNG)
        then:
        mate1.compareTo(mate2) < 0
        when:
        Material mate3 = new MaterialImpl(tcr_, '', new URL('https://aaa.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        then:
        mate1.compareTo(mate3) > 0
    }

    def testCompareTo_equal() {
        when:
        Material mate1 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        Material mate2 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        then:
        mate1.compareTo(mate2) == 0
    }


    
    def testConstructorWithTCaseResultAndFilePath() {
        setup:
        Path filePath = repoRoot_.getBaseDir().resolve('main.TS1/CURA_ProductionEnv/20180530_130419/main.TC1/foo/bar/fixture.xls')
        when:
        Material mate = new MaterialImpl(tcr_, filePath)
        then:
        mate.getParent() == tcr_
        mate.getTCaseResult() == tcr_
        mate.getURL() == null
        mate.getSuffix() == Suffix.NULL
        mate.getFileType() == FileType.XLS
        mate.getSubpath().replace(File.separator, '/') == 'foo/bar'
        mate.getPath().toString().contains('main.TS1/CURA_ProductionEnv/20180530_130419/main.TC1/foo/bar/fixture.xls'.replace('/', File.separator))
    }

    def testEquals() {
        when:
        Material mate1 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        Material mate2 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        then:
        mate1 != null
        mate2 != null
        mate1 == mate2
    }

    def testEquals_differentURL() {
        Material mate1 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        when:
        Material mate3 = new MaterialImpl(tcr_, '', new URL('https://www.yahoo.com/'),
                                            Suffix.NULL, FileType.PNG)
        then:
        mate3 != null
        mate1 != mate3
    }

    def testEquals_differentSuffix() {
        Material mate1 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        when:
        Material mate3 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            new Suffix(1), FileType.PNG)
        then:
        mate3 != null
        mate1 != mate3
    }

    def testEquals_differentFileType() {
        Material mate1 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        when:
        Material mate3 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.JPEG)
        then:
        mate3 != null
        mate1 != mate3
    }

    def testGetEncodedHrefRelativeToRepositoryRoot() {
        when:
        Material mate = tcr_.getMaterial('', new URL('http://demoaut.katalon.com/'),
                                            new Suffix(1), FileType.PNG)
        then:
        mate != null
        when:
        String href = mate.getEncodedHrefRelativeToRepositoryRoot()
        then:
        href != null
        href == 'main.TS1/CURA_ProductionEnv/20180530_130419/main.TC1/http%253A%252F%252Fdemoaut.katalon.com%252F(1).png'
        !href.contains('file:///')
    }

    def testGetFileName() {
        when:
        List<Material> materials = getMaterialList(repoRoot_,
                new TSuiteName("Test Suites/main/TS1"),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance("20180718_142832"))
        then:
        materials.size() > 0
        when:
        Material mate = materials[0]
        then:
        mate.getFileName().equals("smilechart.xls")      // "main.TC4/foo/bar/smilechart.xls"
    }

    def testGetHrefRelativeToRepositoryRoot() {
        when:
        Material mate = tcr_.getMaterial('', new URL('http://demoaut.katalon.com/'),
                                            new Suffix(1), FileType.PNG)
        then:
        mate != null
        when:
        String href = mate.getHrefRelativeToRepositoryRoot()
        then:
        href != null
        href == 'main.TS1/CURA_ProductionEnv/20180530_130419/main.TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png'
        !href.contains('file:///')
    }

    def testGetIdentifier_FileTypeOmmited() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS3'),
                new TExecutionProfile('default'),
                TSuiteTimestamp.newInstance('20180627_140853'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC3'))
        when:
        Material mate = tcr.getMaterial('', new URL('http://files.shareholder.com/downloads/AAPL/6323171818x0xS320193-17-70/320193/filing.pdf'),
            Suffix.NULL, FileType.PDF)
        then:
        mate != null
        when:
        String title = mate.getIdentifier()
        logger_.debug("#testGetModalWindowTitle_FileTypeOmmited title=${title}")
        then:
        title == 'http://files.shareholder.com/downloads/AAPL/6323171818x0xS320193-17-70/320193/filing.pdf'
    }
    
    def testGetIdentifier_withoutSuffix() {
        setup:
        Material mate = new MaterialImpl(tcr_, '', new URL('http://demoaut.katalon.com/'),
                                            Suffix.NULL, FileType.PNG)
        when:
        String title = mate.getIdentifier()
        then:
        title == 'http://demoaut.katalon.com/ PNG'
    }


    def testGetIdentifier_withSuffix() {
        when:
        Material mate = new MaterialImpl(tcr_, '', new URL('http://demoaut.katalon.com/'),
                                            new Suffix(1), FileType.PNG)
        then:
        mate != null
        when:
        String title = mate.getIdentifier()
        then:
        title == 'http://demoaut.katalon.com/ (1) PNG'
    }

    def testGetIdentifier_ExcelFile() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS4'),
                new TExecutionProfile('default'),
                TSuiteTimestamp.newInstance('20180712_142755'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        Material mate = tcr.getMaterial(Paths.get('smilechart.xls'))
        then:
        mate != null
        when:
        String id = mate.getIdentifier()
        then:
        id == 'smilechart.xls'
    }

    /**
     *
     * @return
     */
    def testGetParentDirectoryPathRelativeToTSuiteResult() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile('CURA_ProductionEnv'),
                TSuiteTimestamp.newInstance('20180718_142832'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC4'))
        Material mate = tcr.getMaterial('foo', new URL('http://demoaut.katalon.com/'),
                                            Suffix.NULL, FileType.PNG)
        logger_.debug("#testGetDirpathRelativeToTSuiteResult mate.getSubpath()=${mate.getParentDirectoryPathRelativeToTSuiteResult()}")
        then:
        mate != null
        mate.getParentDirectoryPathRelativeToTSuiteResult().equals(Paths.get('main.TC4/foo'))
    }


    def testGetPath() {
        when:
        Material mate = tcr_.getMaterial('', new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        then:
        mate != null
        when:
        Path path = mate.getPath()
        logger_.debug("#testGetPath path=${path.toString()}")
        then:
        path.toString().contains(
            'main.TS1/CURA_ProductionEnv/20180530_130419/main.TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png'.replace('/', File.separator))
        !path.toString().contains('..')   // should be normalized
        mate.fileExists()
    }

    def testGetPath_Excel() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS4'),
                new TExecutionProfile('default'),
                TSuiteTimestamp.newInstance('20180712_142755'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        List<Material> materials = tcr.getMaterialList()
        for (Material mate : materials) {
            logger_.debug("#testGetPath_Excel mate.getPath()=${mate.getPath()}")
            assert !mate.getPath().contains('..')
            mate.fileExists()
        }
        then:
        true
    }

    def testGetPath_withSubpath() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile('CURA_ProductionEnv'),
                TSuiteTimestamp.newInstance('20180718_142832'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC4'))
        Material png = tcr.getMaterial(Paths.get('foo/http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
        then:
        png != null
        png.getPath().toString().contains(
                'main.TS1/CURA_ProductionEnv/20180718_142832/main.TC4/foo/http%3A%2F%2Fdemoaut.katalon.com%2F.png'.replace('/', File.separator))
        png.fileExists()
    }
    
    /**
     * It is possible to instanciate a Material object which does NOT have a file on disk
     * 
     * @return
     */
    def test_fileExists_falsy() {
        when:
        Material mate = new MaterialImpl(tcr_, '', new URL('https://demoaut.katalon.com/'),
                                Suffix.NULL, FileType.PNG)
        then:
        ! mate.fileExists()
    }

    def testGetPathRelativeToTSuiteTimestamp() {
        when:
        Material mate = tcr_.getMaterial('', new URL('http://demoaut.katalon.com/'), new Suffix(1), FileType.PNG)
        Path relative = mate.getPathRelativeToTSuiteTimestamp()
        then:
        relative != null
        relative.toString().replace(File.separator, '/') == 'main.TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png'
    }

    def testGetSubpath_noSubpath() {
        when:
        Material mate = tcr_.getMaterial('', new URL('http://demoaut.katalon.com/'),
                                            new Suffix(1), FileType.PNG)
        then:
        mate != null
        mate.getSubpath() == ''
    }

    def testGetSubpath_withoutSubpath() {
        when:
        List<Material> materials = getMaterialList(repoRoot_,
                new TSuiteName("Test Suites/main/TS1"),
                new TExecutionProfile('CURA_ProductionEnv'),
                TSuiteTimestamp.newInstance("20180530_130419"))
        then:
        materials.size() > 0
        when:
        Material mate = materials[0]
        then:
        mate.getSubpath() == ''    // "main.TC4/smilechart.xls" has no subpath in between TCaseName and fileName
    }

    def testGetSubpath_withSubpath() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile('CURA_ProductionEnv'),
                TSuiteTimestamp.newInstance('20180718_142832'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC4'))
        Material mate = tcr.getMaterial('foo', new URL('http://demoaut.katalon.com/'),
                                            Suffix.NULL, FileType.PNG)
        logger_.debug("#testGetSDirpath_withSubpath mate.getSubpath()=${mate.getSubpath()}")
        then:
        mate != null
        mate.getSubpath() == 'foo'
    }
    
    def testGetSubpath_withSubpath_onceMore() {
        when:
        List<Material> materials = getMaterialList(repoRoot_,
                new TSuiteName("Test Suites/main/TS1"),
                new TExecutionProfile('CURA_ProductionEnv'),
                TSuiteTimestamp.newInstance("20180718_142832"))
        then:
        materials.size() > 0
        when:
        Material mate = materials[0]
        then:
        mate.getSubpath().equals("foo/bar".replace('/', File.separator))    // "main.TC4/foo/bar/smilechart.xls"
    }

    def testGetTCaseName() {
        when:
        List<Material> materials = getMaterialList(repoRoot_,
                new TSuiteName("Test Suites/main/TS1"),
                new TExecutionProfile('CURA_ProductionEnv'),
                TSuiteTimestamp.newInstance("20180718_142832"))
        then:
        materials.size() > 0
        when:
        Material mate = materials[0]
        then:
        mate.getTCaseName().equals(new TCaseName("main/TC4"))    // "main.TC4/foo/bar/smilechart.xls"
    }

    def testHashCode() {
        when:
        Material mate1 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        Material mate2 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        then:
        mate1.hashCode() == mate2.hashCode()
        when:
        Material mate3 = new MaterialImpl(tcr_, '', new URL('https://www.google.com/'),
                                            new Suffix(1), FileType.PNG)
        then:
        mate1.hashCode() != mate3.hashCode()
    }

    def testHashCodeWithAncestors() {
        setup:
        TSuiteResult tsr1 = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile('CURA_ProductionEnv'),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TSuiteResult tsr2 = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS2'),
                new TExecutionProfile('CURA_ProductionEnv'),
                TSuiteTimestamp.newInstance('20180612_111256'))
        TCaseResult tcr1 = tsr1.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        TCaseResult tcr2 = tsr2.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        Material mate1 = new MaterialImpl(tcr1, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        Material mate2 = new MaterialImpl(tcr2, '', new URL('https://www.google.com/'),
                                            Suffix.NULL, FileType.PNG)
        logger_.debug("#testHashCodeWithAncestors mate1.hashCode()=${mate1.hashCode()}")
        logger_.debug("#testHashCodeWithAncestors mate2.hashCode()=${mate2.hashCode()}")
        then:
        // Path determines the hashCode value.
        mate1.hashCode() != mate2.hashCode()
    }

    def testSetGetLastModified_long() {
        setup:
        Material mate = new MaterialImpl(tcr_, '', new URL('http://demoaut.katalon.com/'),
                                            new Suffix(3), FileType.PNG)
        LocalDateTime ldtNow = LocalDateTime.now()
        ZonedDateTime zdt = ldtNow.atZone(ZoneId.of("UTC"))
        Instant instantNow = zdt.toInstant()
        long longNow = instantNow.toEpochMilli()
        when:
        mate.setLastModified(longNow)
        then:
        mate.getLastModified() == ldtNow
    }
    
    def testSetGetLength() {
        setup:
        Material mate = new MaterialImpl(tcr_, '', new URL('http://demoaut.katalon.com/'),
                                            new Suffix(3), FileType.PNG)
        long length = 2_000_000
        when:
        Material result = mate.setLength(length)
        then:
        mate.getLength() == length
        result.getLength() == length 
    }

    def testSetParent_GetParent() {
        when:
        Material mate = new MaterialImpl(tcr_, '', new URL('http://demoaut.katalon.com/'),
                                            new Suffix(2), FileType.PNG)
        Material modified = mate.setParent(tcr_)
        then:
        modified.getParent() == tcr_
    }
    

    def testString() {
        when:
        Material mate = tcr_.getMaterial('', new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        def str = mate.toString()
        println("#testToJson:\n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"Material":{"url":"')
        str.contains('"suffix":')
        str.contains('"path":')
        str.contains(Helpers.escapeAsJsonText(mate.getPath().toString()))
        str.contains('"fileType":')
        str.endsWith('"}}')
    }
}
