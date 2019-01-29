package com.kazurayam.materials

import com.kazurayam.materials.model.repository.RepositoryRoot
import com.kazurayam.materials.model.TSuiteResult

abstract class SelectBy {
    
    /**
     * 
     * @param tSuiteTimestamp
     * @return
     */
    static SelectBy tSuiteTimestampBefore(TSuiteTimestamp tSuiteTimestamp) {
        return new SelectByTSuiteTimestampBefore(tSuiteTimestamp)
    }

    /*
    static SelectBy latest() {
        return new SelectByLatest()
    }
    static SelectBy beforeHours(int beforeHours) {
        return new SelectByBeforeHours(beforeHours)
    }
    static SelectBy beforeMinutes(int beforeMinutes) {
        return new SelectByBeforeMinutes(beforeMinutes)
    }
    */

    /**
     *
     * @param mr
     * @return
     */
    abstract List<TSuiteResult> findTSuiteResults(SearchContext context)

    /**
     *
     * @param mr
     * @return
     */
    abstract TSuiteResult findTSuiteResult(SearchContext context)


    @Override
    boolean equals(Object other) {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }

    @Override
    int hashCode() {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }

    /**
     *
     */
    static class SelectByTSuiteTimestampBefore extends SelectBy {
        
        private TSuiteTimestamp tSuiteTimestamp_
        
        SelectByTSuiteTimestampBefore(TSuiteTimestamp tst) {
            this.tSuiteTimestamp_ = tst
        }
        
        @Override
        List<TSuiteResult> findTSuiteResults(SearchContext context) {
            RepositoryRoot rr = context.getRepositoryRoot()
            TSuiteName tsn = context.getTSuiteName()
            return rr.getTSuiteResultsBeforeExclusive(tsn, tSuiteTimestamp_)
        }
        
        @Override
        TSuiteResult findTSuiteResult(SearchContext context) {
            RepositoryRoot rr = context.getRepositoryRoot()
            TSuiteName tsn = context.getTSuiteName()
            List<TSuiteResult> tSuiteResults = rr.getTSuiteResultsBeforeExclusive(tsn, tSuiteTimestamp_)
            if (tSuiteResults.size() > 0) {
                return tSuiteResults[0]
            } else {
                return TSuiteResult.NULL
            }
        }
    }

    /*
    static class SelectByLatest extends SelectBy {}
     */
    /*
    static class SelectByBeforeHours extends SelectBy {}
     */
    /*
    static class SelectByBeforeMinutes extends SelectBy {}
     */
    
    // ------------------------------------------------------------
    static class SearchContext {
        private RepositoryRoot rr_
        private TSuiteName tsn_
        private SearchContext(RepositoryRoot rr, TSuiteName tsn) {
            rr_ = rr   
            tsn_ = tsn
        }
        RepositoryRoot getRepositoryRoot() {
            return rr_
        }
        TSuiteName getTSuiteName() {
            return tsn_
        }
    }

}
