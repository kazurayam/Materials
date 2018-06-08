package com.kazurayam.visualtesting

import java.nio.file.Path

interface VisualTestingDriver {
    void setOutput(Path outputDir)
    void execute()
}
