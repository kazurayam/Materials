package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Indexer
import com.kazurayam.materials.repository.RepositoryFileScanner
import com.kazurayam.materials.repository.RepositoryRoot

class CarouselIndexer implements Indexer {
    
    static Logger logger_ = LoggerFactory.getLogger(CarouselIndexer.class)
    
    private Path baseDir_
    private Path reportsDir_
    private Path output_
    
    @Override
    Path getOutput() {
        return this.output_
    }

    @Override
    void setBaseDir(Path baseDir) {
        if (baseDir == null) {
            def msg = "#setBaseDir baseDir argument is null"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        if (Files.notExists(baseDir)) {
            def msg = "#setBaseDir basedir ${baseDir.toString()} does not exist"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        this.baseDir_ = baseDir
    }
    
    @Override
    void setReportsDir(Path reportsDir) {
        if (reportsDir == null) {
            def msg = "#setReportsDir reportsDir argument is null"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        if (Files.notExists(reportsDir)) {
            def msg = "#setReportsDir reportsDir ${reportsDir.toString()} does not exist"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        this.reportsDir_ = reportsDir
    }
    
    @Override
    void setOutput(Path output) {
        Objects.requireNonNull(output)
        output_ = output
        Helpers.ensureDirs(output.getParent())
    }
    
    @Override
    void execute() throws IOException {
        if (baseDir_ == null) {
            def msg = "#execute baseDir_ is null"
            logger_.error(msg)
            throw new IllegalStateException(msg)
        }
        if (reportsDir_ == null) {
            def msg = "#execute reportsDir_ is null"
            logger_.error(msg)
            throw new IllegalStateException(msg)
        }
        if (output_ == null) {
            def msg = "#execute output_ is null"
            logger_.error(msg)
            throw new IllegalStateException(msg)
        }
        RepositoryFileScanner scanner = new RepositoryFileScanner(baseDir_, reportsDir_)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        OutputStream os = output_.toFile().newOutputStream()
        generate(repoRoot, os)
        logger_.info("generated ${output_.toString()}")
            throw new UnsupportedOperationException("TODO")
    }
    
    void generate(RepositoryRoot repoRoot, OutputStream os) throws IOException {
        throw new UnsupportedOperationException("TODO")
    }
}
