package com.kazurayam.carmina

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestResultsRepositoryFactory {

    static Logger logger_ = LoggerFactory.getLogger(TestResultsRepositoryFactory.class)

    private TestResultsRepositoryFactory() {}

    static TestResultsRepository createInstance(Path baseDir) {
        return new TestResultsRepositoryImpl(baseDir)
    }

}
