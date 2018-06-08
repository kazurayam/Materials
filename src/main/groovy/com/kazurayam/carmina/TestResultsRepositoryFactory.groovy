package com.kazurayam.carmina

import java.nio.file.Path

class TestResultsRepositoryFactory {

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
