package com.kazurayam.materials

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.IgnoreRest
import spock.lang.Specification

//@Ignore
class MaterialRepositorySpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositorySpec.class);

    private static Path specOutputDir_
	private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialRepository mr_

    def setupSpec() {
        specOutputDir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(MaterialRepositorySpec.class)}")
        if (specOutputDir_.toFile().exists()) {
            Helpers.deleteDirectoryContents(specOutputDir_)
        } else {
            specOutputDir_.toFile().mkdirs()
        }
		/*
        def ant = new AntBuilder()
        ant.copy(todir:specOutputDir_.toFile(), overwrite:'yes') {
            fileset(dir:fixture_) {
                exclude(name:'Materials/CURA.twins_capture/**')
                exclude(name:'Materials/CURA.twins_exam/**')
            }
        }
        */
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
	 * this Helper is intended to shorten test case methods
	 * 
	 * @param methodName
	 * @return
	 */
	private MaterialRepository prepareMR(String methodName, String tsn = 'Test Suites/TS1') {
		Path caseDir = specOutputDir_.resolve(methodName)
		TSuiteName tSuiteName = new TSuiteName(tsn)
		Helpers.copyDirectory(fixture_.resolve('Materials').resolve(tSuiteName.getValue()), caseDir.resolve('Materials').resolve(tSuiteName.getValue()))
		return MaterialRepositoryFactory.createInstance(caseDir.resolve('Materials'))
	}
	
	private MaterialRepository prepareBulkyMR(String methodName) {
		Path caseDir = specOutputDir_.resolve(methodName)
		Helpers.copyDirectory(fixture_.resolve('Materials'), caseDir.resolve('Materials'))
		return MaterialRepositoryFactory.createInstance(caseDir.resolve('Materials'))
	}
	
    def testToJsonText() {
		setup:
		MaterialRepository mr = prepareMR('testToJsonText')
		when:
        mr.markAsCurrent('Test Suites/TS1')
        def text = mr.toJsonText()
        logger_.debug("#testToJsonText text=" + text)
        logger_.debug("#testToJsonText prettyPrint(text)=" + JsonOutput.prettyPrint(text))
        then:
        text.contains('TS1')
    }

    def testConstructor_Path_tsn() {
		setup:
		MaterialRepository mr = prepareMR('testConstructor_Path_tsn')
        when:
        mr.markAsCurrent('Test Suites/TS1')
        String str = mr.toString()
        then:
        str.contains('main.TS1')
    }

    def test_getCurrentTestSuiteDirectory() {
		setup:
		MaterialRepository mr = prepareMR('test_getCurrentTestSuiteDirectory')
        when:
        mr.markAsCurrent('Test Suites/TS1','20180810_140106')
        Path tSuiteDir = mr.getCurrentTestSuiteDirectory()
        then:
		tSuiteDir != null
        tSuiteDir.getFileName().toString() == '20180810_140106'
    }
 
    def test_getSetOfMaterialPathRelativeToTSuiteTimestamp() {
		setup:
		MaterialRepository mr = prepareMR('test_getSetOfMaterialPathRelativeToTSuiteTimestamp')
        when:
        TSuiteName tsn = new TSuiteName('Test Suites/TS1')
        Set<Path> paths = mr.getSetOfMaterialPathRelativeToTSuiteTimestamp(tsn)
        logger_.debug( "#test_getSetOfMaterialPathRelativeToTSuiteTimestamp: " + paths)
        then:
        paths.size() == 1
    }

	def test_getTCaseResult() {
		setup:
		MaterialRepository mr = prepareMR('test_getTCaseResult')
        when:
        TCaseResult tCaseResult = mr.getTCaseResult(new TSuiteName('Test Suites/TS1'),
                                        new TSuiteTimestamp('20180810_140106'),
                                        new TCaseName('Test Cases/TC1'))
        then:
        tCaseResult != null    
    }
    
    def test_getTestCaseDirectory() {
		setup:
		MaterialRepository mr = prepareMR('test_getTestCaseDirectory', 'Test Suites/main/TS1')
        when:
        mr.markAsCurrent('Test Suites/main/TS1', '20180530_130419')
        Path testCaseDir = mr.getTestCaseDirectory('Test Cases/main/TC1')
        then:
        testCaseDir.getFileName().toString() == 'main.TC1'
    }
    
    def test_getTSuiteNameList() {
		setup:
		Path caseDir = specOutputDir_.resolve('test_getTSuiteNameList')
		Helpers.copyDirectory(fixture_.resolve('Materials'), caseDir.resolve('Materials'))
		MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseDir.resolve('Materials'))
        when:
        List<TSuiteName> tsnList = mr.getTSuiteNameList()
        then:
        tsnList.size() == 10
    }
    
    def test_getTSuiteResult_withTSuiteNameAndTSuiteTimestamp() {
        when:
        TSuiteName tsn = new TSuiteName('Test Suites/main/TS1')
        TSuiteTimestamp tst = new TSuiteTimestamp('20180530_130419')
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
		MaterialRepository mr = prepareMR('test_getTSuiteResult_withTSuiteNameAndTSuiteTimestamp', 'Test Suites/main/TS1')
        mr.markAsCurrent(tsri)
        TSuiteResult tsr = mr.getTSuiteResult(tsri)
        then:
        tsr != null
        tsr.getId().getTSuiteName().equals(tsn)
        tsr.getId().getTSuiteTimestamp().equals(tst)
        
    }
    
	def testGetTSuiteResultList_withTSuiteName() {
		setup:
		MaterialRepository mr = prepareBulkyMR'test_getTSuiteNameList'()
		when:
        List<TSuiteResultId> tsriList = mr.getTSuiteResultIdList(new TSuiteName('Test Suites/main/TS1'))
        List<TSuiteResult> list = mr.getTSuiteResultList(tsriList)
        then:
        list != null
        list.size() == 6
    }
    
	def test_getTSuiteResultIdList_withTSuiteName() {
		setup:
		MaterialRepository mr = prepareBulkyMR('test_getTSuiteNameList_withTSuiteName')
        when:
        List<TSuiteResultId> list = mr.getTSuiteResultIdList(new TSuiteName('Test Suites/main/TS1'))
        then:
        list != null
        list.size() == 6
    }
    
	def test_getTSuiteResultIdList() {
    	setup:
		MaterialRepository mr = prepareBulkyMR('test_getTSuiteIdList')
        when:
        List<TSuiteResultId> list = mr.getTSuiteResultIdList()
        then:
        list != null
        list.size()== 18
    }
    
	def test_getTSuiteResultList_noArgs() {
		setup:
		MaterialRepository mr = prepareBulkyMR('test_getTSuiteResultList_noArgs')
        when:
        List<TSuiteResult> list = mr.getTSuiteResultList()
        then:
        list != null
        list.size() == 18
    }

	def test_resolveMaterialPath() {
		setup:
		MaterialRepository mr = prepareMR('test_resolveMaterialPath', 'Test Suites/main/TS1')
        when:
		mr.markAsCurrent("Test Suites/main/TS1", "20180530_130419")
		mr.ensureTSuiteResultPresent("Test Suites/main/TS1", "20180530_130419")
		String fileName = "http%3A%2F%2Fdemoaut.katalon.com%2F.png"
        Path path = mr.resolveMaterialPath('Test Cases/main/TC1', fileName)
        then:
        path.toString().replace('\\', '/').endsWith("Materials/main.TS1/20180530_130419/main.TC1/${fileName}")
    }
    
	def test_resolveMaterialPath_withSubpath() {
		setup:
		MaterialRepository mr = prepareMR('test_resolveMaterialPath_withSubpath', 'Test Suites/main/TS1')
        when:
        mr.markAsCurrent('Test Suites/main/TS1','20180530_130419')
        mr.ensureTSuiteResultPresent('Test Suites/main/TS1','20180530_130419')
        Path path = mr.resolveMaterialPath('Test Cases/main/TC1', 'aaa/bbb', 'screenshot1.png')
        then:
        path.toString().replace('\\', '/').endsWith('Materials/main.TS1/20180530_130419/main.TC1/aaa/bbb/screenshot1.png')
    }
    
	def test_resolveScreenshotPath() {
		setup:
		MaterialRepository mr = prepareMR('test_resolveScreenshotPath', 'Test Suites/main/TS1')
        when:
        mr.markAsCurrent(    'Test Suites/main/TS1','20180530_130419')
        mr.ensureTSuiteResultPresent('Test Suites/main/TS1','20180530_130419')
        Path path = mr.resolveScreenshotPath('Test Cases/main/TC1', new URL('http://demoaut.katalon.com/'))
        then:
        path.getFileName().toString() == 'http%3A%2F%2Fdemoaut.katalon.com%2F(2).png'
    }

	def test_resolveScreenshotPath_byURLPathComponents_top() {
		setup:
		MaterialRepository mr = prepareMR('test_resolveScreenshotPath_byURLPathComponents_top', 'Test Suites/main/TS1')
		when:
        mr.markAsCurrent(    'Test Suites/main/TS1','20180530_130419')
        mr.ensureTSuiteResultPresent('Test Suites/main/TS1','20180530_130419')
        Path path = mr.resolveScreenshotPathByURLPathComponents(
            'Test Cases/main/TC1', new URL('http://demoaut.katalon.com/'), 0, 'top')
        then:
        path.getFileName().toString()== 'top.png'
            path.toString().replace('\\', '/').endsWith('Materials/main.TS1/20180530_130419/main.TC1/top.png')
    }

	def test_resolveScreenshotPath_byURLPathComponents_login() {
		setup:
		MaterialRepository mr = prepareMR('test_resolveScreenshotPath_byURLPathComponents_login', 'Test Suites/main/TS1')
        when:
        mr.markAsCurrent('Test Suites/main/TS1','20180530_130419')
        mr.ensureTSuiteResultPresent('Test Suites/main/TS1','20180530_130419')
        Path path = mr.resolveScreenshotPathByURLPathComponents(
            'Test Cases/main/TC1', new URL('https://katalon-demo-cura.herokuapp.com/profile.php#login'))
        then:
        path.getFileName().toString()== 'profile.php%23login.png'
        path.toString().replace('\\', '/').endsWith('Materials/main.TS1/20180530_130419/main.TC1/profile.php%23login.png')
    }

	def test_createMaterialPairs_TSuiteNameOnly() {
		setup:
		MaterialRepository mr = prepareMR('test_createMaterialPairs_TSuiteNameOnly')
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
        MaterialRepository mr = prepareMR("test_deleteBaseDirContents")
        when:
        mr.deleteBaseDirContents()
        List<String> contents = mr.getBaseDir().toFile().list()
        then:
        contents.size() == 0
    }

	def test_clear_withArgTSuiteTimestamp() {
        setup:
        MaterialRepository mr = prepareBulkyMR("test_cClear_withArgTSuiteTimestamp")
        when:
        TSuiteName tsn = new TSuiteName("Test Suites/main/TS1")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180530_130419")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
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
        MaterialRepository mr = prepareBulkyMR("test_clear_withArgOnlyTSuiteName")
        when:
        TSuiteName tsn = new TSuiteName("Test Suites/main/TS1")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180530_130419")
        int count = mr.clear(tsn)        // HERE is difference
        then:
        count == 22
        when:
        List<TSuiteResultId> tsriList = mr.getTSuiteResultIdList(tsn)
        List<TSuiteResult> list = mr.getTSuiteResultList(tsriList)
        then:
        list.size() == 0
        when:
        Path tsnDir = mr.getBaseDir().resolve(tsn.getValue())
        then:
        ! Files.exists(tsnDir)
    }

	@IgnoreRest
	def test_markAsCurrent_ensureTSuiteResultPresent_oneStringArg() {
		setup:
		MaterialRepository mr = prepareMR('test_markAsCurrent_ensureTSuiteResultPresent_oneStringArg')
		when:
		mr.markAsCurrent('oneStringArg')
		mr.ensureTSuiteResultPresent('oneStringArg')
		Path timestampdir = mr.getCurrentTestSuiteDirectory()
		then:
		timestampdir.toString().contains('oneStringArg')
		when:
		String dirName = timestampdir.getFileName()
		LocalDateTime ldt = TSuiteTimestamp.parse(dirName)
		then:
		true
	}

	@IgnoreRest
	def test_markAsCurrent_ensureTSuiteResultPresent_twoStringArgs() {
		setup:
		MaterialRepository mr = prepareMR('test_markAsCurrent_ensureTSuiteResultPresent_twoSgringArgs')
		when:
		mr.markAsCurrent('twoStringArgs', '20180616_160000')
		mr.ensureTSuiteResultPresent('twoStringArgs', '20180616_160000')
		Path timestampdir = mr.getCurrentTestSuiteDirectory()
		then:
		timestampdir.toString().contains('twoStringArgs')
		timestampdir.getFileName().toString().contains('20180616_160000')
	}

	@IgnoreRest
	def test_markAsCurrent_ensureTSuiteResultPresnt_TSuiteNameAndTSuiteTimestampAsArgs() {
		setup:
		MaterialRepository mr = prepareMR('test_markAsCurrent_ensureTSuiteResultPresnt_TSuiteNameAndTSuiteTimestampAsArgs')
		when:
		def tSuiteName = new TSuiteName('TSuiteNameAndTSuiteTimestampAsArgs')
		def tSuiteTimestamp = new TSuiteTimestamp('20180616_160000')
		mr.markAsCurrent(tSuiteName, tSuiteTimestamp)
		mr.ensureTSuiteResultPresent(tSuiteName, tSuiteTimestamp)
		Path timestampdir = mr.getCurrentTestSuiteDirectory()
		then:
		timestampdir.toString().contains('TSuiteNameAndTSuiteTimestampAsArgs')
		timestampdir.getFileName().toString().contains('20180616_160000')
	}
}

