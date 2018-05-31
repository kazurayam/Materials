package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path

class ScreenshotWrapper {

    private TargetPage parentTargetPage
    private Integer seq

    ScreenshotWrapper(TargetPage parent, Integer seq) {
        this.parentTargetPage = parent
        this.seq = seq
    }

    TargetPage getTargetPage() {
        return parentTargetPage
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
            Path screenshot = testCaseDirPath.resolve("${encodedUrl}${ext}.png")
            return screenshot
        }
        catch (IOException ex) {
            System.err.println(ex)
            return null
        }
    }

    @Override
    boolean equals(Object obj) {
        if (this == obj) { return true }
        if (!(obj instanceof ScreenshotWrapper)) { return false }
        ScreenshotWrapper other = (ScreenshotWrapper)obj
        if (this.targetPage == other.getTargetPage() && this.seq == other.getSeq()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + this.getTargetPage().hashCode()
        result = prime * result + this.getSeq().hashCode()
        return result
    }
}
