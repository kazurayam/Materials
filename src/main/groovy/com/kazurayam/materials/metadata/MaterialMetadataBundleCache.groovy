package com.kazurayam.materials.metadata

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl

/**
 * creates a in-memory cache of MaterialmetadataBundle instances for better performance
 */
final class MaterialMetadataBundleCache {
	
	static Logger logger_ = LoggerFactory.getLogger(
		MaterialMetadataBundleCache.class)

	static final String classShortName = Helpers.getClassShortName(
		MaterialMetadataBundleCache.class)
	
	private Map<Path, MaterialMetadataBundle> cache_
	private VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
	
	MaterialMetadataBundleCache() {
		cache_ = new HashMap<Path, MaterialMetadataBundle>()
	}
	
	void setVisualTestingLogger(VisualTestingLogger vtLogger) {
		this.vtLogger_ = vtLogger
	}
	
	MaterialMetadataBundle get(Path bundleFile) {
		if (cache_.containsKey(bundleFile)) {
			return cache_.get(bundleFile)
		} else {
			MaterialMetadataBundle bundle
			try {
				bundle = MaterialMetadataBundle.deserialize(bundleFile)
				cache_.put(bundleFile, bundle)
			} catch (Exception e) {
				String msg = this.class.getSimpleName() + "#get failed to deserialize an instance of MaterialMetadataBundle from ${bundleFile}"
				logger_.warn(msg)
				//vtLogger_.failed(msg)
				return null
			}
			return bundle
		}
	}
}
