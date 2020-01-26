package com.kazurayam.materials.metadata

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class MaterialMetadataBundle {

    static Logger logger_ = LoggerFactory.getLogger(MaterialMetadataBundle.class)
    
    static final String SERIALIZED_FILE_NAME = 'material-metadata-bundle.json'
    static final String TOP_PROPERTY_NAME = 'MaterialMetadataBundle'
    
    private static List<MaterialMetadata> metadataBundle_
    
    MaterialMetadataBundle() {
        metadataBundle_ = new ArrayList<MaterialMetadata>()
    }

    static MaterialMetadataBundle deserialize(Path jsonPath) {
        return deserialize(jsonPath.toFile().text)
    }
    
    static MaterialMetadataBundle deserialize(String jsonText) {
        def jsonObject = new JsonSlurper().parseText(jsonText)
        if (jsonObject[TOP_PROPERTY_NAME]) {
            return deserialize((Map)jsonObject)
        } else {
            throw new IllegalArgumentException("No \'${TOP_PROPERTY_NAME}\' found in ${jsonText}")
        }
    }
    
    static MaterialMetadataBundle deserialize(Map jsonObject) {
        MaterialMetadataBundle instance = new MaterialMetadataBundle()
        def bundle = jsonObject[TOP_PROPERTY_NAME]
        if (bundle == null) {
            throw new IllegalArgumentException("No \'${TOP_PROPERTY_NAME}\' found in ${jsonObject}")
        }
        for (def metadataJsonObj : bundle) {
            MaterialMetadata metadata = MaterialMetadataImpl.deserialize((Map)metadataJsonObj)
            instance.add(metadata)
        }
        return instance
    }
    
    void serialize(Writer writer) {
        writer.print(JsonOutput.prettyPrint(this.toJsonText()))
        writer.flush()
    }

    void add(MaterialMetadata pathResolutionLog) {
        this.metadataBundle_.add(pathResolutionLog)
    }
    
    int size() {
        return this.metadataBundle_.size()
    }
    
    MaterialMetadata get(int index) {
        return this.metadataBundle_.get(index)
    }
    
    List<MaterialMetadata> findByMaterialPath(String materialPath) {
        List<MaterialMetadata> list = new ArrayList<MaterialMetadata>()
        for (MaterialMetadata entry : metadataBundle_) {
            if (entry.getMaterialPath() == materialPath) {
                list.add(entry)
            }
        }
        return list
    }
    
    /**
     * 
     * @param materialPath
     * @return
     */
    MaterialMetadata findLastByMaterialPath(String materialPath) {
        List<MaterialMetadata> list = this.findByMaterialPath(materialPath)
        if (list.size() > 0) {
            Collections.sort(list)
            list.get(list.size() - 1)
        } else {
            return null
        }
    }
    
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append("\"${TOP_PROPERTY_NAME}\":[")
        int count = 0
        for (MaterialMetadata resolution: this.metadataBundle_) {
            if (count > 0) {
                sb.append(',')
            }
            count += 1
            sb.append(resolution.toJsonText())
        }
        sb.append(']')
        sb.append('}')
        return sb.toString()
    }
    
    @Override
    String toString() {
        return this.toJsonText()
    }

}
