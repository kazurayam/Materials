package com.kazurayam.carmina

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestMaterialsRepositoryFactory {

    static Logger logger_ = LoggerFactory.getLogger(TestMaterialsRepositoryFactory.class)

    private TestMaterialsRepositoryFactory() {}

    static TestMaterialsRepository createInstance(Path baseDir) {
        return new TestMaterialsRepositoryImpl(baseDir)
    }

}
