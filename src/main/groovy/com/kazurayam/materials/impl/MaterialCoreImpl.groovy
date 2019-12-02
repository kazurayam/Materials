package com.kazurayam.materials.impl

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.MaterialCore

import groovy.json.JsonSlurper

class MaterialCoreImpl implements MaterialCore, Comparable<MaterialCore> {
    
    static Logger logger_ = LoggerFactory.getLogger(MaterialCoreImpl.class)
    
    // following properties are required
    private Path baseDir_ = null
    private Path path_ = null
    
    // following properties are optional
    private String description_ = null
    
    /**
     * construct a MaterialCoreImpl with a json text which was created by the toJsonText() of
     * its own. Only the path property will be picked up from the json text, while the 1st
     * argument baseDir is required.
     * 
     * @param baseDir Path of the 'Materials' directory
     * @param jsonText <PRE>
{
    "MaterialCore": {
        "path": "build/tmp/testOutput/ComparisonResultBundleSpec/test_deserializeToJsonObject/Materials/47News_chronos_capture/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png"
     }
}
     * </PRE>
     */
    MaterialCoreImpl(Path baseDir, String jsonText) {
        Objects.requireNonNull(baseDir, "baseDir must not be null")
        Objects.requireNonNull(jsonText, "jsonText must not be null")
        this.baseDir_ = baseDir.normalize()
        
        JsonSlurper slurper = new JsonSlurper()
        def jsonObject = slurper.parseText(jsonText)
        if (jsonObject == null) {
            throw new IllegalArgumentException("can not parse jsonText: ${jsonText}")
        }
        // we need to look for 'Material' property, not 'MaterialCore' property,
        // so that we can digest the comparison-result-bundle.json file created by
        // the ImageCollectionDiffer object.
        //
        if (jsonObject.Material == null) {
            throw new IllegalArgumentException("jsonText is not a Material json: ${jsonText}")
        }
        
        //if (jsonObject.Material.path == null) {
        //    throw new IllegalArgumentException("Material.path is not found in : ${jsonText}")
        //}
        //   I should not rely on the jsonObject.Material.path
        
        //this.path_ = Paths.get(jsonObject.Material.path).normalize()
        //   the above 1 line was inappropriate.
        //   I should not rely on the jsonObject.Material.path value because the JSON file might be imported from other PC,
        //   therefore the jsonObject.Materail.Path value has other PC's path info, which we should not refer to.
        
        // Instead I should rely on the jsonObject.Material.hrefRelativeToRepositoryRoot info;
        // as the relative path is valid on anywhere.
        if (jsonObject.Material.hrefRelativeToRepositoryRoot == null) {
            throw new IllegalArgumentException("Material.hrefRelativeToRepositoryRoot is not found in : ${jsonText}")
        }
        this.path_ = baseDir.resolve(jsonObject.Material.hrefRelativeToRepositoryRoot).normalize()
                
        if (jsonObject.Material.description != null) {
            this.setDescription(jsonObject.Material.description)
        }
    }
    
    /**
     * 
     * @param baseDir of the 'Materials' directory
     * @param path of the Material file
     */
    MaterialCoreImpl(Path baseDir, Path path) {
        this.baseDir_ = baseDir.normalize()
        this.path_    = path.normalize()
    }
    
    @Override
    Path getBaseDir() {
        return this.baseDir_
    }
    
    @Override
    Path getPath() {
        return this.path_
    }
    
    @Override
    boolean fileExists() {
        return Files.exists(this.getPath())
    }
    
    @Override
    Path getPathRelativeToRepositoryRoot() {
        Path p = baseDir_.relativize(path)
        logger_.debug("#getPathRelativeToRepositoryRoot baseDir_ is ${baseDir_.toString()}, " + 
            "path_ is ${path_.toString()}, returning ${p.toString()}")
        return p
    }
    
	@Override
	String getEncodedHrefRelativeToRepositoryRoot() {
		String baseUri = this.baseDir_.toUri()
		if ( ! baseUri.endsWith('/')) {
			baseUri += '/'
		}
		String mateUri = this.path_.toUri()
		//println "MaterialCoreImpl#getEncodedHrefRelativeToRepositoryRoot baseUri=${baseUri}"
		//println "MaterialCoreImpl#getEncodedHrefRelativeToRepositoryRoot mateUri=${mateUri}"
		String result = mateUri.substring(baseUri.length())
		return result
	}
    
    @Override
    String getHrefRelativeToRepositoryRoot() {
        Path p = this.getPathRelativeToRepositoryRoot()
        return p.toString().replace('\\', '/')
    }
    
    @Override
    String getDescription() {
        return this.description_
    }
    
    @Override
    void setDescription(String description) {
        this.description_ = description
    }
    
    // --------- overriding methods inherited from java.lang.Object -----------
    @Override
    String toString() {
        return this.toJsonText()
    }
    
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"Material":{')
        //sb.append('"baseDir":"' + Helpers.escapeAsJsonText(this.getBaseDir().toString()) + '",')
        //sb.append('"pathRelativeToRepositoryRoot":"' + 
        //    Helpers.escapeAsJsonText(this.getPathRelativeToRepositoryRoot().toString()) + '",')
        sb.append('"path":"' + Helpers.escapeAsJsonText(this.getPath().toString()) + '",')
        sb.append('"hrefRelativeToRepositoryRoot":"' + Helpers.escapeAsJsonText(this.getHrefRelativeToRepositoryRoot()) + '"')
        if (this.getDescription() != null) {
            sb.append(',')
            sb.append('"description":"' + Helpers.escapeAsJsonText(this.getDescription()) + '"')
        }
        sb.append('}')
        sb.append('}')
        return sb.toString()
    }
    
    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof MaterialCoreImpl))
            return false
        MaterialCoreImpl other = (MaterialCoreImpl)obj
        return this.getBaseDir().equals(other.getBaseDir()) &&
               this.getPath().equals(other.getPath())
    }
    
    @Override
    public int hashCode() {
        int hash = 7
        hash = 31 * hash + (int) this.getBaseDir().hashCode()
        hash = 31 * hash + (int) this.getPath().hashCode()
        return hash
    }
    
    @Override
    int compareTo(MaterialCore other) {
        int orderOfBaseDir = this.getBaseDir().compareTo(other.getBaseDir())
    }
}
