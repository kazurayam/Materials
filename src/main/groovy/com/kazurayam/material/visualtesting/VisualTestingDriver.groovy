package com.kazurayam.material.visualtesting

import java.nio.file.Path

interface VisualTestingDriver {
    void setOutput(Path outputDir)
    void execute()
}
