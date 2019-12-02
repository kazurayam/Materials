package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Indexer
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPairs
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.MaterialStorageFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.imagedifference.ImageCollectionDiffer
import com.kazurayam.materials.impl.TSuiteResultIdImpl
import com.kazurayam.materials.stats.ImageDeltaStats
import com.kazurayam.materials.stats.StorageScanner

import spock.lang.IgnoreRest
import spock.lang.Specification

class IndexerCarouselSpec extends Specification {
    
    static Logger logger_ = LoggerFactory.getLogger(IndexerCarouselSpec.class)
    
    // fields
    static Path specOutputDir
    static Path fixtureDir
    
    // fixture methods
    def setupSpec() {
        Path projectDir = Paths.get('.')
        Path testOutputDir = projectDir.resolve('./build/tmp/testOutput')
        specOutputDir = testOutputDir.resolve("${Helpers.getClassShortName(IndexerCarouselSpec.class)}")
        //if (specOutputDir.toFile().exists()) {
        //    Helpers.deleteDirectoryContents(specOutputDir)
        //}
        fixtureDir = projectDir.resolve('src').resolve('test').resolve('fixture')
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def testSmoke() {
        setup:
        Path caseOutputDir = specOutputDir.resolve('testSmoke')
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir, true)  // 3rd arg means 'skipIfIdentical'
        Indexer indexer = makeIndexerCarousel(caseOutputDir)
        when:
        indexer.execute()
        Path index = indexer.getOutput()
        logger_.debug("#testSmoke index=${index.toString()}")
        then:
        Files.exists(index)
        when:
        String html = index.toFile().text
        then:
        html.contains('<html')
        html.contains('<head')
        html.contains('http-equiv')
        html.contains('<meta charset')
        // html.contains('<!-- [if lt IE 9]')
        html.contains('bootstrap.min.css')
        html.contains('bootstrap-treeview.min.css')
        // html.contains('.list-group-item > .badge {')
        html.contains('<body>')
        html.contains('<div id="tree"')
        html.contains('<div id="footer"')
        
        // div tags as Modal
        html.contains('<div id="modal-windows"')
        html.contains('class="modal fade"')
        html.contains('<div class="modal-dialog modal-lg')
        html.contains('<div class="modal-content"')
        html.contains('<div class="modal-header"')
        html.contains('<p class="modal-title"')
        html.contains('<div class="modal-body"')
        html.contains('<img src="')
        html.contains('<div class="modal-footer"')
        html.contains('class="btn')
        
        
        // script tags
        html.contains('jquery')
        html.contains('popper')
        html.contains('bootstrap')
        html.contains('bootstrap-treeview')
        
        // Bootstrap Treeview data
        html.contains('function getTree() {')
        html.contains('var data = [')
        html.contains('function modalize() {')
        html.contains('$(\'#tree\').treeview({')
        html.contains('modalize();')
    }
    
