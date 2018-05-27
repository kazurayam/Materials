package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path

interface IScreenshotRepository {
    Path getBaseDirPath()
    TestSuiteResult getTestSuiteResult()
    void setCurrentTestCaseId(String testCaseId)
    TestCaseResult findOrNewTestCaseResult(String testCaseId)
    Path resolveScreenshotFilePath(String pageUrl)
    void buildReport() throws IOException
    void buildArchive(OutputStream outputStream) throws IOException
}
