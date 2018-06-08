package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.Specification

class RepositoryScannerSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")
    private static RepositoryScanner scanner

    // fixture methods
    def setup() {
    }
    def cleanup() {}
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(RepositoryScannerSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
        scanner = new RepositoryScanner(workdir)
    }
    def cleanupSpec() {}

    // feature methods
    def testScan() {
        when:
        List<TSuiteResult> tSuiteResults = scanner.scan()
        then:
        tSuiteResults != null
    }

    // helper methods
}
