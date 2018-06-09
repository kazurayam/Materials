package com.kazurayam.carmina

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Miscelleneous tests
 */
class MiscSpec extends Specification {
// fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")
    private static Path source

    // fixture methods
    def setup() {
    }
    def cleanup() {}
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(MiscSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        //Helpers.copyDirectory(fixture, workdir)
        source = workdir.resolve('source')
        Writer wt = Files.newBufferedWriter(source)
        wt.write(MaterialWrapper.MAGIC_DELIMITER + ' is usable as a part of file name')
        wt.flush()
        wt.close()
    }
    def cleanupSpec() {}

    // feature methods
    def testResistant() {
        expect:
        resistant('._')
        resistant('0123456789')
        resistant('abcdefghijklmnopqrstuvwxyz')
    }

    def testNonResistant() {
        expect:
        !resistant(':')
        !resistant('/')
        !resistant('\\')
    }

    def testSection() {
        expect:
        !resistant('§')
        usableAsFileName('§')
    }
    // helper methods
    def boolean resistant(String s) {
        String encoded = URLEncoder.encode(s, 'UTF-8')
        return s == encoded
    }

    def boolean usableAsFileName(String str) {
        Path target = workdir.resolve(str)
        try {
            Files.copy(source, target)
            return true
        } catch (IOException e) {
            return false
        }
    }
}
