package com.kazurayam.webtestingresultstorage

import java.nio.file.Path

class WebTestingResultStorageFactory {

    private WebTestingResultStorageFactory() {}

    static WebTestingResultStorage createInstance(Path baseDir, String testSuiteId) {
        return createInstance(baseDir, new TestSuiteName(testSuiteId))
    }

    static WebTestingResultStorage createInstance(Path baseDir, TestSuiteName testSuiteName) {
        return new WebTestingResultStorageImpl(baseDir, testSuiteName)
    }

}
