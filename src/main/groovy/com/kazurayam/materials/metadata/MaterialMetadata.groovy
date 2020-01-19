package com.kazurayam.materials.metadata

import java.nio.file.Path

import com.kazurayam.materials.TCaseName

/**
 * The <pre>resolveMatherialPath(TCaseName tCaseName, String subpath, String fileName)</pre> method of 
 * <pre>com.kazurayam.materials.impl.MaterialRepositoryImpl</pre> class creates 
 * <pre>tSuiteName/tSuiteTimestamp/path-resolution-log.json</pre> file, which contains 
 * information how the method call resolved and returned a Path based on the given parameters. 
 * The following snippet shows an example.
 * <pre>
 * {
 *   "MaterialMetadata": {
 *     "MaterialPath": "build\\tmp\\testOutput\\PathResolutionLogSpec\\testSerializeAndDeserializeWithSubPath\\Materials\\Monitor47News\\20190123_153854\\main.TC1\\dir1\\47NEWS_TOP.png",
 *     "TCaseName": "Test Cases/main/TC1",
 *     "InvokedMethodName": "resolveScreenshotPathByUrlPathComponents",
 *     "SubPath": "dir1",
 *     "URL": "https://www.47news.jp/"
 *   }
 * }
 * </pre>
 */
interface MaterialMetadata {
    
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
