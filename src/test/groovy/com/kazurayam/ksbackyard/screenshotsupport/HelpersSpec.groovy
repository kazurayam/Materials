package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.regex.Pattern

import spock.lang.Specification

/**
 * http://spockframework.org/spock/docs/1.0/spock_primer.html
 */
class HelpersSpec extends Specification {

    // fields
    private static Path workdir

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getName()}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    /**
     *
     * @return
     */
    def testEnsureDirs() {
        setup:
        Path subdir = workdir.resolve('testEnsureDirs')
        when:
        Helpers.ensureDirs(subdir)
        then:
        subdir.toFile().exists()
        cleanup:
        Helpers.deleteDirectory(subdir)
    }

    def testTouch() {
        setup:
        Path subdir = workdir.resolve('testTouch')
        Helpers.ensureDirs(subdir)
        Path file = subdir.resolve('dummy')
        when:
        Helpers.touch(file)
        then:
        file.toFile().exists()
        cleanup:
        Helpers.deleteDirectory(subdir)
    }

    def testDeleteDirectory() {
        setup:
        Path subdir = workdir.resolve('testDeleteDirectory')
        Helpers.ensureDirs(subdir)
        Path file = subdir.resolve('dummy')
        Helpers.touch(file)
        when:
        Helpers.deleteDirectory(subdir)
        then:
        !subdir.toFile().exists()
    }

    def testGetTimestampAsString() {
        setup:
        //Pattern pattern = Pattern.compile('[12][0-9]{3}[01][0-9][0-5][0-9]_[012][0-5][0-5][0-9]') // 20180529_110342
        Pattern pattern = Pattern.compile('[0-9]{8}_[0-9]{6}')
        when:
        String tstamp = Helpers.getTimestampAsString(LocalDateTime.now())
        then:
        pattern.matcher(tstamp).matches()
    }

    // helper methods
    private boolean someHelper() {
        return true
    }
}
