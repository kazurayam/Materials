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

    private TSuiteName tsn
    private TSuiteTimestamp tst
    private TSuiteResultId tsri
    
    // fixture methods
    def setupSpec() {
        //workdir = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(TsNameSpec.class)}")
        //if (!workdir.toFile().exists()) {
        //    workdir.toFile().mkdirs()
        //}
        //Helpers.copyDirectory(fixture, workdir)
    }
    def setup() {
        tsn = new TSuiteName("Test Suites/TS1")
        tst = TSuiteTimestamp.newInstance("20190202_073500")
        tsri = TSuiteResultIdImpl.newInstance(tsn, tst)
    }
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def testGetTSuiteName() {
        expect:
        tsri.getTSuiteName().equals(tsn)
        tsri.getTSuiteTimestamp().equals(tst)
    }
    
    def testEquals() {
        when:
        TSuiteResultId tsri0 = TSuiteResultIdImpl.newInstance(tsn, tst)
        TSuiteResultId tsri1 = TSuiteResultIdImpl.newInstance(tsn, tst)
        TSuiteResultId tsri2 = TSuiteResultIdImpl.newInstance(new TSuiteName("TS2"), tst)
        TSuiteResultId tsri3 = TSuiteResultIdImpl.newInstance(tsn, TSuiteTimestamp.newInstance("20190203_070321"))
        then:
        tsri0.equals(tsri1)
        !tsri0.equals(tsri2)
        !tsri0.equals(tsri3)
    }
    
    def testHashCode() {
        when:
        TSuiteResultId tsri0 = TSuiteResultIdImpl.newInstance(tsn, tst)
        TSuiteResultId tsri1 = TSuiteResultIdImpl.newInstance(tsn, tst)
        TSuiteResultId tsri2 = TSuiteResultIdImpl.newInstance(new TSuiteName("TS2"), tst)
        TSuiteResultId tsri3 = TSuiteResultIdImpl.newInstance(tsn, TSuiteTimestamp.newInstance("20190203_070321"))
        then:
        tsri0.hashCode() == tsri1.hashCode()
        tsri0.hashCode() != tsri2.hashCode()
        tsri0.hashCode() != tsri3.hashCode()
    }

}
