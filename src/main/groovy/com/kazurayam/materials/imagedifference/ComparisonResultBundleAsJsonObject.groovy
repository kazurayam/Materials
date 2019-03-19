package com.kazurayam.materials.imagedifference

import java.nio.file.Path

import groovy.json.JsonSlurper

class ComparisonResultBundleAsJsonObject {
    
    private def bundleAsJsonObject_
    
    /**
     * @param jsonText
     * <PRE>
{
    "ComparisonResultBundle": [
        {
            "ComparisonResult": {
                "expectedMaterial": {
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
                },
                "actualMaterial": {
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
                        "path": "build/tmp/testOutput/ComparisonResultBundleSpec/test_deserializeToJsonObject/Materials/47News_chronos_capture/20190216_204329/main.TC_47News.visitSite/47NEWS_TOP.png",
                        "lastModified": "2019-02-22T22:22:11"
                    }
                },
                "criteriaPercentage": 30.197159598135954,
                "imagesAreSimilar": true,
                "diffRatio": 16.86,
                "diff": "build/tmp/testOutput/ComparisonResultBundleSpec/test_deserializeToJsonObject/Materials/ImageDiff/20190216_210203/ImageDiff/main.TC_47News.visitSite/47NEWS_TOP.20190216_064354_-20190216_204329_.(16.86).png"
            }
        }
    ]
}
     * </PRE>
     */
    ComparisonResultBundleAsJsonObject(String jsonText) {
        JsonSlurper slurper = new JsonSlurper()
        this.bundleAsJsonObject_ = slurper.parseText(jsonText)
        if (bundleAsJsonObject_.ComparisonResultBundle == null) {
            throw new IllegalArgumentException("given jsonText is not ComparisonResultBundle; ${jsonText}")
        }
    }
    
    int size() {
        return bundleAsJsonObject_.ComparizonResultBundle.size()
    }
    
    Map get(int index) {
        
    }

    /**
     * look up a Map object as ComparisonResult in the bundle.
     * The argumet imageDiffPath is matched against the diff propert of each
     * contained Map objects as ComparisonResult.
     * 
     * @param imageDiffPath
     * @return
     */
    Map get(Path imageDiffPath) {
        
    }
    
    boolean containsImageDiff(Path imageDiffPath) {
        return this.get(imageDiffPath) != null
    }
    
    String srcOfExpectedMaterial(Path imageDiffPath) {
        throw new UnsupportedOperationException("TODO")
    }
    
    String srcOfActualMaterial(Path imageDiffPath) {
        throw new UnsupportedOperationException("TODO")
    }
}

