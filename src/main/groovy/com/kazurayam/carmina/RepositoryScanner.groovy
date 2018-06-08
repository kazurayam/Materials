package com.kazurayam.carmina

import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * RepositoryScanner scans file system tree under the baseDir directory, and it builds object trees of
 * TSuiteResult + TCaseResult + TargetURL + MaterialWrapper
 *
 * @author kazurayam
 *
 */
class RepositoryScanner {

    private Path baseDir

    RepositoryScanner(Path baseDir) {
        this.baseDir = baseDir
    }

    List<TSuiteResult> scan() {
        throw new UnsupportedOperationException('TODO')
    }

    class RepositoryFileScanner extends SimpleFileVisitor<Path> {
        
        /**
         * Invoked for a directory before entries in the directory are visited.
         */
        @Override
        FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {}

        /**
         * Invoked for a directory after entries in the directory, and all of their descendants, have been visited.
         */
        @Override
        FileVisitResult postVisitDirectory(Path path, IOException exception) throws IOException {}

        /**
         * Invoked for a file in a directory.
         */
        @Override
        FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {}

        /**
         * Invoked for a file that could not be visited.
         */
        @Override
        FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {}
    }
}
