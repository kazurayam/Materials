package com.kazurayam.carmina

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Ignore
import spock.lang.Specification

//@Ignore
class IndexerSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(IndexerSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")
    private static TestMaterialsRepositoryImpl tmri_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(IndexerSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        tmri_ = new TestMaterialsRepositoryImpl(workdir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    
    def testMakeIndex() {
        setup:
        TSuiteResult tsr = tmri_.getTSuiteResult(new TSuiteName("TS1"), new TSuiteTimestamp('20180530_130419'))
        Path file = tsr.getTSuiteTimestampDirectory().resolve('Materials.html')
        if (Files.exists(file)) {
            Files.delete(file)
        }
        OutputStream os = Files.newOutputStream(file)
        when:
        Indexer.makeIndex(tsr, os)
        then:
        Files.exists(file)
    }

    def testMakeIndex2() {
        setup:
        TSuiteResult tsr = tmri_.getTSuiteResult(new TSuiteName("TS1"), new TSuiteTimestamp('20180530_130419'))
        Path file = tsr.getTSuiteTimestampDirectory().resolve('Materials.html')
        if (Files.exists(file)) {
            Files.delete(file)
        }
        OutputStream os = Files.newOutputStream(file)
        Indexer indexer = new Indexer()
        when:
        indexer.makeIndex2(tsr, os)
        then:
        Files.exists(file)
    }

    /**
     * test loading content of the bootstram-treeview.js file from Java CLASSPATH
     *
     * @return
     */
    def testLoadingBootstrapTreeviewJsFromClasspath() {
        setup:
        Indexer indexer = new Indexer()
        when:
        String jsContent = indexer.getResource("bootstrap-treeview/bootstrap-treeview.js")
        logger_.debug("bootstrap-treeview.js:\n${jsContent}")
        then:
        jsContent.length() > 0
    }

    /**
     * test loading content of the bootstram-treeview.css file from Java CLASSPATH
     *
     * @return
     */
    def testLoadingBootstrapTreeviewCssFromClasspath() {
        setup:
        Indexer indexer = new Indexer()
        when:
        String cssContent = indexer.getResource("bootstrap-treeview/bootstrap-treeview.css")
        logger_.debug("bootstrap-treeview.js:\n${cssContent}")
        then:
        cssContent.length() > 0
    }

    @Ignore
    def testIgnoring() {}

    // helper methods
    def void anything() {}
}
