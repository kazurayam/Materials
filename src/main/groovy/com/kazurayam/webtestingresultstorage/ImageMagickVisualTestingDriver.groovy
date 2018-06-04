package com.kazurayam.webtestingresultstorage

import java.nio.file.Path

/**
 * Visual Testing engine using ImageMagick
 *
 * @author kazurayam
 *
 */
class ImageMagickVisualTestingDriver implements VisualTestingDriver {

    private ScreenshotRepositoryImpl scRepo
    private Path outputDir

    ImageMagickVisualTestingDriver(ScreenshotRepositoryImpl scRepo) {
        this.scRepo = scRepo
    }

    void setOutput(Path outputDir) {
        this.outputDir = outputDir
    }

    void execute() {
        //throw new UnsupportedOperationException("TODO")
    }

}
