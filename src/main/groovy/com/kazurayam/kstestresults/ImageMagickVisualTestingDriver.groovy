package com.kazurayam.kstestresults

import java.nio.file.Path

/**
 * Visual Testing engine using ImageMagick
 *
 * @author kazurayam
 *
 */
class ImageMagickVisualTestingDriver implements VisualTestingDriver {

    private TestResultsImpl scRepo
    private Path outputDir

    ImageMagickVisualTestingDriver(TestResultsImpl scRepo) {
        this.scRepo = scRepo
    }

    void setOutput(Path outputDir) {
        this.outputDir = outputDir
    }

    void execute() {
        //throw new UnsupportedOperationException("TODO")
    }

}
