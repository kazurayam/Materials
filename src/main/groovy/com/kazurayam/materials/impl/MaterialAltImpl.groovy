package com.kazurayam.materials.impl

import java.nio.file.Path

import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.model.MaterialFileName

class MaterialAltImpl implements Material {
    
    // following properties are required
    private Path path_
    private MaterialFileName materialFileName_
    
    // following properties can be null
    private TCaseResult parent_
    
    /**
     * the path will be parsed to identify properties as parent_, materialFileName_ 
     * @param baseDir Path of the 'Materials' directory
     * @param jsonText <PRE>
{
    "Material": {
        "url": "null",
        "suffix": "",
        "fileType": {
            "FileType": {
                "extension": "png",
                "mimeTypes": [
                    "image/png"
                ]
            }
        },
        "path": "build/tmp/testOutput/ComparisonResultBundleSpec/test_deserializeToJsonObject/Materials/47News_chronos_capture/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png",
        "lastModified": "2019-02-22T22:22:11"
     }
}
     * </PRE>
     */
    MaterialAltImpl(Path baseDir, String jsonText) {
        
    }
    
    @Override
    String getFileName() {
        Objects.requireNonNull(materialFileName_, "materialFileName_ must not be null")
        materialFileName_.getFileName()
    }
    
    /**
     * @return a TCaseResult object, may return null
     */
    @Override
    TCaseResult getParent() {
        return this.getTCaseResult()
    }
    
    /**
     * @return a TCaseName object, same as this.getTCaseResult().getTcaseName(); may return null
     */
    @Override
    TCaseName getTCaseName() {
        if (this.getTCaseResult() != null) {
            return this.getTCaseResult().getTCaseName()
        } else {
            return null
        }
    }
    /**
     * @return a TCaseResult object, may return null
     */
    @Override
    TCaseResult getTCaseResult() {
        return parent_
    }
    
    /**
     * @return a URL object set by the constructor, may return null
     */
    @Override
    URL getURL() {
        return materialFileName_.getURL()
    }
    
    @Override
    Material setParent(TCaseResult parent) {
        Objects.requireNonNull(parent)
        parent_ = parent
        return this
    }

}
