package com.kazurayam.carmina

import java.nio.file.Path

class TestResultsRepositoryFactory {

    private TestResultsRepositoryFactory() {}

    static TestResultsRepository createInstance(Path baseDir) {
        return createInstance(baseDir)
    }

    static TestResultsRepository createInstance(Path baseDir, TsName tsName) {
        return new TestResultsRepositoryImpl(baseDir, tsName)
    }

    static TestResultsRepository createInstance(Path baseDir, TsName tsName, TsTimestamp tsTimestamp) {
        return new TestResultsRepositoryImpl(baseDir, tsName, tsTimestamp)
    }

    static TestResultsRepository createInstance(Path baseDir, String testSuiteId) {
        return createInstance(baseDir, new TsName(testSuiteId))
    }

}
