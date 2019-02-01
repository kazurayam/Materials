package com.kazurayam.materials

import java.nio.file.Path

import com.kazurayam.materials.model.TSuiteResult
import com.kazurayam.materials.model.repository.RepositoryRoot

/**
 * MaterialStorage is an external directory for Materials outside a Katalon Studio project.
 * You can backup your Materials from the project's Materials directory to the MaterialStorage.
 * Also you can restore a version of TSuiteExecution 
 * 
 * @author kazurayam
 */
interface MaterialStorage {
    
    /**
     * copy a set of Material files idenfified by a pair of a tSuiteName and a specific tSuiteTimestamp 
     * from the project's Materials folder (fromMR) into this Material Storage.
     * 
     * @param fromMR
     * @param tSuiteName
     * @param tSuiteTimestamp
     * @return number of Material files transfered
     * @throws IOException
     */
    int backup(MaterialRepository fromMR, TSuiteName tSuiteName,
        TSuiteTimestamp tSuiteTimestamp) throws IOException

    /**
     * copy a set of Material files identified by a pair of a tSuiteName and a SelectBy object
     * from the project's Materials folder (fromMR) into this Material Storage.
     * 
     * @param fromMR
     * @param tSuiteName
     * @param selectBy
     * @return number of Material files transfered
     * @throws IOException
     */
    int backup(MaterialRepository fromMR, TSuiteName tSuiteName,
        RetrievalBy selectBy) throws IOException
    
    /**
     * copy a set of Material files in the project's Materials folder (fromMR) beloging to the tSuiteName  
     * at the timing of invokation into this Material Storage
     * 
     * @param fromMR
     * @return
     * @throws IOException
     */
    int backup(MaterialRepository fromMR, TSuiteName tSuiteName) throws IOException
    
    /**
     * copy all Material files in the project's Materials folder (fromMR)
     * at the timing of invokation into this Material Storage
     *
     * @param fromMR
     * @return
     * @throws IOException
     */
    int backup(MaterialRepository fromMR) throws IOException
    
    /**
     * delete all of subdirectories and material files which belogns to 
     * the tSuiteName + tSuiteTimestamp in this Material Storage.
     * Will remove the tSuiteTimestamp directory, but retain the tSuiteName directory
     *  
     * @param tSuiteName
     * @param tSuiteTimestamp
     * @return number of Material files deleted. number of deleted directories are not included.
     */
    int clear(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) throws IOException
    
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
     * delete Material files in this Material Storage, which belongs to the tSuiteName and
     * with the TSuiteTimestamp specified by the argument
     * 
     * @param tSuiteName
     * @param tSuiteTimestamp
     * @return
     * @throws IOException
     */
    int expire(TSuiteName tSuiteName,
        TSuiteTimestamp tSuiteTimestamp) throws IOException
    
    /**
     * delete Material files in this Material Storage, which belongs to the tSuiteName and
     * with the TSuiteTimestamp grouped by the groupBy argument
     *  
     * @param tSuiteName
     * @param groupBy
     * @return
     * @throws IOException
     */
    int expire(TSuiteName tSuiteName,
        RetrievalBy selectBy) throws IOException

    /**
     * 
     * @return the baseDir Path
     */
    Path getBaseDir()
    
    /**
     * 
     * @param tSuiteName
     * @param tSuiteTimestamp
     * @return
     */
    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp)
    
    /**
     * 
     * @param tSuiteName
     * @return
     */
    List<TSuiteResult> getTSuiteResultList(TSuiteName tSuiteName)
    
    
    /**
     * 
     */
    List<TSuiteResult> getTSuiteResultList()
    
    /**
     * copy a set of Material files idenfified by a pair of a tSuiteName and a specific tSuiteTimestamp
     * from this Material Storage into the project's Materials folder (intoMR).
     *
     * @param intoMR
     * @param tSuiteName
     * @param tSuiteTimestamp
     * @return number of Material files transfered
     * @throws IOException
     */
    int restore(MaterialRepository intoMR, TSuiteName tSuiteName,
        TSuiteTimestamp tSuiteTimestamp) throws IOException
    
    /**
     * copy a set of Material files identified by a pair of a tSuiteName and a SelectBy object
     * from this Material Storage into the project's Materials folder (intoMR).
     *
     * @param intoMR
     * @param tSuiteName
     * @param selectBy
     * @return number of Material files transfered
     * @throws IOException
     */
    int restore(MaterialRepository intoMR, TSuiteName tSuiteName,
        RetrievalBy selectBy) throws IOException
    

}
