package com.kazurayam.materials

import com.kazurayam.materials.Material
import com.kazurayam.materials.metadata.MaterialMetadataBundle
import groovy.json.JsonOutput
import org.junit.Ignore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.IgnoreRest
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

//@Ignore
class MaterialRepositorySpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositorySpec.class);

    private static Path specOutputDir_
	private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialRepository mr_
    private static TSuiteResultId tSuiteResultId_

    def setupSpec() {
        specOutputDir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(MaterialRepositorySpec.class)}")
        if (specOutputDir_.toFile().exists()) {
            Helpers.deleteDirectoryContents(specOutputDir_)
        } else {
            specOutputDir_.toFile().mkdirs()
        }

        tSuiteResultId_ = TSuiteResultId.newInstance(
                new TSuiteName('Test Suites/TS1'),
                new TExecutionProfile('CURA_ProductionEnv'),
                TSuiteTimestamp.newInstance("20180810_140105") )
    }
    def setup() {}

    def testJsonOutput() {
        setup:
        List<String> errors = []
        errors << 'Your password was bad.'
        errors << 'Your feet smell.'
        errors << 'I am having a bad day.'
        when:
        def jsonString = JsonOutput.toJson(errors)
        then:
        jsonString == '["Your password was bad.","Your feet smell.","I am having a bad day."]'
    }

	/**
	 * private Helper method to prepare an instance of MaterialRepository for each test case method in the specOutputDir
	 * This Helper helps shortening the test case methods.
	 * 
	 * @param methodName
	 * @return
	 */
	private MaterialRepository prepareMR(String methodName, TSuiteResultId tSuiteResultId) {
		Path caseDir = specOutputDir_.resolve(methodName)
        Helpers.copyDirectory(
                fixture_.resolve('Materials')
                        .resolve(tSuiteResultId.getTSuiteName().getValue())
                        .resolve(tSuiteResultId.getTExecutionProfile().getName())
                        .resolve(tSuiteResultId.getTSuiteTimestamp().format()),
                caseDir.resolve('Materials')
                        .resolve(tSuiteResultId.getTSuiteName().getValue())
                        .resolve(tSuiteResultId.getTExecutionProfile().getName())
                        .resolve(tSuiteResultId.getTSuiteTimestamp().format())
        )
		MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseDir.resolve('Materials'))
	    mr.markAsCurrent(tSuiteResultId.getTSuiteName(),
                tSuiteResultId.getTExecutionProfile(),
                tSuiteResultId.getTSuiteTimestamp())
        return mr
    }
	
	private MaterialRepository prepareMR(String methodName, TSuiteName tSuiteName) {
		Path caseDir = specOutputDir_.resolve(methodName)
		Helpers.copyDirectory(
                fixture_.resolve('Materials').resolve(tSuiteName.getValue()),
                caseDir.resolve('Materials').resolve(tSuiteName.getValue())
        )
		MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseDir.resolve('Materials'))
	    return mr
    }
	
    def test_toJsonText() {
        setup:
        String method = 'test_toJsonText'
        MaterialRepository mr = prepareMR(method, tSuiteResultId_)
		when:
        def text = mr.toJsonText()
        //logger_.debug("#${method} text=" + text)
        //logger_.debug("#${method} prettyPrint(text)=" + JsonOutput.prettyPrint(text))
        then:
        text.contains('TS1')
    }

    def test_toString() {
        setup:
        String method = 'test_toString'
        MaterialRepository mr = prepareMR(method, tSuiteResultId_)
        when:
        String str = mr.toString()
        then:
        str.contains('TS1')
    }

    def test_getCurrentTestSuiteDirectory() {
		setup:
        String method = 'test_getCurrentTestSuiteDirectory'
        MaterialRepository mr = prepareMR(method, tSuiteResultId_)
        when:
        Path tSuiteDir = mr.getCurrentTestSuiteDirectory()
        then:
		tSuiteDir != null
        tSuiteDir.getFileName().toString() == '20180810_140105'
    }
 
    def test_getSetOfMaterialPathRelativeToTSuiteTimestamp() {
		setup:
        String method = 'test_getSetOfMaterialPathRelativeToTSuiteTimestamp'
        MaterialRepository mr = prepareMR(method, tSuiteResultId_)
        when:
        TSuiteName tsn = new TSuiteName('Test Suites/TS1')
        TExecutionProfile tep = new TExecutionProfile('CURA_ProductionEnv')
        Set<Path> paths = mr.getSetOfMaterialPathRelativeToTSuiteTimestamp(tsn, tep)
        //logger_.debug( "#${method} " + paths)
        then:
        paths.size() == 1
    }

	def test_getTCaseResult() {
		setup:
        String method = 'test_getTCaseResult'
        MaterialRepository mr = prepareMR(method, tSuiteResultId_)
        when:
        TCaseResult tCaseResult =
                mr.getTCaseResult(new TSuiteName('Test Suites/TS1'),
                        new TExecutionProfile("CURA_ProductionEnv"),
                        new TSuiteTimestamp('20180810_140105'),
                        new TCaseName('Test Cases/TC1'))
        then:
        tCaseResult != null
    }
    
    def test_getTestCaseDirectory() {
		setup:
        String method = 'test_getTestCaseDirectory'
        MaterialRepository mr = prepareMR(method, tSuiteResultId_)
        when:
        Path testCaseDir = mr.getTestCaseDirectory('Test Cases/TC1')
        then:
        testCaseDir != null
        testCaseDir.getFileName().toString() == 'TC1'
    }
    
    def test_getTSuiteNameList() {
		setup:
        String method = 'test_getTSuiteNameList'
        Path caseDir = specOutputDir_.resolve(method)
		Helpers.copyDirectory(fixture_.resolve('Materials'), caseDir.resolve('Materials'))
		MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseDir.resolve('Materials'))
        when:
        List<TSuiteName> tsnList = mr.getTSuiteNameList()
        then:
        tsnList.size() == 10
    }
    
    def test_getTSuiteResult_withTSuiteNameAndTSuiteTimestamp() {
        when:
        String method = 'test_getTSuiteResult_withTSuiteNameAndTSuiteTimestamp'

        TSuiteName tsn = new TSuiteName('Test Suites/TS1')
        TExecutionProfile tep = new TExecutionProfile('CURA_ProductionEnv')
        TSuiteTimestamp tst = new TSuiteTimestamp('20180810_140105')

        TSuiteResultId tSuiteResultId = TSuiteResultId.newInstance(tsn, tep, tst)

        MaterialRepository mr = prepareMR(method, tSuiteResultId)
        TSuiteResult tsr = mr.getTSuiteResult(tSuiteResultId)
        then:
        tsr != null
        tsr.getId().getTSuiteName().equals(tsn)
        tsr.getId().getTExecutionProfile().equals(tep)
        tsr.getId().getTSuiteTimestamp().equals(tst)
        
    }

    /**
     * test MaterialRepository#getTSuiteResultList() method with
     * a fixture Materials directory that contains multiple
     * TSuiteResults of a TSuiteName
     *
     * @return
     */
    def test_getTSuiteResultList_withoutArgs() {
        setup:
        String method = 'test_getTSuiteResultList_withoutArgs'

        TSuiteName tSuiteName = new TSuiteName('Test Suites/main/TS1')
        MaterialRepository mr = prepareMR(method, tSuiteName)
        when:
        List<TSuiteResultId> tsriList = mr.getTSuiteResultIdList()
        List<TSuiteResult> list = mr.getTSuiteResultList(tsriList)
        then:
        list != null
        list.size() == 6
    }
    
	def test_getTSuiteResultIdList_withArgs() {
		setup:
        String method = 'test_getTSuiteResultIdList_withArgs'

        TSuiteName tSuiteName = new TSuiteName('Test Suites/main/TS1')
        MaterialRepository mr = prepareMR(method, tSuiteName)
        when:
        TExecutionProfile tep = new TExecutionProfile("CURA_ProductionEnv")
        List<TSuiteResultId> list = mr.getTSuiteResultIdList(tSuiteName, tep)
        then:
        list != null
        list.size() == 6
    }
    
	def test_getTSuiteResultIdList_withoutArgs() {
    	setup:
        String method = 'test_getTSuiteResultIdList'
        TSuiteName tSuiteName = new TSuiteName("Test Suites/main/TS1")
        MaterialRepository mr = prepareMR(method, tSuiteName)
        when:
        List<TSuiteResultId> list = mr.getTSuiteResultIdList()
        then:
        list != null
        list.size()== 6
    }

	def test_getTSuiteResultList_noArgs() {
		setup:
        String method = 'test_getTSuiteResultList_noArgs'

        TSuiteName tSuiteName = new TSuiteName("Test Suites/main/TS1")
        MaterialRepository mr = prepareMR(method, tSuiteName)
        when:
        List<TSuiteResult> list = mr.getTSuiteResultList()
        then:
        list != null
        list.size() == 6
    }

	def test_resolveMaterialPath() {
		setup:
        String method = 'test_resolveMaterialPath'

        MaterialRepository mr = prepareMR(method, tSuiteResultId_)
        when:
		String fileName = "http%3A%2F%2Fdemoaut.katalon.com%2F.png"
        Path path = mr.resolveMaterialPath('Test Cases/main/TC1', fileName)
        then:
        path.toString().replace('\\', '/').endsWith(
                "Materials/TS1/CURA_ProductionEnv/20180810_140105/main.TC1/${fileName}")
    }
    
	def test_resolveMaterialPath_withSubpath() {
		setup:
        String method = 'test_resolveMaterialPath_withSubpath'

        MaterialRepository mr = prepareMR(method, tSuiteResultId_)
        when:
        Path path = mr.resolveMaterialPath('Test Cases/main/TC1',
                'aaa/bbb', 'screenshot1.png')
        then:
        path.toString().replace('\\', '/').endsWith(
                'Materials/TS1/CURA_ProductionEnv/20180810_140105/main.TC1/aaa/bbb/screenshot1.png')
    }
    
	def test_resolveScreenshotPath() {
		setup:
        String method = 'test_resolveScreenshotPath'

        MaterialRepository mr = prepareMR(method, tSuiteResultId_)
        when:
        Path path = mr.resolveScreenshotPath('Test Cases/main/TC1', new URL('http://demoaut.katalon.com/'))
        then:
        path.getFileName().toString() == 'http%3A%2F%2Fdemoaut.katalon.com%2F.png'
    }

	def test_resolveScreenshotPath_byURLPathComponents_top() {
		setup:
        String method = 'test_resolveScreenshotPath_byURLPathComponents_top'

        MaterialRepository mr = prepareMR(method, tSuiteResultId_)
		when:
        Path path = mr.resolveScreenshotPathByURLPathComponents(
            'Test Cases/main/TC1', new URL('http://demoaut.katalon.com/'), 0, 'top')
        then:
        path.getFileName().toString()== 'top.png'
            path.toString().replace('\\', '/').endsWith(
                    'Materials/TS1/CURA_ProductionEnv/20180810_140105/main.TC1/top.png')
    }

	def test_resolveScreenshotPath_byURLPathComponents_login() {
		setup:
        String method = 'test_resolveScreenshotPath_byURLPathComponents_login'

        MaterialRepository mr = prepareMR(method, tSuiteResultId_)
        when:
        Path path = mr.resolveScreenshotPathByURLPathComponents(
            'Test Cases/main/TC1',
                new URL('https://katalon-demo-cura.herokuapp.com/profile.php#login'))
        then:
        path.getFileName().toString()== 'profile.php%23login.png'
        path.toString().replace('\\', '/').endsWith(
                'Materials/TS1/CURA_ProductionEnv/20180810_140105/main.TC1/profile.php%23login.png')
    }

    def test_makeIndex() {
		setup:
        String method = 'test_makeIndex'

        TSuiteName tSuiteName = new TSuiteName("Test Suites/main/TS1")
        MaterialRepository mr = prepareMR(method, tSuiteName)
		Helpers.copyDirectory(
			fixture_.resolve('Reports').resolve('main/TS1'),
			mr.getBaseDir().resolve('..').resolve('Reports').resolve('main/TS1')
			)
		when:
		Path index = mr.makeIndex()
		then:
		Files.exists(index)
	}

	def test_createMaterialPairs_TSuiteNameOnly() {
		setup:
        String method = 'test_createMaterialPairs_TSuiteNameOnly'

        TSuiteName tSuiteName = new TSuiteName("Test Suites/TS1")
        MaterialRepository mr = prepareMR(method, tSuiteName)
        when:
        MaterialPairs mps = mr.createMaterialPairs(new TSuiteName('TS1'))
        then:
        mps.size() == 1
        when:
        MaterialPair mp = mps.get(Paths.get('TC1/CURA_Healthcare_Service.png'))
        Material expected = mp.getExpected()
        Material actual = mp.getActual()
        then:
        expected.getPathRelativeToTSuiteTimestamp() == Paths.get('TC1/CURA_Healthcare_Service.png')
        actual.getPathRelativeToTSuiteTimestamp()   == Paths.get('TC1/CURA_Healthcare_Service.png')
    }
    
    
    
    /**
     * This will test deleteBaseDirContents() method.
     * This will create a fixture for its own.
     *
     * @throws IOException
     */
	def test_deleteBaseDirContents() throws IOException {
        setup:
        String method = 'test_deleteBaseDirContents'

        MaterialRepository mr = prepareMR(method, tSuiteResultId_)
        when:
        mr.deleteBaseDirContents()
        List<String> contents = mr.getBaseDir().toFile().list()
        then:
        contents.size() == 0
    }

	def test_clear() {
        setup:
        String method = 'test_clear'

        TSuiteName tsn = new TSuiteName("Test Suites/main/TS1")
        TExecutionProfile tep = new TExecutionProfile('CURA_ProductionEnv')
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180530_130419")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
        MaterialRepository mr = prepareMR(method, tsri)
        when:
        int count = mr.clear(tsri)
        then:
        count == 2
        when:
        TSuiteResult result= mr.getTSuiteResult(tsri)
        then:
        result == null
        when:
        mr.markAsCurrent(tsri)
        mr.ensureTSuiteResultPresent(tsri)
        Path tsnDir = mr.getCurrentTestSuiteDirectory()
        Path tstDir = tsnDir.resolve(tst.format())
        then:
        ! Files.exists(tstDir)
    }

    def test_clear_withArgOnlyTSuiteName() {
        setup:
        String method = 'test_clear_withArgOnlyTSuiteName'

        TSuiteName tsn = new TSuiteName("Test Suites/main/TS1")
        MaterialRepository mr = prepareMR(method, tsn)
        when:
        int count = mr.clear(tsn)        // HERE is a difference
        then:
        count == 22
        when:
        List<TSuiteResultId> tsriList = mr.getTSuiteResultIdList()
        List<TSuiteResult> list = mr.getTSuiteResultList(tsriList)
        then:
        list.size() == 0
        when:
        Path tsnDir = mr.getBaseDir().resolve(tsn.getValue())
        then:
        ! Files.exists(tsnDir)
    }

	def test_markAsCurrent_ensureTSuiteResultPresent_oneStringArg() {
		setup:
        String method = 'test_markAsCurrent_ensureTSuiteResultPresent_oneStringArg'

        MaterialRepository mr = prepareMR(method)
		when:
		Path timestampdir = mr.getCurrentTestSuiteDirectory()
		then:
		timestampdir.toString().contains('oneStringArg')
		when:
		String dirName = timestampdir.getFileName()
		LocalDateTime ldt = TSuiteTimestamp.parse(dirName)
		then:
		true
	}

	def test_markAsCurrent_ensureTSuiteResultPresent_twoStringArgs() {
		setup:
        String method = 'test_markAsCurrent_ensureTSuiteResultPresent_twoStringArgs'

        MaterialRepository mr = prepareMR(method)
		when:
		mr.markAsCurrent('twoStringArgs', '20180616_160000')
		mr.ensureTSuiteResultPresent('twoStringArgs', '20180616_160000')
		Path timestampdir = mr.getCurrentTestSuiteDirectory()
		then:
		timestampdir.toString().contains('twoStringArgs')
		timestampdir.getFileName().toString().contains('20180616_160000')
	}


    def test_markAsCurrent_ensureTSuiteResultPresent_TSuiteNameAndTSuiteTimestampAsArgs() {
        setup:
        String method = 'test_markAsCurrent_ensureTSuiteResultPresent_TSuiteNameAndTSuiteTimestampAsArgs'

        MaterialRepository mr = prepareMR(method)
        when:
        def tSuiteName = new TSuiteName('TSuiteNameAndTSuiteTimestampAsArgs')
        def tSuiteTimestamp1 = new TSuiteTimestamp('20180616_160000')
        mr.markAsCurrent(tSuiteName, tSuiteTimestamp1)
        TSuiteResult ensured1 = mr.ensureTSuiteResultPresent(tSuiteName, tSuiteTimestamp1)
        Path timestampDir = mr.getCurrentTestSuiteDirectory()
        then:
        timestampDir.toString().contains('TSuiteNameAndTSuiteTimestampAsArgs')
        timestampDir.getFileName().toString().contains('20180616_160000')
        
        when:
        def tSuiteTimestamp2 = new TSuiteTimestamp('20180505_000000')
        TSuiteResult ensured2 = mr.ensureTSuiteResultPresent(tSuiteName, tSuiteTimestamp2)
        then:
        ensured2.getTSuiteName() == tSuiteName
        ensured2.getTSuiteTimestamp() == tSuiteTimestamp2
    }

    def test_findMaterialMetadataBundleOfCurrentTSuite() {
        setup:
        String method = 'test_findMaterialMetadataBundleOfCurrentTSuite'

        MaterialRepository mr = prepareMR(method)
        when:
        def tSuiteName = new TSuiteName('TS1')
        def tSuiteTimestamp = new TSuiteTimestamp('20180810_140105')
        mr.markAsCurrent(tSuiteName, tSuiteTimestamp)
        mr.resolveScreenshotPath('TC1', new URL('http://demo-auto.katalon.com/'),
                new MaterialDescription("category text", "description text"))
        MaterialMetadataBundle mmb = mr.findMaterialMetadataBundleOfCurrentTSuite()
		then:
		mmb != null
    }
}