    def testCarousel() {
        setup:
            Path caseOutputDir = specOutputDir.resolve('testCarousel')
            Path materials = caseOutputDir.resolve('Materials')
            Path storage = caseOutputDir.resolve('Storage')
            Path reports = caseOutputDir.resolve('Reports')
            Files.createDirectories(reports)
            Files.createDirectories(materials)
            Files.createDirectories(storage)
            Helpers.copyDirectory(fixtureDir.resolve('Storage'), storage)
            MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
            MaterialStorage ms = MaterialStorageFactory.createInstance(storage)
            //
            TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
            TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
            Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)
            //
            TSuiteName tsn = new TSuiteName('47News_chronos_capture')
            ms.restore(mr, new TSuiteResultIdImpl(tsn, TSuiteTimestamp.newInstance('20190216_204329')))
            ms.restore(mr, new TSuiteResultIdImpl(tsn, TSuiteTimestamp.newInstance('20190216_064354')))
            mr.scan()
            mr.markAsCurrent('Test Suites/ImageDiff', '20190216_210203')
            def r = mr.ensureTSuiteResultPresent('Test Suites/ImageDiff', '20190216_210203')
        when:
            MaterialPairs materialPairs = mr.createMaterialPairs(tsn)
            StorageScanner.Options options = new StorageScanner.Options.Builder().
            previousImageDeltaStats(previousIDS).
                shiftCriteriaPercentageBy(15.0).       // THIS IS THE POINT
                build()
            StorageScanner storageScanner = new StorageScanner(ms, options)
            ImageDeltaStats imageDeltaStats = storageScanner.scan(tsn)
            //
            storageScanner.persist(imageDeltaStats, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
            double ccp = imageDeltaStats.getCriteriaPercentage(
                            new TSuiteName("47News_chronos_capture"),
                            Paths.get('main.TC_47News.visitSite').resolve('47NEWS_TOP.png'))
        then:
            30.0 < ccp && ccp < 31.0
        when:
            ImageCollectionDiffer icd = new ImageCollectionDiffer(mr)
            icd.makeImageCollectionDifferences(
                materialPairs,
                new TCaseName('Test Cases/ImageDiff'),
                imageDeltaStats)
            mr.scan()
            List<TSuiteResultId> tsriList = mr.getTSuiteResultIdList(new TSuiteName('Test Suites/ImageDiff'))
            assert tsriList.size() == 1
            TSuiteResultId tsri = tsriList.get(0)
            TSuiteResult tsr = mr.getTSuiteResult(tsri)
            TCaseResult tcr = tsr.getTCaseResult(new TCaseName("Test Cases/ImageDiff"))
            List<Material> mateList = tcr.getMaterialList()
        then:
        mateList.size() == 2          // diffImage + comparison-result-bundle.json
        when:
            Indexer indexer = makeIndexerCarousel(caseOutputDir)
            indexer.execute()
            Path index = indexer.getOutput()
            logger_.debug("#testSmoke index=${index.toString()}")
        then:
            Files.exists(index)
        when:
            String html = index.toFile().text
        then:
            html.contains('<html')
            html.contains('<head')
            html.contains('http-equiv')
            html.contains('<meta charset')
            // html.contains('<!-- [if lt IE 9]')
            html.contains('bootstrap.min.css')
            html.contains('bootstrap-treeview.min.css')
            html.contains('.list-group-item > .badge {')
            html.contains('.carousel-item img {')
            html.contains('<body>')
            html.contains('<div id="tree"')
            html.contains('<div id="footer"')
        
            // div tags as Modal
            html.contains('<div id="modal-windows"')
            html.contains('class="modal fade"')
            html.contains('<div class="modal-dialog modal-lg')
            html.contains('<div class="modal-content"')
            html.contains('<div class="modal-header"')
            html.contains('<p class="modal-title"')
            html.contains('<div class="modal-body"')
            html.contains('<img src="')
            html.contains('<div class="modal-footer"')
            html.contains('class="btn')
        
        
            // script tags
            html.contains('jquery')
            html.contains('popper')
            html.contains('bootstrap')
            html.contains('bootstrap-treeview')
        
            // Bootstrap Treeview data
            html.contains('function getTree() {')
            html.contains('var data = [')
            html.contains('function modalize() {')
            html.contains('$(\'#tree\').treeview({')
            html.contains('modalize();')
    }

