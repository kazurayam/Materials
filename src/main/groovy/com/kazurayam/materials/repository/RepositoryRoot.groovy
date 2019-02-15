package com.kazurayam.materials.repository

import java.nio.file.Path
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp

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
     * List of TSuiteResult which has the given TSuiteName, the Timestamp before the given 2nd arg.
     * Excluding the timestamp of {before`
     * The entries returned are sorted in descending order or the timestamp. Therefore the latest
     * entry before the given timestamp will come at [0].
     * 
     * @param tSuiteName
     * @param before
     * @return
     */
    List<TSuiteResult> getTSuiteResultsBeforeExclusive(TSuiteName tSuiteName, TSuiteTimestamp before) {
        Objects.requireNonNull(tSuiteName, "argument \'tSuiteName\' must not be null")
        Objects.requireNonNull(before, "argument \'before\' must not be null")
        List<TSuiteResult> result = new ArrayList<TSuiteResult>()
        for (TSuiteResult tsr : tSuiteResults_) {
            if (tSuiteName.equals(tsr.getId().getTSuiteName())) {
                if (TSuiteResultComparator_.compare(tsr, TSuiteResult.newInstance(tSuiteName, before)) > 0) {
                    // use < to select entries exclusively
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
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"RepositoryRoot":{')
        sb.append('"tSuiteResults":[')
        def count = 0
        for (TSuiteResult tsr : tSuiteResults_) {
            if (count > 0) { sb.append(',') }
            count += 1
            sb.append(tsr.toJson())
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }

}
