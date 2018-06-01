package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.util.regex.Matcher
import java.util.regex.Pattern

class TargetPage {

    private TestCaseResult parentTestCaseResult
    private URL url
    private List<ScreenshotWrapper> screenshotWrappers

    // ---------------------- constructors & initializers ---------------------
    protected TargetPage(TestCaseResult parent, URL url) {
        this.parentTestCaseResult = parent
        this.url = url
        this.screenshotWrappers = new ArrayList<ScreenshotWrapper>()
    }

    // --------------------- properties getter & setter -----------------------
    TestCaseResult getParentTestCaseResult() {
        return this.parentTestCaseResult
    }

    URL getUrl() {
        return this.url
    }

    // --------------------- create/add/get child nodes -----------------------

    /**
     * This is the core trick.
     *
     * @param targetPageUrl
     * @return
     */
    ScreenshotWrapper findOrNewScreenshotWrapper(URL targetPageUrl) {

    }

    ScreenshotWrapper findOrNewScreenshotWrapper(Path imageFilePath) {
        ScreenshotWrapper sw = this.getScreenshotWrapper(imageFilePath)
        if (sw == null) {
            sw = new ScreenshotWrapper(this, imageFilePath)
        }
        return sw
    }

    void addScreenshotWrapper(ScreenshotWrapper screenshotWrapper) {
        boolean found = false
        for (ScreenshotWrapper sw : screenshotWrappers) {
            if (sw == screenshotWrapper) {
                found = true
            }
        }
        if (!found) {
            screenshotWrappers.add(screenshotWrapper)
        }
    }

    ScreenshotWrapper getScreenshotWrapper(Path imageFilePath) {
        for (ScreenshotWrapper sw : screenshotWrappers) {
            if (sw.getScreenshotFilePath() == imageFilePath) {
                return sw
            }
        }
        return null
    }

    // --------------------- helpers ------------------------------------------
    String getUrlAsEncodedString() {
        return URLEncoder.encode(this.url.toExternalForm(), 'UTF-8')
    }

    /**
     * accept a string in a format (<any string>[/\])(<enocoded URL string>)(.[0-9]+)?(.png)
     * and returns a List<String> of ['<decoded URL>', '[1-9][0-9]*'] or ['<decoded URL>']
     * @param screenshotFileName
     * @return empty List<String> if unmatched
     */
    static final int flag = Pattern.CASE_INSENSITIVE
    static final String EXTENSION_PART_REGEX = '(\\.([0-9]+))?\\.png$'
    static final Pattern EXTENSION_PART_PATTERN = Pattern.compile(EXTENSION_PART_REGEX, flag)
    static List<String> parseScreenshotFileName(String screenshotFileName) {
        List<String> values = new ArrayList<String>()
        String preprocessed = screenshotFileName.replaceAll('\\\\', '/')  // Windows File path separator -> UNIX
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

    // ------------------------ equals, hashCode ------------------------------
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