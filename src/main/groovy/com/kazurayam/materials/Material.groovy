package com.kazurayam.materials

import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime

import com.kazurayam.materials.model.Suffix

interface Material extends MaterialCore {

    Path getParentDirectoryPathRelativeToTSuiteResult()
    
    Path getPathRelativeToTSuiteTimestamp()
    String getSubpath()
    String getHrefToReport()

    String getFileName()
    Suffix getSuffix()
    FileType getFileType()
    
    /**
     * returns the identifier of the Material which is used as
     * - the name in the Bootstrap Treeview
     * - the title of Modal window
     *
     * @return
     */
    String getIdentifier()
    
    LocalDateTime getLastModified()
    long getLength()
    
    TCaseResult getParent()
    
    TCaseName getTCaseName()
    TCaseResult getTCaseResult()
    URL getURL()
    Material setLastModified(Instant lastModified)
    Material setLastModified(long lastModified)
    Material setLength(long length)
    Material setParent(TCaseResult parent)
    String toJsonText()

}
