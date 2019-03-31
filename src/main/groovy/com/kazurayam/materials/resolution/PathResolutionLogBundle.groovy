package com.kazurayam.materials.resolution

import java.nio.file.Path

class PathResolutionLogBundle {

    static final String SERIALIZED_FILE_NAME = 'path-resolution-logs.json'

    private List<PathResolutionLog> bundle_
    
    PathResolutionLogBundle() {
        this.bundle_ = new ArrayList<PathResolutionLog>()
    }
    
    
    
    void add(PathResolutionLog pathResolutionLog) {
        this.bundle_.add(pathResolutionLog)
    }
    
    int getSize() {
        return this.bundle_.size()
    }
    
    PathResolutionLog get(int index) {
        return this.bundle_.get(index)
    }
    
    List<PathResolutionLog> findByMaterialPath(Path materialPath) {
        throw new UnsupportedOperationException("TODO")
    }
    
    /**
     * 
     * @param materialPath
     * @return
     */
    PathResolutionLog findLastByMaterialPath(Path materialPath) {
        throw new UnsupportedOperationException("TODO")
    }
    
    String toJsonText() {
        throw new UnsupportedOperationException("TODO")
    }
    
    @Override
    String toString() {
        throw this.toJsonText()
    }
    
    static PathResolutionLogBundle deserialize(def jsonObject) {
        throw new UnsupportedOperationException("TODO")
    }
    
    static PathResolutionLogBundle deserialize(Path jsonPath) {
        throw new UnsupportedOperationException("TODO")
    }
    
    void serialize(Writer writer) {
        throw new UnsupportedOperationException("TODO")
    }
}
