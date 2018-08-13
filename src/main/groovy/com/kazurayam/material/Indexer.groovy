package com.kazurayam.material

import java.nio.file.Path

interface Indexer {

    void setBaseDir(Path baseDir)

    void execute() throws IOException

    Path getOutput()

}
