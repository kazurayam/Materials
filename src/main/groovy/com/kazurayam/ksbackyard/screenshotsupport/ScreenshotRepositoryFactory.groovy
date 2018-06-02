package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.nio.file.Paths

class ScreenshotRepositoryFactory {

    private ScreenshotRepositoryFactory() {}

    static ScreenshotRepository createInstance(Path baseDir, String testSuiteId) {
        createInstance(baseDir, new TestSuiteName(testSuiteId))
    }

    static ScreenshotRepository createInstance(Path baseDir, TestSuiteName testSuiteName) {
        return new ScreenshotRepositoryImpl(baseDir, testSuiteName)
    }
}
