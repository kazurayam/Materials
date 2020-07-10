package com.kazurayam.materials.repository

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteResultTree
import com.kazurayam.materials.TSuiteTimestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path
import java.time.LocalDateTime
import java.util.stream.Collectors

final class RepositoryRoot implements TSuiteResultTree {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryRoot.class)

    private Path baseDir_

    private Set<TSuiteResult> tSuiteResults_

    RepositoryRoot(Path baseDir) {
        Objects.requireNonNull(baseDir, "baseDir must not be null")
        Helpers.ensureDirs(baseDir)
        baseDir_ = baseDir
        tSuiteResults_ = new HashSet<TSuiteResult>()
    }

    Path getBaseDir() {
        return baseDir_
    }



    // ================================================================
    //
    //     methods unique to RepositoryRoot
    //
    // ----------------------------------------------------------------
    /**
     * returns a List of TSuiteResult which has the given TSuiteName and
     * the given TExecutionProfile,
     * the Timestamp before the given 2nd arg.
     * The TSuiteResult with a timestamp exactly equal to 'before' will be excluded.
     * The entries returned are sorted in descending order of the timestamp.
     * Therefore the latest entry comes at [0].
     *
     * @param tSuiteName
     * @param timestamp
     * @return
     */
    List<TSuiteResult> getTSuiteResultsBeforeExclusive(TSuiteName tSuiteName,
                                                       TExecutionProfile tExecutionProfile,
                                                       TSuiteTimestamp timestamp) {
        Objects.requireNonNull(tSuiteName, "argument \'tSuiteName\' must not be null")
        Objects.requireNonNull(tExecutionProfile, "argument \'tExecutionProfile\' must not be null")
        Objects.requireNonNull(timestamp, "argument \'timestamp\' must not be null")
        List<TSuiteResult> result = new ArrayList<TSuiteResult>()
        for (TSuiteResult tsr : this.getTSuiteResultList()) {
            if (tSuiteName.equals(tsr.getId().getTSuiteName()) &&
                    tExecutionProfile.equals(tsr.getId().getTExecutionProfile())) {
                if (TSuiteResultComparator_.compare(tsr,
                        TSuiteResult.newInstance(tSuiteName, tExecutionProfile, timestamp))
                        > 0) {
                    // use > to select entries exclusively
                    result.add(tsr)
                }
            }
        }
        Collections.sort(result, TSuiteResultComparator_)
        return result
    }

    /**
     * returns a List of TSuiteResult which has the given 1st argument, and the Timestamp exactly at or before the 2nd argument.
     * The TSuiteResult with a timestamp which is exactly equal to 'timestamp' will be included.
     *
     * @param tSuiteName
     * @param before
     * @return
     */
    List<TSuiteResult> getTSuiteResultsBeforeInclusive(TSuiteName tSuiteName,
                                                       TExecutionProfile tExecutionProfile,
                                                       TSuiteTimestamp timestamp) {
        Objects.requireNonNull(tSuiteName, "argument \'tSuiteName\' must not be null")
        Objects.requireNonNull(tExecutionProfile, "argument \'tExecutionProfile\' must not be null")
        Objects.requireNonNull(timestamp, "argument \'timestamp\' must not be null")
        List<TSuiteResult> result = new ArrayList<TSuiteResult>()
        //println("RepositoryRoot#getTSuiteResultsBeforeIncludes() was invoked")
        for (TSuiteResult tsr : tSuiteResults_) {
            //println("tsr=${tsr}")
            //println("tSuiteName=${tSuiteName}, tsr.getId().getTSuiteName()=${tsr.getId().getTSuiteName()}")
            //println("tExecutionProfile=${tExecutionProfile}, tsr.getId().getTExecutionProfile()=${tsr.getId().getTExecutionProfile()}")
            if (tSuiteName.equals(tsr.getId().getTSuiteName()) &&
                    tExecutionProfile.equals(tsr.getId().getTExecutionProfile())) {
                if (TSuiteResultComparator_.compare(tsr,
                        TSuiteResult.newInstance(tSuiteName, tExecutionProfile, timestamp))
                        >= 0) {
                    //  ^^^^ we are inclusive!
                    result.add(tsr)
                }
            }
        }
        Collections.sort(result, TSuiteResultComparator_)
        return result
    }


    /**
     * returns the sorted list of TSuiteResults ordered by
     * (1) TSuiteName in natural order
     * (2) TSuiteTimestamp in the REVERSE order
     *
     * @return
     */
    List<TSuiteResult> getSortedTSuiteResults() {
        List<TSuiteResult> list = this.getTSuiteResultList()
        Collections.sort(list, TSuiteResultComparator_)
        return list
    }
    /**
     * Comparator for TSuiteResult in the natural order :
     *      ascending order of TSuiteName + TExecutionProfile + TSuiteTimestamp
     */
    private static Comparator<TSuiteResult> TSuiteResultComparator_ =
            new Comparator<TSuiteResult>() {
                @Override
                int compare(TSuiteResult o1, TSuiteResult o2) {
                    int v = o1.getId().getTSuiteName().compareTo(o2.getId().getTSuiteName())
                    if (v < 0) {
                        return v // natural order of TSuiteName
                    } else if (v == 0) {
                        v = o1.getId().getTExecutionProfile().compareTo(o1.getId().getTExecutionProfile())
                        if (v < 0) {
                            return v
                        } else if (v == 0) {
                            LocalDateTime ldt1 = o1.getId().getTSuiteTimestamp().getValue()
                            LocalDateTime ldt2 = o2.getId().getTSuiteTimestamp().getValue()
                            return ldt1.compareTo(ldt2) * -1  // reverse order of TSuiteTimestamp
                        } else {
                            return v
                        }
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
        List<TSuiteResult> tSuiteResults = this.getTSuiteResultList()
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
     * @param tSuiteName
     * @param tExecutionProfile
     * @param tSuiteTimestamp
     * @param tCaseName
     * @return
     */
    TCaseResult getTCaseResult(TSuiteName tSuiteName,
                               TExecutionProfile tExecutionProfile,
                               TSuiteTimestamp tSuiteTimestamp,
                               TCaseName tCaseName) {
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(tExecutionProfile, "tExecutionProfile must not be null")
        Objects.requireNonNull(tSuiteTimestamp, "tSuiteTimestamp must not be null")
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
        TCaseResult tCaseResult = null
        TSuiteResult tsr = this.getTSuiteResult(tSuiteName, tExecutionProfile, tSuiteTimestamp)
        if (tsr != null) {
            tCaseResult = tsr.getTCaseResult(tCaseName)
        }
        return tCaseResult
    }




    // ================================================================
    //
    //     implementing TSuiteResutTree interface
    //
    // ----------------------------------------------------------------
    /**
     * implementing TSuiteResultTree
     */
    @Override
    void addTSuiteResult(TSuiteResult tSuiteResult) {
        Objects.requireNonNull(tSuiteResult)
        boolean found = false
        for (tsr in tSuiteResults_) {
            if (tsr == tSuiteResult) {
                found = true
            }
        }
        if (!found) {
            tSuiteResults_.add(tSuiteResult)
        }
    }


    /**
     * return true if RepositoryRoot has the given TSuiteResult
     *
     * implementing TSuiteResultTree
     */
    @Override
    boolean hasTSuiteResult(TSuiteResult given) {
        Objects.requireNonNull(given, "arg 'given' must not be null")
        TSuiteResult result =
                this.getTSuiteResult(given.getTSuiteName(),
                        given.getTExecutionProfile(),
                        given.getTSuiteTimestamp())
        return (result != null && result == given)
    }

    /**
     * implementing TSuiteResultTree
     */
    List<TSuiteName> getTSuiteNameList() {
        Set<TSuiteName> set = new HashSet<TSuiteName>()
        for (tsr in this.getTSuiteResultList()) {
            set.add(tsr.getTSuiteName())
        }
        return set.stream().collect(Collectors.toList())
    }


    /**
     * implementing TSuiteResultTree
     */
    @Override
    TSuiteResult getTSuiteResult(TSuiteName tSuiteName,
                                 TExecutionProfile tExecutionProfile,
                                 TSuiteTimestamp tSuiteTimestamp) {
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(tExecutionProfile, "tExecutionProfile must not be null")
        Objects.requireNonNull(tSuiteTimestamp, "tSuiteTimestamp must not be null")
        for (tsr in this.getTSuiteResultList()) {
            if (tsr.getId().getTSuiteName() == tSuiteName &&
                    tsr.getId().getTExecutionProfile() == tExecutionProfile &&
                    tsr.getId().getTSuiteTimestamp() == tSuiteTimestamp) {
                return tsr
            }
        }
        return null
    }

    /**
     * implementing TSuiteResultTree
     */
    @Override
    TSuiteResult getTSuiteResult(TSuiteResultId tSuiteResultId) {
        Objects.requireNonNull(tSuiteResultId, "tSuiteResultId must not be null")
        return this.getTSuiteResult(
                tSuiteResultId.getTSuiteName(),
                tSuiteResultId.getTExecutionProfile(),
                tSuiteResultId.getTSuiteTimestamp()
        )
    }


    /**
     * implementing TSuiteResultTree
     */
    @Override
    List<TSuiteResultId> getTSuiteResultIdList(TSuiteName tSuiteName,
                                               TExecutionProfile tExecutionProfile) {
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(tExecutionProfile, "tExecutionProfile must not be null")
        List<TSuiteResultId> list = new ArrayList<TSuiteResultId>()
        for (tsr in this.getTSuiteResultList()) {
            if (tsr.getId().getTSuiteName() == tSuiteName &&
                    tsr.getId().getTExecutionProfile() == tExecutionProfile) {
                list.add(tsr.getId())
            }
        }
        Collections.sort(list)
        return list
    }


    /**
     * implementing TSuiteResultTree
     */
    @Override
    List<TSuiteResultId> getTSuiteResultIdList() {
        List<TSuiteResultId> list = new ArrayList<TSuiteResultId>()
        for (tsr in this.getTSuiteResultList()) {
            list.add(tsr.getId())
        }
        Collections.sort(list)
        return list
    }


    /**
     * implementing TSuiteResultTree
     */
    @Override
    List<TSuiteResult> getTSuiteResultList() {
        List<TSuiteResult> result = new ArrayList<TSuiteResult>()
        for (tsr in tSuiteResults_) {
            result.add(tsr)
        }
        Collections.sort(result)
        return result
    }


    /**
     * implementing TSuiteResultTree
     */
    @Override
    List<TSuiteResult> getTSuiteResultList(TSuiteName tSuiteName) {
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        List<TSuiteResult> result = new ArrayList<TSuiteResult>()
        for (TSuiteResult tsr : this.getTSuiteResultList()) {
            if (tsr.getId().getTSuiteName() == tSuiteName) {
                result.add(tsr)
            }
        }
        Collections.sort(result)
        return result
    }


    /**
     * implementing TSuiteResultTree
     */
    @Override
    List<TSuiteResult> getTSuiteResultList(TSuiteName tSuiteName,
                                           TExecutionProfile tExecutionProfile) {
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(tExecutionProfile, "tExecutionProfile must not be null")
        List<TSuiteResult> result = new ArrayList<TSuiteResult>()
        for (TSuiteResult tsr : this.getTSuiteResultList()) {
            if (tsr.getId().getTSuiteName() == tSuiteName &&
                    tsr.getId().getTExecutionProfile() == tExecutionProfile) {
                result.add(tsr)
            }
        }
        Collections.sort(result)
        return result
    }


    @Override
    List<TSuiteResult> getTSuiteResultList(List<TSuiteResultId> tSuiteResultIdList) {
        Objects.requireNonNull(tSuiteResultIdList, "tSuiteResultIdList must not be null")
        List<TSuiteResult> list = new ArrayList<TSuiteResult>()
        for (TSuiteResultId subject : tSuiteResultIdList) {
            for (TSuiteResult tsr : this.getTSuiteResultList()) {
                if (tsr.getId().getTSuiteName() == subject.getTSuiteName() &&
                        tsr.getId().getTSuiteTimestamp() == subject.getTSuiteTimestamp()) {
                    list.add(tsr)
                }
            }
        }
        Collections.sort(list)
        return list
    }



    // ================================================================
    //
    //     overriding java.lang.Object methods
    //
    // ----------------------------------------------------------------
    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof RepositoryRoot)) {
            return false
        }
        RepositoryRoot other = (RepositoryRoot) obj
        List<TSuiteResult> ownList = this.getTSuiteResultList()
        List<TSuiteResult> otherList = other.getTSuiteResultList()
        logger_.debug("ownList=${ownList.toString()}")
        logger_.debug("otherList=${otherList.toString()}")
        if (ownList.size() == otherList.size()) {
            for (int i; i < ownList.size(); i++) {
                if (!ownList.get(i).equals(otherList.get(i))) {
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
            if (count > 0) {
                sb.append(',')
            }
            count += 1
            sb.append(tsr.toJsonText())
        }
        sb.append('],')
        sb.append('"baseDir":"' + Helpers.escapeAsJsonText(this.getBaseDir().toString()) + '"')
        sb.append('}}')
        return sb.toString()
    }


}
