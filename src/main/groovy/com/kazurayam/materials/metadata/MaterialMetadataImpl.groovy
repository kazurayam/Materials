package com.kazurayam.materials.metadata

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.MaterialDescription
import com.kazurayam.materials.TCaseName
import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MaterialMetadataImpl implements MaterialMetadata, Comparable<Object> {

    static Logger logger_ = LoggerFactory.getLogger(MaterialMetadataImpl.class)
    
    // mandatory properties
    private InvokedMethodName invokedMethodName_
    private TCaseName tCaseName_
    private String materialPath_
    private MaterialDescription description_

    // optional properties
    private String subPath_
    private URL url_
    private String fileName_
    private String executionProfileName_

	/**
	 * 
	 * @param invokedMethodName
	 * @param tCaseName
	 * @param materialPath Path of a Material file relative to the baseDir of MaterialRepository = the 'Materials' directory
	 */
    MaterialMetadataImpl(InvokedMethodName invokedMethodName,
                         TCaseName tCaseName,
                         String materialPath,
                         MaterialDescription description) {
        this.invokedMethodName_ = invokedMethodName
        this.tCaseName_ = tCaseName
        this.materialPath_ = materialPath
        this.description_ = description
    }

    static MaterialMetadata deserialize(Map jsonObject) {
        Objects.requireNonNull(jsonObject, "jsonObject must not be null")
        String pp = JsonOutput.prettyPrint(JsonOutput.toJson(jsonObject))
        String imn = jsonObject.MaterialMetadata['InvokedMethodName']
        String tcn = jsonObject.MaterialMetadata['TCaseName']
        String mp  = jsonObject.MaterialMetadata['MaterialPath']
        def desc   = jsonObject.MaterialMetadata['MaterialDescription']
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
        if (desc == null) {
            throw new IllegalArgumentException(
                    "No \'MaterialDescription\' is found in ${pp}")
        }
        TCaseName tCaseName = new TCaseName(jsonObject.MaterialMetadata['TCaseName'])
        String materialPath = mp
        MaterialDescription description = MaterialDescription.newInstance(desc)

        logger_.debug("#deserialize mp                 =${mp}")
        logger_.debug("#deserialize materialPath       =${materialPath}")

        MaterialMetadata metadata = new MaterialMetadataImpl(
                                        InvokedMethodName.get(imn),
                                        tCaseName,
                                        materialPath,
                                        description)
        //
        if (jsonObject.MaterialMetadata['SubPath']) {
            metadata.setSubPath(jsonObject.MaterialMetadata['SubPath'])
        }
        if (jsonObject.MaterialMetadata['URL']) {
            metadata.setUrl(new URL(jsonObject.MaterialMetadata['URL']))
        }
        if (jsonObject.MaterialMetadata['FileName']) {
            metadata.setFileName(jsonObject.MaterialMetadata['FileName'])
        }
        if (jsonObject.MaterialMetadata['ExecutionProfileName']) {
            metadata.setExecutionProfileName(jsonObject.MaterialMetadata['ExecutionProfileName'])
        }
        
        //
        return metadata
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
    String getMaterialPath() {
        return this.materialPath_
    }

    @Override
    MaterialDescription getMaterialDescription() {
        return this.description_
    }

    @Override
    void setSubPath(String subPath) {
        this.subPath_ = subPath
    }
    
    @Override
    String getSubPath() {
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
    void setExecutionProfileName(String profileName) {
        this.executionProfileName_ = profileName
    }
    
    @Override
    String getExecutionProfileName() {
        return this.executionProfileName_
    }
    
    @Override
    boolean equals(Object obj) {
        if (! obj instanceof MaterialMetadata) {
            return false
        }
        MaterialMetadata other = (MaterialMetadata)obj
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
        sb.append('\"MaterialMetadata\":{')
        sb.append('\"MaterialPath\":\"')
        sb.append(Helpers.escapeAsJsonText(this.getMaterialPath()))
        sb.append('\",')
        sb.append('\"TCaseName\":\"')
        sb.append(Helpers.escapeAsJsonText(this.getTCaseName().getId()))
        sb.append('\",')

        sb.append('\"MaterialDescription\":')
        sb.append(this.getMaterialDescription().toJsonText())
        sb.append(',')

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
        if (this.getExecutionProfileName() != null) {
            sb.append(',\"ExecutionProfileName\":\"')
            sb.append(Helpers.escapeAsJsonText(this.getExecutionProfileName()))
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
        if (! object instanceof MaterialMetadata) {
            throw new IllegalArgumentException("object is not instance of MaterialMedata")
        }
        MaterialMetadata other = (MaterialMetadata)object
		int categoryComparison = this.getMaterialDescription().getCategory().compareTo(other.getMaterialDescription().getCategory())
		if (categoryComparison == 0) {
			int descriptionComparison = this.getMaterialDescription().getDescription().compareTo(other.getMaterialDescription().getDescription())
			if (descriptionComparison == 0) {
				int pathComparison = this.getMaterialPath().compareTo(other.getMaterialPath())
				return pathComparison
			} else {
				return descriptionComparison
			}
		} else {
			return categoryComparison
		}
    }
}
