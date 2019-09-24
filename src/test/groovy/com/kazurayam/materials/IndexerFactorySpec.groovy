package com.kazurayam.materials

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.view.CarouselIndexer

import spock.lang.Specification

class IndexerFactorySpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(IndexerFactorySpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")

    // fixture methods
    def setupSpec() {}
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods

    def testNewIndexerDefault() {
        when:
        Indexer indexer = IndexerFactory.newIndexer()
        then:
        indexer != null
        indexer.getClass().getName() == CarouselIndexer.class.getName()
    }

    def testNewIndexerWithArg_throwsClassNotFoundException() {
        when:
        Indexer indexer = IndexerFactory.newIndexer('com.kazurayam.materials.Foo')
        then:
        def ex = thrown(ClassNotFoundException)
        ex.getMessage().contains('Foo')
    }

}
