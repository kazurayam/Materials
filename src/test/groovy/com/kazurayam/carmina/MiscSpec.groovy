package com.kazurayam.carmina

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification

/**
 * Miscelleneous tests
 */
class MiscSpec extends Specification {
    // fields
    static Logger logger = LoggerFactory.getLogger(MiscSpec.class)

    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")
    private static Path source

    // fixture methods
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(MiscSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        //Helpers.copyDirectory(fixture, workdir)
        source = workdir.resolve('source')
        Writer wt = Files.newBufferedWriter(source)
        wt.write(Material.MAGIC_DELIMITER + ' is usable as a part of file name')
        wt.flush()
        wt.close()
    }
    def setup() {}
    def cleanup() {}
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
        if (Files.exists(target)) {
            try {
                Files.delete(target)
            } catch (IOException e) {
                logger.debugEnabled('no need to delete ${target.toString()} as it is not there')
            }
        }
        try {
            Files.copy(source, target)
            return true
        } catch (IOException e) {
            return false
        }
    }
}
