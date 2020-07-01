package com.kazurayam.materials.view

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialCore
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.MaterialStorageFactory
import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.ReportsAccessorFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.impl.MaterialCoreImpl
import groovy.xml.MarkupBuilder
import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class RepositoryVisitorGeneratingHtmlDivsAsModalBaseSpec extends Specification {

    private static Path specOutputDir_
    private static Path fixture_ = Paths.get(
        "./src/test/fixtures/com.kazurayam.materials.view.RepositoryVisitorGeneratingHtmlDivsXXXXSpec")

    def setupSpec() {
        specOutputDir_ = Paths.get(
                "./build/tmp/testOutput/${Helpers.getClassShortName(RepositoryVisitorGeneratingHtmlDivsAsModalBaseSpec.class)}"
        )
        Files.createDirectories(specOutputDir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    @Ignore
    def test_findTestSuiteTimestamp() {
        expect:
        true == false
    }

    def test_findExecutionProfileName() {
        setup:
        // copy files from the fixtures directory to the Storage directory
        Path caseOutputDir = specOutputDir_.resolve('test_findExecutionProfileName')
        Helpers.copyDirectory(fixture_, caseOutputDir)
        //
        Path materialsDir = caseOutputDir.resolve('Materials')
        Path storageDir = caseOutputDir.resolve('Storage')
        Path reportsDir = caseOutputDir.resolve('Reports')
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materialsDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(storageDir)
        // copy files from the Storage directory to the Materials directory
        ms.restore(mr, [
                TSuiteResultId.newInstance(
                        new TSuiteName('Test Suites/47News/chronos_capture'),
                        new TExecutionProfile('default'),
                        new TSuiteTimestamp('20190923_112138')),
                TSuiteResultId.newInstance(
                        new TSuiteName('Test Suites/47News/chronos_capture'),
                        new TExecutionProfile('default'),
                        new TSuiteTimestamp('20190923_112816')),
        ])
        ReportsAccessor ra = ReportsAccessorFactory.createInstance(reportsDir)

        Path output = materialsDir.resolve("test_findExecutionProfileName.html")
        Writer writer = new OutputStreamWriter(new FileOutputStream(output.toFile()), 'utf-8')
        MarkupBuilder markupBuilder = new MarkupBuilder(writer)
        RepositoryVisitorGeneratingHtmlDivsAsModalConcise visitor = new RepositoryVisitorGeneratingHtmlDivsAsModalConcise(mr.getRepositoryRoot(), markupBuilder)
        visitor.setReportsAccessor(ra)
        assert visitor != null

        when:
        mr.scan()
        TSuiteResult tsr = mr.getTSuiteResult(TSuiteResultId.newInstance(
                new TSuiteName('Test Suites/47News/chronos_capture'),
                new TExecutionProfile('default'),
                new TSuiteTimestamp('20190923_112816')
        ))
        assert tsr != null
        TCaseResult tcr = tsr.getTCaseResult(
                new TCaseName('Test Cases/47News/visitSite'))
        assert tcr != null
        List<Material> materialList = tcr.getMaterialList()
        assert materialList != null
        Material mate = materialList.get(0)
        MaterialCore mc = new MaterialCoreImpl(mate.getBaseDir(), mate.getPath())
        String executionProfile = visitor.findExecutionProfileName(mr.getRepositoryRoot(), mc)
        then:
        executionProfile != null
        executionProfile == 'default'
    }
}
