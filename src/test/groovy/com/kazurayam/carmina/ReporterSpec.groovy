package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

//@Ignore
class ReporterSpec extends Specification {
    
    static Logger logger_ = LoggerFactory.getLogger(ReporterSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Results")
    private static TestResultsRepositoryImpl trri_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(ReporterSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        trri_ = new TestResultsRepositoryImpl(workdir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testReport() {
        setup:
        TSuiteResult tsr = trri_.getTSuiteResult(new TSuiteName("TS1"), new TSuiteTimestamp('20180530_130419'))
        Path file = tsr.getTSuiteTimestampDirectory().resolve('Result.html')
        if (Files.exists(file)) {
            Files.delete(file)
        }
        OutputStream os = Files.newOutputStream(file)
        when:
        Reporter.report(tsr, os)
        then:
        Files.exists(file)
    }

    @Ignore
    def testIgnoring() {}

    // helper methods
    def void anything() {}
}
