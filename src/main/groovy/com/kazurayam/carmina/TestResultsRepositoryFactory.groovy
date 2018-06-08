package com.kazurayam.carmina

import java.nio.file.Path

class TestResultsRepositoryFactory {

    private TestResultsRepositoryFactory() {}

    static TestResultsRepository createInstance(Path baseDir) {
        return createInstance(baseDir)
    }

    static TestResultsRepository createInstance(Path baseDir, TSuiteName tsName) {
        return new TestResultsRepositoryImpl(baseDir, tsName)
    }

    static TestResultsRepository createInstance(Path baseDir, TSuiteName tsName, TSuiteTimestamp tsTimestamp) {
        return new TestResultsRepositoryImpl(baseDir, tsName, tsTimestamp)
    }

    static TestResultsRepository createInstance(Path baseDir, String testSuiteId) {
        return createInstance(baseDir, new TSuiteName(testSuiteId))
    }

}
