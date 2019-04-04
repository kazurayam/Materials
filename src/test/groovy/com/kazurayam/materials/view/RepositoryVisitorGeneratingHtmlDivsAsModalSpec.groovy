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
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp

import groovy.xml.MarkupBuilder

import spock.lang.Ignore
import spock.lang.Specification

class RepositoryVisitorGeneratingHtmlDivsAsModalSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryVisitorGeneratingHtmlDivsAsModalSpec.class)

    // fields
    private static Path specOutputDir_
    private static Path fixture_ = Paths.get("./src/test/fixture")

    // fixture methods
    def setupSpec() {
        specOutputDir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(RepositoryVisitorGeneratingHtmlDivsAsModalSpec.class)}")
        Files.createDirectories(specOutputDir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testSmoke() {
        setup:
            Path inputDirOfCapture = fixture_.resolve('Storage').resolve('47news.chronos_capture')
            Path caseOutputDir = specOutputDir_.resolve('testSmoke')
            Path materialsDir = caseOutputDir.resolve('Materials')
            Path storageDir = caseOutputDir.resolve('Storage')
            Path outputDirOfCapture = storageDir.resolve('47news.chronos_capture')
            Files.createDirectories(outputDirOfCapture)
            Helpers.copyDirectory(inputDirOfCapture, outputDirOfCapture)
            MaterialRepository mr = MaterialRepositoryFactory.createInstance(materialsDir)
            MaterialStorage ms = MaterialStorageFactory.createInstance(storageDir)
            ms.restore(mr, [
                TSuiteResultId.newInstance(new TSuiteName('Test Suites/47news/chronos_capture'), new TSuiteTimestamp('20190401_142150')),
                TSuiteResultId.newInstance(new TSuiteName('Test Suites/47news/chronos_capture'), new TSuiteTimestamp('20190401_142748')),
            ])
        when:
            Path output = caseOutputDir.resolve('output.html')
            Writer writer = new OutputStreamWriter(new FileOutputStream(output.toFile()), 'utf-8')
            MarkupBuilder markupBuilder = new MarkupBuilder(writer)
            RepositoryVisitorGeneratingHtmlDivsAsModal visitor = new RepositoryVisitorGeneratingHtmlDivsAsModal(markupBuilder)
        then:
            visitor != null
        when:
            TSuiteResult tsr = mr.getTSuiteResult(TSuiteResultId.newInstance(
                                    new TSuiteName('Test Suites/47news/chronos_capture'),
                                    new TSuiteTimestamp('20190401_142150')))
        then:
            tsr != null
        when:
            TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/47news/visitSite'))
        then:
            tcr != null
        when:
            List<Material> materialList = tcr.getMaterialList()
        then:
            materialList != null
            materialList.size()> 0
        when:
            Material mate = materialList.get(0)
            visitor.visitMaterial(mate)
            writer.flush()
        then:
            Files.exists(output)
        when:
            String html = output.toFile().text
        then:
            html.contains('Origin')
    }

    @Ignore
    def testIgnoring() {}

    // helper methods
    def void anything() {}

}
