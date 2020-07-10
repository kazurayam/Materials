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
interface TSuiteResultTree {

    void addTSuiteResult(TSuiteResult tSuiteResult)

    boolean hasTSuiteResult(TSuiteResult given)

    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TExecutionProfile tExecutionProfile, TSuiteTimestamp tSuiteTimestamp)

    TSuiteResult getTSuiteResult(TSuiteResultId tSuiteResultId)

    //List<TSuiteResult> getTSuiteResultList(
    //        TSuiteName tSuiteName, TExecutionProfile tExecutionProfile)

}