package com.kazurayam.materials

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.model.MaterialRepositoryImpl
import com.kazurayam.materials.model.TSuiteResult
import com.kazurayam.materials.model.TSuiteTimestampImpl
import com.kazurayam.materials.model.repository.RepositoryRoot

import spock.lang.Specification

class SelectBySpec extends Specification {
    
    // fields
    static Logger logger_ = LoggerFactory.getLogger(SelectBySpec.class)
    
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialRepositoryImpl mri_
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(SelectBySpec.class)}")
        Helpers.copyDirectory(fixture_, workdir_)
        //
        mri_ = new MaterialRepositoryImpl(workdir_.resolve("Materials"))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    
    def test_tSuiteTimestampBefore_oneOrMoreFound() {
        setup:
        TSuiteName tsn = new TSuiteName("TS1")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180810_140106")
        RepositoryRoot rr = mri_.getRepositoryRoot()
        SelectBy.SearchContext context = new SelectBy.SearchContext(rr, tsn)
        SelectBy by = SelectBy.before(tst)
        //
        when:
        List<TSuiteResult> list = by.findTSuiteResults(context)
        then:
        list.size() == 1
    }

    def test_tSuiteTimestampBefore_noneFound() {
        setup:
        TSuiteName tsn = new TSuiteName("Monitor47News")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190123_153854")
        RepositoryRoot rr = mri_.getRepositoryRoot()
        SelectBy.SearchContext context = new SelectBy.SearchContext(rr, tsn)
        SelectBy by = SelectBy.before(tst)
        //
        when:
        List<TSuiteResult> list = by.findTSuiteResults(context)
        then:
        list.size() == 0
    }
    
}
