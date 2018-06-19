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
        Path file = tsr.getTSuiteTimestampDirectory().resolve('Result.html')
        if (Files.exists(file)) {
            Files.delete(file)
        }
        OutputStream os = Files.newOutputStream(file)
        when:
        Indexer.makeIndex(tsr, os)
        then:
        Files.exists(file)
    }

    @Ignore
    def testIgnoring() {}

    // helper methods
    def void anything() {}
}
