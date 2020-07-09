package com.kazurayam.materials.repository

import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp

import java.nio.file.FileSystemLoopException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime

import static java.nio.file.FileVisitResult.CONTINUE
import static java.nio.file.FileVisitResult.SKIP_SUBTREE
import static java.nio.file.FileVisitResult.TERMINATE

final class TreeTrunkVisitor extends SimpleFileVisitor<Path> {

    private static Logger logger_ = LoggerFactory.getLogger(TreeTrunkVisitor.class)

    private RepositoryRoot repoRoot_

    private TSuiteName tSuiteName_
    private TExecutionProfile tExecutionProfile_
    private TSuiteTimestamp tSuiteTimestamp_
    private TSuiteResult tSuiteResult_

    private Stack<TreeLayer> directoryTransition_

    TreeTrunkVisitor(RepositoryRoot repoRoot) {
        Objects.requireNonNull(repoRoot, "repoRoot must not be null")
        repoRoot_ = repoRoot
        directoryTransition_ = new Stack<TreeLayer>()
        directoryTransition_.push(TreeLayer.INIT)
    }

    @Override
    FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        def from = directoryTransition_.peek()
        switch (from) {
            case TreeLayer.INIT:
                directoryTransition_.push(TreeLayer.ROOT)
                logger_.debug("#preVisitDirectory visiting ${dir} as ROOT")
                return CONTINUE

            case TreeLayer.ROOT:
                directoryTransition_.push(TreeLayer.TESTSUITE)
                logger_.debug("#preVisitDirectory visiting ${dir} as TESTSUITE")
                tSuiteName_ = new TSuiteName(dir)
                return CONTINUE

            case TreeLayer.TESTSUITE:
                directoryTransition_.push(TreeLayer.EXECPROFILE)
                logger_.debug("#preVisitDirectory visiting ${dir} as EXECPROFILE")
                tExecutionProfile_ = new TExecutionProfile(dir)
                return CONTINUE

            case TreeLayer.EXECPROFILE:
                //directoryTransition_.push(Layer.TIMESTAMP)
                logger_.debug("#preVisitDirectory visiting ${dir} as TIMESTAMP")
                LocalDateTime ldt = TSuiteTimestamp.parse(dir.getFileName().toString())
                if (ldt != null) {
                    tSuiteTimestamp_ = new TSuiteTimestamp(ldt)
                    Objects.requireNonNull(tSuiteName_, "tSuiteName_ must not be null")
                    Objects.requireNonNull(tExecutionProfile_, "tExecutionProfile_ must not be null")
                    Objects.requireNonNull(tSuiteTimestamp_, "tSuiteTimestamp_ must not be null")
                    tSuiteResult_ = TSuiteResult.newInstance(tSuiteName_, tExecutionProfile_, tSuiteTimestamp_)
                    tSuiteResult_ = tSuiteResult_.setParent(repoRoot_)
                    if (tSuiteResult_ == null) {
                        throw new IllegalStateException("tSuiteResult_ is null when "
                                + "tSuiteName=\"${tSuiteName_}\""
                                + ", tExecutionProfile=\"${tExecutionProfile_}\""
                                + ", tSuiteTimestamp=\"${tSuiteTimestamp_}\""
                                + ", repoRoot=\"${repoRoot_}\"")
                    }
                    repoRoot_.addTSuiteResult(tSuiteResult_)
                } else {
                    logger_.warn("#preVisitDirectory ${dir} is ignored,"
                            + " as it's fileName '${dir.getFileName()}' is not compliant to"
                            + " the TSuiteTimestamp format (${TSuiteTimestamp.DATE_TIME_PATTERN})")
                }
                /**
                 * important ! we stop digging the tree here.
                 * we will let TBranchVisitor to do it
                 */
                return SKIP_SUBTREE

            default:
                logger_.error("#preVisitDirectory visiting ${dir} from ${from}")
                return TERMINATE
        }
    }

    @Override
    FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
        def to = directoryTransition_.peek()
        switch (to) {
            case TreeLayer.TIMESTAMP :
                logger_.debug("#postVisitDirectory leaving ${dir} as TIMESTAMP")
                directoryTransition_.pop()
                return CONTINUE

            case TreeLayer.EXECPROFILE :
                logger_.debug("#postVisitDirectory leaving ${dir} as EXECPROFILE")
                directoryTransition_.pop()
                return CONTINUE

            case TreeLayer.TESTSUITE :
                logger_.debug("#postVisitDirectory leaving ${dir} as TESTSUITE")
                directoryTransition_.pop()
                return CONTINUE

            case TreeLayer.ROOT :
                logger_.debug("#postVisitDirectory leaving ${dir} as ROOT")
                directoryTransition_.pop()
                return CONTINUE

            default:
                logger_.error("#postVisitDirectory leaving ${dir} as unknown")
                return TERMINATE
        }
    }

    /**
     * Invoked for a file in a directory.
     */
    @Override
    FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        switch (directoryTransition_.peek()) {
            case TreeLayer.ROOT :
                logger_.debug("#visitFile ${file} in ROOT; this file is ignored")
                return CONTINUE

            case TreeLayer.TESTSUITE :
                logger_.debug("#visitFile ${file} in TESTSUITE; this file is ignored")
                return CONTINUE

            case TreeLayer.TESTSUITE :
                logger_.debug("#visitFile ${file} in EXECPROFILE; this file is ignored")
                return CONTINUE

            case TreeLayer.TIMESTAMP :
                logger_.debug("#visitFile ${file} in TIMESTAMP; this file is ignored")
                return CONTINUE

            default:
                logger_.error("visitFile ${file} in unknown")
                return TERMINATE
        }
    }

    @Override
    FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        if (exc instanceof FileSystemLoopException) {
            //logger_.warn("circular link was detected: " + file)
            System.err.println("[RepositoryFileVisitor#visitFileFailed] circular link was detected: "
                    + file + ", which will be ignored")
        } else {
            //logger_.warn("unable to process file: " + file)
            System.err.println("[RepositoryFileVisitor#visitFileFailed] unable to process file: "
                    + file + ", which will be ignored")
        }
        return CONTINUE
    }


}
