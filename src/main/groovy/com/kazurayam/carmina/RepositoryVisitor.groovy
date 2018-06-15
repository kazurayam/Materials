package com.kazurayam.carmina

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

    private TSuiteName tSuiteName_
    private TSuiteTimestamp tSuiteTimestamp_
    private TSuiteResult tSuiteResult_
    private TCaseName tCaseName_
    private TCaseResult tCaseResult_
    private TargetURL targetURL_
    private Material material_

    private static enum Layer {
        INIT, BASEDIR, TESTSUITE, TIMESTAMP, TESTCASE, MATERIAL
    }
    private Stack<Layer> directoryTransition_

    private Path baseDir_
    private List<TSuiteResult> tSuiteResults_

    RepositoryVisitor(Path baseDir, List<TSuiteResult> tSuiteResults) {
        baseDir_ = baseDir
        tSuiteResults_ = tSuiteResults
        directoryTransition_ = new Stack<Layer>()
        directoryTransition_.push(Layer.INIT)
        logger_.debug("baseDir=${baseDir}")
    }

    /**
     * Invoked for a directory before entries in the directory are visited.
     */
    @Override
    FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        def from = directoryTransition_.peek()
        switch (from) {
            case Layer.INIT :
                logger_.debug("#preVisitDirectory visiting ${dir} as BASEDIR")
                directoryTransition_.push(Layer.BASEDIR)
                break
            case Layer.BASEDIR :
                logger_.debug("#preVisitDirectory visiting ${dir} as TESTSUITE")
                tSuiteName_ = new TSuiteName(dir.getFileName().toString())
                directoryTransition_.push(Layer.TESTSUITE)
                break
            case Layer.TESTSUITE:
                logger_.debug("#preVisitDirectory visiting ${dir} as TIMESTAMP")
                LocalDateTime ldt = TSuiteTimestamp.parse(dir.getFileName().toString())
                if (ldt != null) {
                    tSuiteTimestamp_ = new TSuiteTimestamp(ldt)
                    tSuiteResult_ = new TSuiteResult(tSuiteName_, tSuiteTimestamp_).setParent(baseDir_)
                    tSuiteResults_.add(tSuiteResult_)
                } else {
                    logger_.info("#preVisitDirectory ${dir} is ignored, as it's fileName '${dir.getFileName()}' is not compliant to" +
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
                directoryTransition_.pop()
                logger_.debug("#postVisitDirectory back to ${dir} as TESTCASE")
                break
            case Layer.TIMESTAMP :
                directoryTransition_.pop()
                logger_.debug("#postVisitDirectory back to ${dir} as TIMESTAMP")
                break
            case Layer.TESTSUITE :
                directoryTransition_.pop()
                logger_.debug("#postVisitDirectory back to ${dir} as TESTSUITE")
                break
            case Layer.BASEDIR :
                directoryTransition_.pop()
                logger_.debug("#postVisitDirectory back to ${dir} as BASEDIR")
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
            case Layer.BASEDIR :
                logger_.debug("#visitFile ${file} in BASEDIR")
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
                        TargetURL targetURL = tCaseResult_.getTargetURL(url)
                        //logger.debug("#visitFile targetURL=${targetURL.toString()} pre")
                        if (targetURL == null) {
                            targetURL = new TargetURL(url).setParent(tCaseResult_)
                            tCaseResult_.addTargetURL(targetURL)
                        }
                        Material mw = new Material(file, fileType).setParent(targetURL)
                        targetURL.addMaterial(mw)
                        //logger.debug("#visitFile targetURL=${targetURL.toString()} post")
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
