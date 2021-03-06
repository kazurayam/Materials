package com.kazurayam.materials

import spock.lang.IgnoreRest

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.RetrievalBy.SearchContext
import com.kazurayam.materials.metadata.MaterialMetadataBundle

import spock.lang.Specification

class MaterialStorageSpec extends Specification {
    
    // fields
    static Logger logger_ = LoggerFactory.getLogger(MaterialStorageSpec)
    
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialRepository mr_
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(MaterialStorageSpec.class)}")
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
        Helpers.deleteDirectoryContents(msdir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName("Monitor47News"),
                new TExecutionProfile("default"),
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
        Path storageDir = stepWork.resolve("Storage")
        Helpers.deleteDirectoryContents(storageDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(storageDir)
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName("main/TS1"),
                new TExecutionProfile("CURA_ProductionEnv"),
                new TSuiteTimestamp('20180530_130419')
        )
        int numberOfMaterialsCopied = ms.backup(mr_, tsri)
        then:
        numberOfMaterialsCopied > 1
    }

    def testClear_withTSuiteNameAndTSuiteTimestamp() {
        setup:
        Path stepWork = workdir_.resolve("testClear")
        Path msdir = stepWork.resolve("Storage")
        Helpers.deleteDirectoryContents(msdir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("Monitor47News")
        TExecutionProfile tep = new TExecutionProfile("default")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190123_153854")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
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
        Helpers.deleteDirectoryContents(msdir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        TSuiteName tsn = new TSuiteName("Monitor47News")
        TExecutionProfile tep = new TExecutionProfile("default")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190123_153854")
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
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
        Helpers.deleteDirectoryContents(msdir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("Monitor47News")
        TExecutionProfile tep = new TExecutionProfile("default")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190123_153854")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
        int num = ms.backup(mr_, tsri)
        then:
        num == 1
        when:
        ms.empty()
        List<TSuiteResult> tSuiteResults = ms.getTSuiteResultList()
        then:
        tSuiteResults.size() == 0
    }

    def testGetSetOfMaterialPathRelativeToTSuiteName() {
        setup:
        Path stepWork = workdir_.resolve("testGetSetOfMaterialPathRelativeToTSuiteName")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        TExecutionProfile tep = new TExecutionProfile('CURA_ProductionEnv')
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180805_081908")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
        int num = ms.backup(mr_, tsri)
        Set<Path> set = ms.getSetOfMaterialPathRelativeToTSuiteName(tsn, tep)
        logger_.debug("#testGetSetOfMaterialPathRelativeToTSuiteName " + set)
        then:
        set.size() == 2
    }
        
    def testGetTSuiteResult() {
        setup:
        Path stepWork = workdir_.resolve("testGetTSuiteResult")
        Path msdir = stepWork.resolve("Storage")
        Helpers.deleteDirectoryContents(msdir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        TExecutionProfile tep = new TExecutionProfile('CURA_ProductionEnv')
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180805_081908")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
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
        Helpers.deleteDirectoryContents(msdir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        TExecutionProfile tep = new TExecutionProfile('CURA_ProductionEnv')
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180805_081908")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
        int num = ms.backup(mr_, tsri)
        then:
        num == 2
        when:
        List<TSuiteResultId> list = ms.getTSuiteResultIdList(tsn, tep)
        then:
        list != null
        list.size() == 1
    }
    
    def testGetTSuiteResultIdList_withTSuiteName() {
        setup:
        Path stepWork = workdir_.resolve("testGetTSuiteResultIdList_withTSuiteName")
        Path msdir = stepWork.resolve("Storage")
        Helpers.deleteDirectoryContents(msdir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        TExecutionProfile tep = new TExecutionProfile('CURA_ProductionEnv')
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180805_081908")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
        int num = ms.backup(mr_, tsri)
        then:
        num == 2
        when:
        List<TSuiteResultId> list = ms.getTSuiteResultIdList(tsn, tep)
        then:
        list != null
        list.size() == 1
    }
    
    def testGetTSuiteResultIdList() {
        setup:
        Path stepWork = workdir_.resolve("testGetTSuiteResultIdList")
        Path msdir = stepWork.resolve("Storage")
        Helpers.deleteDirectoryContents(msdir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        TExecutionProfile tep = new TExecutionProfile('CURA_ProductionEnv')
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180805_081908")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
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
        Helpers.deleteDirectoryContents(msdir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        TExecutionProfile tep =  new TExecutionProfile('CURA_ProductionEnv')
        List<TSuiteResultId> tsriList = mr_.getTSuiteResultIdList(tsn, tep)
        int num = ms.backup(mr_, tsriList)
        then:
        num == 22
        when:
        List<TSuiteResult> list = ms.getTSuiteResultList()
        then:
        list != null
        list.size() == 6
    }
    
    def testReduce() {
        setup:
        Path stepWork = workdir_.resolve("testReduce")
        Path msdir = stepWork.resolve("Storage")
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName("main/TS1"),
                new TExecutionProfile("CURA_ProductionEnv"),
                new TSuiteTimestamp('20180530_130419')
        )
        int numberOfMaterialsCopied = ms.backup(mr_, tsri)
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

    /**
     * MaterialStorage#restore() method should be able to copy
     */
    def testRestore_including_MaterialMetadataBundle_file() {
        setup:
        Path caseOutputDir = workdir_.resolve('testRestore_including_MaterialMetadataBundle_file')
        Path materialsDir = caseOutputDir.resolve('Materials')
        Path storageDir = caseOutputDir.resolve('Storage')

        Path fixtureSource = fixture_.resolve('Storage/47news.chronos_capture')
        Path fixtureTarget = storageDir.resolve('47news.chronos_capture')
        Helpers.deleteDirectoryContents(fixtureTarget)
        Helpers.copyDirectory(fixtureSource, fixtureTarget)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materialsDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(storageDir)
        when:
        // test MaterialStorage#restore() method
        // copy a set of files under Storage directory to Materials directory
        // and make sure TCaseResult/material-metadata-bundle.json file is copied
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName('47news/chronos_capture'),
                new TExecutionProfile('default'),
                new TSuiteTimestamp('20190401_142150'))
        RestoreResult restoreResult = ms.restore(mr, tsri)
        then:
        restoreResult.getCount() >= 0
        when:
        Path tSuiteTimestampDir_inMR = mr.getTSuiteResult(tsri).getTSuiteTimestampDirectory()
        then:
        Files.exists(tSuiteTimestampDir_inMR.resolve(MaterialMetadataBundle.SERIALIZED_FILE_NAME))
        when:
        // test backup() method
        String clonedFileName = "__" + MaterialMetadataBundle.SERIALIZED_FILE_NAME
        Files.copy(tSuiteTimestampDir_inMR.resolve(MaterialMetadataBundle.SERIALIZED_FILE_NAME),
                    tSuiteTimestampDir_inMR.resolve(clonedFileName), StandardCopyOption.REPLACE_EXISTING)
        int count = ms.backup(mr, tsri)
        then:
        count >= 0
        Files.exists(ms.getTSuiteResult(tsri).getTSuiteTimestampDirectory().resolve(clonedFileName))
    }

    def testRestore_specifyingTSuiteTimestamp() {
        setup:
        Path stepWork = workdir_.resolve("testRestore_specifyingTSuiteTimestamp")
        Path msdir = stepWork.resolve("Storage")
        Path restoredDir = stepWork.resolve("Materials")
        Helpers.deleteDirectoryContents(msdir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("Monitor47News")
        TExecutionProfile tep = new TExecutionProfile('default')
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190123_153854")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
        int num = ms.backup(mr_, tsri)
        then:
        num == 1
        when:
        Helpers.deleteDirectoryContents(restoredDir)
        MaterialRepository restored = MaterialRepositoryFactory.createInstance(restoredDir)
        RestoreResult restoreResult = ms.restore(restored, tsri)
        then:
        restoreResult.getCount() == 1
    }

    def test_retrievingRestoreUnaryExclusive() {
        setup:
        Path stepWork = workdir_.resolve("test_retrievingRestoreUnaryExclusive")
        Path msdir = stepWork.resolve("Storage")
        Helpers.deleteDirectoryContents(msdir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
        when:
        TSuiteName tsn = new TSuiteName("main/TS1")
        TExecutionProfile tep = new TExecutionProfile('CURA_ProductionEnv')
        List<TSuiteResultId> tsriList = mr_.getTSuiteResultIdList(tsn, tep)
        int num = ms.backup(mr_, tsriList)
        then:
        num == 22
        when:
        Path restoredDir = stepWork.resolve("Materials")
        Helpers.deleteDirectoryContents(restoredDir)
        MaterialRepository restored = MaterialRepositoryFactory.createInstance(restoredDir)
        LocalDateTime baseD = LocalDateTime.of(2018, 8, 5, 8, 19, 8)
        RestoreResult restoreResult = 
            ms.retrievingRestoreUnaryExclusive(restored, tsn, tep, RetrievalBy.by(baseD))
        then:
        restoreResult.getCount() == 2
        when:
        List<TSuiteResultId> tsriListRestored = restored.getTSuiteResultIdList(tsn, tep)
        then:
        tsriListRestored.size()== 1
        tsriListRestored.contains(
                TSuiteResultId.newInstance(tsn, tep,
                        TSuiteTimestamp.newInstance("20180718_142832")))
    }
	
	def test_retrievingRestoreUnaryInclusive() {
		setup:
		Path stepWork = workdir_.resolve("test_retrievingRestoreUnaryInclusive")
		Path msdir = stepWork.resolve("Storage")
		Helpers.deleteDirectoryContents(msdir)
		MaterialStorage ms = MaterialStorageFactory.createInstance(msdir)
		when:
		TSuiteName tsn = new TSuiteName("main/TS1")
        TExecutionProfile tep = new TExecutionProfile('CURA_ProductionEnv')
		List<TSuiteResultId> tsriList = mr_.getTSuiteResultIdList(tsn, tep)
		int num = ms.backup(mr_, tsriList)
		then:
		num == 22
		when:
		Path restoredDir = stepWork.resolve("Materials")
		Helpers.deleteDirectoryContents(restoredDir)
		MaterialRepository restored = MaterialRepositoryFactory.createInstance(restoredDir)
		LocalDateTime baseD = LocalDateTime.of(2018, 8, 5, 8, 19, 8)
		RestoreResult restoreResult =
			ms.retrievingRestoreUnaryInclusive(restored, tsn, tep, RetrievalBy.by(baseD))
		then:
		restoreResult.getCount() == 2
		when:
		List<TSuiteResultId> tsriListRestored = restored.getTSuiteResultIdList(tsn, tep)
		then:
		tsriListRestored.size()== 1
		tsriListRestored.contains(TSuiteResultId.newInstance(tsn, tep,
                TSuiteTimestamp.newInstance("20180805_081908")))
	}

}
