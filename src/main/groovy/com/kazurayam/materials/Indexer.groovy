package com.kazurayam.materials

import java.nio.file.Path


interface Indexer {

    void setBaseDir(Path baseDir)
    
    void setReportsDir(Path reportsDir)

    void setOutput(Path outputFile)

    void execute() throws IOException

}
