package com.kazurayam.ksbackyard.screenshotsupport

import groovy.json.JsonBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors

class TargetPage {

    private TestCaseResult parentTestCaseResult
    private URL url
    private List<ScreenshotWrapper> screenshotWrappers

    static final String IMAGE_FILE_EXTENSION = '.png'

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
    ScreenshotWrapper getScreenshotWrapper(String postFix) {
        String encodedUrl = URLEncoder.encode(url.toExternalForm(), 'UTF-8')
        Path p = this.parentTestCaseResult.getTestCaseDir().resolve("${encodedUrl}${postFix}${IMAGE_FILE_EXTENSION}")
        return new ScreenshotWrapper(this, p)
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
        for (ScreenshotWrapper sw : this.screenshotWrappers) {
            if (sw == screenshotWrapper) {
                found = true
            }
        }
        if (!found) {
            this.screenshotWrappers.add(screenshotWrapper)
        }
    }

    ScreenshotWrapper getScreenshotWrapper(Path imageFilePath) {
        for (ScreenshotWrapper sw : this.screenshotWrappers) {
            if (sw.getScreenshotFilePath() == imageFilePath) {
                return sw
            }
        }
        return null
    }

    // --------------------- helpers ------------------------------------------

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

    // ------------------------ overriding Object properties ------------------
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

    @Override
    String toString() {
        def json = new JsonBuilder()
        json (
                ["url": this.url.toString()],
                ["screenshotWrappers": this.screenshotWrappers]
        )
        return json.toString()
    }
}