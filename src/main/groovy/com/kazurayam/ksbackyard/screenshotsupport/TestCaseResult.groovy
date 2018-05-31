package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path

/**
 *
 */
class TestCaseResult {

    private TestSuiteResult parentTestSuiteResult
    private String testCaseId
    private Map<String, TargetPage> targetPageMap
    private String testCaseStatus

    protected TestCaseResult(TestSuiteResult parentTestSuiteResult, String testCaseId) {
        assert parentTestSuiteResult != null
        assert testCaseId != null
        this.parentTestSuiteResult = parentTestSuiteResult
        this.testCaseId = testCaseId
        this.targetPageMap = new HashMap<String, TargetPage>()
        this.testCaseStatus = ''
    }

    protected TestSuiteResult getParentTestSuiteResult() {
        return this.parentTestSuiteResult
    }

    String getTestCaseId() {
        return testCaseId
    }

    void setTestCaseStatus(String testCaseStatus) {
        assert testCaseStatus != null
        this.testCaseStatus = testCaseStatus
    }

    String getTestCaseStatus() {
        return this.testCaseStatus
    }

    List<String> getUrlList() {
        List<String> urlList = new ArrayList(targetPageMap.keySet())
        Collections.sort(urlList)
        return urlList
    }

    TargetPage getTargetPage(String url) {
        return targetPageMap.get(url)
    }

    protected TargetPage findOrNewTargetPage(String url) {
        if (targetPageMap.containsKey(url)) {
            return targetPageMap.get(url)
        }
        else {
            TargetPage targetPage = new TargetPage(this, url)
            this.targetPageMap.put(url, targetPage)
            return targetPage
        }
    }

    protected Path resolveTestCaseDirPath() {
        Path testSuiteOutputDirPath = parentTestSuiteResult.resolveTestSuiteOutputDirPath()
        return testSuiteOutputDirPath.resolve(testCaseId.replaceFirst('^Test Cases/', ''))
    }

    @Override
    boolean equals(Object obj) {
        if (this == obj) { return true }
        if (!(obj instanceof TestCaseResult)) { return false }
        TestCaseResult other = (TestCaseResult)obj
        if (this.testCaseId == other.getTestCaseId()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        return this.testCaseId.hashCode()
    }

}


