package com.kazurayam.material

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Document

import spock.lang.Specification

//@Ignore
class JUnitReportSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(JUnitReportSpec.class);

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialRepositoryImpl mri_
    private static Document doc_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(JUnitReportSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        mri_ = new MaterialRepositoryImpl(workdir_.resolve('Materials'))
        TSuiteResult tsr = mri_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180805_081908'))
        doc_ = tsr.createReportDocument()
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testConstructor() {
        when:
        JUnitReport obj = new JUnitReport(doc_)
        then:
        obj != null
    }

    def testGetTestSuiteSummary() {
        setup:
        JUnitReport obj = new JUnitReport(doc_)
        when:
        String summary = obj.getTestSuiteSummary('Test Suites/main/TS1')
        then:
        summary == 'PASSED: 1, FAILED: 1, ERROR: 0'
    }

    def testGetTestCaseStatus_PASSED() {
        setup:
        JUnitReport obj = new JUnitReport(doc_)
        when:
        String status = obj.getTestCaseStatus('Test Cases/main/TC1')
        then:
        status == 'PASSED'
    }

    def testGetTestCaseStatus_FAILED() {
        setup:
        JUnitReport obj = new JUnitReport(doc_)
        when:
        String status = obj.getTestCaseStatus('Test Cases/main/TC2')
        then:
        status == 'FAILED'
    }


    // helper methods
}
