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

    private static enum Layer {
        INIT, BASEDIR, TESTSUITE, TIMESTAMP, TESTCASE, MATERIAL
    }

    static Logger logger_ = LoggerFactory.getLogger(RepositoryVisitor.class)

    private TSuiteName tSuiteName
    private TSuiteTimestamp tSuiteTimestamp
    private TSuiteResult tSuiteResult
    private TCaseName tCaseName
    private TCaseResult tCaseResult
    private TargetURL targetURL
    private Material material

    private Stack<Layer> directoryTransition

    private Path baseDir
    private List<TSuiteResult> tSuiteResults

    RepositoryVisitor(Path baseDir, List<TSuiteResult> tSuiteResults) {
        this.baseDir = baseDir
        this.tSuiteResults = tSuiteResults
        directoryTransition = new Stack<Layer>()
        directoryTransition.push(Layer.INIT)
        logger_.debug("baseDir=${baseDir}")
    }

    /**
     * Invoked for a directory before entries in the directory are visited.
     */
    @Override
    FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        def from = directoryTransition.peek()
        switch (from) {
            case Layer.INIT :
                logger_.debug("#preVisitDirectory visiting ${dir} as BASEDIR")
                directoryTransition.push(Layer.BASEDIR)
                break
            case Layer.BASEDIR :
                logger_.debug("#preVisitDirectory visiting ${dir} as TESTSUITE")
                tSuiteName = new TSuiteName(dir.getFileName().toString())
                directoryTransition.push(Layer.TESTSUITE)
                break
            case Layer.TESTSUITE:
                logger_.debug("#preVisitDirectory visiting ${dir} as TIMESTAMP")
                LocalDateTime ldt = TSuiteTimestamp.parse(dir.getFileName().toString())
                if (ldt != null) {
                    tSuiteTimestamp = new TSuiteTimestamp(ldt)
                    tSuiteResult = new TSuiteResult(tSuiteName, tSuiteTimestamp).setParent(baseDir)
                    tSuiteResults.add(tSuiteResult)
                } else {
                    logger_.info("#preVisitDirectory ${dir} is ignored, as it's fileName '${dir.getFileName()}' is not compliant to" +
                            " the TSuiteTimestamp format (${TSuiteTimestamp.DATE_TIME_PATTERN})")
                }
                directoryTransition.push(Layer.TIMESTAMP)
                break
            case Layer.TIMESTAMP :
                logger_.debug("#preVisitDirectory visiting ${dir} as TESTCASE")
                tCaseName = new TCaseName(dir.getFileName().toString())
                tCaseResult = tSuiteResult.getTCaseResult(tCaseName)
                if (tCaseResult == null) {
                    tCaseResult = new TCaseResult(tCaseName).setParent(tSuiteResult)
                    tSuiteResult.addTCaseResult(tCaseResult)
                }
                directoryTransition.push(Layer.TESTCASE)
                break
            case Layer.TESTCASE :
                logger_.debug("#preVisitDirectory visiting ${dir} as MATERIAL")
                //
                directoryTransition.push(Layer.MATERIAL)
                break
        }
        return CONTINUE
    }

    /**
     * Invoked for a directory after entries in the directory, and all of their descendants, have been visited.
     */
    @Override
    FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
        def to = directoryTransition.peek()
        switch (to) {
            case Layer.TESTCASE :
                directoryTransition.pop()
                logger_.debug("#postVisitDirectory back to ${dir} as TESTCASE")
                break
            case Layer.TIMESTAMP :
                directoryTransition.pop()
                logger_.debug("#postVisitDirectory back to ${dir} as TIMESTAMP")
                break
            case Layer.TESTSUITE :
                directoryTransition.pop()
                logger_.debug("#postVisitDirectory back to ${dir} as TESTSUITE")
                break
            case Layer.BASEDIR :
                directoryTransition.pop()
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
        switch (directoryTransition.peek()) {
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
                        TargetURL targetURL = this.tCaseResult.getTargetURL(url)
                        //logger.debug("#visitFile targetURL=${targetURL.toString()} pre")
                        if (targetURL == null) {
                            targetURL = new TargetURL(url).setParent(this.tCaseResult)
                            tCaseResult.addTargetURL(targetURL)
                        }
                        Material mw = new Material(file, fileType).setParent(targetURL)
                        targetURL.addMaterial(mw)
                        //logger.debug("#visitFile targetURL=${targetURL.toString()} post")
                    } else {
                        logger_.info("#visitFile unable to parse ${file} into a URL")
                    }
                } else {
                    logger_.info("#visitFile ${file} has no known FileType")
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
