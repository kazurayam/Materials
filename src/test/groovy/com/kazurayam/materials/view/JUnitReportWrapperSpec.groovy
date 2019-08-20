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

//@Ignore
class JUnitReportWrapperSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(JUnitReportWrapperSpec.class);

    // fields
    private static Path fixture_ = Paths.get("./src/test/fixture")
	private static Path workdir_
    private static Path reportsDir_
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(JUnitReportWrapperSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        reportsDir_   = workdir_.resolve('Reports')
    }
	
    def setup() {
    }
    
	def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testConstructor() {
		when:
		Path p = fixture_.resolve('Reports/main/TS1/20180805_081908/JUnit_Report.xml')
		JUnitReportWrapper instance = new JUnitReportWrapper(p)
        then:
        instance != null
    }

    def testGetTestSuiteSummary() {
        setup:
		Path p = fixture_.resolve('Reports/main/TS1/20180805_081908/JUnit_Report.xml')
		JUnitReportWrapper instance = new JUnitReportWrapper(p)
		when:
        String summary = instance.getTestSuiteSummary('Test Suites/main/TS1')
        then:
        summary == 'EXECUTED:2,FAILED:1,ERROR:0'
    }

    def testGetTestCaseStatus_PASSED() {
        setup:
		Path p = fixture_.resolve('Reports/main/TS1/20180805_081908/JUnit_Report.xml')
		JUnitReportWrapper instance = new JUnitReportWrapper(p)
		when:
        String status = instance.getTestCaseStatus('Test Cases/main/TC1')
        then:
        status == 'PASSED'
    }

    def testGetTestCaseStatus_FAILED() {
        setup:
		Path p = fixture_.resolve('Reports/main/TS1/20180805_081908/JUnit_Report.xml')
		JUnitReportWrapper instance = new JUnitReportWrapper(p)
		when:
        String status = instance.getTestCaseStatus('Test Cases/main/TC2')
        then:
        status == 'FAILED'
    }

	/**
	 * test createInstance((Path reportsDir, TSuiteResult tSuiteResult) method.
	 * Katalon Studio version 6.2.2 and the prior versions generates a JUnit_Report.xml file at
	 * - Reports/CURA/twins_exam/20190820_134959/JUnit_Report.xml
	 * 
	 * @return
	 */
	def testNewInstance_KS6_2_2() {
		setup:
		Path p = fixture_.resolve('Reports/main/TS1/20180805_081908/JUnit_Report.xml')
		TSuiteResultId tSuiteResultId = TSuiteResultId.newInstance(new TSuiteName('main/TS1'), new TSuiteTimestamp('20180805_081908'))
		JUnitReportWrapper instance = JUnitReportWrapper.newInstance(reportsDir_, tSuiteResultId)
		when:
		String summary = instance.getTestSuiteSummary('Test Suites/main/TS1')
		then:
		summary == 'EXECUTED:2,FAILED:1,ERROR:0'
	}
	
	/**
	 * test createInstance((Path reportsDir, TSuiteResult tSuiteResult) method.
	 * Katalon Studio version 6.2.2 and the prior versions generates a JUnit_Report.xml file at
	 * - Reports/20190820_133032/CURA/twins_exam/20190820_133035/JUnit_Report.xml
	 * 
	 * @return
	 */
	def testNewInstance_KS6_3_0() {
		expect:
		false
	}

    // helper methods
}
