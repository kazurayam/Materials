package com.kazurayam.materials

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.model.TSuiteResult

import spock.lang.Specification

class MaterialStorageSpec extends Specification {
    
    // fields
    static Logger logger_ = LoggerFactory.getLogger(MaterialStorageSpec);
    
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialRepository mr_
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(MaterialStorageSpec.class)}")
        Helpers.copyDirectory(fixture_, workdir_)
        //
        mr_ = MaterialRepositoryFactory.createInstance(workdir_.resolve("Materials"))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def testBackup_specifyingTSuiteTimestamp() {
        setup:
        Path stepWork = workdir_.resolve("testBackup_specifyingTSuiteTimestamp")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        int num = ms.backup(mr_, new TSuiteName("Monitor47News"), TSuiteTimestamp.newInstance("20190123_153854"))
        then:
        num == 1
    }
    
    def testBackup_all() {
        setup:
        Path stepWork = workdir_.resolve("testBackup_all")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        int num = ms.backup(mr_)
        then:
        num == 1
    }
    
    def testClear_withTSuiteTimestamp() {
        setup:
        Path stepWork = workdir_.resolve("testClear")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("Monitor47News")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190123_153854")
        int num = ms.backup(mr_, tsn, tst)
        then:
        num == 1
        when:
        ms.clear(tsn, tst)
        List<TSuiteResult> tSuiteResults = ms.getTSuiteResults()
        then:
        tSuiteResults.size() == 0
    }

    def testClear_withOnlyTSuiteName() {
        setup:
        Path stepWork = workdir_.resolve("testClear")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("Monitor47News")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190123_153854")
        int num = ms.backup(mr_, tsn, tst)
        then:
        num == 1
        when:
        ms.clear(tsn)    // HERE is difference
        List<TSuiteResult> tSuiteResults = ms.getTSuiteResults()
        then:
        tSuiteResults.size() == 0
    }

    def testEmpty() {
        setup:
        Path stepWork = workdir_.resolve("testEmpty")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("Monitor47News")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190123_153854")
        int num = ms.backup(mr_, tsn, tst)
        then:
        num == 1
        when:
        ms.empty()
        List<TSuiteResult> tSuiteResults = ms.getTSuiteResults()
        then:
        tSuiteResults.size() == 0
    }
        
    def testGetTSuiteResult() {
        expect:
        false
    }
    
    def testGetTSuiteResults_withTSuiteName() {
        expect:
        false
    }
    
    def testGetTSuiteResults_noArgs() {
        expect:
        false
    }
    
    def testRestore_specifyingTSuiteTimestamp() {
        setup:
        Path stepWork = workdir_.resolve("testRestore_specifyingTSuiteTimestamp")
        Path msdir = stepWork.resolve("Storage")
        Path restoredDir = stepWork.resolve("Materials_restored")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        int num = ms.backup(mr_, new TSuiteName("Monitor47News"), TSuiteTimestamp.newInstance("20190123_153854"))
        then:
        num == 1
        when:
        MaterialRepository restored = MaterialRepositoryFactory.createInstance(restoredDir)
        num = ms.restore(restored, new TSuiteName("Monitor47News"), TSuiteTimestamp.newInstance("20190123_153854"))
        then:
        num == 1
    }

}
