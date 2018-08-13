package com.kazurayam.material

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Ignore
import spock.lang.Specification

//@Ignore
class IndexerImplSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(IndexerImplSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(IndexerImplSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods

    def testMakeIndex() {
        setup:
        Indexer indexer = IndexerFactory.newIndexer()
        indexer.setBaseDir(workdir_.resolve('Materials'))
        when:
        indexer.execute()
        Path index = indexer.getOutput()
        then:
        index != null
        Files.exists(index)
        when:
        logger_.debug("#test makeIndex ${index.toFile().getText('UTF-8')}")
        then:
        true
    }


    @Ignore
    def testIgnoring() {}

    // helper methods
    def void anything() {}
}
