package com.kazurayam.material

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.NoSuchFileException
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

    def testPathOperations() {
        when:
        Path path = Paths.get('C:\\home\\joe\\foo')
        logger_.debug("#testPathOperations toString: ${path.toString()}")
        logger_.debug("#testPathOperations getFileName: ${path.getFileName()}")
        logger_.debug("#testPathOperations getName(0): ${path.getName(0)}")
        logger_.debug("#testPathOperations getNameCount: ${path.getNameCount()}")
        logger_.debug("#testPathOperations subpath(0,2): ${path.subpath(0,2)}")
        logger_.debug("#testPathOperations getParent: ${path.getParent()}")
        logger_.debug("#testPathOperations getRoot: ${path.getRoot()}")
        then:
        true
    }

    def testPathOperations_relativePath() {
        when:
        Path path = Paths.get('sally/bar')
        logger_.debug("#testPathOperations_relativePath toString: ${path.toString()}")
        logger_.debug("#testPathOperations_relativePath getFileName: ${path.getFileName()}")
        logger_.debug("#testPathOperations_relativePath getName(0): ${path.getName(0)}")
        logger_.debug("#testPathOperations_relativePath getNameCount: ${path.getNameCount()}")
        logger_.debug("#testPathOperations_relativePath subpath(0,2): ${path.subpath(0,2)}")
        logger_.debug("#testPathOperations_relativePath getParent: ${path.getParent()}")
        logger_.debug("#testPathOperations_relativePath getRoot: ${path.getRoot()}")
        then:
        true
    }

    def testNormalize() {
        when:
        Path path1 = Paths.get('/home/./joe/foo')
        then:
        path1.normalize().toString() == '/home/joe/foo'.replace('/', File.separator)
        when:
        Path path2 = Paths.get('/home/sally/../joe/foo')
        then:
        path2.normalize().toString() == '/home/joe/foo'.replace('/', File.separator)
    }

    def testToUri() {
        when:
        Path path1 = Paths.get("/home/logfile")
        then:
        path1.toUri().toString() == 'file:///C:/home/logfile'
    }

    def testToUri_Material() {
        when:
        def first = "src/test/fixture/Materials/TS1/20180530_130419/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png"
        Path path = Paths.get(first)
        then:
        path.toUri().toString().startsWith('file:///')
        path.toUri().toString().endsWith('http%253A%252F%252Fdemoaut.katalon.com%252F(1).png')
    }

    def testToAbsolutePath() {
        when:
        Path path = Paths.get('foo').toAbsolutePath()
        logger_.debug("#testToAbsolutePath toString: ${path.toString()}")
        logger_.debug("#testToAbsolutePath getFileName: ${path.getFileName()}")
        logger_.debug("#testToAbsolutePath getName(0): ${path.getName(0)}")
        logger_.debug("#testToAbsolutePath getNameCount: ${path.getNameCount()}")
        logger_.debug("#testToAbsolutePath subpath(0,${path.getNameCount()}): " + path.subpath(0,path.getNameCount()))
        logger_.debug("#testToAbsolutePath getParent: ${path.getParent()}")
        logger_.debug("#testToAbsolutePath getRoot: ${path.getRoot()}")
        then:
        true
    }

    def testToRealPath_whenFileExists() {
        when:
        Path path = Paths.get('src/test/fixture/Materials/TS1/20180530_130419/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png')
        path = path.toRealPath()
        logger_.debug("#testToRealPath toString: ${path.toString()}")
        then:
        true
    }

    def testToRealPath_whenFileNotExists() {
        when:
        Path path = Paths.get('foo')
        path = path.toRealPath()
        then:
        NoSuchFileException e = thrown()
        e.cause == null
    }

    def testResolve() {
        when:
        Path p1 = Paths.get('/home/joe/foo')
        Path p2 = p1.resolve('bar')
        then:
        p2.toString().replace(File.separator, '/').contains('/home/joe/foo/bar')
    }

    def testResolve_absolutePath() {
        when:
        Path p1 = Paths.get('/home/joe/foo')
        Path p2 = p1.resolve('/bar')
        then:
        p2.toString().replace(File.separator, '/') == '/bar'
    }

    def testRelativize() {
        setup:
        Path p1 = Paths.get('joe')
        Path p2 = Paths.get('sally')
        when:
        Path p1_to_p2 = p1.relativize(p2)
        then:
        p1_to_p2.toString().replace(File.separator, '/') == '../sally'
        when:
        Path p2_to_p1 = p2.relativize(p1)
        then:
        p2_to_p1.toString().replace(File.separator, '/') == '../joe'
    }

    def testRelativize_underHome() {
        setup:
        Path p1 = Paths.get('home')
        Path p3 = Paths.get('home/sally/bar')
        when:
        Path p1_to_p3 = p1.relativize(p3)
        Path p3_to_p1 = p3.relativize(p1)
        then:
        p1_to_p3.toString().replace(File.separator, '/') == 'sally/bar'
        p3_to_p1.toString().replace(File.separator, '/') == '../..'
    }

    def testEquals() {
        when:
        Path path = Paths.get('src')
        Path otherPath = Paths.get('build')
        then:
        assert !path.equals(otherPath)
    }

    def testStartsWith() {
        when:
        Path path = Paths.get('src/test/fixture/Materials')
        then:
        assert path.startsWith(Paths.get('src'))
    }

    def testEndsWith() {
        when:
        Path path = Paths.get('src/test/fixture/Materials')
        then:
        assert path.endsWith(Paths.get('Materials'))
    }

    def testIterability() {
        when:
        Path path = Paths.get('src/test/fixture/Materials')
        then:
        for (Path name: path) {
            logger_.debug("#testIterability name=${name}")
        }
    }

    def testComparability() {
        when:
        Path p1 = Paths.get('src/main')
        Path p2 = Paths.get('src/test')
        def result = p1.compareTo(p2)
        then:
        result < 0
    }

    def testFilesIsSameFile() {
        when:
        Path p1 = Paths.get('src/test/fixture/Materials/TS1/20180530_130419/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png')
        Path p2 = Paths.get('src/test/fixture/Materials/TS1/20180530_130419/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png')
        then:
        Files.isSameFile(p1, p2)
    }
}
