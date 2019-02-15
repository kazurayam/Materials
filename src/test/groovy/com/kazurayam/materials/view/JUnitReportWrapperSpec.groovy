package com.kazurayam.materials.view

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.impl.MaterialRepositoryImpl

import spock.lang.Specification

//@Ignore
class JUnitReportWrapperSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(JUnitReportWrapperSpec.class);

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialRepositoryImpl mri_
    private static JUnitReportWrapper instance_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(JUnitReportWrapperSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        mri_ = MaterialRepositoryImpl.newInstance(workdir_.resolve('Materials'))

    }
    def setup() {
        Path p = fixture_.resolve('Reports/main/TS1/20180805_081908/JUnit_Report.xml')
        instance_ = new JUnitReportWrapper(p)
    }
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testConstructor() {
        expect:
        instance_ != null
    }

    def testGetTestSuiteSummary() {
        when:
        String summary = instance_.getTestSuiteSummary('Test Suites/main/TS1')
        then:
        summary == 'EXECUTED:2,FAILED:1,ERROR:0'
    }

    def testGetTestCaseStatus_PASSED() {
        when:
        String status = instance_.getTestCaseStatus('Test Cases/main/TC1')
        then:
        status == 'PASSED'
    }

    def testGetTestCaseStatus_FAILED() {
        when:
        String status = instance_.getTestCaseStatus('Test Cases/main/TC2')
        then:
        status == 'FAILED'
    }


    // helper methods
}
