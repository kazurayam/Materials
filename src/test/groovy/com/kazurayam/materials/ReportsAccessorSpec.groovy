package com.kazurayam.materials

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.view.ExecutionPropertiesWrapper
import com.kazurayam.materials.view.JUnitReportWrapper

import spock.lang.Specification

class ReportsAccessorSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(ReportsAccessorSpec)
    
    private static Path specOutputDir_
    private static Path fixtureKS622
	private static Path fixtureKS630
    
    def setupSpec() {
        specOutputDir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(ReportsAccessorSpec.class)}")
        fixtureKS622 = Paths.get("./src/test/fixtures/com.kazurayam.materials.ReportsAccessorSpec/KS6.2.2")
		fixtureKS630 = Paths.get("./src/test/fixtures/com.kazurayam.materials.ReportsAccessorSpec/KS6.3.0")
		
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    def test_KS622_constructingWithFactory() {
        setup:
        Path caseOutputDir = specOutputDir_.resolve("test_KS622_constructingWithFactory")
        Helpers.copyDirectory(fixtureKS622, caseOutputDir)
        Path materialsDir = caseOutputDir.resolve("Materials")
        Path reportsDir   = caseOutputDir.resolve("Reports")
        when:
        ReportsAccessor ra = ReportsAccessorFactory.createInstance(reportsDir)
        then:
        ra != null
    }
    
    def test_KS622_getJUnitReportWrapper() {
        setup:
        Path caseOutputDir = specOutputDir_.resolve("test_KS622_getJUnitReportWrapper")
        Helpers.copyDirectory(fixtureKS622, caseOutputDir)
        Path materialsDir = caseOutputDir.resolve("Materials")
        Path reportsDir   = caseOutputDir.resolve("Reports")
        //
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materialsDir)
        ReportsAccessor ra = ReportsAccessorFactory.createInstance(reportsDir)
        TSuiteName tSuiteName = new TSuiteName("Test Suites/CURA/twins_exam")
        TSuiteTimestamp tSuiteTimestamp = new TSuiteTimestamp("20190528_111335")
        TSuiteResultId tSuiteResultId = TSuiteResultId.newInstance(tSuiteName, tSuiteTimestamp)
        when:
        JUnitReportWrapper junitReportWrapper = ra.getJUnitReportWrapper(tSuiteResultId)
        then:
        junitReportWrapper != null
        when:
        String testCaseStatus = junitReportWrapper.getTestCaseStatus("Test Cases/CURA/ImageDiff_twins")
        String testSuiteSummary = junitReportWrapper.getTestSuiteSummary("Test Suites/CURA/twins_exam")
        println "#test_KS622_getJUnitReportWrapper testCaseStatus='${testCaseStatus}'"
        println "#test_KS622_getJUnitReportWrapper testSuiteSummary='${testSuiteSummary}'"
        then:
        testCaseStatus.equals("FAILED")
        testSuiteSummary.equals("EXECUTED:1,FAILED:1,ERROR:0")
    }
    
    def test_KS622_getExecutionPropertiesWrapper() {
        setup:
        Path caseOutputDir = specOutputDir_.resolve("test_KS622_getExecutionPropertiesWrapper")
        Helpers.copyDirectory(fixtureKS622, caseOutputDir)
        Path materialsDir = caseOutputDir.resolve("Materials")
        Path reportsDir   = caseOutputDir.resolve("Reports")
        //
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materialsDir)
        ReportsAccessor ra = ReportsAccessorFactory.createInstance(reportsDir)
        TSuiteName tSuiteName = new TSuiteName("Test Suites/CURA/twins_exam")
        TSuiteTimestamp tSuiteTimestamp = new TSuiteTimestamp("20190528_111335")
        TSuiteResultId tSuiteResultId = TSuiteResultId.newInstance(tSuiteName, tSuiteTimestamp)
        when:
        ExecutionPropertiesWrapper executionPropertiesWrapper = ra.getExecutionPropertiesWrapper(tSuiteResultId)
        then:
        executionPropertiesWrapper != null
        when:
        String driverName = executionPropertiesWrapper.getDriverName()
        println "#test_KS622_getExecutionPropertiesWrapper driverName='${driverName}'"
        then:
        driverName.equals('Firefox')
        when:
        TExecutionProfile executionProfile = executionPropertiesWrapper.getTExecutionProfile()
        then:
        executionProfile != null
        when:
        String name = executionProfile.getName()
        println "#test_KS622_getExecutionPropertiesWrapper name='${name}'"
        then:
        name.equals('CURA_DevelopmentEnv')
    }
    
    def test_KS622_getHrefToReport() {
        setup:
        Path caseOutputDir = specOutputDir_.resolve("test_KS622_getHrefToReport")
        Helpers.copyDirectory(fixtureKS622, caseOutputDir)
        Path materialsDir = caseOutputDir.resolve("Materials")
        Path reportsDir   = caseOutputDir.resolve("Reports")
        //
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materialsDir)
        ReportsAccessor ra = ReportsAccessorFactory.createInstance(reportsDir)
        when:
        TSuiteName tSuiteName = new TSuiteName("Test Suites/CURA/twins_exam")
        TSuiteTimestamp tSuiteTimestamp = new TSuiteTimestamp("20190528_111335")
        TSuiteResultId tSuiteResultId = TSuiteResultId.newInstance(tSuiteName, tSuiteTimestamp)
        TSuiteResult tSuiteResult = mr.getTSuiteResult(tSuiteResultId)
        TCaseName tCaseName = new TCaseName("Test Cases/CURA/ImageDiff_twins")
        TCaseResult tCaseResult = tSuiteResult.getTCaseResult(tCaseName)
        Material material = tCaseResult.getMaterial(Paths.get('CURA.visitSite/home(47.87)FAILED.png'))
        then:
        material != null
        when:
        String href = ra.getHrefToReport(material)
        println "#test_KS622_getHrefToReport getHrefToReport href='${href}'"
        then:
        href != null
        href == '../Reports/CURA/twins_exam/20190528_111335/20190528_111335.html'
    }
	
	def test_KS630_getJUnitReportWrapper() {
		setup:
		Path caseOutputDir = specOutputDir_.resolve("test_KS630_getJUnitReportWrapper")
		Helpers.copyDirectory(fixtureKS630, caseOutputDir)
		Path materialsDir = caseOutputDir.resolve("Materials")
		Path reportsDir   = caseOutputDir.resolve("Reports")
		//
		MaterialRepository mr = MaterialRepositoryFactory.createInstance(materialsDir)
		ReportsAccessor ra = ReportsAccessorFactory.createInstance(reportsDir)
		TSuiteName tSuiteName = new TSuiteName("Test Suites/CURA/twins_exam")
		TSuiteTimestamp tSuiteTimestamp = new TSuiteTimestamp("20190821_143321")
		TSuiteResultId tSuiteResultId = TSuiteResultId.newInstance(tSuiteName, tSuiteTimestamp)
		when:
		JUnitReportWrapper junitReportWrapper = ra.getJUnitReportWrapper(tSuiteResultId)
		then:
		junitReportWrapper != null
		when:
		String testCaseStatus = junitReportWrapper.getTestCaseStatus("Test Cases/CURA/ImageDiff_twins")
		String testSuiteSummary = junitReportWrapper.getTestSuiteSummary("Test Suites/CURA/twins_exam")
		println "#test_KS630_getJUnitReportWrapper testCaseStatus='${testCaseStatus}'"
		println "#test_KS630_getJUnitReportWrapper testSuiteSummary='${testSuiteSummary}'"
		then:
		testCaseStatus.equals("FAILED")
		testSuiteSummary.equals("EXECUTED:1,FAILED:1,ERROR:0")
	}
	
	def test_KS630_getExecutionPropertiesWrapper() {
		setup:
		Path caseOutputDir = specOutputDir_.resolve("test_KS630_getExecutionPropertiesWrapper")
		Helpers.copyDirectory(fixtureKS630, caseOutputDir)
		Path materialsDir = caseOutputDir.resolve("Materials")
		Path reportsDir   = caseOutputDir.resolve("Reports")
		//
		MaterialRepository mr = MaterialRepositoryFactory.createInstance(materialsDir)
		ReportsAccessor ra = ReportsAccessorFactory.createInstance(reportsDir)
		TSuiteName tSuiteName = new TSuiteName("Test Suites/CURA/twins_exam")
		TSuiteTimestamp tSuiteTimestamp = new TSuiteTimestamp("20190821_143321")
		TSuiteResultId tSuiteResultId = TSuiteResultId.newInstance(tSuiteName, tSuiteTimestamp)
		when:
		ExecutionPropertiesWrapper executionPropertiesWrapper = ra.getExecutionPropertiesWrapper(tSuiteResultId)
		then:
		executionPropertiesWrapper != null
		when:
		String driverName = executionPropertiesWrapper.getDriverName()
		println "#test_KS630_getExecutionPropertiesWrapper driverName='${driverName}'"
		then:
		driverName.equals('Firefox')
		when:
		TExecutionProfile executionProfile = executionPropertiesWrapper.getTExecutionProfile()
		then:
		executionProfile != null
		when:
		String name = executionProfile.getName()
		println "#test_KS630_getExecutionPropertiesWrapper name='${name}'"
		then:
		name.equals('CURA_DevelopmentEnv')
	}

	def test_KS630_getHrefToReport() {
		setup:
		Path caseOutputDir = specOutputDir_.resolve("test_KS630_getHrefToReport")
		Helpers.copyDirectory(fixtureKS630, caseOutputDir)
		Path materialsDir = caseOutputDir.resolve("Materials")
		Path reportsDir   = caseOutputDir.resolve("Reports")
		//
		MaterialRepository mr = MaterialRepositoryFactory.createInstance(materialsDir)
		ReportsAccessor ra = ReportsAccessorFactory.createInstance(reportsDir)
		when:
		TSuiteName tSuiteName = new TSuiteName("Test Suites/CURA/twins_exam")
		TSuiteTimestamp tSuiteTimestamp = new TSuiteTimestamp("20190821_143321")
		TSuiteResultId tSuiteResultId = TSuiteResultId.newInstance(tSuiteName, tSuiteTimestamp)
		TSuiteResult tSuiteResult = mr.getTSuiteResult(tSuiteResultId)
		TCaseName tCaseName = new TCaseName("Test Cases/CURA/ImageDiff_twins")
		TCaseResult tCaseResult = tSuiteResult.getTCaseResult(tCaseName)
		Material material = tCaseResult.getMaterial(Paths.get('CURA.visitSite/home(7.23)FAILED.png'))
		then:
		material != null
		when:
		String href = ra.getHrefToReport(material)
		println "#test_KS630_getHrefToReport getHrefToReport href='${href}'"
		then:
		href != null
		href == '../Reports/20190821_143318/CURA/twins_exam/20190821_143321/20190821_143321.html'
		Files.exists(materialsDir.resolve(href))
	}

}
