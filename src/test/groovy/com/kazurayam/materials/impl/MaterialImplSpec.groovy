package com.kazurayam.materials.impl

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.impl.MaterialImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class MaterialImplSpec extends Specification {
	
	// fields
	static Logger logger_ = LoggerFactory.getLogger(MaterialImplSpec.class)

	private static Path fixtureDir
    private static Path fixture_origin
	private static Path specOutputDir
	

	// fixture methods
	def setupSpec() {
		Path projectDir = Paths.get(".")
		fixtureDir = projectDir.resolve("src/test/fixture")
        fixture_origin = projectDir.resolve("src/test/fixture_origin")
		Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
		specOutputDir = testOutputDir.resolve(Helpers.getClassShortName(MaterialImplSpec.class))
	}
	def setup() {}
	def cleanup() {}
	def cleanupSpec() {}
	
	// feature methods
	def test_getEncodedHrefRelativeToRepositoryRoot_URLbased() {
		setup:
		Path caseOutputDir = specOutputDir.resolve('test_getEncodedHrefRelativeToRepositoryRoot_URLbased')
		Path materials = caseOutputDir.resolve('Materials').normalize()
		Files.createDirectories(materials)
		Helpers.copyDirectory(fixtureDir.resolve('Materials'), materials)
		when:
		MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
		TSuiteResult tSuiteResult = 
			mr.getTSuiteResult(
				TSuiteResultIdImpl.newInstance(
						new TSuiteName('CURA/twins_capture'),
						new TExecutionProfile("default"),
						new TSuiteTimestamp('20190412_161621')))
		assert tSuiteResult != null
		TCaseResult tCaseResult = tSuiteResult.getTCaseResult(
			new TCaseName('CURA/visitSite'))
		assert tCaseResult != null
		MaterialImpl mi = new MaterialImpl(tCaseResult,
			materials.resolve('CURA.twins_capture/default/20190412_161621/CURA.visitSite/appointment.php%23summary.png'))
		String s = mi.getEncodedHrefRelativeToRepositoryRoot()
		then:
		assert s.equals(
			'CURA.twins_capture/default/20190412_161621/CURA.visitSite/appointment.php%2523summary.png')
	}

	@Ignore
	def test_getEncodedHrefRelativeToRepositoryRoot_CJK() {
		setup:
		Path caseOutputDir = specOutputDir.resolve('test_getEncodedHrefRelativeToRepositoryRoot_CJK')
		Path materials = caseOutputDir.resolve('Materials').normalize()
		Files.createDirectories(materials)
		Helpers.copyDirectory(fixtureDir.resolve('Materials'), materials)
		when:
		MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
		TSuiteResult tSuiteResult =
			mr.getTSuiteResult(
				TSuiteResultIdImpl.newInstance(
						new TSuiteName('CURA/twins_capture'),
						new TExecutionProfile("default"),
						new TSuiteTimestamp('20190412_161621')))
		assert tSuiteResult != null
		TCaseResult tCaseResult = tSuiteResult.getTCaseResult(
			new TCaseName('CURA/visitSite'))
		assert tCaseResult != null
		MaterialImpl mi = new MaterialImpl(tCaseResult,
			materials.resolve('CURA.twins_capture/default/20190412_161621/CURA.visitSite/トップ.png'))
		String s = mi.getEncodedHrefRelativeToRepositoryRoot()
		then:
		//assert s.equals(
		//	'CURA.twins_capture/default/20190412_161621/CURA.visitSite/トップ.png')
        assert s.equals(
          'CURA.twins_capture/default/20190412_161621/CURA.visitSite/%E3%83%88%E3%83%83%E3%83%95%E3%82%9A.png')
        
	}

}
