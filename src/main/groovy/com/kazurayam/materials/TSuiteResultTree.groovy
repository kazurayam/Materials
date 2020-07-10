package com.kazurayam.materials

/**
 * This interface defines a set of method for a collection of
 * TSuiteResult objects how to add/retrieve members.
 *
 * * This interface is supposed to be implemented by
 *  * - com.kazurayam.materials.repository.RepositoryRoot
 *
 * This interface is supposed to be extended by
 * - com.kazurayam.materials.MaterialRepository
 * - com.kazurayam.materials.MaterialStorage
 *
 */
interface TSuiteResultTree extends VTLoggerEnabled {

    void addTSuiteResult(TSuiteResult tSuiteResult)

    boolean hasTSuiteResult(TSuiteResult given)

    List<TSuiteName> getTSuiteNameList()

    // methods that return TSuiteResult

    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TExecutionProfile tExecutionProfile, TSuiteTimestamp tSuiteTimestamp)

    TSuiteResult getTSuiteResult(TSuiteResultId tSuiteResultId)


    // methods that returns List<TSuiteResultId>

    List<TSuiteResultId> getTSuiteResultIdList(TSuiteName tSuiteName, TExecutionProfile tExecutionProfile)

    List<TSuiteResultId> getTSuiteResultIdList()



    // methods that returns List<TSuiteResult>

    List<TSuiteResult> getTSuiteResultList()

    List<TSuiteResult> getTSuiteResultList(TSuiteName tSuiteName)

    List<TSuiteResult> getTSuiteResultList(TSuiteName tSuiteName, TExecutionProfile tExecutionProfile)

    List<TSuiteResult> getTSuiteResultList(List<TSuiteResultId> tSuiteResultIdList)



}