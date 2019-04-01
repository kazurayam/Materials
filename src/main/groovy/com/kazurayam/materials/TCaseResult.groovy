package com.kazurayam.materials

import java.nio.file.Path
import java.time.LocalDateTime

import com.kazurayam.materials.impl.TCaseResultImpl
import com.kazurayam.materials.model.Suffix

/**
 *
 */
abstract class TCaseResult implements Comparable<TCaseResult> {

    static TCaseResult newInstance(TCaseName tCaseName) {
        return new TCaseResultImpl(tCaseName)
    }
    
    abstract TCaseResult setParent(TSuiteResult parent)

    abstract TSuiteResult getParent()

    //abstract TSuiteResult getTSuiteResult()

    abstract TCaseName getTCaseName()

    abstract Path getTCaseDirectory()

    abstract TCaseResult setLastModified(LocalDateTime lastModified)

    abstract LocalDateTime getLastModified()
    
    abstract TCaseResult setSize(long size)
    
    abstract long getSize()

    abstract List<Material> getMaterialList()

    abstract List<Material> getMaterialList(String pattern, boolean isRegex)
    
    //abstract List<Material> getMaterialList(Path dirpath, URL url, FileType fileType)

    abstract Material getMaterial(String subpath, URL url, Suffix suffix, FileType fileType)
    
    abstract Material getMaterial(Path subpathUnderTCaseResult)
    
    abstract boolean addMaterial(Material material)
    
    abstract Suffix allocateNewSuffix(String subpath, URL url, FileType fileType)
    
    abstract String toJsonText()

}


