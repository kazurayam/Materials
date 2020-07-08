package com.kazurayam.materials.repository

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput

/**
 * TreeTrunkScanner scans a file system tree under the baseDir directory.
 *
 * Materials/<Test Suite name>/<Execution Profile name>/<Test Suite timestamp>/
 *
 * It makes a List of TSuiteResult which contains TCaseResult and  Material
 * as found in the baseDir.
 *
 * @author kazurayam
 */
final class TreeTrunkScanner {

    static Logger logger_ = LoggerFactory.getLogger(TreeTrunkScanner.class)

    private RepositoryRoot repoRoot_

    TreeTrunkScanner(Path baseDir) {
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
     * scan the baseDir to return an instance of RepositoryRoot
     */
    void scan() {
        Files.walkFileTree(
                repoRoot_.getBaseDir(),
                new HashSet<>(),
                Integer.MAX_VALUE,
                new TreeTrunkVisitor(repoRoot_)
        )
        //
        if (repoRoot_.getLatestModifiedTSuiteResult() != null) {
            repoRoot_.getLatestModifiedTSuiteResult().setLatestModified(true)
        }
    }

    RepositoryRoot getRepositoryRoot() {
        return repoRoot_
    }


    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TreeTrunkScanner":{')
        sb.append('"repoRoot":' + repoRoot_.toJsonText())
        sb.append('}}')
        return sb.toString()
    }

    public static void main(String[] args) {
        logger_.info("#main " + ("Hello, I am TreeTrunkScanner."))
        Path baseDir = Paths.get(System.getProperty('user.dir') + '/src/test/fixture/Materials')
        TreeTrunkScanner scanner = new TreeTrunkScanner(baseDir)
        scanner.scan()
        logger_.info("#main " + JsonOutput.prettyPrint(scanner.toJsonText()))
    }

}
