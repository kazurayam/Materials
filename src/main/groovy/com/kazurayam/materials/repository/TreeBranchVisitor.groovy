package com.kazurayam.materials.repository

import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.impl.MaterialImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.FileSystemLoopException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

import static java.nio.file.FileVisitResult.CONTINUE
import static java.nio.file.FileVisitResult.TERMINATE

class TreeBranchVisitor extends SimpleFileVisitor<Path> {

    private static Logger logger_ = LoggerFactory.getLogger(TreeBranchVisitor.class)

    private RepositoryRoot repoRoot_

    private TSuiteResult targetTSuiteResult_
    private TCaseName tCaseName_
    private TCaseResult tCaseResult_

    private int subdirDepth_ = 0
    private Stack<TreeLayer> directoryTransition_

    /**
     *
     * @param repoRoot
     * @param tTimestampDirectoryAsTarget
     */
    TreeBranchVisitor(RepositoryRoot repoRoot, TSuiteResult tSuiteResult) {
        Objects.requireNonNull(repoRoot)
        Objects.requireNonNull(tSuiteResult)
        repoRoot_ = repoRoot
        targetTSuiteResult_ = tSuiteResult

        // make sure that the given tSuiteResult is already linked in the RepositoryRoot tree
        if ( ! repoRoot.hasTSuiteResult(targetTSuiteResult_)) {
            throw new IllegalArgumentException("TSuiteResult ${tSuiteResult} is not linked to the RepositoryRoot object")
        }

        directoryTransition_ = new Stack<TreeLayer>()
        directoryTransition_.push(TreeLayer.INIT)
    }

    @Override
    FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        def from = directoryTransition_.peek()
        switch (from) {
            case TreeLayer.INIT:
                directoryTransition_.push(TreeLayer.TIMESTAMP)
                logger_.debug("#preVisitDirectory visiting ${dir} as TIMESTAMP")
                return CONTINUE

            case TreeLayer.TIMESTAMP:
                directoryTransition_.push(TreeLayer.TESTCASE)
                logger_.debug("#preVisitDirectory visiting ${dir} as TESTCASE")
                tCaseName_ = new TCaseName(dir)
                tCaseResult_ = targetTSuiteResult_.ensureTCaseResult(tCaseName_)
                return CONTINUE

            case TreeLayer.TESTCASE:
                directoryTransition_.push(TreeLayer.SUBDIR)
                logger_.debug("preVisitDirectory visiting ${dir} as SUBIDR(${subdirDepth_})")
                subdirDepth_ += 1
                return CONTINUE

            case TreeLayer.SUBDIR:
                logger_.debug("preVisitDirectory visiting ${dir} as SUBDIR(${subdirDepth_})")
                subdirDepth_ += 1
                return CONTINUE

            default:
                logger_.debug("#preVisitDirectory visiting ${dir} as unknown layer")
                return TERMINATE
        }
    }

    @Override
    FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
        def to = directoryTransition_.peek()
        switch (to) {
            case TreeLayer.SUBDIR:
                logger_.debug("#postVisitDirectory leaving ${dir} as SUBIDR(${subdirDepth_})")
                subdirDepth_ -= 1
                if (subdirDepth_ == 0) {
                    directoryTransition_.pop()
                }
                return CONTINUE

            case TreeLayer.TESTCASE:
                logger_.debug('#postVisitDirectory leaving ${dir} as TESTCASE')
                directoryTransition_.pop()
                return CONTINUE

            case TreeLayer.TIMESTAMP:
                logger_.debug('#postVisitDirectory leaving ${dir} as TIMESTAMP')
                assert targetTSuiteResult_.getTSuiteTimestampDirectory() == dir
                directoryTransition_.pop()
                return CONTINUE
        }
    }

    @Override
    FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        switch (directoryTransition_.peek()) {
            case TreeLayer.TIMESTAMP :
                logger_.debug("#visitFile ${file} in TIMESTAMP; this file is ignored")
                return CONTINUE

            case TreeLayer.TESTCASE :
            case TreeLayer.SUBDIR :
                Material material = new MaterialImpl(tCaseResult_, file)
                material.setLastModified(file.toFile().lastModified())
                material.setLength(file.toFile().length())
                material.setDescription(tCaseResult_.getParent().getTSuiteTimestamp().format())
                tCaseResult_.addMaterial(material)
                logger_.debug("#visitFile ${file} in TESTCASE, tCaseResult=${tCaseResult_.toString()}")
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

    // helpers




}
