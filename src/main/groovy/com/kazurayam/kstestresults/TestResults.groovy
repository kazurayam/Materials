package com.kazurayam.kstestresults

import java.nio.file.Path

interface TestResults {

    void setTcStatus(String testCaseId, String testCaseStatus)

    Path resolveMaterialFilePath(String testCaseId, String url, FileExtension ext)
    Path resolveMaterialFilePath(String testCaseId, String url, String suffix, FileExtension ext)

    Path resolveScreenshotFilePath(String testCaseId, String url)
    Path resolveScreenshotFilePath(String testCaseId, String url, String suffix)

    //Path resolveJsonFilePath(String testCaseId, String url)
    //Path resolveJsonFilePath(String testCaseId, String url, String suffix)

    //Path resolveXmlFilePath(String testCaseId, String url)
    //Path resolveXmlFilePath(String testCaseId, String url, String suffix)

    //Path resolvePdfFilePath(String testCaseId, String url)
    //Path resolvePdfFilePath(String testCaseId, String url, String suffix)

    Path report() throws IOException

}
