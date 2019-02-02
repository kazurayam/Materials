package com.kazurayam.materials

import java.nio.file.Path
import java.nio.file.Paths
import java.time.DayOfWeek
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.model.TSuiteResult

import spock.lang.Specification

class RetrievalBySpec extends Specification {
    
    // fields
    static Logger logger_ = LoggerFactory.getLogger(RetrievalBySpec.class)
    
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialRepository mr_
    private static MaterialStorage ms_
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(RetrievalBySpec.class)}")
        Path materials = workdir_.resolve("Materials")
        Path storage   = workdir_.resolve("Storage")
        Helpers.copyDirectory(fixture_, materials)
        Helpers.copyDirectory(materials, storage)
        //
        mr_ = MaterialRepositoryFactory.createInstance(materials)
        ms_ = MaterialStorageFactory.createInstance(storage)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    
    def testBefore_TSuiteTimestamp_oneOrMoreFound() {
        setup:
        TSuiteName tsn = new TSuiteName("TS1")
        RetrievalBy.SearchContext context = new RetrievalBy.SearchContext(mr_, tsn)
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20180810_140106")
        when:
        RetrievalBy by = RetrievalBy.before(tst)
        List<TSuiteResult> list = by.findTSuiteResults(context)
        then:
        list.size() == 1
    }

    def testBefore_TSuiteTimestamp_noneFound() {
        setup:
        TSuiteName tsn = new TSuiteName("Monitor47News")
        RetrievalBy.SearchContext context = new RetrievalBy.SearchContext(mr_, tsn)
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190123_153854")
        when:
        RetrievalBy by = RetrievalBy.before(tst)
        List<TSuiteResult> list = by.findTSuiteResults(context)
        then:
        list.size() == 0
    }

    /**
     * Retrieving a single TSuiteResult object.
     * 
     * @return
     */
    def testBefore_findUnaryTSuiteResult() {
        setup:
        TSuiteName tsn = new TSuiteName("main/TS1")
        RetrievalBy.SearchContext context = new RetrievalBy.SearchContext(mr_, tsn)
        when:
        LocalDateTime base = LocalDateTime.of(2018, 7, 18, 23, 59, 59)
        RetrievalBy by = RetrievalBy.before(base, 0, 0, 0)
        TSuiteResult tsr = by.findTSuiteResult(context)
        then:
        tsr.getTSuiteResultId().getTSuiteName().equals(tsn)
        tsr.getTSuiteResultId().getTSuiteTimestamp().equals(TSuiteTimestamp.newInstance('20180530_130604'))
    }

    /**
     * retrieving TSuiteResults before the specified day + time
     */
    def testBefore_LocalDateTime_theDay() {
        setup:
        TSuiteName tsn = new TSuiteName("main/TS1")
        RetrievalBy.SearchContext context = new RetrievalBy.SearchContext(mr_, tsn)
        when:
        LocalDateTime base = LocalDateTime.of(2018, 7, 18, 23, 59, 59)
        RetrievalBy by = RetrievalBy.before(base, 0, 0, 0)
        List<TSuiteResult> list = by.findTSuiteResults(context)
        then:
        list.size() == 2
        list[0].getTSuiteResultId().getTSuiteName().equals(tsn)
        list[0].getTSuiteResultId().getTSuiteTimestamp().equals(TSuiteTimestamp.newInstance('20180530_130604'))
        list[1].getTSuiteResultId().getTSuiteTimestamp().equals(TSuiteTimestamp.newInstance('20180530_130419'))
    }
   
    /**
     *  retrieving TSuiteResults before the day (1 day prior to the specified date) + time
     */
    def testBefore_LocalDateTime_previousDay() {
        setup:
        TSuiteName tsn = new TSuiteName("main/TS1")
        RetrievalBy.SearchContext context = new RetrievalBy.SearchContext(mr_, tsn)
        when:
        LocalDateTime base = LocalDateTime.of(2018, 7, 19, 23, 59, 59)
        LocalDateTime shifted = base.minusDays(1)
        then:
        // 1 day previous of the base == 2018/07/18
        shifted.getYear() == 2018
        shifted.getMonthValue() == 7
        shifted.getDayOfMonth() == 18
        when:
        RetrievalBy by = RetrievalBy.before(shifted, 0, 0, 0)  
        List<TSuiteResult> list = by.findTSuiteResults(context)
        then:
        list.size() == 2
        list[0].getTSuiteResultId().getTSuiteName().equals(tsn)
        list[0].getTSuiteResultId().getTSuiteTimestamp().equals(TSuiteTimestamp.newInstance('20180530_130604'))
        list[1].getTSuiteResultId().getTSuiteTimestamp().equals(TSuiteTimestamp.newInstance('20180530_130419'))
    }
    
    /**
     *  retrieving TSuiteResults before the last friday 17:29:59 (prior to the specified day) + time
     */
    def testBefore_LocalDateTime_lastFriday() {
        setup:
        TSuiteName tsn = new TSuiteName("main/TS1")
        RetrievalBy.SearchContext context = new RetrievalBy.SearchContext(mr_, tsn)
        when:
        LocalDateTime base = LocalDateTime.of(2018, 7, 19, 23, 59, 59)
        LocalDateTime shifted = base.minusWeeks(1).with(DayOfWeek.FRIDAY)
        then:
        // the last friday prior to 2018/07/19 == 2018/07/13
        shifted.getYear() == 2018
        shifted.getMonthValue() == 7
        shifted.getDayOfMonth() == 13
        when:
        RetrievalBy by = RetrievalBy.before(shifted, 0, 0, 0)
        List<TSuiteResult> list = by.findTSuiteResults(context)
        then:
        list.size() == 2
        list[0].getTSuiteResultId().getTSuiteName().equals(tsn)
        list[0].getTSuiteResultId().getTSuiteTimestamp().equals(TSuiteTimestamp.newInstance('20180530_130604'))
        list[1].getTSuiteResultId().getTSuiteTimestamp().equals(TSuiteTimestamp.newInstance('20180530_130419'))
    }
    
    /**
     * test getting the last business day prior to the day given.
     * @return
     */
    def testLastBusinessDay() {
        expect:
        RetrievalBy.lastBusinessDay(LocalDateTime.of(2018,1,29,0,0,0)).equals(LocalDateTime.of(2018,1,28,0,0,0))
        RetrievalBy.lastBusinessDay(LocalDateTime.of(2018,1,28,0,0,0)).equals(LocalDateTime.of(2018,1,25,0,0,0))
        RetrievalBy.lastBusinessDay(LocalDateTime.of(2018,1,27,0,0,0)).equals(LocalDateTime.of(2018,1,25,0,0,0))
        RetrievalBy.lastBusinessDay(LocalDateTime.of(2018,1,26,0,0,0)).equals(LocalDateTime.of(2018,1,25,0,0,0))
        RetrievalBy.lastBusinessDay(LocalDateTime.of(2018,1,25,0,0,0)).equals(LocalDateTime.of(2018,1,24,0,0,0))
        RetrievalBy.lastBusinessDay(LocalDateTime.of(2018,1,24,0,0,0)).equals(LocalDateTime.of(2018,1,23,0,0,0))
        RetrievalBy.lastBusinessDay(LocalDateTime.of(2018,1,23,0,0,0)).equals(LocalDateTime.of(2018,1,22,0,0,0))
        RetrievalBy.lastBusinessDay(LocalDateTime.of(2018,1,22,0,0,0)).equals(LocalDateTime.of(2018,1,21,0,0,0))
        RetrievalBy.lastBusinessDay(LocalDateTime.of(2018,1,21,0,0,0)).equals(LocalDateTime.of(2018,1,18,0,0,0))
    }
    
    def testBefore_LocalDateTime_lastBusinessDay() {
        setup:
        TSuiteName tsn = new TSuiteName("main/TS1")
        RetrievalBy.SearchContext context = new RetrievalBy.SearchContext(mr_, tsn)
        when:
        LocalDateTime base = LocalDateTime.of(2018, 5, 31, 0, 0, 0)
        RetrievalBy by = RetrievalBy.before(base, 0, 0, 0)
        List<TSuiteResult> list = by.findTSuiteResults(context)
        then:
        list.size() == 2
        list[0].getTSuiteResultId().getTSuiteName().equals(tsn)
        list[0].getTSuiteResultId().getTSuiteTimestamp().equals(TSuiteTimestamp.newInstance('20180530_130604'))
        list[1].getTSuiteResultId().getTSuiteTimestamp().equals(TSuiteTimestamp.newInstance('20180530_130419'))
    }
    
    /**
     * retrieving TSuiteResult befor 25th of the last month (prior to the specified day) + 18:00:00
     */
    def testBefore_LocalDateTime_25lastMonth() {
        setup:
        TSuiteName tsn = new TSuiteName("main/TS1")
        RetrievalBy.SearchContext context = new RetrievalBy.SearchContext(mr_, tsn)
        when:
        LocalDateTime base = LocalDateTime.of(2018, 7, 19, 23, 59, 59)
        LocalDateTime shifted = base.minusMonths(1).withDayOfMonth(25)
        then:
        // 25th of the the last month prior to 2018/07/19 == 2018/06/25
        shifted.getYear() == 2018
        shifted.getMonthValue() == 6
        shifted.getDayOfMonth() == 25
        when:
        RetrievalBy by = RetrievalBy.before(shifted, 0, 0, 0)
        List<TSuiteResult> list = by.findTSuiteResults(context)
        then:
        list.size() == 2
        list[0].getTSuiteResultId().getTSuiteName().equals(tsn)
        list[0].getTSuiteResultId().getTSuiteTimestamp().equals(TSuiteTimestamp.newInstance('20180530_130604'))
        list[1].getTSuiteResultId().getTSuiteTimestamp().equals(TSuiteTimestamp.newInstance('20180530_130419'))
    }
    
   

}
