package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path

interface VisualTestingDriver {
    void setOutput(Path outputDir)
    void execute()
}
