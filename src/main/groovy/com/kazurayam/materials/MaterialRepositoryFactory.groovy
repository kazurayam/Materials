package com.kazurayam.materials

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.model.MaterialRepositoryImpl

final class MaterialRepositoryFactory {

    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositoryFactory.class)

    private MaterialRepositoryFactory() {}

    static MaterialRepository createInstance(Path baseDir) {
        Objects.requireNonNull(baseDir, "baseDir must not be null")
        Helpers.ensureDirs(baseDir)
        return (MaterialRepository)MaterialRepositoryImpl.newInstance(baseDir)
    }

}
