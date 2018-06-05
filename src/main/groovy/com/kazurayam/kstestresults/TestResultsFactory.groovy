package com.kazurayam.kstestresults

import java.nio.file.Path

class TestResultsFactory {

    private TestResultsFactory() {}

    static TestResults createInstance(Path baseDir, String testSuiteId) {
        return createInstance(baseDir, new TsName(testSuiteId))
    }

    static TestResults createInstance(Path baseDir, TsName tsName) {
        return new TestResultsImpl(baseDir, tsName)
    }

}
