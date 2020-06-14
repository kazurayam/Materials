package com.kazurayam.materials

import java.time.DayOfWeek
import java.time.LocalDateTime

import com.kazurayam.materials.impl.MaterialRepositoryImpl
import com.kazurayam.materials.impl.MaterialStorageImpl
import com.kazurayam.materials.repository.RepositoryRoot

/**
 * Strategy class that implements how to scan the MaterialRepository and
 * the MaterialStorage for a List<TSuiteResults>.
 *
 * Caller need to instantiate an instance of RetrievalBy class specifying
 * 1. TSuiteName object
 * 2. TExecutionProfile object
 *
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
    
    static RetrievalBy by(TSuiteTimestamp tSuiteTimestamp) {
        return new RetrievalByImpl(tSuiteTimestamp)
    }

    static RetrievalBy by(LocalDateTime base, int hour, int minute, int second) {
        return new RetrievalByImpl(base, hour, minute, second)    
    }
    
    static RetrievalBy by(LocalDateTime base) {
        return new RetrievalByImpl(base, base.getHour(), base.getMinute(), base.getSecond())
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
    abstract TSuiteResult findTSuiteResultBeforeExclusive(SearchContext context)

    
	/**
	 *
	 * @param mr
	 * @return
	 */
	abstract List<TSuiteResult> findTSuiteResultsBeforeExclusive(SearchContext context)

	
	/**
	 *
	 * @param mr
	 * @return
	 */
	abstract TSuiteResult findTSuiteResultBeforeInclusive(SearchContext context)

	
	/**
	 *
	 * @param mr
	 * @return
	 */
	abstract List<TSuiteResult> findTSuiteResultsBeforeInclusive(SearchContext context)
	
	
    /**
     * Implementation of the RetrivalBy interface
     */
    static class RetrievalByImpl extends RetrievalBy {
		  
        private TSuiteTimestamp tSuiteTimestamp_
        
		/**
         * 
         * @param tSuiteTimestamp
         */
        RetrievalByImpl(TSuiteTimestamp tSuiteTimestamp) {
            Objects.requireNonNull(tSuiteTimestamp, "tSuiteTimestamp must not be null")
            this.tSuiteTimestamp_ = tSuiteTimestamp
        }
        
        RetrievalByImpl(LocalDateTime base, int hour, int minute, int second) {
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
        List<TSuiteResult> findTSuiteResultsBeforeExclusive(SearchContext context) {
            Objects.requireNonNull(context, "context must not be null")
            RepositoryRoot rr = context.getRepositoryRoot()
            TSuiteName tsn = context.getTSuiteName()
            return rr.getTSuiteResultsBeforeExclusive(tsn, tSuiteTimestamp_)
        }
        
        @Override
        TSuiteResult findTSuiteResultBeforeExclusive(SearchContext context) {
            Objects.requireNonNull(context, "context must not be null")
            RepositoryRoot rr = context.getRepositoryRoot()
            TSuiteName tsn = context.getTSuiteName()
            TExecutionProfile tep = context.getTExecutionProfile()
            List<TSuiteResult> results = rr.getTSuiteResultsBeforeExclusive(tsn, tep, tSuiteTimestamp_)
			
			// for DEBUG
			/*
			StringBuilder sb = new StringBuilder()
			sb.append("RetrievalBy#findTSuiteResult" +
				" rr.getTSuiteResultsBeforeExclusive(tsn, tSuiteTimestamp_).size()=${results.size()}\n")
			sb.append("RetrievalBy#findTSuiteResult" +
				" results=\n${results}\n")
			sb.append("RetrievalBy#findTSuiteResult" +
				" results[0]=\n${results[0]}\n")
			throw new IllegalStateException(sb.toString())
			*/
			
            if (results.size() > 0) {
                return results[0]
            } else {
                return TSuiteResult.NULL
            }
        }
		
		@Override
		List<TSuiteResult> findTSuiteResultsBeforeInclusive(SearchContext context) {
			Objects.requireNonNull(context, "context must not be null")
			RepositoryRoot rr = context.getRepositoryRoot()
			TSuiteName tsn = context.getTSuiteName()
            TExecutionProfile tep = context.getTExecutionProfile()
			return rr.getTSuiteResultsBeforeInclusive(tsn, tep, tSuiteTimestamp_)
		}
		
		@Override
		TSuiteResult findTSuiteResultBeforeInclusive(SearchContext context) {
			Objects.requireNonNull(context, "context must not be null")
			RepositoryRoot rr = context.getRepositoryRoot()
			TSuiteName tsn = context.getTSuiteName()
            TExecutionProfile tep = context.getTExecutionProfile()
			List<TSuiteResult> results = rr.getTSuiteResultsBeforeInclusive(tsn, tep, tSuiteTimestamp_)
			if (results.size() > 0) {
				return results[0]
			} else {
				return TSuiteResult.NULL
			}
		}
    }
    
	
	
	
    /**
     * One more layer of abstraction.
     * We want to see a MaterialRepository object and a MaterialStorage object
     * just the same way in terms of content search.
     */
    static class SearchContext {
        
        private RepositoryRoot repositoryRoot_
        private TSuiteName tSuiteName_
        private TExecutionProfile tExecutionProfile_
        
        SearchContext(MaterialRepository materialRepository,
                      TSuiteName tSuiteName,
                      TExecutionProfile tExecutionProfile) {
            Objects.requireNonNull(materialRepository, "materialRepository must not be null")
            Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
            Objects.requireNonNull(tExecutionProfile, "tExecutionProfile must not be null")
            MaterialRepositoryImpl mri = (MaterialRepositoryImpl)materialRepository
            repositoryRoot_ = mri.getRepositoryRoot()
            tSuiteName_ = tSuiteName
            tExecutionProfile_ = tExecutionProfile
        }
        
        SearchContext(MaterialStorage materialStorage,
                      TSuiteName tSuiteName,
                      TExecutionProfile tExecutionProfile) {
            Objects.requireNonNull(materialStorage, "materialStorage must not be null")
            Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
            Objects.requireNonNull(tExecutionProfile, "tExecutionProfile must not be null")
            MaterialStorageImpl msi = (MaterialStorageImpl)materialStorage
            repositoryRoot_ = msi.getRepositoryRoot()
            tSuiteName_ = tSuiteName
            tExecutionProfile_ = tExecutionProfile
        }
        
        RepositoryRoot getRepositoryRoot() {
            return repositoryRoot_
        }
        
        TSuiteName getTSuiteName() {
            return tSuiteName_
        }

        TExecutionProfile getTExecutionProfile() {
            return tExecutionProfile_
        }
        
        String toJsonText() {
            StringBuilder sb = new StringBuilder()
            sb.append("{")
            sb.append("\"repositoryRoot\":")
            sb.append(repositoryRoot_.toJsonText())
            sb.append(",")
            sb.append("\tSuiteName\":")
            sb.append(tSuiteName_.toJsonText())
            sb.append("}")
            return sb.toString()
        }
        
        @Override
        String toString() {
            return this.toJsonText()
        }
    }

}
