package com.kazurayam.kstestresults

import java.nio.file.Path

interface TestResults {

    Path resolveMaterialFilePath(String testCaseId, String url)
    Path resolveMaterialFilePath(String testCaseId, String url, String postFix)
    void setTcStatus(String testCaseId, String testCaseStatus)
    void report()

}
