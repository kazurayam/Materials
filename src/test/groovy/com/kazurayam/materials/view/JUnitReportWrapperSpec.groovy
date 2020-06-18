package com.kazurayam.materials.view

import com.kazurayam.materials.Helpers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

//@Ignore
class JUnitReportWrapperSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(JUnitReportWrapperSpec.class);

    // fields
    private static Path fixtureKS6_2_2 = Paths.get("./src/test/fixture")
	private static Path reportsDirKS6_2_2
	
    // fixture methods
    def setupSpec() {
        Path workdir = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(JUnitReportWrapperSpec.class)}/KS6.2.2")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixtureKS6_2_2, workdir)
        reportsDirKS6_2_2   = workdir.resolve('Reports')
		
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

    def testGetTestSuiteTime() {
        setup:
        Path p = fixtureKS6_2_2.resolve('Reports/main/TS1/20180805_081908/JUnit_Report.xml')
        JUnitReportWrapper instance = new JUnitReportWrapper(p)
        when:
        String time = instance.getTestSuiteTime('Test Suites/main/TS1')
        then:
        time == '49'
    }

    // helper methods
}
