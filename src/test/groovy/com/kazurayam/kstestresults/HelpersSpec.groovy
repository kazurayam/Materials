package com.kazurayam.kstestresults

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.regex.Pattern

import spock.lang.Specification

/**
 * http://spockframework.org/spock/docs/1.0/spock_primer.html
 */
//@Ignore
class HelpersSpec extends Specification {

    // fields
    private static Path workdir

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(HelpersSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }
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

    def testCopyDirectory() {
        setup:
        Path sourceDir = Paths.get('./src/test/fixture/Screenshots')
        Path targetDir = workdir.resolve('testCopyDirectory')
        when:
        Helpers.copyDirectory(sourceDir, targetDir)
        then:
        Files.exists(targetDir.resolve('TS1'))
        Files.exists(targetDir.resolve('TS1/20180530_130419'))
        Files.exists(targetDir.resolve('TS1/20180530_130419/TC1'))
        Files.exists(targetDir.resolve('TS1/20180530_130419/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
        Files.exists(targetDir.resolve('TS1/20180530_130604'))
        Files.exists(targetDir.resolve('TS1/20180530_130604/TC1'))
        Files.exists(targetDir.resolve('TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
    }

    def testGetClassShortName() {
        expect:
        Helpers.getClassShortName(Helpers.class) == 'Helpers'
    }

    def testEscapeAsJsonText() {
        expect:
        Helpers.escapeAsJsonText('/') == '\\/'
        Helpers.escapeAsJsonText('\\') == '\\\\'
        Helpers.escapeAsJsonText('"') == '\\"'
    }

    // helper methods
    private boolean someHelper() {
        return true
    }
}