    def testCarouselWithLinkToOrigin() {
        setup:
            Path caseOutputDir = specOutputDir.resolve('testCarouselWithLinkToOrigin')
            Path materials = caseOutputDir.resolve('Materials')
            Path storage = caseOutputDir.resolve('Storage')
            Path reports = caseOutputDir.resolve('Reports')
            Files.createDirectories(materials)
            Files.createDirectories(storage)
            Files.createDirectories(reports)
            Helpers.copyDirectory(fixtureDir.resolve('Storage'), storage)
            MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
            MaterialStorage ms = MaterialStorageFactory.createInstance(storage)
            //
            TSuiteName tSuiteNameExam = new TSuiteName("Test Suites/47news/chronos_exam")
            TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
            Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)
            //
            TSuiteName tsn = new TSuiteName('Test Suites/47news/chronos_capture')
            ms.restore(mr, new TSuiteResultIdImpl(tsn, TSuiteTimestamp.newInstance('20190401_142150')))
            ms.restore(mr, new TSuiteResultIdImpl(tsn, TSuiteTimestamp.newInstance('20190401_142748')))
            mr.scan()
            mr.markAsCurrent('Test Suites/47news/ImageDiff', '20190401_142749')
            def r = mr.ensureTSuiteResultPresent('Test Suites/47news/ImageDiff', '20190401_142749')
        when:
            MaterialPairs materialPairs = mr.createMaterialPairs(tsn)
            StorageScanner.Options options = new StorageScanner.Options.Builder().
            previousImageDeltaStats(previousIDS).
                shiftCriteriaPercentageBy(15.0).       // THIS IS THE POINT
                build()
            StorageScanner storageScanner = new StorageScanner(ms, options)
            ImageDeltaStats imageDeltaStats = storageScanner.scan(tsn)
            //
            storageScanner.persist(imageDeltaStats, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
            double ccp = imageDeltaStats.getCriteriaPercentage(
                                                new TSuiteName("47news/chronos_capture"),
                                                Paths.get('47news.visitSite').resolve('top.png'))
        then:
            27.0 < ccp && ccp < 28.0
        when:
            ImageCollectionDiffer icd = new ImageCollectionDiffer(mr)
            icd.makeImageCollectionDifferences(
                materialPairs,
                new TCaseName('Test Cases/47news/ImageDiff'),
                imageDeltaStats)
            mr.scan()
            List<TSuiteResultId> tsriList = mr.getTSuiteResultIdList(new TSuiteName('Test Suites/47news/ImageDiff'))
            assert tsriList.size() == 1
            TSuiteResultId tsri = tsriList.get(0)
            TSuiteResult tsr = mr.getTSuiteResult(tsri)
            TCaseResult tcr = tsr.getTCaseResult(new TCaseName("Test Cases/47news/ImageDiff"))
            List<Material> mateList = tcr.getMaterialList()
        then:
            mateList.size() == 13          // diffImage + comparison-result-bundle.json
        when:
            Indexer indexer = makeIndexerCarousel(caseOutputDir)
            indexer.execute()
            Path index = indexer.getOutput()
            logger_.debug("#testSmoke index=${index.toString()}")
        then:
            Files.exists(index)
        when:
            String html = index.toFile().text
        then:
            html.contains('<a')
            html.contains('btn btn-link')
            html.contains('Origin')
    }

	
	
    /**
     * The src attribute of img element contains a relative URL to the PNG file as a ImageDiff. 
     * The PNG file name may contain some special characters that require URL-encoding.
     * For example, a '%' character must be escaped as '%25'.
     * 
     * Therefore should not generate this:
     *    <img src="CURA.visitSite/appointment.php%23summary.20190411_130900_ProductionEnv-20190411_130900_ProductionEnv.(0.00).png" ... >
     * 
     * Rather should generate this:
     *    <img src="CURA.visitSite/appointment.php%2523summary.20190411_130900_ProductionEnv-20190411_130900_ProductionEnv.(0.00).png" ...>
     *
     * @return
     */
    def testAnchorsToURLsThatContainsSpecialCharactersWhichRequireURLEncoding() {
        setup:
            Path caseOutputDir = specOutputDir.resolve('testAnchorsToURLsThatContainsSpecialCharactersWhichRequireURLEncoding')
            def ant = new AntBuilder()
            ant.copy(todir:caseOutputDir.toFile(), overwrite:'yes') {
                fileset(dir:fixtureDir) {
                    include(name:'Materials/CURA.twins_capture/**')
                    include(name:'Materials/CURA.twins_exam/**')
                }
            }
            Path materials = caseOutputDir.resolve('Materials')
            Path reports = caseOutputDir.resolve('Reports')
            Files.createDirectories(reports)
        when:
            MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
            Indexer indexer = makeIndexerCarousel(caseOutputDir)
            indexer.execute()
            Path index = indexer.getOutput()
        then:
            Files.exists(index)
        when:
            String html = index.toFile().text
        then:
            html.contains('CURA.twins_exam/20190412_161622/CURA.ImageDiff_twins/CURA.visitSite/top%2523appointment.20190412_161620_ProductionEnv-20190412_161621_DevelopmentEnv.(0.00).png')
            //                                                                                    ^^^                                                                           ^^^    ^^^
    }
    
    /**
     * helper to make a CarouselIndexer object
     * @param caseOutputDir
     * @return a CarouselIndexer object
     */
    private Indexer makeIndexerCarousel(Path caseOutputDir) {
        Path materialsDir = caseOutputDir.resolve('Materials')
        Path reportsDir   = caseOutputDir.resolve('Reports')
        Indexer indexer = new IndexerCarousel()
        indexer.setBaseDir(materialsDir)
        indexer.setReportsDir(reportsDir)
        Path index = materialsDir.resolve('index.html')
        indexer.setOutput(index)
        return indexer
    }
}
