package com.kazurayam.materials

import com.kazurayam.materials.model.storage.GroupBy
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
        SelectBy selectBy) throws IOException
    
    
    /**
     * delete material files which belogns to the tSuiteName + tSuiteTimestamp in this Material Storage
     *  
     * @param tSuiteName
     * @param tSuiteTimestamp
     * @return
     */
    int clear(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) throws IOException
    
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
        GroupBy groupBy) throws IOException
    
    /**
     * @return List of Material objects contained in the current MaterialStorage
     */
    List<Material> getMaterials()

    /**
     * @param tSuiteName
     * @param tSuiteTimestamp
     * @return List of Material objects belonging to the tSuiteName + tSuiteTimestamp 
     * contained in the current MaterialStorage
     */
    List<Material> getMaterials(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp)

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
        SelectBy selectBy) throws IOException
    

}
