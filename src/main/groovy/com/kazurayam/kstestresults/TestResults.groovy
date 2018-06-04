package com.kazurayam.kstestresults

import java.nio.file.Path

interface TestResults {

    Path resolveScreenshotFilePath(String testCaseId, String url)
    Path resolveScreenshotFilePath(String testCaseId, String url, String postFix)
    void setTestCaseStatus(String testCaseId, String testCaseStatus)
    void report()

}
