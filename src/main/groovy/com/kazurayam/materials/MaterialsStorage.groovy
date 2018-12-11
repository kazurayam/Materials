package com.kazurayam.materials

interface MaterialsStorage extends Comparable<MaterialsStorage> {
    
    List<TSuiteExecutionRecord> list()
    
    List<TSuiteExecutionRecord> listOf(TSuiteName tSuiteName)
    
    /**
     * copy all Material files belonging to the tSuiteName from the repository to the storage.
     * 
     */
    int sync(MaterialRepository repository, TSuiteName tSuiteName, Boolean delete) throws MaterialsException
    
    /**
     * copy all Material files belonging to the tSuiteExecutionRecord from the repository to the storage.
     * if the delete option is set true, then the files are deleted from the repository.
     */
    int sync(MaterialRepository repository, TSuiteExecutionRecord tSuiteExecutionRecord, Boolean delete) throws MaterialsException
    
}
