package com.kazurayam.materials.view

import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.VTLoggerEnabled
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

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
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryWalker
import groovy.xml.MarkupBuilder

import spock.lang.Ignore
import spock.lang.Specification

/**
 * test RepositoryVisitorGeneratingBootstraTreeviewData class
 */
class RepositoryVisitorGeneratingBootstrapTreeviewDataSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryVisitorGeneratingBootstrapTreeviewDataSpec.class)

    VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()

    // fields
    private static Path specOutputDir_
    private static Path fixture_ = Paths.get("./src/test/fixture_origin")

    def setupSpec() {
        specOutputDir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(RepositoryVisitorGeneratingBootstrapTreeviewDataSpec.class)}")
        Files.createDirectories(specOutputDir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    @Ignore
    def testFoo() {}

    def testSmoke() {
        setup:
        // copy fixture files from the fixture_origin directory to the Storage directory
        Path  caseOutputDir = specOutputDir_.resolve("testSmoke")
        Helpers.copyDirectory(fixture_, caseOutputDir)
        //
        Path materialsDir = caseOutputDir.resolve("Materials")
        Path storageDir = caseOutputDir.resolve("Storage")
        Path reportsDir = caseOutputDir.resolve("Reports")
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materialsDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(storageDir)
        // copy files from the Stroage directory to the Materials directory
        ms.restore(mr, [
                TSuiteResultId.newInstance(
                        new TSuiteName('Test Suites/47news/chronos_capture'),
                        new TExecutionProfile('default'),
                        new TSuiteTimestamp('20190404_111956')),
                TSuiteResultId.newInstance(
                        new TSuiteName('Test Suites/47news/chronos_capture'),
                        new TExecutionProfile('default'),
                        new TSuiteTimestamp('20190404_112053')),
        ])
        ReportsAccessor ra = ReportsAccessorFactory.createInstance(reportsDir)
        when:
        StringWriter jsonSnippet = new StringWriter()
        VTLoggerEnabled visitor =
                new RepositoryVisitorGeneratingBootstrapTreeviewData(jsonSnippet)
        visitor.setReportsAccessor(ra)
        visitor.setVisualTestingLogger(vtLogger_)
        RepositoryWalker.walkRepository(mr.getRepositoryRoot(), visitor)
        JsonSlurper slurper = new JsonSlurper()
        logger_.debug(JsonOutput.prettyPrint(jsonSnippet.toString()))
        def json = slurper.parseText(jsonSnippet.toString())
        then:

        // 47news.chronos_capture/20190404_112053
        json[0].tags[0] == "EXECUTED:3,FAILED:0,ERROR:0"
        json[0].tags[1] == "TIME:15"
        json[0].tags[2] == "PNG:1"
        json[0].tags[3] == "default"
        json[0].tags[4] == "Firefox"

        // 47news.chronos_capture/20190404_111956
        json[1].tags[0] == "EXECUTED:3,FAILED:0,ERROR:0"
        json[1].tags[1] == "TIME:15"
        json[1].tags[2] == "PNG:1"
        json[1].tags[3] == "default"
        json[1].tags[4] == "Firefox"

        // 47news.chronos_exam/20190404_112054
        json[2].tags[0] == "EXECUTED:1,FAILED:0,ERROR:0"
        json[2].tags[1] == "TIME:6"
        json[2].tags[2] == "PNG:1"
        json[2].tags[3] == "default"
        json[2].tags[4] == "Firefox"

    }


}

























