package com.kazurayam.carmina.material

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.carmina.material.Helpers
import com.kazurayam.carmina.material.Material

import spock.lang.Specification

/**
 * Miscelleneous tests
 */
class MiscSpec extends Specification {
    // fields
    static Logger logger_ = LoggerFactory.getLogger(MiscSpec.class)

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")
    private static Path source_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(MiscSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        //Helpers.copyDirectory(fixture, workdir)
        source_ = workdir_.resolve('source')
        Writer wt = Files.newBufferedWriter(source_)
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

    def testURLEncoder_colon() {
        expect:
        URLEncoder.encode(':', 'UTF-8') == '%3A'
    }

    def testURLEncoder_solidus() {
        expect:
        URLEncoder.encode('/', 'UTF-8') == '%2F'
    }

    def testURLEncoder_hyphen() {
        expect:
        URLEncoder.encode('-', 'UTF-8') == '-'
    }

    def testURLEncoder_period() {
        expect:
        URLEncoder.encode('.', 'UTF-8') == '.'
    }




    // helper methods
    def boolean resistant(String s) {
        String encoded = URLEncoder.encode(s, 'UTF-8')
        return s == encoded
    }

    def boolean usableAsFileName(String str) {
        Path target = workdir_.resolve(str)
        if (Files.exists(target)) {
            try {
                Files.delete(target)
            } catch (IOException e) {
                logger_.debugEnabled('no need to delete ${target.toString()} as it is not there')
            }
        }
        try {
            Files.copy(source_, target)
            return true
        } catch (IOException e) {
            return false
        }
    }
}
