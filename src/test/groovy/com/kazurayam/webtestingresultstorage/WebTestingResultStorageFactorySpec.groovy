package com.kazurayam.webtestingresultstorage

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.webtestingresultstorage.Helpers
import com.kazurayam.webtestingresultstorage.WebTestingResultStorage
import com.kazurayam.webtestingresultstorage.WebTestingResultStorageFactory

import spock.lang.Specification
//@Ignore
class WebTestingResultStorageFactorySpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Screenshots")

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(WebTestingResultStorageFactorySpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testCreateInstance() {
        setup:
        String dirName = 'testCreateInstance'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
        when:
        WebTestingResultStorage scRepo = WebTestingResultStorageFactory.createInstance(workdir, 'Test Suites/TS1')
        then:
        scRepo != null
        scRepo.toString().contains('TS1')
    }

    // helper methods

}
