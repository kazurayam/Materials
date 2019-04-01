package com.kazurayam.materials.resolution

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class PathResolutionLogBundle {

    static Logger logger_ = LoggerFactory.getLogger(PathResolutionLogBundle.class)
    
    static final String SERIALIZED_FILE_NAME = 'path-resolution-log-bundle.json'
    static final String TOP_PROPERTY_NAME = 'PathResolutionLogBundle'
    
    private static List<PathResolutionLog> bundle_
    
    PathResolutionLogBundle() {
        bundle_ = new ArrayList<PathResolutionLog>()
    }

    static PathResolutionLogBundle deserialize(Path jsonPath) {
        return deserialize(jsonPath.toFile().text)
    }
    
    static PathResolutionLogBundle deserialize(String jsonText) {
        def jsonObject = new JsonSlurper().parseText(jsonText)
        if (jsonObject[TOP_PROPERTY_NAME]) {
            return deserialize((Map)jsonObject)
        } else {
            throw new IllegalArgumentException("No \'${TOP_PROPERTY_NAME}\' found in ${jsonText}")
        }
    }
    
    static PathResolutionLogBundle deserialize(Map jsonObject) {
        PathResolutionLogBundle instance = new PathResolutionLogBundle()
        def logs = jsonObject[TOP_PROPERTY_NAME]
        if (logs == null) {
            throw new IllegalArgumentException("No \'${TOP_PROPERTY_NAME}\' found in ${jsonObject}")
        }
        for (def logJsonObject : logs) {
            logger_.debug("#deserialize logJsonObject=${JsonOutput.prettyPrint(JsonOutput.toJson(logJsonObject))}")
            PathResolutionLog log = PathResolutionLogImpl.deserialize((Map)logJsonObject)
            instance.add(log)
        }
        return instance
    }
    
    void serialize(Writer writer) {
        writer.print(JsonOutput.prettyPrint(this.toJsonText()))
        writer.flush()
    }

    void add(PathResolutionLog pathResolutionLog) {
        this.bundle_.add(pathResolutionLog)
    }
    
    int size() {
        return this.bundle_.size()
    }
    
    PathResolutionLog get(int index) {
        return this.bundle_.get(index)
    }
    
    List<PathResolutionLog> findByMaterialPath(String materialPath) {
        List<PathResolutionLog> list = new ArrayList<PathResolutionLog>()
        for (PathResolutionLog entry : bundle_) {
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
    PathResolutionLog findLastByMaterialPath(String materialPath) {
        List<PathResolutionLog> list = this.findByMaterialPath(materialPath)
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
        sb.append('\"PathResolutionLogBundle\":[')
        int count = 0
        for (PathResolutionLog resolution: this.bundle_) {
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
        throw this.toJsonText()
    }
    
}
