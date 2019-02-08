package com.kazurayam.materials

import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime

import com.kazurayam.materials.model.Suffix
import com.kazurayam.materials.model.TCaseResult

interface Material extends Comparable<Material> {

    Path getDirpath()
    Path getDirpathRelativeToTSuiteResult()
    String getEncodedHrefRelativeToRepositoryRoot()
    String getFileName()
    FileType getFileType()
    String getHrefRelativeToRepositoryRoot()

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
    Path getPath()
    Path getPathRelativeToRepositoryRoot()
    Path getPathRelativeToTSuiteTimestamp()
    Path getSubpath()
    Suffix getSuffix()
    TCaseName getTCaseName()
    TCaseResult getTCaseResult()
    URL getURL()
    Material setLastModified(Instant lastModified)
    Material setLastModified(long lastModified)
    Material setLength(long length)
    Material setParent(TCaseResult parent)
    String toJson()

}
