package com.kazurayam.materials

import java.time.DayOfWeek
import java.time.LocalDateTime

import com.kazurayam.materials.impl.MaterialRepositoryImpl
import com.kazurayam.materials.model.MaterialStorageImpl
import com.kazurayam.materials.model.repository.RepositoryRoot

/**
 * Strategy class that implements how to scan the MaterialRepository and the MaterialStorage for a List<TSuiteResults>.
 * Two strategies are implemented.
 * 1. before(TSuiteTimestamp)
 * 2. before(LocalDateTime base, int hour, int minute, in second)
 * 
 * The caller have chance to set the base with various values.
 * 1. base = LocalDateTime.now() means 'now'
 * 2. base = LocalDateTime.of(2018, 1, 31, 10, 50, 0) means 31,January 2018 10hours 50minutes 0seconds
 * 3. base = LocalDateTime.now().minusDays(1) means 'yesterday'
 * 4. base = LocalDateTime.now().minusWeeks(1).with(DayOfWeek.FRIDAY) means 'the last friday prior to today'
 * 5. base = LocalDateTime.now().minusMonths(1).withDayOfMonth(25) means '25 of the last months prior to today'
 * 
 * See the source of src/test/groovy/com/kazurayam/materials/RetrievalBySpec.groovy where
 * you can find a few sample codes how to use RetrievalBy.
 * 
 * @author kazurayam
 */
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
     * Criteria to retrieve a set of TSuiteResults before the given day + 
     * @param base
     * @param days
     * @return
     */
    static RetrievalBy before(LocalDateTime base, int hour, int minute, int second) {
        return new RetrievalByBefore(base, hour, minute, second)    
    }
    
    /**
     * 
     * @param base
     * @return
     */
    static RetrievalBy before(LocalDateTime base) {
        return new RetrievalByBefore(base, base.getHour(), base.getMinute(), base.getSecond())
    }

    /**
     * Criteria to retrieve a set of TSuiteResults before the last business day + hour + minute + second. 
     * select exclusively.
     * The last business day means MONDAY, TUESDAY, WEDNSDAY, THURSDAY and FRIDAY before today.
     * Today means now.
     * 
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    static RetrievalBy beforeLastBusinessDay(int hour, int minute, int second) {
        return beforeLastBusinessDay(LocalDateTime.now(), hour, minute, second)
    }
    
    static RetrievalBy beforeLastBusinessDay(LocalDateTime d, int hour, int minute, int second) {
        LocalDateTime shifted = lastBusinessDay(d)
        return new RetrievalByBefore(shifted, hour, minute, second)
    }
    
    static LocalDateTime lastBusinessDay(LocalDateTime d) {
        LocalDateTime prev = d.minusDays(1)
        if (d.getDayOfWeek() == DayOfWeek.SUNDAY ||
            d.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return lastBusinessDay(prev)
        } else {
            return prev
        }
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

    
    
    
    /**
     * Implementation of the RetrivalBy interface
     */
    static class RetrievalByBefore extends RetrievalBy {    
        private TSuiteTimestamp tSuiteTimestamp_
        /**
         * 
         * @param tSuiteTimestamp
         */
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
        
        SearchContext(MaterialRepository materialRepository, TSuiteName tSuiteName) {
            Objects.requireNonNull(materialRepository, "materialRepository must not be null")
            Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
            MaterialRepositoryImpl mri = (MaterialRepositoryImpl)materialRepository
            repositoryRoot_ = mri.getRepositoryRoot()
            tSuiteName_ = tSuiteName
        }
        
        SearchContext(MaterialStorage materialStorage, TSuiteName tSuiteName) {
            Objects.requireNonNull(materialStorage, "materialStorage must not be null")
            Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
            MaterialStorageImpl msi = (MaterialStorageImpl)materialStorage
            repositoryRoot_ = msi.getRepositoryRoot()
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
