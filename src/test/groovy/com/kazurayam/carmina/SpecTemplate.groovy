package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.carmina.Helpers

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class SpecTemplate extends Specification {
    
    static Logger logger = LoggerFactory.getLogger(SpecTemplate.class)

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")

    // fixture methods
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(SpecTemplate.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testSomething() {
        setup:
        anything()
        when:
        anything()
        then:
        anything()
        cleanup:
        anything()
    }

    @Ignore
    def testIgnoring() {}

    // helper methods
    def void anything() {}
}
