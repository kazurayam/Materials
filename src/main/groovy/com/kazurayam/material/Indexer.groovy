package com.kazurayam.material

import java.nio.file.Path

interface Indexer {

    void setBaseDir(Path baseDir)

    void setOutput(Path outputFile)

    void execute() throws IOException

}
