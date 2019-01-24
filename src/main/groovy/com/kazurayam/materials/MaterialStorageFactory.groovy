package com.kazurayam.materials

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.model.MaterialStorageImpl

final class MaterialStorageFactory {

    static Logger logger_ = LoggerFactory.getLogger(MaterialStorageFactory.class)
    
    private MaterialStorageFactory() {}
    
    static MaterialStorage createInstance(Path baseDir) {
        Objects.requireNonNull(baseDir, "baseDir must not be null")
        Helpers.ensureDirs(baseDir)
        return (MaterialStorage)MaterialStorageImpl.newInstance(baseDir)
    }
    
}
