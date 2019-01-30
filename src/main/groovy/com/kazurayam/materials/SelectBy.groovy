package com.kazurayam.materials

import com.kazurayam.materials.model.repository.RepositoryRoot
import com.kazurayam.materials.model.TSuiteResult

abstract class SelectBy {
    
    /**
     * 
     * @param tSuiteTimestamp
     * @return
     */
    static SelectBy before(TSuiteTimestamp tSuiteTimestamp) {
        return new SelectByBefore(tSuiteTimestamp)
    }

    /*
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
    static class SelectByBefore extends SelectBy {
        
        private TSuiteTimestamp tSuiteTimestamp_
        
        SelectByBefore(TSuiteTimestamp tst) {
            this.tSuiteTimestamp_ = tst
        }
        
        @Override
        List<TSuiteResult> findTSuiteResults(SearchContext context) {
            Objects.requireNonNull(context, "context must not be null")
            RepositoryRoot rr = context.getRepositoryRoot()
            TSuiteName tsn = context.getTSuiteName()
            return rr.getTSuiteResultsBeforeExclusive(tsn, tSuiteTimestamp_)
        }
        
        @Override
        TSuiteResult findTSuiteResult(SearchContext context) {
            Objects.requireNonNull(context, "context must not be null")
            RepositoryRoot rr = context.getRepositoryRoot()
            TSuiteName tsn = context.getTSuiteName()
            List<TSuiteResult> results = rr.getTSuiteResultsBeforeExclusive(tsn, tSuiteTimestamp_)
            if (results.size() > 0) {
                return results[0]
            } else {
                return TSuiteResult.NULL
            }
        }
    }

    /*
    static class SelectByBeforeHours extends SelectBy {}
     */
    /*
    static class SelectByBeforeMinutes extends SelectBy {}
     */
    
    
    
    
    
    /**
     * 
     */
    static class SearchContext {
        
        private RepositoryRoot repositoryRoot_
        private TSuiteName tSuiteName_
        
        private SearchContext(RepositoryRoot repositoryRoot, TSuiteName tSuiteName) {
            Objects.requireNonNull(repositoryRoot, "repositoryRoot must not be null")
            repositoryRoot_ = repositoryRoot   
            tSuiteName_ = tSuiteName
        }
        
        RepositoryRoot getRepositoryRoot() {
            return repositoryRoot_
        }
        
        TSuiteName getTSuiteName() {
            return tSuiteName_
        }
    }

}
