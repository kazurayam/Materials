package com.kazurayam.materials.repository

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.view.ExecutionPropertiesWrapper
import com.kazurayam.materials.view.JUnitReportWrapper

import groovy.json.JsonSlurper

final class RepositoryRoot {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryRoot.class)

    private Path baseDir_
    private List<TSuiteResult> tSuiteResults_

    RepositoryRoot(Path baseDir) {
        Objects.requireNonNull(baseDir)
        Helpers.ensureDirs(baseDir)
        baseDir_ = baseDir
        tSuiteResults_ = new ArrayList<TSuiteResult>()
    }

    // ------------------- getter -------------------------------------------
    Path getBaseDir() {
        return baseDir_
    }

    // ------------------- child nodes operation ----------------------------
    void addTSuiteResult(TSuiteResult tSuiteResult) {
        Objects.requireNonNull(tSuiteResult)
        boolean found = false
        for (TSuiteResult tsr : tSuiteResults_) {
            if (tsr == tSuiteResult) {
                found = true
            }
        }
        if (!found) {
            tSuiteResults_.add(tSuiteResult)
            Collections.sort(tSuiteResults_)
        }
    }

    TCaseResult getTCaseResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp, TCaseName tCaseName) {
        TCaseResult tCaseResult = null
        TSuiteResult tsr = this.getTSuiteResult(tSuiteName, tSuiteTimestamp)
        if (tsr != null) {
            tCaseResult = tsr.getTCaseResult(tCaseName)
        }
        return tCaseResult  
    }
    
    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        Objects.requireNonNull(tSuiteName)
        Objects.requireNonNull(tSuiteTimestamp)
        for (TSuiteResult tsr : tSuiteResults_) {
            if (tsr.getId().getTSuiteName() == tSuiteName &&
                tsr.getId().getTSuiteTimestamp() == tSuiteTimestamp) {
                return tsr
            }
        }
        return null
    }

    /**
     * 
     * @param tSuiteName
     * @return unmodifiable List<TSuiteResult> 
     */
    List<TSuiteResult> getTSuiteResults(TSuiteName tSuiteName) {
        Objects.requireNonNull(tSuiteName)
        List<TSuiteResult> result = new ArrayList<TSuiteResult>()
        logger_.debug("#getTSuiteResults tSuiteResults_.size()=${tSuiteResults_.size()}")
        for (TSuiteResult tsr : tSuiteResults_) {
            logger_.debug("#getTSuiteResults tsr.getTSuiteName()=${tsr.getId().getTSuiteName()}")
            if (tSuiteName.equals(tsr.getId().getTSuiteName())) {
                result.add(tsr)
            }
        }
        return Collections.unmodifiableList(result)
    }
    
    
    /**
     * 
     * @return List of all TSuiteResult in the Repository, the List is unmodifiable
     */
    List<TSuiteResult> getTSuiteResults() {
        List<TSuiteResult> result = new ArrayList<TSuiteResult>()
        for (TSuiteResult tsr : tSuiteResults_) {
            result.add(tsr)
        }
        return Collections.unmodifiableList(result)
    }
    
    
    /**
     * returns a List of TSuiteResult which has the given TSuiteName, the Timestamp before the given 2nd arg.
     * The TSuiteResult with a timestamp exactly equal to 'before' will be excluded.
     * The entries returned are sorted in descending order of the timestamp.
     * Therefore the latest entry comes at [0].
     * 
     * @param tSuiteName
     * @param timestamp
     * @return
     */
    List<TSuiteResult> getTSuiteResultsBeforeExclusive(TSuiteName tSuiteName, TSuiteTimestamp timestamp) {
        Objects.requireNonNull(tSuiteName, "argument \'tSuiteName\' must not be null")
        Objects.requireNonNull(timestamp, "argument \'timestamp\' must not be null")
        List<TSuiteResult> result = new ArrayList<TSuiteResult>()
        for (TSuiteResult tsr : tSuiteResults_) {
            if (tSuiteName.equals(tsr.getId().getTSuiteName())) {
                if (TSuiteResultComparator_.compare(tsr, TSuiteResult.newInstance(tSuiteName, timestamp)) > 0) {
                    // use < to select entries exclusively
                    result.add(tsr)
                }
            }
        }
        Collections.sort(result, TSuiteResultComparator_)
        return Collections.unmodifiableList(result)
    }
	
	/**
	 * returns a List of TSuiteResult which has the given 1st argument, and the Timestamp exactly at or before the 2nd argument.
	 * The TSuiteResult with a timestamp which is exactly equal to 'timestamp' will be included.
	 * 
	 * @param tSuiteName
	 * @param before
	 * @return
	 */
	List<TSuiteResult> getTSuiteResultsBeforeInclusive(TSuiteName tSuiteName, TSuiteTimestamp timestamp) {
		Objects.requireNonNull(tSuiteName, "argument \'tSuteName\' must not be null")
		Objects.requireNonNull(timestamp, "argument \'before\' must not be null")
		List<TSuiteResult> result = new ArrayList<TSuiteResult>()
		for (TSuiteResult tsr : tSuiteResults_) {
			if (tSuiteName.equals(tsr.getId().getTSuiteName())) {
				if (TSuiteResultComparator_.compare(tsr, TSuiteResult.newInstance(tSuiteName, timestamp)) >= 0) {
					//                                                                        Inclusive!  ^^^^
					result.add(tsr)
				}
			}
		}
		Collections.sort(result, TSuiteResultComparator_)
		return Collections.unmodifiableList(result)
	}
    
    
    
    /**
     * returns the sorted list of TSuiteResults ordered by
     * (1) TSuiteName in natural order
     * (2) TSuiteTimestamp in the reverse order
     *
     * @return
     */
    List<TSuiteResult> getSortedTSuiteResults() {
        List<TSuiteResult> sorted = tSuiteResults_
        Collections.sort(sorted, TSuiteResultComparator_)
        return Collections.unmodifiableList(sorted)
    }
    
    /**
     * Comparator for TSuiteResult in the natural order : ascending order of TSuiteName + TSuiteTimestamp
     */
    private static Comparator<TSuiteResult> TSuiteResultComparator_ = 
        new Comparator<TSuiteResult>() {
            @Override
            public int compare(TSuiteResult o1, TSuiteResult o2) {
                int v = o1.getId().getTSuiteName().compareTo(o2.getId().getTSuiteName())
                if (v < 0) {
                    return v // natural order of TSuiteName
                } else if (v == 0) {
                    LocalDateTime ldt1 = o1.getId().getTSuiteTimestamp().getValue()
                    LocalDateTime ldt2 = o2.getId().getTSuiteTimestamp().getValue()
                    return ldt1.compareTo(ldt2) * -1  // reverse order of TSuiteTimestamp
                } else {
                    return v  // natural order of TSuiteName
                }
            }
        }

    /**
     * 
     * @return
     */
    TSuiteResult getLatestModifiedTSuiteResult() {
        LocalDateTime lastModified = LocalDateTime.MIN
        TSuiteResult result = null
        List<TSuiteResult> tSuiteResults = this.getTSuiteResults()
        for (TSuiteResult tsr : tSuiteResults) {
            if (tsr.getLastModified() > lastModified) {
                result = tsr
                lastModified = tsr.getLastModified()
            }
        }
        return result
    }

    /**
     * 
     * @return
     */
    List<Material> getMaterials() {
        List<Material> list = new ArrayList<Material>()
        for (TSuiteResult tsr : this.getSortedTSuiteResults()) {
            List<Material> mates = tsr.getMaterialList()
            for (Material mate : mates) {
                list.add(mate)
            }
        }
        return Collections.unmodifiableList(list)
    }

    /**
     *
     * @param tSuiteName
     * @param tSuiteTimestamp
     * @return
     */
    List<Material> getMaterials(TSuiteName tSuiteName) {
        List<Material> list = new ArrayList<Material>()
        for (TSuiteResult tsr: this.getSortedTSuiteResults()) {
            if (tsr.getId().getTSuiteName().equals(tSuiteName)) {
                List<Material> mates =  tsr.getMaterialList()
                for (Material mate : mates) {
                    list.add(mate)
                }
            }
        }
        return Collections.unmodifiableList(list)
    }


    /**
     * 
     * @param tSuiteName
     * @param tSuiteTimestamp
     * @return
     */
    List<Material> getMaterials(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        List<Material> list = new ArrayList<Material>()
        for (TSuiteResult tsr: this.getSortedTSuiteResults()) {
            if (tsr.getId().getTSuiteName().equals(tSuiteName) &&
                tsr.getId().getTSuiteTimestamp().equals(tSuiteTimestamp)) {
                    List<Material> mates =  tsr.getMaterialList()
                    for (Material mate : mates) {
                        list.add(mate)
                    }
            }
        }
        return Collections.unmodifiableList(list)
    }

    Material getMaterial(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp, TCaseName tCaseName) {
        List<Material> materials = this.getMaterials(tSuiteName, tSuiteTimestamp)
        if (materials.size() > 0) {
            for (Material material : materials) {
                if (material.getTCaseName().equals(tCaseName)) {
                    return material
                }
            }
            return null
        } else {
            return null
        }
    }
    
    // -------------- overriding java.lang.Object methods ---------------------
    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof RepositoryRoot)) { return false }
        RepositoryRoot other = (RepositoryRoot)obj
        List<TSuiteResult> ownList = this.getTSuiteResults()
        List<TSuiteResult> otherList = other.getTSuiteResults()
        logger_.debug("ownList=${ownList.toString()}")
        logger_.debug("otherList=${otherList.toString()}")
        if (ownList.size() == otherList.size()) {
            for (int i; i < ownList.size(); i++) {
                if ( ! ownList.get(i).equals(otherList.get(i)) ) {
                    return false
                }
            }
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        return com.kazurayam.materials.repository.RepositoryRoot.class.hashCode()
    }

    @Override
    String toString() {
        return this.toJsonText()
    }

    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"RepositoryRoot":{')
        sb.append('"tSuiteResults":[')
        def count = 0
        for (TSuiteResult tsr : tSuiteResults_) {
            if (count > 0) { sb.append(',') }
            count += 1
            sb.append(tsr.toJsonText())
        }
        sb.append('],')
        sb.append('"baseDir":"' + Helpers.escapeAsJsonText(this.getBaseDir().toString()) + '"')
        sb.append('}}')
        return sb.toString()
    }

    
}
