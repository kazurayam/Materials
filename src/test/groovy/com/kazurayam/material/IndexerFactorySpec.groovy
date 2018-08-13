package com.kazurayam.material

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification

class IndexerFactorySpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(IndexerFactorySpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")

    // fixture methods
    def setupSpec() {
        /*
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(IndexerFactorySpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        */
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods

    def testNewIndexerDefault() {
        when:
        Indexer indexer = IndexerFactory.newIndexer()
        then:
        indexer != null
        indexer.getClass().getName() == 'com.kazurayam.material.IndexerImpl'
    }

    def testNewIndexerWithArg() {
        when:
        Indexer indexer = IndexerFactory.newIndexer('com.kazurayam.material.IndexerImpl')
        then:
        indexer != null
        indexer.getClass().getName() == 'com.kazurayam.material.IndexerImpl'
    }
    
    def testNewIndexerWithArg_throwsClassNotFoundException() {
        when:
        Indexer indexer = IndexerFactory.newIndexer('com.kazurayam.material.Foo')
        then:
        def ex = thrown(ClassNotFoundException)
        ex.getMessage().contains('Foo')
    }
    
}
