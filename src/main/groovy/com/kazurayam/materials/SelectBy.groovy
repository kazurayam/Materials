package com.kazurayam.materials

import com.kazurayam.materials.model.repository.RepositoryRoot
import com.kazurayam.materials.model.TSuiteResult

abstract class SelectBy {
    
    static SelectBy tSuiteTimestamp(TSuiteTimestamp tSuiteTimestamp) {
        return new SelectByTSuiteTimestamp(tSuiteTimestamp)
    }

    static SelectBy latest() {
        return new SelectByLatest()
    }

    static SelectBy beforeHours(int beforeHours) {
        return new SelectByBeforeHours(beforeHours)
    }

    static SelectBy beforeMinutes(int beforeMinutes) {
        return new SelectByBeforeMinutes(beforeMinutes)
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


    @Override
    boolean equals(Object other) {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }

    @Override
    int hashCode() {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }

    // ------------------------------------------------------------
    static class SearchContext {
        private SerachContext() {}
        SearchContext build(RepositoryRoot rr, TSuiteResult currentTSuiteResult) {
            
        }
    }

    /**
     *
     */
    static class SelectByTSuiteTimestamp extends SelectBy {}

    /**
     *
     */
    static class SelectByLatest extends SelectBy {}

    /**
     *
     */
    static class SelectByBeforeHours extends SelectBy {}

    /**
     *
     */
    static class SelectByBeforeMinutes extends SelectBy {}
}
