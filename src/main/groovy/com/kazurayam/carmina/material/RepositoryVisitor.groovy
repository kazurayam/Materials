package com.kazurayam.carmina.material

import static java.nio.file.FileVisitResult.*

import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 */
class RepositoryVisitor extends SimpleFileVisitor<Path> {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryVisitor.class)

    private RepositoryRoot repoRoot_

    private TSuiteName tSuiteName_
    private TSuiteTimestamp tSuiteTimestamp_
    private TSuiteResult tSuiteResult_
    private TCaseName tCaseName_
    private TCaseResult tCaseResult_
    private Material material_

    private static enum Layer {
        INIT, ROOT, TESTSUITE, TIMESTAMP, TESTCASE, MATERIAL
    }
    private Stack<Layer> directoryTransition_

    RepositoryVisitor(RepositoryRoot repoRoot) {
        repoRoot_ = repoRoot
        directoryTransition_ = new Stack<Layer>()
        directoryTransition_.push(Layer.INIT)
    }

    /**
     * Invoked for a directory before entries in the directory are visited.
     */
    @Override
    FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        def from = directoryTransition_.peek()
        switch (from) {
            case Layer.INIT :
                logger_.debug("#preVisitDirectory visiting ${dir} as ROOT")
                directoryTransition_.push(Layer.ROOT)
                break
            case Layer.ROOT :
                logger_.debug("#preVisitDirectory visiting ${dir} as TESTSUITE")
                tSuiteName_ = new TSuiteName(dir.getFileName().toString())
                directoryTransition_.push(Layer.TESTSUITE)
                break
            case Layer.TESTSUITE:
                logger_.debug("#preVisitDirectory visiting ${dir} as TIMESTAMP")
                LocalDateTime ldt = TSuiteTimestamp.parse(dir.getFileName().toString())
                if (ldt != null) {
                    tSuiteTimestamp_ = new TSuiteTimestamp(ldt)
                    tSuiteResult_ = new TSuiteResult(tSuiteName_, tSuiteTimestamp_).setParent(repoRoot_)
                    repoRoot_.addTSuiteResult(tSuiteResult_)
                } else {
                    logger_.warn("#preVisitDirectory ${dir} is ignored, as it's fileName '${dir.getFileName()}' is not compliant to" +
                            " the TSuiteTimestamp format (${TSuiteTimestamp.DATE_TIME_PATTERN})")
                }
                directoryTransition_.push(Layer.TIMESTAMP)
                break
            case Layer.TIMESTAMP :
                logger_.debug("#preVisitDirectory visiting ${dir} as TESTCASE")
                tCaseName_ = new TCaseName(dir.getFileName().toString())
                tCaseResult_ = tSuiteResult_.getTCaseResult(tCaseName_)
                if (tCaseResult_ == null) {
                    tCaseResult_ = new TCaseResult(tCaseName_).setParent(tSuiteResult_)
                    tSuiteResult_.addTCaseResult(tCaseResult_)
                }
                directoryTransition_.push(Layer.TESTCASE)
                break
            case Layer.TESTCASE :
                logger_.debug("#preVisitDirectory visiting ${dir} as MATERIAL")
                //
                directoryTransition_.push(Layer.MATERIAL)
                break
        }
        return CONTINUE
    }

    /**
     * Invoked for a directory after entries in the directory, and all of their descendants, have been visited.
     */
    @Override
    FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
        def to = directoryTransition_.peek()
        switch (to) {
            case Layer.TESTCASE :
                logger_.debug("#postVisitDirectory back to ${dir} as TESTCASE")
                directoryTransition_.pop()
                break
            case Layer.TIMESTAMP :
                logger_.debug("#postVisitDirectory back to ${dir} as TIMESTAMP")
                directoryTransition_.pop()
                break
            case Layer.TESTSUITE :
                logger_.debug("#postVisitDirectory back to ${dir} as TESTSUITE")
                directoryTransition_.pop()
                break
            case Layer.ROOT :
                logger_.debug("#postVisitDirectory back to ${dir} as ROOT")
                directoryTransition_.pop()
                break
        }
        return CONTINUE
    }

    /**
     * Invoked for a file in a directory.
     */
    @Override
    FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        switch (directoryTransition_.peek()) {
            case Layer.ROOT :
                logger_.debug("#visitFile ${file} in ROOT")
                break
            case Layer.TESTSUITE :
                logger_.debug("#visitFile ${file} in TESTSUITE")
                break
            case Layer.TIMESTAMP :
                logger_.debug("#visitFile ${file} in TIMESTAMP")
                break
            case Layer.TESTCASE :
                logger_.debug("#visitFile ${file} in TESTCASE")
                //logger.debug("#visitFile tCaseResult=${tCaseResult.toString()}")
                String fileName = file.getFileName()
                FileType fileType = Material.parseFileNameForFileType(fileName)
                if (fileType != FileType.NULL) {
                    URL url = Material.parseFileNameForURL(fileName)
                    //logger.debug("#visitFile url=${url.toString()}")
                    if (url != null) {
                        Suffix suffix = Material.parseFileNameForSuffix(fileName)
                        Material material = new Material(url, suffix, fileType).setParent(tCaseResult_)
                        material.setLastModified(file.toFile().lastModified())
                        tCaseResult_.addMaterial(material)
                    } else {
                        logger_.debug("#visitFile unable to parse ${file} into a URL")
                    }
                } else {
                    logger_.debug("#visitFile ${file} has no known FileType")
                }
                break
        }
        return CONTINUE
    }

    /**
     * Invoked for a file that could not be visited.
     *
     @Override
      FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {}
     */
}
