package com.kazurayam.carmina

import java.nio.file.Path

interface TestResultsRepository {

    void setCurrentTestSuite(String testSuiteId)
    void setCurrentTestSuite(String testSuiteId, String testSuiteTimestamp)

    Path getCurrentTestSuiteDirectory()

    Path getTestCaseDirectory(String testCaseId)

    void setTestCaseStatus(String testCaseId, String testCaseStatus)

    Path resolveMaterial(String testCaseId, String url, FileType fileType)
    Path resolveMaterial(String testCaseId, String url, String suffix, FileType fileType)

    Path makeIndex() throws IOException
}
