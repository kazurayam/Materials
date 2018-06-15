package com.kazurayam.carmina

import java.nio.file.Path

interface TestResultsRepository {

    void setCurrentTestSuite(String testSuiteId)
    void setCurrentTestSuite(String testSuiteId, String testSuiteTimestamp)

    Path getCurrentTestSuiteDirectory()

    Path getTestCaseDirectory(String testCaseId)

    void setTestCaseStatus(String testCaseId, String testCaseStatus)

    Path resolvePngFilePath(String testCaseId, String url)
    Path resolvePngFilePath(String testCaseId, String url, String suffix)

    Path resolveJsonFilePath(String testCaseId, String url)
    Path resolveJsonFilePath(String testCaseId, String url, String suffix)

    Path resolveXmlFilePath(String testCaseId, String url)
    Path resolveXmlFilePath(String testCaseId, String url, String suffix)

    Path resolvePdfFilePath(String testCaseId, String url)
    Path resolvePdfFilePath(String testCaseId, String url, String suffix)

    Path report() throws IOException
}
