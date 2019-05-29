package com.kazurayam.materials

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.impl.ReportsAccessorImpl

final class ReportsAccessorFactory {
    
    static Logger logger_ = LoggerFactory.getLogger(ReportsAccessorFactory.class)
    
    private ReportsAccessorFactory() {}
    
    static ReportsAccessor createInstance(Path reportsDir) {
        Objects.requireNonNull(reportsDir, "reportsDir must not be null")
        if ( !Files.exists(reportsDir)) {
            throw new IllegalArgumentException("${reportsDir} does not exist")
        }
        if ( !reportsDir.getFileName().toString().endsWith("Reports")) {
            throw new IllegalArgumentException("reportsDir(${reportsDir}) is expected to have a diretory name 'Reports' but not")
        }
        // now return an instance of com.kazurayam.materials.impl.ReportsAccessorImpl
        return (ReportsAccessor)ReportsAccessorImpl.newInstance(reportsDir)
    }
}
