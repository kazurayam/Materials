package com.kazurayam.materials.repository

import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp

import java.nio.file.Files
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.attribute.BasicFileAttributes

class TreeBranchVisitor extends SimpleFileVisitor<Path> {

    private static Logger logger_ = LoggerFactory.getLogger(TreeBranchVisitor.class)

    private RepositoryRoot repoRoot_
    private Path targetDir_

    private TSuiteName tSuiteName_
    private TExecutionProfile tExecutionProfile_
    private TSuiteTimestamp tSuiteTimestamp_
    private TSuiteResult tSuiteResult_
    private TCaseName tCaseName_
    private TCaseResult tCaseResult_

    private int subdirDepth_ = 0
    private Stack<TreeLayer> directoryTransition_

    /**
     *
     * @param repoRoot
     * @param tTimestampDirectoryAsTarget
     */
    TreeBranchVisitor(RepositoryRoot repoRoot, Path tTimestampDirectoryAsTarget) {
        Objects.requireNonNull(repoRoot)
        Objects.requireNonNull(tTimestampDirectoryAsTarget)
        repoRoot_ = repoRoot
        targetDir_ = tTimestampDirectoryAsTarget
        if ( ! Files.exists(targetDir_) ) {
            throw new IllegalArgumentException("${targetDir_} does not exist" )
        }
        directoryTransition_ = new Stack<TreeLayer>()
        directoryTransition_.push(TreeLayer.TIMESTAMP)
    }

    @Override
    FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        throw new RuntimeException("TODO")
    }

    @Override
    FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
        throw new RuntimeException("TODO")
    }

    @Override
    FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        throw new RuntimeException("TODO")
    }

    @Override
    FileVisitResult visitFileFailed(Path file, IOException exception) throws IOException {
        throw new RuntimeException("TODO")
    }

    // helpers




}
