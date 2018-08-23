package com.kazurayam.materials

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MaterialRepositoryFactory {

    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositoryFactory.class)

    private MaterialRepositoryFactory() {}

    static MaterialRepository createInstance(Path baseDir) {
        Helpers.ensureDirs(baseDir)
        return new MaterialRepositoryImpl(baseDir)
    }

}
