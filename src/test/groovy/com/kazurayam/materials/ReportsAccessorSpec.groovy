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
    private static Path fixtureDir_
    
    def setupSpec() {
        specOutputDir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(ReportsAccessorSpec.class)}")
        fixtureDir_ = Paths.get("./src/test/fixtures/com.kazurayam.materials.ReportsAccessorSpec")
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    def test_constructingWithFactory() {
        setup:
        Path caseOutputDir = specOutputDir_.resolve("test_constructingWithFactory")
        Helpers.copyDirectory(fixtureDir_, caseOutputDir)
        Path materialsDir = caseOutputDir.resolve("Materials")
        Path reportsDir   = caseOutputDir.resolve("Reports")
        when:
        ReportsAccessor ra = ReportsAccessorFactory.createInstance(reportsDir)
        then:
        ra != null
    }
    
    def test_getJUnitReportWrapper() {
        setup:
        Path caseOutputDir = specOutputDir_.resolve("test_getJUnitReportWrapper")
        Helpers.copyDirectory(fixtureDir_, caseOutputDir)
        Path materialsDir = caseOutputDir.resolve("Materials")
        Path reportsDir   = caseOutputDir.resolve("Reports")
        //
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materialsDir)
        ReportsAccessor ra = ReportsAccessorFactory.createInstance(reportsDir)
        TSuiteName tSuiteName = new TSuiteName("Test Suites/CURA/twins_exam")
        TSuiteTimestamp tSuiteTimestamp = new TSuiteTimestamp("20190528_111335")
        TSuiteResultId tSuiteResultId = TSuiteResultId.newInstance(tSuiteName, tSuiteTimestamp)
        TSuiteResult tSuiteResult = mr.getTSuiteResult(tSuiteResultId)
        when:
        JUnitReportWrapper junitReportWrapper = ra.getJUnitReportWrapper(tSuiteResult)
        then:
        junitReportWrapper != null
        when:
        String testCaseStatus = junitReportWrapper.getTestCaseStatus("Test Cases/CURA/ImageDiff_twins")
        String testSuiteSummary = junitReportWrapper.getTestSuiteSummary("Test Suites/CURA/twins_exam")
        println "#test_getJUnitReportWrapper testCaseStatus='${testCaseStatus}'"
        println "#test_getJUnitReportWrapper testSuiteSummary='${testSuiteSummary}'"
        then:
        testCaseStatus.equals("FAILED")
        testSuiteSummary.equals("EXECUTED:1,FAILED:1,ERROR:0")
    }
    
    def test_getExecutionPropertiesWrapper() {
        setup:
        Path caseOutputDir = specOutputDir_.resolve("test_getExecutionPropertiesWrapper")
        Helpers.copyDirectory(fixtureDir_, caseOutputDir)
        Path materialsDir = caseOutputDir.resolve("Materials")
        Path reportsDir   = caseOutputDir.resolve("Reports")
        //
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materialsDir)
        ReportsAccessor ra = ReportsAccessorFactory.createInstance(reportsDir)
        TSuiteName tSuiteName = new TSuiteName("Test Suites/CURA/twins_exam")
        TSuiteTimestamp tSuiteTimestamp = new TSuiteTimestamp("20190528_111335")
        TSuiteResultId tSuiteResultId = TSuiteResultId.newInstance(tSuiteName, tSuiteTimestamp)
        TSuiteResult tSuiteResult = mr.getTSuiteResult(tSuiteResultId)
        when:
        ExecutionPropertiesWrapper executionPropertiesWrapper = ra.getExecutionPropertiesWrapper(tSuiteResult)
        then:
        executionPropertiesWrapper != null
        when:
        String driverName = executionPropertiesWrapper.getDriverName()
        println "#test_getExecutionPropertiesWrapper driverName='${driverName}'"
        then:
        driverName.equals('Firefox')
        when:
        ExecutionProfile executionProfile = executionPropertiesWrapper.getExecutionProfile()
        then:
        executionProfile != null
        when:
        String name = executionProfile.getName()
        println "#test_getExecutionPropertiesWrapper name='${name}'"
        then:
        name.equals('CURA_DevelopmentEnv')
    }
    
    def test_getHrefToReport() {
        setup:
        Path caseOutputDir = specOutputDir_.resolve("test_getHrefToReport")
        Helpers.copyDirectory(fixtureDir_, caseOutputDir)
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
        Path subpathUnderTCaseResult = Paths.get('CURA.visitSite/home(47.87)FAILED.png')
        Material material = tCaseResult.getMaterial(subpathUnderTCaseResult)
        then:
        material != null
        when:
        String href = ra.getHrefToReport(material)
        println "#getHrefToReport href='${href}'"
        then:
        href != null
        href == '../Reports/CURA/twins_exam/20190528_111335/20190528_111335.html'
    }
}
