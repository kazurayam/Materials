package com.kazurayam.materials

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.RetrievalBy.SearchContext

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
    def testBackup_specifyingTSuiteNameAndTSuiteTimestamp() {
        setup:
        Path stepWork = workdir_.resolve("testBackup_specifyingTSuiteTimestamp")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(new TSuiteName("Monitor47News"),
                                            TSuiteTimestamp.newInstance("20190123_153854"))
        then:
        tsri instanceof TSuiteResultId
        
        when:
        int num = ms.backup(mr_, tsri)
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
        num > 1
    }
    
    def testClear_withTSuiteNameAndTSuiteTimestamp() {
        setup:
        Path stepWork = workdir_.resolve("testClear")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("Monitor47News")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190123_153854")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        int num = ms.backup(mr_, tsri)
        then:
        num == 1
        when:
        ms.clear(tsri)
        List<TSuiteResult> tSuiteResults = ms.getTSuiteResultList()
        then:
        tSuiteResults.size() == 0
    }

    def testClear_withOnlyTSuiteName() {
        setup:
        Path stepWork = workdir_.resolve("testClear_withOnlyTSuiteName")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        TSuiteName tsn = new TSuiteName("Monitor47News")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190123_153854")
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        int num = ms.backup(mr_, tsri)
        then:
        num == 1
        when:
        ms.clear(tsn)    // HERE is difference
        List<TSuiteResult> tSuiteResults = ms.getTSuiteResultList()
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
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        int num = ms.backup(mr_, tsri)
        then:
        num == 1
        when:
        ms.empty()
        List<TSuiteResult> tSuiteResults = ms.getTSuiteResultList()
        then:
        tSuiteResults.size() == 0
    }
    
    def testGetSetOfMaterialPathRelativeToTSuiteTimestamp() {
        setup:
        Path stepWork = workdir_.resolve("imageDeltaStatsEntries.get(tSuiteName)")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180805_081908")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        int num = ms.backup(mr_, tsri)
        Set<Path> set = ms.getSetOfMaterialPathRelativeToTSuiteTimestamp(tsn)
        logger_.debug("#testGetSetOfMaterialPathRelativeToTSuiteTimestamp " + set)
        then:
        set.size() == 2
    }
        
    def testGetTSuiteResult() {
        setup:
        Path stepWork = workdir_.resolve("testGetTSuiteResult")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180805_081908")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        int num = ms.backup(mr_, tsri)
        then:
        num == 2
        when:
        TSuiteResult tsr = ms.getTSuiteResult(tsri)
        then:
        tsr != null
        tsr.getId().getTSuiteName().equals(tsn)
        tsr.getId().getTSuiteTimestamp().equals(tst)
    }
    
    def testGetTSuiteResultList_withTSuiteName() {
        setup:
        Path stepWork = workdir_.resolve("testGetTSuiteResult_withTSuiteName")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180805_081908")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        int num = ms.backup(mr_, tsri)
        then:
        num == 2
        when:
        List<TSuiteResultId> list = ms.getTSuiteResultIdList(tsn)
        then:
        list != null
        list.size() == 1
    }
    
    def testGetTSuiteResultIdList_withTSuiteName() {
        setup:
        Path stepWork = workdir_.resolve("testGetTSuiteResultIdList_withTSuiteName")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180805_081908")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        int num = ms.backup(mr_, tsri)
        then:
        num == 2
        when:
        List<TSuiteResultId> list = ms.getTSuiteResultIdList(tsn)
        then:
        list != null
        list.size() == 1
    }
    
    def testGetTSuiteResultIdList() {
        setup:
        Path stepWork = workdir_.resolve("testGetTSuiteResultIdList")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180805_081908")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        int num = ms.backup(mr_, tsri)
        then:
        num == 2
        when:
        List<TSuiteResultId> list = ms.getTSuiteResultIdList()
        then:
        list != null
        list.size() == 1
    }
    
    def testGetTSuiteResultList_noArgs() {
        setup:
        Path stepWork = workdir_.resolve("testGetTSuiteResult_noArgs")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        List<TSuiteResultId> tsriList = mr_.getTSuiteResultIdList(tsn)
        int num = ms.backup(mr_, tsriList)
        then:
        num == 12
        when:
        List<TSuiteResult> list = ms.getTSuiteResultList()
        then:
        list != null
        list.size() == 4
    }
    
    def testRestore_specifyingTSuiteTimestamp() {
        setup:
        Path stepWork = workdir_.resolve("testRestore_specifyingTSuiteTimestamp")
        Path msdir = stepWork.resolve("Storage")
        Path restoredDir = stepWork.resolve("Materials_restored")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("Monitor47News")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190123_153854")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        int num = ms.backup(mr_, tsri)
        then:
        num == 1
        when:
        MaterialRepository restored = MaterialRepositoryFactory.createInstance(restoredDir)
        num = ms.restore(restored, tsri)
        then:
        num == 1
    }
    
    def testList_all() {
        setup:
        Path stepWork = workdir_.resolve("testList_all")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        int numberOfMaterialsCopied = ms.backup(mr_)
        when:
        StringWriter sw = new StringWriter()
        Map options = new HashMap()
        ms.list(sw, options)
        String output = sw.toString()
        println output
        then:
        output.contains('TS1')
        output.contains('20180810_140105')
        output.contains('1,924,038')
    }
    
    def testList_one() {
        setup:
        Path stepWork = workdir_.resolve("testList_one")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        int numberOfMaterialsCopied = ms.backup(mr_)
        when:
        StringWriter sw = new StringWriter()
        Map<String, Object> options = new HashMap()
        options.put('TSuiteName', new TSuiteName('Monitor47News'))
        ms.list(sw, options)
        String output = sw.toString()
        println output
        then:
        output.contains('Monitor47News')
        output.contains('20190123_153854')
        output.contains('2,631,409')
    }
    
    def testReduce() {
        setup:
        Path stepWork = workdir_.resolve("testList_one")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        int numberOfMaterialsCopied = ms.backup(mr_)
        when:
        long remainingBytes =ms.reduce(20_000_000)   // make the Storage usage less than 20MB
        then:
        remainingBytes <= 20_000_000
        when:
        long currentSize = ms.getSize()
        println ">>>currentSize is ${currentSize}"
        then:
        currentSize <= 20_000_000
        remainingBytes == currentSize
    }

    def testRestore_RetrieveBy_before_LocalDateTime_restoreUnary() {
        setup:
        Path stepWork = workdir_.resolve("testRestore_RetrieveBy_before_LocalDateTime_restoreUnary")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        List<TSuiteResultId> tsriList = mr_.getTSuiteResultIdList(tsn)
        int num = ms.backup(mr_, tsriList)
        then:
        num == 12
        when:
        Path restoredDir = stepWork.resolve("Materials_restored")
        MaterialRepository restored = MaterialRepositoryFactory.createInstance(restoredDir)
        RetrievalBy.SearchContext context = new SearchContext(ms, tsn)
        LocalDateTime baseD = LocalDateTime.of(2018, 8, 5, 8, 19, 8)
        num = ms.restoreUnary(restored,
                                tsn,
                                RetrievalBy.before(baseD))
        then:
        num == 2
        when:
        List<TSuiteResultId> tsriListRestored = restored.getTSuiteResultIdList(tsn)
        then:
        tsriListRestored.size()== 1
        tsriListRestored.contains(TSuiteResultId.newInstance(tsn, TSuiteTimestamp.newInstance("20180718_142832")))
    }

    def testRestore_RetrieveBy_before_TSuiteTimestamp_restoreCollective() {
        setup:
        Path stepWork = workdir_.resolve("testRestore_RetrieveBy_before_TSuiteTimestamp_restoreCollective")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        List<TSuiteResultId> tsriList = mr_.getTSuiteResultIdList(tsn)
        int num = ms.backup(mr_, tsriList)
        then:
        num == 12
        when:
        Path restoredDir = stepWork.resolve("Materials_restored")
        MaterialRepository restored = MaterialRepositoryFactory.createInstance(restoredDir)
        RetrievalBy.SearchContext context = new RetrievalBy.SearchContext(ms, tsn)
        num = ms.restoreCollective(restored,
                                    tsn,
                                    RetrievalBy.before(TSuiteTimestamp.newInstance("20180805_081908")))
        then:
        num == 10
        when:
        List<TSuiteResultId> tsriListRestored = restored.getTSuiteResultIdList(tsn)
        then:
        tsriListRestored.size()== 3
        tsriListRestored.contains(TSuiteResultId.newInstance(tsn, TSuiteTimestamp.newInstance("20180718_142832")))
    }

}