package com.kazurayam.kstestresults

import java.nio.file.Path

class TestResultsFactory {

    private TestResultsFactory() {}

    static TestResults createInstance(Path baseDir) {
        return createInstance(baseDir)
    }

    static TestResults createInstance(Path baseDir, TsName tsName) {
        return new TestResultsImpl(baseDir, tsName)
    }

    static TestResults createInstance(Path baseDir, TsName tsName, TsTimestamp tsTimestamp) {
        return new TestResultsImpl(baseDir, tsName, tsTimestamp)
    }

    static TestResults createInstance(Path baseDir, String testSuiteId) {
        return createInstance(baseDir, new TsName(testSuiteId))
    }

}
