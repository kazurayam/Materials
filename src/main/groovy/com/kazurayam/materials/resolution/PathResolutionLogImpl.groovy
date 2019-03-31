package com.kazurayam.materials.resolution

import java.nio.file.Path

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.TCaseName

class PathResolutionLogImpl implements PathResolutionLog, Comparable<PathResolutionLogImpl> {

    // mandatory properties
    private String invokedMethodName_
    private TCaseName tCaseName_
    private Path materialPath_
    
    // optional properties
    private Path subPath_
    private URL url_
    private String fileName_
    
    PathResolutionLogImpl(String invokedMethodName, TCaseName tCaseName, Path materialPath) {
        this.invokedMethodName_ = invokedMethodName
        this.tCaseName_ = tCaseName
        this.materialPath_ = materialPath
    }
    
    static PathResolutionLogImpl deserialize(def jsonObject) {
        throw new UnsupportedOperationException("TODO")
    }
    
    @Override
    String getInvokedMethodName() {
        return this.invokedMethodName_
    }
    
    @Override
    TCaseName getTCaseName() {
        return this.tCaseName_
    }
    
    @Override
    Path getMaterialPath() {
        return this.materialPath_
    }
    
    @Override
    void setSubPath(Path subPath) {
        this.subPath_ = subPath
    }
    
    @Override
    Path getSubPath() {
        return subPath_
    }
    
    @Override
    void setUrl(URL url) {
        this.url_ = url
    }
    
    @Override
    URL getUrl() {
        return this.url_
    }
    
    @Override
    void setFileName(String fileName) {
        this.fileName_ = fileName
    }
    
    @Override
    String getFileName() {
        return this.fileName_
    }
    
    @Override
    int compareTo(PathResolutionLogImpl other) {
        throw new UnsupportedOperationException("TODO")
    }
    
    @Override
    boolean equals(Object obj) {
        throw new UnsupportedOperationException("TODO")
    }
    
    @Override
    int hashCode() {
        throw new UnsupportedOperationException("TODO")
    }
    
    @Override
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('\"PathResolutionLog\":{')
        sb.append('\"MaterialPath\":\"')
        sb.append(Helpers.escapeAsJsonText(this.getMaterialPath().toString()))
        sb.append('\",')
        sb.append('\"TCaseName\":\"')
        sb.append(Helpers.escapeAsJsonText(this.getTCaseName().getId()))
        sb.append('\",')
        sb.append('\"InvokedMethodName\":\"')
        sb.append(Helpers.escapeAsJsonText(this.getInvokedMethodName()))
        sb.append('\"')
        if (this.getSubPath() != null) {
            sb.append(',\"SubPath\":\"')
            sb.append(Helpers.escapeAsJsonText(this.getSubPath().toString()))
            sb.append('\"')
        }
        if (this.getUrl() != null) {
            sb.append(',\"URL\":\"')
            sb.append(Helpers.escapeAsJsonText(this.getUrl().toExternalForm().toString()))
            sb.append('\"')
        }
        if (this.getFileName() != null) {
            sb.append(',\"FileName\":\"')
            sb.append(Helpers.escapeAsJsonText(this.getFileName()))
            sb.append('\"')
        }
        sb.append('}')
        sb.append('}')
        
        return sb.toString()
    }
    
    @Override
    String toString() {
        return this.toJsonText()
    }
}
