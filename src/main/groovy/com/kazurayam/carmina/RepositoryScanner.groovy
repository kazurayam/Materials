package com.kazurayam.carmina

import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput

/**
 * RepositoryScanner scans a file system tree under the baseDir directory, and it builds object trees of
 * TSuiteResult under which contained objects of TCaseResult + TargetURL + Material as found in the
 * local storage.
 *
 * @author kazurayam
 */
class RepositoryScanner {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryScanner.class)

    private Path baseDir_
    private List<TSuiteResult> tSuiteResults_

    RepositoryScanner(Path baseDir) {
        assert baseDir != null
        if (!Files.exists(baseDir)) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        if (!Files.isDirectory(baseDir)) {
            throw new IllegalArgumentException("${baseDir} is not a directory")
        }
        baseDir_ = baseDir
        tSuiteResults_ = new ArrayList<TSuiteResult>()
    }

    /**
     * initialize the internal List<TSuiteResult> object, and scan the baseDir
     * to instanciate trees of TSuiteResults
     */
    void scan() {
        tSuiteResults_ = new ArrayList<TSuiteResult>()
        Files.walkFileTree(
                baseDir_,
                EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new RepositoryVisitor(baseDir_, tSuiteResults_)
        )
    }

    List<TSuiteResult> getTSuiteResults() {
        return tSuiteResults_
    }

    List<TSuiteResult> getTSuiteResults(TSuiteName tSuiteName) {
        List<TSuiteResult> tSuiteResults = new ArrayList<TSuiteResult>()
        for (TSuiteResult tSuiteResult : tSuiteResults_) {
            if (tSuiteName == tSuiteResult.getTSuiteName()) {
                tSuiteResults.add(tSuiteResult)
            }
        }
        return tSuiteResults
    }

    List<TSuiteResult> getTSuiteResults(TSuiteTimestamp tSuiteTimestamp) {
        List<TSuiteResult> tSuiteResults = new ArrayList<TSuiteResult>()
        for (TSuiteResult tSuiteResult : tSuiteResults_) {
            if (tSuiteTimestamp == tSuiteResult.getTSuiteTimestamp()) {
                tSuiteResults.add(tSuiteResult)
            }
        }
        return tSuiteResults
    }

    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        for (TSuiteResult tSuiteResult : tSuiteResults_) {
            if (tSuiteName == tSuiteResult.getTSuiteName() && tSuiteTimestamp == tSuiteResult.getTSuiteTimestamp()) {
                return tSuiteResult
            }
        }
        return null
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('[')
        def count = 0
        for (TSuiteResult tSuiteResult : tSuiteResults_) {
            if (count > 0) {
                sb.append(',')
            }
            count += 1
            sb.append(tSuiteResult.toJson())
        }
        sb.append(']')
        return sb.toString()
    }

    /**
     * entry point for performance profiling
     *
     * @param args
     */
    public static void main(String[] args) {
        logger_.info("#main " + ("Hello, I am Carmina RepositoryScanner."))
        Path baseDir = Paths.get(System.getProperty('user.dir') + '/src/test/fixture/Results')
        RepositoryScanner scanner = new RepositoryScanner(baseDir)
        scanner.scan()
        logger_.info("#main " + JsonOutput.prettyPrint(scanner.toJson()))
    }

}
