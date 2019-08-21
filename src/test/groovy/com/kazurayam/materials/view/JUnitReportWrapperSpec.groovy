package com.kazurayam.materials.view

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.impl.MaterialRepositoryImpl

import spock.lang.Specification
import spock.lang.Ignore
import spock.lang.IgnoreRest

//@Ignore
class JUnitReportWrapperSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(JUnitReportWrapperSpec.class);

    // fields
    private static Path fixtureKS6_2_2 = Paths.get("./src/test/fixture")
	private static Path fixtureKS6_3_0 = Paths.get("./src/test/fixture_KS6.3.0")
    private static Path reportsDirKS6_2_2
	private static Path reportsDirKS6_3_0
    
    // fixture methods
    def setupSpec() {
        Path workdir = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(JUnitReportWrapperSpec.class)}/KS6.2.2andOlder")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixtureKS6_2_2, workdir)
        reportsDirKS6_2_2   = workdir.resolve('Reports')
		//
		workdir = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(JUnitReportWrapperSpec.class)}/KS6.3.0andNewer")
		if (!workdir.toFile().exists()) {
			workdir.toFile().mkdirs()
		}
		Helpers.copyDirectory(fixtureKS6_3_0, workdir)
		reportsDirKS6_3_0   = workdir.resolve('Reports')
    }
	
    def setup() {}
	def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testConstructor() {
		when:
		Path p = fixtureKS6_2_2.resolve('Reports/main/TS1/20180805_081908/JUnit_Report.xml')
		JUnitReportWrapper instance = new JUnitReportWrapper(p)
        then:
        instance != null
    }

    def testGetTestSuiteSummary() {
        setup:
		Path p = fixtureKS6_2_2.resolve('Reports/main/TS1/20180805_081908/JUnit_Report.xml')
		JUnitReportWrapper instance = new JUnitReportWrapper(p)
		when:
        String summary = instance.getTestSuiteSummary('Test Suites/main/TS1')
        then:
        summary == 'EXECUTED:2,FAILED:1,ERROR:0'
    }

    def testGetTestCaseStatus_PASSED() {
        setup:
		Path p = fixtureKS6_2_2.resolve('Reports/main/TS1/20180805_081908/JUnit_Report.xml')
		JUnitReportWrapper instance = new JUnitReportWrapper(p)
		when:
        String status = instance.getTestCaseStatus('Test Cases/main/TC1')
        then:
        status == 'PASSED'
    }

    def testGetTestCaseStatus_FAILED() {
        setup:
		Path p = fixtureKS6_2_2.resolve('Reports/main/TS1/20180805_081908/JUnit_Report.xml')
		JUnitReportWrapper instance = new JUnitReportWrapper(p)
		when:
        String status = instance.getTestCaseStatus('Test Cases/main/TC2')
        then:
        status == 'FAILED'
    }

	/**
	 * test createInstance((Path reportsDir, TSuiteResult tSuiteResult) method.
	 * Katalon Studio version 6.2.2 and older versions generates a JUnit_Report.xml file at
	 * - Reports/CURA/twins_exam/20190820_134959/JUnit_Report.xml
	 * 
	 * @return
	 */
	def testNewInstance_KS6_2_2() {
		setup:
		//Path p = fixtureKS6_2_2.resolve('Reports/main/TS1/20180805_081908/JUnit_Report.xml')
		TSuiteResultId tSuiteResultId = TSuiteResultId.newInstance(new TSuiteName('main/TS1'), new TSuiteTimestamp('20180805_081908'))
		when:
		JUnitReportWrapper instance = JUnitReportWrapper.newInstance(reportsDirKS6_2_2, tSuiteResultId)
		then:
		assert instance != null
		when:
		String summary = instance.getTestSuiteSummary('Test Suites/main/TS1')
		then:
		summary == 'EXECUTED:2,FAILED:1,ERROR:0'
	}
	
	/**
	 * test createInstance((Path reportsDir, TSuiteResult tSuiteResult) method.
	 * Katalon Studio version 6.3.0 and newer versions generates a JUnit_Report.xml file at
	 * - Reports/20190821_143318/CURA/twins_exam/20190821_143321/JUnit_Report.xml
	 * 
	 * @return
	 */
	@IgnoreRest
	def testNewInstance_KS6_3_0() {
		setup:
		TSuiteResultId tSuiteResultId = TSuiteResultId.newInstance(new TSuiteName("Test Suites/CURA/twins_exam"), new TSuiteTimestamp('20190821_143321'))
		when:
		JUnitReportWrapper instance = JUnitReportWrapper.newInstance(reportsDirKS6_3_0, tSuiteResultId)
		then:
		assert instance != null
		when:
		String summary = instance.getTestSuiteSummary('Test Suites/CURA/twins_exam')
		then:
		summary == 'EXECUTED:1,FAILED:1,ERROR:0'
	}

    // helper methods
}
