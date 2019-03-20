package com.kazurayam.materials

import java.nio.file.Path

interface MaterialCore {
    
    Path getBaseDir()
    Path getPath()
    Path getPathRelativeToRepositoryRoot()
    
}
