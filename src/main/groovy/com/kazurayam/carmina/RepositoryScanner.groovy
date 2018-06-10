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

    private enum Layer {
        BASEDIR, TESTSUITE, TIMESTAMP, TESTCASE, MATERIALS
    }
    private Stack<Layer> depth
    private Stack<Layer> shift

    private Path baseDir

    RepositoryScanner(Path baseDir) {
        assert baseDir != null
        this.baseDir = baseDir
    }

    List<TSuiteResult> scan() {
        TSuiteName tSuiteName = null
        TSuiteTimestamp tSuiteTimestamp = null
        TSuiteResult tSuiteResult = null
        TCaseName tCaseName = null
        TCaseResult tCaseResult = null
        TargetURL targetURL = null
        MaterialWrapper materialWrapper = null

        List<TSuiteResult> tSuiteResults = new ArrayList<TSuiteResult>()

        depth = new Stack<Layer>()
        depth.push(Layer.BASEDIR)

        Files.walkFileTree(this.baseDir,
                EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>() {
                    /**
                     * Invoked for a directory before entries in the directory are visited.
                     */
                    @Override
                    FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        logger.debug("#scan()/preVisitDirectory dir=${dir}")
                        switch (depth.peek()) {
                            case Layer.BASEDIR :
                                depth.push(Layer.TESTSUITE)
                                break
                            case Layer.TESTSUITE :
                                tSuiteName = new TSuiteName(dir.getFileName().toString())
                                depth.push(Layer.TIMESTAMP)
                                break
                            case Layer.TIMESTAMP :
                                LocalDateTime ldt = TSuiteTimestamp.parse(dir.getFileName().toString())
                                if (ldt != null) {
                                    tSuiteTimestamp = new TSuiteTimestamp(ldt)
                                    tSuiteResult = new TSuiteResult(tSuiteName, tSuiteTimestamp).setParent(baseDir)
                                    tSuiteResults.add(tSuiteResult)
                                } else {
                                    logger.info("#scan() ${dir} is ignored, as it's name is not compliant to" +
                                            " the TSuiteTimestamp format (${TSuiteTimestamp.DATE_TIME_PATTERN})")
                                }
                                depth.push(Layer.TESTCASE)
                                break
                            case Layer.TESTCASE :
                                depth.push(Layer.MATERIALS)
                                shift = new Stack<Layer>()
                                break
                            case Layer.MATERIALS :
                                break
                        }
                        return CONTINUE
                    }

                    /**
                     * Invoked for a directory after entries in the directory, and all of their descendants, have been visited.
                     */
                    @Override
                    FileVisitResult postVisitDirectory(Path path, IOException exception) throws IOException {
                        switch (depth.peek()) {
                            case Layer.BASEDIR :
                                break
                            case Layer.TESTSUITE :
                                tSuiteName = null
                                depth.pop()
                                break
                            case Layer.TIMESTAMP :
                                tSuiteResults.add(tSuiteResult)
                                tSuiteTimestamp = null
                                depth.pop()
                                break
                            case Layer.TESTCASE :
                                break
                            case Layer.MATERIALS :
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
                            case Layer.MATERIALS :
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
        )

        return tSuiteResults
    }
}
