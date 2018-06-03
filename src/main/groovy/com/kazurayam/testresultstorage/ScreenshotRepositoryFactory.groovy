package com.kazurayam.testresultstorage

import java.nio.file.Path

class ScreenshotRepositoryFactory {

    private ScreenshotRepositoryFactory() {}

    static ScreenshotRepository createInstance(Path baseDir, String testSuiteId) {
        return createInstance(baseDir, new TestSuiteName(testSuiteId))
    }

    static ScreenshotRepository createInstance(Path baseDir, TestSuiteName testSuiteName) {
        return new ScreenshotRepositoryImpl(baseDir, testSuiteName)
    }

}
