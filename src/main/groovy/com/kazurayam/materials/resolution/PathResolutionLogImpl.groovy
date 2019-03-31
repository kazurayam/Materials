package com.kazurayam.materials.resolution

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.TCaseName

import groovy.json.JsonOutput

class PathResolutionLogImpl implements PathResolutionLog, Comparable<Object> {

    static Logger logger_ = LoggerFactory.getLogger(PathResolutionLogImpl.class)
    
    // mandatory properties
    private InvokedMethodName invokedMethodName_
    private TCaseName tCaseName_
    private Path materialPath_
    
    // optional properties
    private Path subPath_
    private URL url_
    private String fileName_
    
    PathResolutionLogImpl(InvokedMethodName invokedMethodName, TCaseName tCaseName, Path materialPath) {
        this.invokedMethodName_ = invokedMethodName
        this.tCaseName_ = tCaseName
        this.materialPath_ = materialPath
    }
    
    static PathResolutionLog deserialize(Map jsonObject) {
        String pp = JsonOutput.prettyPrint(JsonOutput.toJson(jsonObject))
        String imn = jsonObject.PathResolutionLog['InvokedMethodName']
        String tcn = jsonObject.PathResolutionLog['TCaseName']
        String mp  = jsonObject.PathResolutionLog['MaterialPath']
        if (imn == null) {
            throw new IllegalArgumentException(
                "No \'InvokedMethodName\' is found in ${pp}")
        }
        if (tcn == null) {
            throw new IllegalArgumentException(
                "No \'TCaseName\' is found in ${pp}")
        }
        if (mp == null) {
            throw new IllegalArgumentException(
                "No \'MaterialPath\' is found in ${pp}")
        }
        TCaseName tCaseName = new TCaseName(jsonObject.PathResolutionLog['TCaseName'])
        Path materialPath = Paths.get(mp)
        PathResolutionLog log = new PathResolutionLogImpl(
                                        InvokedMethodName.get(imn),
                                        tCaseName,
                                        materialPath)
        //
        if (jsonObject.PathResolutionLog['SubPath']) {
            log.setSubPath(Paths.get(jsonObject.PathResolutionLog['SubPath']))
        }
        if (jsonObject.PathResolutionLog['URL']) {
            log.setUrl(new URL(jsonObject.PathResolutionLog['URL']))
        }
        if (jsonObject.PathResolutionLog['FileName']) {
            log.setFileName(jsonObject.PathResolutionLog['FileName'])
        }
        //
        return log
    }
    
    @Override
    void serialize(Writer writer) {
        writer.print(JsonOutput.prettyPrint(this.toJsonText()))
        writer.flush()
    }
    
    @Override
    InvokedMethodName getInvokedMethodName() {
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
    boolean equals(Object obj) {
        if (! obj instanceof PathResolutionLog) {
            return false
        }
        PathResolutionLog other = (PathResolutionLog)obj
        return this.getMaterialPath() == other.getMaterialPath() &&
                this.getInvokedMethodName() == other.getInvokedMethodName() &&
                this.getTCaseName() == other.getTCaseName()
    }
    
    @Override
    int hashCode() {
        return this.getMaterialPath().hashCode()
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
        sb.append(Helpers.escapeAsJsonText(this.getInvokedMethodName().toString()))
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
    
    @Override
    int compareTo(Object object) {
        if (! object instanceof PathResolutionLog) {
            throw new IllegalArgumentException("object is not instance of PathResolutionLog")
        }
        PathResolutionLog other = (PathResolutionLog)object
        return this.getMaterialPath().compareTo(other.getMaterialPath())
    }
}
