package com.kazurayam.webtestingresultstorage

import java.nio.file.Path

interface WebTestingResultStorage {

    Path resolveScreenshotFilePath(String testCaseId, String url)
    Path resolveScreenshotFilePath(String testCaseId, String url, String postFix)
    void setTestCaseStatus(String testCaseId, String testCaseStatus)
    void report()

}
