package com.kazurayam.materials.impl

import com.kazurayam.materials.MaterialStorageRestoreResult
import com.kazurayam.materials.TSuiteResult

class MaterialStorageRestoreResultImpl implements MaterialStorageRestoreResult {
	
	private TSuiteResult tSuiteResult_
	private int count_
	
	MaterialStorageRestoreResultImpl(TSuiteResult tSuiteResult, int count) {
		this.tSuiteResult_ = tSuiteResult
		this.count_ = count
	}
	
	TSuiteResult getTSuiteResult() {
		return tSuiteResult_
	}
	
	int getCount() {
		return count_
	}
}
