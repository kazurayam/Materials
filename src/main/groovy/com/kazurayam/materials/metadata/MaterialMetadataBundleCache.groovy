package com.kazurayam.materials.metadata

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl

import groovy.json.JsonOutput

/**
 * creates a in-memory cache of MaterialmetadataBundle instances for better performance
 */
final class MaterialMetadataBundleCache {

    private Map<Path, MaterialMetadataBundle> cache_
    
    static final Logger logger_ = LoggerFactory.getLogger(MaterialMetadataBundleCache.class)

    private VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()

    MaterialMetadataBundleCache() {
        cache_ = new HashMap<Path, MaterialMetadataBundle>()
    }

    void setVisualTestingLogger(VisualTestingLogger vtLogger) {
        this.vtLogger_ = vtLogger
    }

    boolean containsKey(Path bundleFile) {
        return cache_.containsKey(bundleFile)
    }
    
    MaterialMetadataBundle put(Path bundleFile) {
		MaterialMetadataBundle value = MaterialMetadataBundle.deserialize(bundleFile)
		return cache_.put(bundleFile, value)
    }
    
    MaterialMetadataBundle remove(Path bundleFile) {
        return cache_.remove(bundleFile)
    }
    
    MaterialMetadataBundle get(Path bundleFile) {
        return cache_.get(bundleFile)
    }
	
	int size() {
		return cache_.size()	
	}
	
    /**
     * If the MaterialMetadataBundle of the bundleFile is already on the cache, return its value.
     * If the MaterialMetadataBundle of the bundleFile is not yet on the cache, put it on the cache and then return its value.
     * 
     * @param bundleFile
     * @return
     */
    MaterialMetadataBundle retrieve(Path bundleFile) {
		if (!this.containsKey(bundleFile)) {
            this.put(bundleFile)
        }
        return this.get(bundleFile)
    }

    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"MaterialMetadataBundleCache\":{")
        int count = 0
        cache_.each { key, value ->
            if (count > 0) {
                sb.append(",")
            }
            sb.append('\"' + Helpers.escapeAsJsonText(key.toString()) + '\":')
            if (value != null) {
                sb.append(value.toJsonText())
            } else {
                sb.append('null')
            }
            count += 1
        }
        sb.append("}")
        sb.append("}")
        return sb.toString()
    }

    @Override
    String toString() {
        return this.toJsonText()
    }
}
