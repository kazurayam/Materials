package com.kazurayam.materials.resolution

import java.nio.file.Path

import com.kazurayam.materials.TCaseName

interface PathResolutionLog {
    
    String toJsonText()
    void serialize(Writer writer)
    
    // accessors for mandatory properties
    String getMaterialPath()
    TCaseName getTCaseName()
    InvokedMethodName getInvokedMethodName()
    
    // accessors for optional properties
    void setSubPath(String subpath)
    String getSubPath()
    
    void setUrl(URL url)
    URL getUrl()
    
    void setFileName(String fileName)
    String getFileName()

}
