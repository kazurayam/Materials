package com.kazurayam.materials

import java.nio.file.Path

interface MaterialPairs {
    
    MaterialPair put(Path pathRelativeToTSuiteTimestamp, MaterialPair materialPair)
    
    MaterialPair get(Path pathRelativeToTSuiteTimestamp)
    
    Set<Path> keySet()
    
    boolean containsKey(Path pathRelativeToTSuiteTimestamp)
    
    int size()
    
}
