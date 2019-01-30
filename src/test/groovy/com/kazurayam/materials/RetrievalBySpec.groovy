package com.kazurayam.materials

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.model.MaterialRepositoryImpl
import com.kazurayam.materials.model.TSuiteResult
import com.kazurayam.materials.model.TSuiteTimestampImpl
import com.kazurayam.materials.model.repository.RepositoryRoot

import spock.lang.Specification

class RetrievalBySpec extends Specification {
    
    // fields
    static Logger logger_ = LoggerFactory.getLogger(RetrievalBySpec.class)
    
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialRepositoryImpl mri_
    private static RepositoryRoot rr_
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(RetrievalBySpec.class)}")
        Helpers.copyDirectory(fixture_, workdir_)
        //
        mri_ = new MaterialRepositoryImpl(workdir_.resolve("Materials"))
        rr_ = mri_.getRepositoryRoot()
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    
    def test_beforeTSuiteTimestamp_oneOrMoreFound() {
        setup:
        TSuiteName tsn = new TSuiteName("TS1")
        RetrievalBy.SearchContext context = new RetrievalBy.SearchContext(rr_, tsn)
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180810_140106")
        when:
        RetrievalBy by = RetrievalBy.before(tst)
        List<TSuiteResult> list = by.findTSuiteResults(context)
        then:
        list.size() == 1
    }

    def test_beforeTSuiteTimestamp_noneFound() {
        setup:
        TSuiteName tsn = new TSuiteName("Monitor47News")
        RetrievalBy.SearchContext context = new RetrievalBy.SearchContext(rr_, tsn)
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190123_153854")
        when:
        RetrievalBy by = RetrievalBy.before(tst)
        List<TSuiteResult> list = by.findTSuiteResults(context)
        then:
        list.size() == 0
    }
    
    /**
     * @return
     */
    def test_beforeLocalDateTime_theDay_at_hhmmss() {
        setup:
        TSuiteName tsn = new TSuiteName("main/TS1")
        RetrievalBy.SearchContext context = new RetrievalBy.SearchContext(rr_, tsn)
        when:
        LocalDateTime base = LocalDateTime.of(2018, 6, 1, 0, 0, 0)
        RetrievalBy by = RetrievalBy.before(base, 7, 30, 45)
        List<TSuiteResult> list = by.findTSuiteResults(context)
        then:
        list.size() == 2
    }
   
    // previous day
    
    // last friday 17:29:59
    
    // 10th of last month 10:00:00
    
}
