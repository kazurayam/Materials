package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static java.nio.file.FileVisitResult.*
import java.time.LocalDateTime
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

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
        BASEDIR, TESTSUITE, TIMESTAMP, TESTCASE, MATERIAL, ELEMENT
    }

    private Path baseDir

    RepositoryScanner(Path baseDir) {
        assert baseDir != null
        this.baseDir = baseDir
    }

    List<TSuiteResult> scan() {
        List<TSuiteResult> tSuiteResults = new ArrayList<TSuiteResult>()
        RepositoryVisitor visitor = new RepositoryVisitor(baseDir, tSuiteResults)
        Files.walkFileTree(
                this.baseDir,
                EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                visitor)
        return tSuiteResults
    }

    /**
     *
     */
    static class RepositoryVisitor extends SimpleFileVisitor<Path> {
        private TSuiteName tSuiteName = null
        private TSuiteTimestamp tSuiteTimestamp = null
        private TSuiteResult tSuiteResult = null
        private TCaseName tCaseName = null
        private TCaseResult tCaseResult = null
        private TargetURL targetURL = null
        private MaterialWrapper materialWrapper = null

        private Stack<Layer> depth = null
        private Stack<Layer> shift = null

        private Path baseDir
        private List<TSuiteResult> tSuiteResults

        RepositoryVisitor(Path baseDir, List<TSuiteResult> tSuiteResults) {
            this.baseDir = baseDir
            this.tSuiteResults = tSuiteResults
            depth = new Stack<Layer>()
            depth.push(Layer.BASEDIR)
        }

        /**
         * Invoked for a directory before entries in the directory are visited.
         */
        @Override
        FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            switch (depth.peek()) {
                case Layer.BASEDIR :
                    logger.debug("#scan()/preVisitDirectory ${dir} BASEDIR")
                    depth.push(Layer.TESTSUITE)
                    break
                case Layer.TESTSUITE :
                    logger.debug("#scan()/preVisitDirectory ${dir} TESTSUITE")
                    tSuiteName = new TSuiteName(dir.getFileName().toString())
                    depth.push(Layer.TIMESTAMP)
                    break
                case Layer.TIMESTAMP :
                    logger.debug("#scan()/preVisitDirectory ${dir} TIMESTAMP")
                    LocalDateTime ldt = TSuiteTimestamp.parse(dir.getFileName().toString())
                    if (ldt != null) {
                        tSuiteTimestamp = new TSuiteTimestamp(ldt)
                        tSuiteResult = new TSuiteResult(tSuiteName, tSuiteTimestamp).setParent(baseDir)
                    } else {
                        logger.info("#scan() ${dir} is ignored, as it's name is not compliant to" +
                                " the TSuiteTimestamp format (${TSuiteTimestamp.DATE_TIME_PATTERN})")
                    }
                    depth.push(Layer.TESTCASE)
                    break
                case Layer.TESTCASE :
                    logger.debug("#scan()/preVisitDirectory ${dir} TESTCASE")
                    //depth.push(Layer.MATERIAL)
                    shift = new Stack<Layer>()
                    shift.push(Layer.MATERIAL)
                    break
                case Layer.MATERIAL :
                    logger.debug("#scan()/preVisitDirectory ${dir} MATERIAL")
                    shift.push(Layer.ELEMENT)
                    break
                case Layer.ELEMENT:
                    logger.debug("#scan()/preVisitDirectory ${dir} ELEMENT")
                    shift.push(Layer.ELEMENT)
                    break
            }
            return CONTINUE
        }

        /**
         * Invoked for a directory after entries in the directory, and all of their descendants, have been visited.
         */
        @Override
        FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
            switch (depth.peek()) {
                case Layer.BASEDIR :
                    logger.debug("#scan()/postVisitDirectory ${dir} BASEDIR")
                    depth.pop()
                    break
                case Layer.TESTSUITE :
                    logger.debug("#scan()/postVisitDirectory ${dir} TESTSUITE")
                    tSuiteName = null
                    depth.pop()
                    break
                case Layer.TIMESTAMP :
                    logger.debug("#scan()/postVisitDirectory ${dir} TIMESTAMP")
                    tSuiteResults.add(tSuiteResult)
                    tSuiteTimestamp = null
                    depth.pop()
                    break
                case Layer.TESTCASE :
                    logger.debug("#scan()/postVisitDirectory ${dir} TESTCASE")
                    depth.pop()
                    break
                case Layer.MATERIAL :
                    logger.debug("#scan()/postVisitDirectory ${dir} MATERIAL")
                    depth.pop()
                    break
                case Layer.ELEMENT :
                    logger.debug("#scan()/postVisitDirectory dir=${dir} ELEMENT")
                    shift.pop()
                    break
            }
            return CONTINUE
        }

        /**
         * Invoked for a file in a directory.
         */
        @Override
        FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
            switch (depth.peek()) {
                case Layer.BASEDIR :
                    break
                case Layer.TESTSUITE :
                    break
                case Layer.TIMESTAMP :
                    break
                case Layer.TESTCASE :
                    break
                case Layer.MATERIAL :
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
