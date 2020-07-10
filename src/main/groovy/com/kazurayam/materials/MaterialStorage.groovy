package com.kazurayam.materials

import java.nio.file.Path

/**
 * MaterialStorage is an external directory for Materials outside a Katalon Studio project.
 * You can backup your Materials from the project's Materials directory to the MaterialStorage.
 * Also you can restore a version of TSuiteExecution 
 * 
 * @author kazurayam
 */
interface MaterialStorage extends TSuiteResultTree {

    /**
     * scan the Storage directory to refresh memory so that it sync with the file system
     */
    void scan()


    /**
     * copy a set of Material files idenfified by a pair of a tSuiteName and a specific tSuiteTimestamp 
     * from the project's Materials folder (fromMR) into this Material Storage.
     * 
     * @param fromMR
     * @param tSuiteResultId
     * @param scan whether to call this.scan after backing up this TSuiteResultId
     * @return number of files transfered
     * @throws IOException
     */
    int backup(MaterialRepository fromMR, TSuiteResultId tSuiteResultId, boolean scan) throws IOException

    /**
     * copy a set of Material files identified by a pair of a tSuiteName and a RetrievalBy object
     * from the project's Materials folder (fromMR) into this Material Storage.
     * 
     * @param fromMR
     * @param tSuiteReultId
     * @param scan if true, fromMR.scan() will be called; otherwise no scan() will be called
     * @return number of files transfered
     * @throws IOException
     */
    int backup(MaterialRepository fromMR, List<TSuiteResultId> tSuiteResultIdList) throws IOException

    /**
     * copy all Material files in the project's Materials folder (fromMR)
     * at the timing of invokation into this Material Storage
     *
     * @param fromMR
     * @return number of files transfered 
     * @throws IOException
     */
    //int backup(MaterialRepository fromMR) throws IOException
    
    /**
     * delete all of subdirectories and material files which belogns to 
     * the tSuiteName + tSuiteTimestamp in this Material Storage.
     * Will remove the tSuiteTimestamp directory, but retain the tSuiteName directory
     *  
     * @param tSuiteName
     * @param tSuiteTimestamp
     * @return number of Material files deleted. number of deleted directories are not included.
     */
    int clear(TSuiteResultId tSuiteResultId) throws IOException

    int clear(List<TSuiteResultId> tSuiteResultIdList) throws IOException

    /**
     * delete all of subdirectories and material files which belong to
     * the tSuiteName in this Material Storage.
     * Will remove the tSuiteName directory.
     * 
     * @param tSuiteName
     * @return number of Material files deleted. number of deleted directories are not included.
     * @throws IOException
     */
    int clear(TSuiteName tSuiteName) throws IOException
    
    /**
     * delete all Material files in this Material Storage, keep the baseDir undeleted.
     * 
     * @return number of Matrial files deleted
     * @throws IOException
     */
    void empty() throws IOException

    /**
     * 
     * @return the baseDir Path
     */
    Path getBaseDir()
    
    /**
     * 
     * @return sum of the size of Material files contained in the Storage 
     */
    long getSize()
    
    /**
     * 
     * @param tSuiteName
     * @return
     */
    Set<Path> getSetOfMaterialPathRelativeToTSuiteName(TSuiteName tSuiteName,
                                                       TExecutionProfile tExecutionProfile)

    /**
     * list the current status of the Storage
     * 
     * @param options
     * @return a string containing lines which includes TSuiteName, TSuiteTimestamp, sum of file size
     */
    void status(Writer output, Map<String, Object> options)


    /**
     * Calculate the total file size in the Storage to check if it exceeds the target size in bytes.
     * All of TSuiteNames are inspected as possible deletion target.
     * If exceeding, clear older TSuiteResults to reduce the total less than the target.
     * 
     * e.g., MaterialStorage.reduceSizeTo(20 * 1000 * 1000 * 1000); // 20 giga-bytes
     * 
     * @param targetBytes want to make the Storage size less than this
     * @return the size of the Storage after reduction = sum of remaining Material files
     */
    long reduce(long targetBytes) throws IOException
    
    /**
     * copy a set of Material files idenfified by a pair of a tSuiteName and a specific tSuiteTimestamp
     * from this Material Storage into the project's Materials folder (intoMR).
     *
     * @param intoMR
     * @param tSuiteResultId
     * @param scan wither to call scan() after restoring this TSuiteResultId
     * @return RestoreResult containing a TSuiteResult and int count of copied files
     * @throws IOException
     */
    RestoreResult restore(MaterialRepository intoMR, TSuiteResultId tSuiteResultId, boolean scan) throws IOException


    /**
     * copy a set of Material files identified by a pair of a tSuiteName and a RetrievalBy object
     * from this Material Storage into the project's Materials folder (intoMR).
     *
     * @param intoMR
     * @param tSuiteName
     * @param retrievalBy
     * @return RestoreResult containing a TSuiteResult and int count of Material files transfered
     * @throws IOException
     */
    List<RestoreResult> restore(MaterialRepository intoMR, List<TSuiteResultId> tSuiteResultIdList) throws IOException
    

	/**
	 * 
	 */
    RestoreResult retrievingRestoreUnaryExclusive(MaterialRepository intoMR,
                                                  TSuiteName tSuiteName,
                                                  TExecutionProfile tExecutionProfile,
                                                  RetrievalBy retrievalBy) throws IOException
    
	/**
	 * 
	 */
	RestoreResult retrievingRestoreUnaryInclusive(MaterialRepository intoMR,
                                                  TSuiteName tSuiteName,
                                                  TExecutionProfile tExecutionProfile,
                                                  RetrievalBy retrievalBy) throws IOException



    void setVisualTestingLogger(VisualTestingLogger vtLogger)
    String toJsonText()
}
