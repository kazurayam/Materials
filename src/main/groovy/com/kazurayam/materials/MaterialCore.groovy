package com.kazurayam.materials

import java.nio.file.Path

interface MaterialCore {
    
    Path getBaseDir()
    Path getPath()
    Path getPathRelativeToRepositoryRoot()
    String getEncodedHrefRelativeToRepositoryRoot()
    String getHrefRelativeToRepositoryRoot()
    String getDescription()
    void setDescription(String description)
    boolean fileExists()
}
