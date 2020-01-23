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
	
	private Map<String, MaterialMetadataBundle> cache_
	
	private VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
	
	MaterialMetadataBundleCache() {
		cache_ = new HashMap<String, MaterialMetadataBundle>()
	}
	
	void setVisualTestingLogger(VisualTestingLogger vtLogger) {
		this.vtLogger_ = vtLogger
	}
	
	MaterialMetadataBundle get(Path bundleFile) {
		String pathStr = bundleFile.toString()
		if (!cache_.containsKey(pathStr)) {
			try {
				cache_.put(pathStr, MaterialMetadataBundle.deserialize(bundleFile))
			} catch (Exception e) {
				String msg = this.class.getSimpleName() + "#get failed to deserialize an instance of MaterialMetadataBundle from ${bundleFile}"
				logger_.warn(msg)
				//vtLogger_.failed(msg)
				return null
			}
		}
		return cache_.get(pathStr)
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
			sb.append('\"' + key + '\":')
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
