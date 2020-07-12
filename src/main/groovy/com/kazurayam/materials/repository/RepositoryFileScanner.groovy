package com.kazurayam.materials.repository

import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * RepositoryScanner scans a file system tree under the baseDir directory.
 *
 * Materials/<Test Suite name>/<Execution Profile name>/<Test Suite timestamp>/<Test Case name>
 *
 * It makes a List of TSuiteResult which contains TCaseResult and  Material
 * as found in the baseDir.
 *
 * @author kazurayam
 */
final class RepositoryFileScanner {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryFileScanner.class)

    private RepositoryRoot repoRoot_

    RepositoryFileScanner(Path baseDir) {
        Objects.requireNonNull(baseDir, "baseDir must not be null")
        if (!Files.exists(baseDir)) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        if (!Files.isDirectory(baseDir)) {
            throw new IllegalArgumentException("${baseDir} is not a directory")
        }
        repoRoot_ = new RepositoryRoot(baseDir)
    }

    /**
     * scan the baseDir and all of contained files/subdirectories as one batch.
     * the repoRoot_ variable will hold a pointer to a large tree of
     * TSuiteResults + TCaseResults + Materials.
     *
     */
    void scan() {
        Files.walkFileTree(
                repoRoot_.getBaseDir(),
                new HashSet<>(),
                Integer.MAX_VALUE,
                new RepositoryFileVisitor(repoRoot_)
        )

    }


    RepositoryRoot getRepositoryRoot() {
        return repoRoot_
    }


    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"RepositoryScanner":{')
        sb.append('"repoRoot":' + repoRoot_.toJsonText() + '"')
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
        RepositoryFileScanner scanner = new RepositoryFileScanner(baseDir)
        scanner.scan()
        logger_.info("#main " + JsonOutput.prettyPrint(scanner.toJsonText()))
    }

}
