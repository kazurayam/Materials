package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

class TestResultsRepositoryFactory {

    static Logger logger = LoggerFactory.getLogger(TestResultsRepositoryFactory.class)

    private TestResultsRepositoryFactory() {}

    static TestResultsRepository createInstance(Path baseDir) {
        return createInstance(baseDir)
    }

    static TestResultsRepository createInstance(Path baseDir, TSuiteName tSuiteName) {
        return new TestResultsRepositoryImpl(baseDir, tSuiteName)
    }

    static TestResultsRepository createInstance(Path baseDir, TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        return new TestResultsRepositoryImpl(baseDir, tSuiteName, tSuiteTimestamp)
    }

    static TestResultsRepository createInstance(Path baseDir, String testSuiteId) {
        return createInstance(baseDir, new TSuiteName(testSuiteId))
    }

}
