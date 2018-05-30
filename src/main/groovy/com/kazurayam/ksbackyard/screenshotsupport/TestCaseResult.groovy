package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.util.regex.Pattern
import java.util.regex.Matcher

/**
 *
 */
final class TestCaseResult {

    private TestSuiteResult parent
    private String testCaseId
    private Map<String, TargetPage> targetPageMap
    private String testCaseStatus

    protected TestCaseResult(TestSuiteResult parent, String testCaseId) {
        assert parent != null
        assert testCaseId != null
        this.parent = parent
        this.testCaseId = testCaseId
        this.targetPageMap = new HashMap<String, TargetPage>()
        this.testCaseStatus = ''
    }

    protected getParent() {
        return this.parent
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

    protected TestSuiteResult getParentTestSuiteResult() {
        return parent
    }

    protected Path resolveTestCaseDirPath() {
        Path testSuiteOutputDirPath = parent.resolveTestSuiteOutputDirPath()
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

        String getEncodedUrl() {
            return URLEncoder.encode(this.url, 'UTF-8')
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
         * accept a string in a format (<any string>[¥¥/])(<enocoded URL>)(.[0-9]+)?(.png)
         * and returns a List<String> of ['<decoded URL>', '[0-9]+', '.png']
         * @param screenshotFileName
         * @return empty List<String> if unmatched
         */
        static int flag = Pattern.CASE_INSENSITIVE
        static Pattern pattern = Pattern.compile('([^¥.]+)(¥.[0-9]+)?¥.png$', flag)

        static List<String> parseScreenshotFileName(String screenshotFileName) {
            List<String> values = new ArrayList<String>()
            List<String> elements = screenshotFileName.split('[/¥¥]')
            if (elements.size() > 1) {
                String fileName = elements.getAt(elements.size() - 1)
                Matcher m = pattern.matcher(fileName)
                boolean b = m.matches()
                if (b) {
                    values.add(m.group(1))
                    values.add(m.group(2))
                }
            }
            return values
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

            Integer getSeq() {
                return seq
            }

            Path getScreenshotFilePath() {
                try {
                    TestCaseResult tcr = parentTargetPage.getParentTestCaseResult()
                    Path testCaseDirPath = tcr.resolveTestCaseDirPath()
                    Helpers.ensureDirs(testCaseDirPath)
                    def encodedUrl = parentTargetPage.getEncodedUrl()
                    def ext = (seq == 0) ? '' : ".${seq}"
                    Path screenshotFilePath =
                            testCaseDirPath.resolve("${encodedUrl}${ext}.png")
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


