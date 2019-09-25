package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.MaterialStorageFactory
import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.ReportsAccessorFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp

import groovy.xml.MarkupBuilder

import spock.lang.Ignore
import spock.lang.Specification

class RepositoryVisitorGeneratingHtmlDivsAsModalParallelSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryVisitorGeneratingHtmlDivsAsModalParallelSpec.class)

    // fields
    private static Path specOutputDir_
    private static Path fixture_ = Paths.get(
        "./src/test/fixtures/com.kazurayam.materials.view.RepositoryVisitorGeneratingHtmlDivsParallelSpec")    // Please note this special fixture

    // fixture methods
    def setupSpec() {
        specOutputDir_ = Paths.get(
            "./build/tmp/testOutput/${Helpers.getClassShortName(RepositoryVisitorGeneratingHtmlDivsAsModalParallelSpec.class)}")
        Files.createDirectories(specOutputDir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testSmoke() {
        setup:
            // copy files from the fixture_origin directory to the Storage directory
            Path caseOutputDir = specOutputDir_.resolve('testSmoke')
            Helpers.copyDirectory(fixture_, caseOutputDir)
            //
            Path materialsDir = caseOutputDir.resolve('Materials')
            Path storageDir = caseOutputDir.resolve('Storage')
            Path reportsDir = caseOutputDir.resolve('Reports')
            MaterialRepository mr = MaterialRepositoryFactory.createInstance(materialsDir)
            MaterialStorage ms = MaterialStorageFactory.createInstance(storageDir)
            // copy files from the Storage directory to the Materials directory
            ms.restore(mr, [
                TSuiteResultId.newInstance(new TSuiteName('Test Suites/47News/chronos_capture'), new TSuiteTimestamp('20190923_112138')),
                TSuiteResultId.newInstance(new TSuiteName('Test Suites/47News/chronos_capture'), new TSuiteTimestamp('20190923_112816')),
            ])
            ReportsAccessor ra = ReportsAccessorFactory.createInstance(reportsDir)
        when:
            Path output = materialsDir.resolve('testSmoke.html')
            Writer writer = new OutputStreamWriter(new FileOutputStream(output.toFile()), 'utf-8')
            MarkupBuilder markupBuilder = new MarkupBuilder(writer)
            //
            RepositoryVisitorGeneratingHtmlDivsAsModalParallel visitor = new RepositoryVisitorGeneratingHtmlDivsAsModalParallel(markupBuilder)
            visitor.setReportsAccessor(ra)
        then:
            visitor != null
        when:
            mr.scan()   // refresh MaterialRepository's internal data structure with the updated file tree on disk
            TSuiteResult tsr = mr.getTSuiteResult(TSuiteResultId.newInstance(
                                    new TSuiteName('Test Suites/47News/chronos_capture'),
                                    new TSuiteTimestamp('20190923_112816')))
        then:
            tsr != null
        when:
            TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/47News/visitSite'))
        then:
            tcr != null
        when:
            List<Material> materialList = tcr.getMaterialList()
        then:
            materialList != null
            materialList.size()> 0
        // check
        when:
            Material mate = materialList.get(0)    //
            visitor.visitMaterial(mate)
            writer.flush()
        then:
            Files.exists(output)
        /*
        when:
            String html = output.toFile().text
        then:
            html.contains('Origin')
         */
        /* In order to see "Back origin" and "Forth origin", visitor.visitMaterial(mate) is not enough
         * we need to visit the "Materials" directory as a whole.
        when:
            tsr = mr.getTSuiteResult(TSuiteResultId.newInstance(
                            new TSuiteName('Test Suites/47news/chronos_exam'),
                            new TSuiteTimestamp('20190404_112054')))
            tcr = tsr.getTCaseResult(new TCaseName('Test Cases/47news/ImageDiff'))
            materialList = tcr.getMaterialList()
            mate = materialList.get(0)
            visitor.visitMaterial(mate)
            writer.flush()
            html = output.toFile().text
        then:
            html.contains('Back origin')
            html.contains('Forth origin')
         */
    }

    @Ignore
    def testIgnoring() {}

    // helper methods
    def void anything() {}

    
}
