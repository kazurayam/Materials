package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Indexer
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPair
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

import spock.lang.Specification

class CarouselIndexerSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(CarouselIndexerSpec.class)
    
    
    // fields
    static Path specOutputDir
    static Path fixtureDir
    
    // fixture methods
    def setupSpec() {
        Path projectDir = Paths.get('.')
        Path testOutputDir = projectDir.resolve('./build/tmp/testOutput')
        specOutputDir = testOutputDir.resolve("${Helpers.getClassShortName(CarouselIndexerSpec.class)}")
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
            Path materials = caseOutputDir.resolve('Materials')
            Path storage = caseOutputDir.resolve('Storage')
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
            mr.putCurrentTestSuite('Test Suites/ImageDiff', '20190216_210203')
        when:
            List<MaterialPair> materialPairs =
            mr.createMaterialPairs(tsn).stream().filter { mp ->
                mp.getLeft().getFileType() == FileType.PNG
            }.collect(Collectors.toList())
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
            Indexer indexer = makeIndexer(caseOutputDir)
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
            html.contains('<div class="modal-dialog modal-lg"')
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
    
    /**
     * helper to make a CarouselIndexer object
     * @param caseOutputDir
     * @return a CarouselIndexer object
     */
    private Indexer makeIndexer(Path caseOutputDir) {
        Path materialsDir = caseOutputDir.resolve('Materials')
        Path reportsDir   = caseOutputDir.resolve('Reports')
        Indexer indexer = new CarouselIndexer()
        indexer.setBaseDir(materialsDir)
        indexer.setReportsDir(reportsDir)
        Path index = materialsDir.resolve('index.html')
        indexer.setOutput(index)
        return indexer
    }

}
