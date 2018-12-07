package com.kazurayam.materials

import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime

import com.kazurayam.materials.model.Suffix
import com.kazurayam.materials.model.TCaseResult

interface Material extends Comparable<Material> {

    Material setParent(TCaseResult parent)

    TCaseResult getParent()

    TCaseResult getTCaseResult()

    URL getURL()

    Suffix getSuffix()

    FileType getFileType()

    Path getPath()

    Path getDirpath()
    
    Path getDirpathRelativeToTSuiteResult()
    
    Material setLastModified(long lastModified)

    Material setLastModified(Instant lastModified)

    LocalDateTime getLastModified()

    // ---------------- business ----------------------------------------------
    Path getPathRelativeToTSuiteTimestamp()

    Path getPathRelativeToRepositoryRoot()

    //Path getPathRelativeTo(Path base)

    // --------------------------------------

    String getHrefRelativeToRepositoryRoot()

    //String getHrefRelativeTo(Path base)

    // ---------------------------------------------

    String getEncodedHrefRelativeToRepositoryRoot()

    //String getEncodedHrefRelativeTo(Path base)

    /**
     * returns the identifier of the Material which is used as
     * - the name in the Bootstrap Treeview
     * - the title of Modal window
     *
     * @return
     */
    String getIdentifier()
    
    String toJson()

}
