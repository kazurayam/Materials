package com.kazurayam.materials

/**
 * An instance of MaterialStorageRestoreResult object is returned by 
 * MaterialStrage#restoreUnary(...) method.
 * 
 * An instance of MaterialStorageRestoreResult object contains information
 * what TSuiteReult was restored by the method invokation and
 * how many Materials ware copied from the Stroage dir to the Materials dir.
 */
interface MaterialStorageRestoreResult {

	TSuiteResult getTSuiteResult()
	
	int getCount()
		
}
