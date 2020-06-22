package com.kazurayam.materials.stats

import com.kazurayam.materials.TExecutionProfile

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.MaterialStorageFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp

import spock.lang.Ignore
import spock.lang.Specification

class StatsEntrySpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(StatsEntrySpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static ImageDeltaStats ids_
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(StatsEntrySpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        Path storagedir = workdir_.resolve('Storage')
        MaterialStorage ms = MaterialStorageFactory.createInstance(storagedir)
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfileExam = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfileExam,
                tCaseNameExam)
        StorageScanner.Options options = new com.kazurayam.materials.stats.StorageScanner.Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ids_ = scanner.scan(
                new TSuiteName('47news.chronos_capture'),
                new TExecutionProfile('default'))
        scanner.persist(ids_,
                tSuiteNameExam,
                tExecutionProfileExam,
                new TSuiteTimestamp(),
                tCaseNameExam)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    /**
     * I expected java.nio.file.Path#equals(Path) to be tolerant for the difference of
     * File.seperator in the 2 Path instances.
     * On Windows, this test passed. But to my surprize, this test failed on Mac.
     * 
     * After a few days of research, I realized my misunderstanding about java.nio.file.Path
     * I should not write this: 
     *     <PRE>Paths.get('main.TC_47News.visitSite\\47NEWS_TOP.png')</PRE>
     * Rather I should write this: 
     *     <PRE>Paths.get('main.TC_47News.visitSite').resolve('47NEWS_TOP.png')</PRE> 
     * 
     * Ok, I would accept it.
     * I would ignore this test.
     * 
     * @return
     */
    @Ignore
    def test_Path_equals() {
        when:
        Path winPath = Paths.get('main.TC_47News.visitSite\\47NEWS_TOP.png')
        Path nixPath = Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png')
        then:
        winPath.equals(nixPath)
    }
    
    /**
     * verify if StatsEntry#getMaterialStats() method is tolerant for two kinds of File.separator: '\\' and '/'
     * @return
     */
    def testGetMaterialStats() {
        setup:
        StatsEntry se = ids_.getImageDeltaStatsEntry()
        when:
        MaterialStats mStats1 = se.getMaterialStats(
                Paths.get("47news.visitSite").resolve("47reporters.png"))
        then:
        mStats1 != null
        mStats1.getImageDeltaList().size() > 0
        when:
        MaterialStats mStats2 = se.getMaterialStats(
                Paths.get("47news.visitSite").resolve("47reporters.png"))
        logger_.debug("mStats2: ${mStats2.toString()}")
        then:
        mStats2 != null
        mStats2.getImageDeltaList().size() > 0
    }

    /**
{   "TSuiteName":"47news.chronos_capture",
    "TExecutionProfile":"default",
    "materialStatsList":[
        {
            "path":"47news.visitSite/47reporters.png",
            "degree":1,
            "sum":2.64,
            mean":2.64,
            "variance":0.0,
            "standardDeviation":0.0,
            "tDistribution":0.0,
            "confidenceInterval":{
                "lowerBound":2.64,
                "uppderBound":2.64,
                "confidenceLevel":0.95
            },
            "criteriaPercentage":2.65,
            "data":[2.64],
            "imageDeltaList":[
                {
                    "a":"20190401_142748",
                    "b":"20190401_142150",
                    "d":2.64,"cached":true
                }
            ]
        },
        {
            "path":"47news.visitSite/culture.png","degree":1,"sum":2.52,"mean":2.52,"variance":0.0,"standardDeviation":0.0,"tDistribution":0.0,"confidenceInterval":{"lowerBound":2.52,"uppderBound":2.52,"confidenceLevel":0.95},"criteriaPercentage":2.53,"data":[2.52],"imageDeltaList":[{"a":"20190401_142748","b":"20190401_142150","d":2.52,"cached":true}]},{"path":"47news.visitSite/economics.png","degree":1,"sum":2.67,"mean":2.67,"variance":0.0,"standardDeviation":0.0,"tDistribution":0.0,"confidenceInterval":{"lowerBound":2.67,"uppderBound":2.67,"confidenceLevel":0.95},"criteriaPercentage":2.67,"data":[2.67],"imageDeltaList":[{"a":"20190401_142748","b":"20190401_142150","d":2.67,"cached":true}]},{"path":"47news.visitSite/localnews.png","degree":1,"sum":8.11,"mean":8.11,"variance":0.0,"standardDeviation":0.0,"tDistribution":0.0,"confidenceInterval":{"lowerBound":8.11,"uppderBound":8.11,"confidenceLevel":0.95},"criteriaPercentage":8.11,"data":[8.11],"imageDeltaList":[{"a":"20190401_142748","b":"20190401_142150","d":8.11,"cached":true}]},{"path":"47news.visitSite/national.png","degree":1,"sum":14.15,"mean":14.15,"variance":0.0,"standardDeviation":0.0,"tDistribution":0.0,"confidenceInterval":{"lowerBound":14.15,"uppderBound":14.15,"confidenceLevel":0.95},"criteriaPercentage":14.16,"data":[14.15],"imageDeltaList":[{"a":"20190401_142748","b":"20190401_142150","d":14.15,"cached":true}]},{"path":"47news.visitSite/news.png","degree":1,"sum":15.79,"mean":15.79,"variance":0.0,"standardDeviation":0.0,"tDistribution":0.0,"confidenceInterval":{"lowerBound":15.79,"uppderBound":15.79,"confidenceLevel":0.95},"criteriaPercentage":15.79,"data":[15.79],"imageDeltaList":[{"a":"20190401_142748","b":"20190401_142150","d":15.79,"cached":true}]},{"path":"47news.visitSite/photo.png","degree":1,"sum":3.09,"mean":3.09,"variance":0.0,"standardDeviation":0.0,"tDistribution":0.0,"confidenceInterval":{"lowerBound":3.09,"uppderBound":3.09,"confidenceLevel":0.95},"criteriaPercentage":3.09,"data":[3.09],"imageDeltaList":[{"a":"20190401_142748","b":"20190401_142150","d":3.09,"cached":true}]},{"path":"47news.visitSite/politics.png","degree":1,"sum":3.42,"mean":3.42,"variance":0.0,"standardDeviation":0.0,"tDistribution":0.0,"confidenceInterval":{"lowerBound":3.42,"uppderBound":3.42,"confidenceLevel":0.95},"criteriaPercentage":3.42,"data":[3.42],"imageDeltaList":[{"a":"20190401_142748","b":"20190401_142150","d":3.42,"cached":true}]},{"path":"47news.visitSite/ranking.png","degree":1,"sum":2.96,"mean":2.96,"variance":0.0,"standardDeviation":0.0,"tDistribution":0.0,"confidenceInterval":{"lowerBound":2.96,"uppderBound":2.96,"confidenceLevel":0.95},"criteriaPercentage":2.96,"data":[2.96],"imageDeltaList":[{"a":"20190401_142748","b":"20190401_142150","d":2.96,"cached":true}]},{"path":"47news.visitSite/sports.png","degree":1,"sum":1.31,"mean":1.31,"variance":0.0,"standardDeviation":0.0,"tDistribution":0.0,"confidenceInterval":{"lowerBound":1.31,"uppderBound":1.31,"confidenceLevel":0.95},"criteriaPercentage":1.32,"data":[1.31],"imageDeltaList":[{"a":"20190401_142748","b":"20190401_142150","d":1.31,"cached":true}]},{"path":"47news.visitSite/top.png","degree":1,"sum":12.04,"mean":12.04,"variance":0.0,"standardDeviation":0.0,"tDistribution":0.0,"confidenceInterval":{"lowerBound":12.04,"uppderBound":12.04,"confidenceLevel":0.95},"criteriaPercentage":12.04,"data":[12.04],"imageDeltaList":[{"a":"20190401_142748","b":"20190401_142150","d":12.04,"cached":true}]},{"path":"47news.visitSite/world.png","degree":1,"sum":1.34,"mean":1.34,"variance":0.0,"standardDeviation":0.0,"tDistribution":0.0,"confidenceInterval":{"lowerBound":1.34,"uppderBound":1.34,"confidenceLevel":0.95},"criteriaPercentage":1.35,"data":[1.34],"imageDeltaList":[{"a":"20190401_142748","b":"20190401_142150","d":1.34,"cached":true}]}]}

     */
    def testHasImageDelta() {
        setup:
        StatsEntry se = ids_.getImageDeltaStatsEntry()
        Path pathRelativeToTSuiteTimestampDir =
                Paths.get("47news.visitSite").resolve("47reporters.png")
        when:
        TSuiteTimestamp a = new TSuiteTimestamp("20190401_142748")
        TSuiteTimestamp b = new TSuiteTimestamp("20190401_142150")
        then:
        se.hasImageDelta(pathRelativeToTSuiteTimestampDir, a, b)
        when:
        TSuiteTimestamp another = new TSuiteTimestamp("20190301_065500")
        then:
        ! se.hasImageDelta(pathRelativeToTSuiteTimestampDir, another, b)
    }
    
    def testGetImageDelta() {
        setup:
        StatsEntry se = ids_.getImageDeltaStatsEntry()
        Path pathRelativeToTSuiteTimestampDir =
                Paths.get("47news.visitSite").resolve("47reporters.png")
        when:
        TSuiteTimestamp a = new TSuiteTimestamp("20190401_142748")
        TSuiteTimestamp b = new TSuiteTimestamp("20190401_142150")
        ImageDelta id1 = se.getImageDelta(pathRelativeToTSuiteTimestampDir, a, b)
        then:
        id1 != null
        when:
        TSuiteTimestamp another = new TSuiteTimestamp("20190301_065500")
        ImageDelta id2 = se.getImageDelta(pathRelativeToTSuiteTimestampDir, another, b)
        then:
        id2 == null
    }
    

    @Ignore
    def testIgnoring() {}

    // helper methods
    def void anything() {}
}
