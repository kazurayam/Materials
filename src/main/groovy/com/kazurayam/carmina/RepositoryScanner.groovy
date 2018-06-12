package com.kazurayam.carmina

import static java.nio.file.FileVisitResult.*

import java.nio.file.FileVisitOption
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * RepositoryScanner scans a file system tree under the baseDir directory, and it builds object trees of
 * TSuiteResult under which contained objects of TCaseResult + TargetURL + MaterialWrapper as found in the
 * local storage.
 *
 * @author kazurayam
 */
class RepositoryScanner {

    static Logger logger = LoggerFactory.getLogger(RepositoryScanner.class);

    private static enum Layer {
        INIT, BASEDIR, TESTSUITE, TIMESTAMP, TESTCASE, MATERIAL
    }

    private Path baseDir
    private List<TSuiteResult> tSuiteResults

    RepositoryScanner(Path baseDir) {
        assert baseDir != null
        if (!Files.exists(baseDir)) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        if (!Files.isDirectory(baseDir)) {
            throw new IllegalArgumentException("${baseDir} is not a directory")
        }
        this.baseDir = baseDir
    }

    void scan() {
        tSuiteResults = new ArrayList<TSuiteResult>()
        Files.walkFileTree(
                this.baseDir,
                EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new RepositoryVisitor(this.baseDir, this.tSuiteResults)
        )
    }
    
    List<TSuiteResult> getTSuiteResults() {
        return tSuiteResults
    }
    
    List<TSuiteResult> getTSuiteResults(TSuiteName tSuiteName) {
        List<TSuiteResult> tSuiteResults = new ArrayList<TSuiteResult>()
        for (TSuiteResult tSuiteResult : this.tSuiteResults) {
            if (tSuiteName == tSuiteResult.getTSuiteName()) {
                tSuiteResults.add(tSuiteResult)
            }
        }
        return tSuiteResults
    }
    
    List<TSuiteResult> getTSuiteResults(TSuiteTimestamp tSuiteTimestamp) {
        List<TSuiteResult> tSuiteResults = new ArrayList<TSuiteResult>()
        for (TSuiteResult tSuiteResult : this.tSuiteResults) {
            if (tSuiteTimestamp == tSuiteResult.getTSuiteTimestamp()) {
                tSuiteResults.add(tSuiteResult)
            }
        }
        return tSuiteResults
    }

    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        for (TSuiteResult tSuiteResult : this.tSuiteResults) {
            if (tSuiteName == tSuiteResult.getTSuiteName() && tSuiteTimestamp == tSuiteResult.getTSuiteTimestamp()) {
                return tSuiteResult
            }
        }
        return null
    }

    

    /**
     *
     */
    static class RepositoryVisitor extends SimpleFileVisitor<Path> {

        private TSuiteName tSuiteName
        private TSuiteTimestamp tSuiteTimestamp
        private TSuiteResult tSuiteResult
        private TCaseName tCaseName
        private TCaseResult tCaseResult
        private TargetURL targetURL
        private MaterialWrapper materialWrapper

        private Stack<Layer> directoryTransition

        private Path baseDir
        private List<TSuiteResult> tSuiteResults

        RepositoryVisitor(Path baseDir, List<TSuiteResult> tSuiteResults) {
            this.baseDir = baseDir
            this.tSuiteResults = tSuiteResults
            directoryTransition = new Stack<Layer>()
            directoryTransition.push(Layer.INIT)
            logger.debug("baseDir=${baseDir}")
        }

        /**
         * Invoked for a directory before entries in the directory are visited.
         */
        @Override
        FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            def from = directoryTransition.peek()
            switch (from) {
                case Layer.INIT :
                    logger.debug("#preVisitDirectory visiting ${dir} as BASEDIR")
                    directoryTransition.push(Layer.BASEDIR)
                    break
                case Layer.BASEDIR :
                    logger.debug("#preVisitDirectory visiting ${dir} as TESTSUITE")
                    tSuiteName = new TSuiteName(dir.getFileName().toString())
                    directoryTransition.push(Layer.TESTSUITE)
                    break
                case Layer.TESTSUITE:
                    logger.debug("#preVisitDirectory visiting ${dir} as TIMESTAMP")
                    LocalDateTime ldt = TSuiteTimestamp.parse(dir.getFileName().toString())
                    if (ldt != null) {
                        tSuiteTimestamp = new TSuiteTimestamp(ldt)
                        tSuiteResult = new TSuiteResult(tSuiteName, tSuiteTimestamp).setParent(baseDir)
                        tSuiteResults.add(tSuiteResult)
                    } else {
                        logger.info("#preVisitDirectory ${dir} is ignored, as it's fileName '${dir.getFileName()}' is not compliant to" +
                                " the TSuiteTimestamp format (${TSuiteTimestamp.DATE_TIME_PATTERN})")
                    }
                    directoryTransition.push(Layer.TIMESTAMP)
                    break
                case Layer.TIMESTAMP :
                    logger.debug("#preVisitDirectory visiting ${dir} as TESTCASE")
                    tCaseName = new TCaseName(dir.getFileName().toString())
                    tCaseResult = new TCaseResult(tCaseName).setParent(tSuiteResult)
                    tSuiteResult.addTCaseResult(tCaseResult)
                    directoryTransition.push(Layer.TESTCASE)
                    break
                case Layer.TESTCASE :
                    logger.debug("#preVisitDirectory visiting ${dir} as MATERIAL")
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
                    logger.debug("#postVisitDirectory back to ${dir} as TESTCASE")
                    break
                case Layer.TIMESTAMP :
                    directoryTransition.pop()
                    logger.debug("#postVisitDirectory back to ${dir} as TIMESTAMP")
                    break
                case Layer.TESTSUITE :
                    directoryTransition.pop()
                    logger.debug("#postVisitDirectory back to ${dir} as TESTSUITE")
                    break
                case Layer.BASEDIR :
                    directoryTransition.pop()
                    logger.debug("#postVisitDirectory back to ${dir} as BASEDIR")
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
                    logger.debug("#visitFile ${file} in BASEDIR")
                    break
                case Layer.TESTSUITE :
                    logger.debug("#visitFile ${file} in TESTSUITE")
                    break
                case Layer.TIMESTAMP :
                    logger.debug("#visitFile ${file} in TIMESTAMP")
                    break
                case Layer.TESTCASE :
                    logger.debug("#visitFile ${file} in TESTCASE")
                    String fileName = file.getFileName()
                    FileType fileType = MaterialWrapper.parseFileNameForFileType(fileName)
                    if (fileType != FileType.NULL) {
                        URL url = MaterialWrapper.parseFileNameForURL(fileName)
                        if (url != null) {
                            TargetURL targetURL = this.tCaseResult.getTargetURL(url)
                            if (targetURL == null) {
                                targetURL = new TargetURL(url).setParent(this.tCaseResult)
                                tCaseResult.addTargetURL(targetURL)
                            }
                            MaterialWrapper mw = new MaterialWrapper(file, fileType).setParent(targetURL)
                            targetURL.addMaterialWrapper(mw)
                        } else {
                            logger.debugEnabled("#visitFile ${file} unable to convert to a URL object")
                        }
                    } else {
                        logger.debug("#visitFile ${file} does not have known file name extension")
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
}
