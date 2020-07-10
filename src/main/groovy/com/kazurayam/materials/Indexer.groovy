package com.kazurayam.materials

import java.nio.file.Path


interface Indexer extends VTLoggerEnabled {
    
    void setBaseDir(Path baseDir)
    
    void setReportsDir(Path reportsDir)

    void setOutput(Path outputFile)
    
    Path getOutput()

    void execute() throws IOException

}
