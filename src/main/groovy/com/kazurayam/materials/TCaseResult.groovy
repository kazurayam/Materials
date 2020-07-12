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

    // ------------------ overriding Object properties ------------------------
    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof TCaseResult)) {
            return false
        }
        TCaseResult other = (TCaseResult) obj
        println "this.getParent() != null && other.getParent() != null: ${this.getParent() != null && other.getParent() != null}"
        if (this.getParent() != null && other.getParent() != null) {
            int v = (this.getParent() <=> other.getParent())
            if (v == 0) {
                return this.getTCaseName() == other.getTCaseName()
            } else {
                return false
            }
        } else {
            return this.getTCaseName() == other.getTCaseName()
        }
    }

    @Override
    int hashCode() {
        //return this.getTCaseName().hashCode()
        final int prime = 31
        int result = 1
        if (this.getParent() != null) {
            result = prime * result + this.getParent().hashCode()
        }
        result = prime * result + this.getTCaseName().hashCode()
        return result
    }

    @Override
    int compareTo(TCaseResult other) {
        if (this.getParent() != null && other.getParent() != null) {
            int v = this.getParent().compareTo(other.getParent())
            if (v == 0) {
                return this.getTCaseName() <=> other.getTCaseName()
            } else {
                return v
            }
        } else {
            return this.getTCaseName() <=> other.getTCaseName()
        }
    }

}


