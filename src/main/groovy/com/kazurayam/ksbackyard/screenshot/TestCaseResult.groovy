package com.kazurayam.ksbackyard.screenshot

import java.nio.file.Path

/**
 *
 */
final class TestCaseResult {

    private TestSuiteResult parent
    private String testCaseId
    private Map<String, TargetPage> targetPageMap
    private String testCaseStatus

    protected TestCaseResult(TestSuiteResult parent, String testCaseId) {
        this.parent = parent
        this.testCaseId = testCaseId
        this.targetPageMap = new HashMap<String, TargetPage>()
        this.testCaseStatus = ''
    }

    String getTestCaseId() {
        return testCaseId
    }

    void setTestCaseStatus(String testCaseStatus) {
        this.testCaseStatus = testCaseStatus
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

    protected TestSuiteResult getParentTestSuiteResult() {
        return parent
    }

    protected Path resolveTestCaseDirPath() {
        Path testSuiteOutputDirPath = parent.resolveTestSuiteOutputDirPath()
        return testSuiteOutputDirPath.resolve(testCaseId.replaceFirst('^Test Cases/', ''))
    }

    class TargetPage {

        private TestCaseResult parentTestCaseResult
        private String url
        private List<ScreenshotWrapper> screenshotWrapperList

        protected TargetPage(TestCaseResult parent, String url) {
            this.parentTestCaseResult = parent
            this.url = url
            this.screenshotWrapperList = new ArrayList<ScreenshotWrapper>()
        }

        List<ScreenshotWrapper> getScreenshotWrapperList() {
            return this.screenshotWrapperList
        }

        String getUrl() {
            return this.url
        }

        ScreenshotWrapper createScreenshotWrapper() {
            ScreenshotWrapper sw =
                    new ScreenshotWrapper(this, screenshotWrapperList.size())
            screenshotWrapperList.add(sw)
            return sw
        }

        TestCaseResult getParentTestCaseResult() {
            return this.parentTestCaseResult
        }

        /**
         *
         */
        class ScreenshotWrapper {

            private TargetPage parentTargetPage
            private Integer seq

            ScreenshotWrapper(TargetPage parent, Integer seq) {
                this.parentTargetPage = parent
                this.seq = seq
            }

            Path getScreenshotFilePath() {
                try {
                    TestCaseResult tcr = parentTargetPage.getParentTestCaseResult()
                    Path testCaseDirPath = tcr.resolveTestCaseDirPath()
                    Helpers.ensureDirs(testCaseDirPath)
                    def encodedUrl = URLEncoder.encode(parentTargetPage.getUrl(), 'UTF-8')
                    def ext = (seq == 0) ? '' : "${seq}"
                    Path screenshotFilePath =
                            testCaseDirPath.resolve("${encodedUrl}.${ext}.png")
                    return screenshotFilePath
                }
                catch (IOException ex) {
                    System.err.println(ex)
                    return null
                }
            }
        }
    }
}


