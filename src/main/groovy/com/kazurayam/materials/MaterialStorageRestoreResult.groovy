package com.kazurayam.materials


import com.kazurayam.materials.impl.MaterialStorageRestoreResultImpl
import com.kazurayam.materials.impl.TSuiteResultImpl

/**
 * An instance of MaterialStorageRestoreResult object is returned by 
 * MaterialStrage#restoreUnary(...) method.
 * 
 * An instance of MaterialStorageRestoreResult object contains information
 * what TSuiteReult was restored by the method invokation and
 * how many Materials ware copied from the Stroage dir to the Materials dir.
 */
interface MaterialStorageRestoreResult {

	static MaterialStorageRestoreResult NULL = new MaterialStorageRestoreResultImpl(new TSuiteResultImpl(TSuiteName.NULL, TSuiteTimestamp.NULL), 0)
	
	TSuiteResult getTSuiteResult()
	
	int getCount()
		
}
