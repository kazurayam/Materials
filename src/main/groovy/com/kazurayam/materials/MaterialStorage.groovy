package com.kazurayam.materials

import com.kazurayam.materials.model.storage.SelectBy

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
     * @return
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
     * @return
     * @throws IOException
     */
    int backup(MaterialRepository fromMR, TSuiteName tSuiteName,
        SelectBy selectBy) throws IOException
    
    /**
     * copy a set of Material files idenfified by a pair of a tSuiteName and a specific tSuiteTimestamp 
     * from this Material Storage into the project's Materials folder (intoMR).
     * 
     * @param intoMR
     * @param tSuiteName
     * @param tSuiteTimestamp
     * @return
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
     * @return
     * @throws IOException
     */
    int restore(MaterialRepository intoMR, TSuiteName tSuiteName,
        SelectBy selectBy) throws IOException

}
