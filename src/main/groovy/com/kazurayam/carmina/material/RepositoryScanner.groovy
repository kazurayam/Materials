package com.kazurayam.carmina.material

import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput

/**
 * RepositoryScanner scans a file system tree under the baseDir directory.
 * It assumes the file tree is organized in the format as this:
 *
 * <pre>
 * baseDir
 * |
 * +-TS1
 * |  +-20180530_130419
 * |  |  +-TC1
 * |  |          http%3A%2F%2Fdemoaut.katalon.com%2F.png
 * |  |          http%3A%2F%2Fdemoaut.katalon.com%2F§1.png
 * |  |
 * |  +-20180530_130604
 * |      +-TC1
 * |      |      http%3A%2F%2Fdemoaut.katalon.com%2F.png
 * |      |
 * |      +-TC2
 * |             http%3A%2F%2Fdemoaut.katalon.com%2F§atoz.png
 * |
 * +-TS2
 * |  +-20180612_111256
 * |      |
 * |      +-TC1
 * |             http%3A%2F%2Fdemoaut.katalon.com%2F.png
 * |
 * +-_
 *    +-_
 *        +-TC1
 *               http%3A%2F%2Fdemoaut.katalon.com%2F.png
 * </pre>
 *
 * It makes a List of TSuiteResult which containes TCaseResult and  Material
 * as found in the baseDir.
 *
 * @author kazurayam
 */
class RepositoryScanner {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryScanner.class)

    private RepositoryRoot repoRoot_

    RepositoryScanner(Path baseDir) {
        assert baseDir != null
        if (!Files.exists(baseDir)) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        if (!Files.isDirectory(baseDir)) {
            throw new IllegalArgumentException("${baseDir} is not a directory")
        }
        repoRoot_ = new RepositoryRoot(baseDir)
    }

    /**
     * scan the baseDir to return an instance of RepositoryRoot
     */
    void scan() {
        Files.walkFileTree(
                repoRoot_.getBaseDir(),
                EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new RepositoryVisitor(repoRoot_)
        )
        //
        if (repoRoot_.getLatestModifiedTSuiteResult() != null) {
            repoRoot_.getLatestModifiedTSuiteResult().setLatestModified(true)
        }
        //
        logger_.debug("#scan repoRoot_=${JsonOutput.prettyPrint(repoRoot_.toJson())}")
    }

    RepositoryRoot getRepositoryRoot() {
        return repoRoot_
    }


    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"RepositoryScanner":{')
        sb.append('"repoRoot":' + repoRoot_.toJson() + '"')
        sb.append('}}')
        return sb.toString()
    }

    /**
     * entry point for performance profiling
     *
     * @param args
     */
    public static void main(String[] args) {
        logger_.info("#main " + ("Hello, I am Carmina RepositoryScanner."))
        Path baseDir = Paths.get(System.getProperty('user.dir') + '/src/test/fixture/Materials')
        RepositoryScanner scanner = new RepositoryScanner(baseDir)
        scanner.scan()
        logger_.info("#main " + JsonOutput.prettyPrint(scanner.toJson()))
    }

}
