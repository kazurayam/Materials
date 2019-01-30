package com.kazurayam.materials

import java.time.LocalDateTime
import com.kazurayam.materials.model.repository.RepositoryRoot
import com.kazurayam.materials.model.TSuiteResult

abstract class RetrievalBy {
    
    /**
     * identify TSuiteResult object(s) before the given tSuiteTimestamp.
     * 
     * @param tSuiteTimestamp
     * @return
     */
    static RetrievalBy before(TSuiteTimestamp tSuiteTimestamp) {
        return new RetrievalByBefore(tSuiteTimestamp)
    }

    /**
     * 
     * @param base
     * @param days
     * @return
     */
    static RetrievalBy before(LocalDateTime base, int hour, int minute, int second) {
        return new RetrievalByBefore(base, hour, minute, second)    
    }

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


    /*
    @Override
    boolean equals(Object other) {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    */

    /*
    @Override
    int hashCode() {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    */

    /**
     *
     */
    static class RetrievalByBefore extends RetrievalBy {
        
        private TSuiteTimestamp tSuiteTimestamp_
        
        RetrievalByBefore(TSuiteTimestamp tSuiteTimestamp) {
            Objects.requireNonNull(tSuiteTimestamp, "tSuiteTimestamp must not be null")
            this.tSuiteTimestamp_ = tSuiteTimestamp
        }
        
        RetrievalByBefore(LocalDateTime base, int hour, int minute, int second) {
            Objects.requireNonNull(base, "base must not be null")
            if (hour < 0 || 23 < hour) {
                throw new IllegalArgumentException("hour(${hour}) must be in the range of 0..23")
            }
            if (minute < 0 || 59 < minute) {
                throw new IllegalArgumentException("minute(${minute}) must be in the range of 0..59")
            }
            if (second < 0 || 59 < second) {
                throw new IllegalArgumentException("second(${second}) must be in the range of 0..59")
            }
            LocalDateTime d = base.withHour(hour).withMinute(minute).withSecond(second)
            this.tSuiteTimestamp_ = TSuiteTimestamp.newInstance(d)
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
