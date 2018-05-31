package com.kazurayam.ksbackyard.screenshotsupport

import java.util.regex.Matcher
import java.util.regex.Pattern

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

    static final int flag = Pattern.CASE_INSENSITIVE
    static final String EXTENSION_PART_REGEX = '(\\.([0-9]+))?\\.png$'
    static final Pattern EXTENSION_PART_PATTERN = Pattern.compile(EXTENSION_PART_REGEX, flag)

    /**
     * accept a string in a format (<any string>[/\])(<enocoded URL string>)(.[0-9]+)?(.png)
     * and returns a List<String> of ['<decoded URL>', '[1-9][0-9]*'] or ['<decoded URL>']
     * @param screenshotFileName
     * @return empty List<String> if unmatched
     */
    static List<String> parseScreenshotFileName(String screenshotFileName) {
        List<String> values = new ArrayList<String>()
        String preprocessed = screenshotFileName.replaceAll('\\\\', '/')  // Windows File path separator -> UNIX
        //System.out.println("    original screenshotFileName=${screenshotFileName}")
        //System.out.println("preprocessed screenshotFileName=${preprocessed}")
        List<String> elements = preprocessed.split('[/]')
        if (elements.size() > 0) {
            String fileName = elements.getAt(elements.size() - 1)
            Matcher m = EXTENSION_PART_PATTERN.matcher(fileName)
            boolean b = m.find()
            if (b) {
                String encodedUrl = fileName.replaceFirst(EXTENSION_PART_REGEX, '')
                String decodedUrl = URLDecoder.decode(encodedUrl, 'UTF-8')
                values.add(decodedUrl)
                if (m.group(2) != null) {
                    values.add(m.group(2))
                }
            }
        }
        return values
    }

    @Override
    boolean equals(Object obj) {
        if (this == obj) { return true }
        if (!(obj instanceof TargetPage)) { return false }
        TargetPage other = (TargetPage)obj
        if (this.parentTestCaseResult == other.getParentTestCaseResult()
            && this.url == other.getUrl()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + this.getParentTestCaseResult().hashCode()
        result = prime * result + this.getUrl().hashCode()
        return result
    }
}