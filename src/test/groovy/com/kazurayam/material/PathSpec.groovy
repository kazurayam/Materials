package com.kazurayam.material

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification

/**
 * Some tests to learn the features of java.nio.file.Path
 *
 * See https://docs.oracle.com/cd/E26537_01/tutorial/essential/io/pathOps.html
 */
class PathSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(PathSpec.class)

    def testCreatingPath() {
        when:
        Path p1 = Paths.get("/tmp/foo")
        then:
        p1 != null
    }

    def testPathsGet() {
        when:
        String userHome = System.getProperty('user.home')
        Path p4 = FileSystems.getDefault().getPath(userHome, 'tmp')
        then:
        p4 != null
        when:
        Path p5 = Paths.get(userHome, 'tmp', 'smilechart.xls')
        then:
        p5 != null
    }

    def testPathConcept() {
        when:
        Path path = Paths.get('C:\\home\\joe\\foo')
        logger_.debug("toString: %{path.toString}")
        then:
        true
    }

}
