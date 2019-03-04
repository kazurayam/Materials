package com.kazurayam.materials

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.impl.MaterialRepositoryImpl

final class MaterialRepositoryFactory {

    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositoryFactory.class)

    private MaterialRepositoryFactory() {}

    static MaterialRepository createInstance(Path baseDir) {
        Objects.requireNonNull(baseDir, "baseDir must not be null")
        Helpers.ensureDirs(baseDir)
        // set the defalult location of the Reports dir 
        Path reportsDir = baseDir.resolve('../Reports')
        return createInstance(baseDir, reportsDir)
    }
    
    /**
     * creates the baseDir and the reportsDir, then instanciate a MaterialRepository object
     * 
     * @param baseDir
     * @param reportsDir
     * @return
     */
    static MaterialRepository createInstance(Path baseDir, Path reportsDir) {
        Objects.requireNonNull(baseDir, "baseDir must not be null")
        Objects.requireNonNull(reportsDir, "reportsDir must not be null")
        Helpers.ensureDirs(baseDir)
        Helpers.ensureDirs(reportsDir)
        return (MaterialRepository)MaterialRepositoryImpl.newInstance(baseDir, reportsDir)
    }

}
