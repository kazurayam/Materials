package com.kazurayam.materials.impl

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp

import spock.lang.Specification

class TSuiteResultIdImplSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(TSuiteResultIdImplSpec.class)

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")

    // fixture methods
    def setupSpec() {
        //workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TsNameSpec.class)}")
        //if (!workdir.toFile().exists()) {
        //    workdir.toFile().mkdirs()
        //}
        //Helpers.copyDirectory(fixture, workdir)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def testGetTSuiteName() {
        setup:
        TSuiteName tsn = new TSuiteName("Test Suites/TS1")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190202_073500")
        TSuiteResultId tsri = TSuiteResultIdImpl.newInstance(tsn, tst)
        expect:
        tsri.getTSuiteName().equals(tsn)
        tsri.getTSuiteTimestamp().equals(tst)
    }

}
