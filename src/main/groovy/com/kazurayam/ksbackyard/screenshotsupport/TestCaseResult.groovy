package com.kazurayam.ksbackyard.screenshotsupport

/**
 *
 */
class TestCaseResult {

    private TestSuiteResult parentTestSuiteResult
    private String testCaseId
    private List<TargetPage> targetPages
    private String testCaseStatus

    // --------------------- constructors and initializer ---------------------
    TestCaseResult(TestSuiteResult parentTestSuiteResult, String testCaseId) {
        assert parentTestSuiteResult != null
        assert testCaseId != null
        this.parentTestSuiteResult = parentTestSuiteResult
        this.testCaseId = testCaseId
        this.targetPages = new ArrayList<TargetPage>()
        this.testCaseStatus = ''
    }

    // --------------------- properties getter & setters ----------------------
    TestSuiteResult getParentTestSuiteResult() {
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


    // --------------------- create/add/get child nodes -----------------------

    TargetPage findOrNewTargetPage(String url) {
        TargetPage ntp = this.getTargetPage(url)
        if (ntp == null) {
            ntp = new TargetPage(this, url)
        }
        return ntp
    }

    TargetPage getTargetPage(String url) {
        for (TargetPage tp : this.targetPages) {
            if (tp.getUrl() == url) {
                return tp
            }
        }
        return null
    }

    void addTargetPage(TargetPage targetPage) {
        boolean found = false
        for (TargetPage tp : this.targetPages) {
            if (tp == targetPage) {
                found = true
            }
        }
        if (!found) {
            this.targetPages.add(targetPage)
        }
    }

    // -------------------------- helpers -------------------------------------
    /*
    List<String> getUrlList() {
        List<String> urlList = new ArrayList(targetPageMap.keySet())
        Collections.sort(urlList)
        return urlList
    }
     */

    /*
    protected Path resolveTestCaseDirPath() {
        Path testSuiteOutputDirPath = parentTestSuiteResult.resolveTestSuiteDirPath()
        return testSuiteOutputDirPath.resolve(testCaseId.replaceFirst('^Test Cases/', ''))
    }
     */

    // -------------------------- equals , hashCode ---------------------------
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


