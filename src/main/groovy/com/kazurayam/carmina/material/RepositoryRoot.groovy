package com.kazurayam.carmina.material

import java.nio.file.Path
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RepositoryRoot {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryRoot.class)

    private Path baseDir_
    private List<TSuiteResult> tSuiteResults_

    RepositoryRoot(Path baseDir) {
        assert baseDir != null
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
        boolean found = false
        for (TSuiteResult tsr : tSuiteResults_) {
            if (tsr == tSuiteResult) {
                found = true
            }
        }
        if (!found) {
            tSuiteResults_.add(tSuiteResult)
        }
    }

    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        for (TSuiteResult tsr : tSuiteResults_) {
            if (tsr.getTSuiteName() == tSuiteName && tsr.getTSuiteTimestamp() == tSuiteTimestamp) {
                return tsr
            }
        }
        return null
    }

    List<TSuiteResult> getTSuiteResults() {
        return tSuiteResults_
    }

    /**
     * returns the sorted list of TSuiteResults ordered by TSuiteTimestamp in the reverse order
     * @return
     */
    List<TSuiteResult> getTSuiteResultsSortedByTSuiteTimestampReverseOrder() {
        Comparator<TSuiteResult> comparator = new Comparator<TSuiteResult>() {
            @Override
            public int compare(TSuiteResult o1, TSuiteResult o2) {
                LocalDateTime ldt1 = o1.getTSuiteTimestamp().getValue()
                LocalDateTime ldt2 = o2.getTSuiteTimestamp().getValue()
                return ldt1.compareTo(ldt2) * -1
            }
        }
        List<TSuiteResult> sorted = tSuiteResults_
        Collections.sort(sorted, comparator)
        return sorted
    }

    List<Material> getMaterials() {
        List<Material> list = new ArrayList<Material>()
        for (TSuiteResult tsr : tSuiteResults_) {
            List<Material> mates = tsr.getMaterials()
            for (Material mate : mates) {
                list.add(mate)
            }
        }
        return list
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
                if (ownList.get(i) != otherList.get(i)) { return false }
            }
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        return com.kazurayam.carmina.material.RepositoryRoot.class.hashCode()
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

    /**
     * generate a JSON text for bootstrap-treeview
     *
     * <pre>
     * {@code
     * [
     *   {
     *     text: "TS1/20180530_130419",
     *     nodes: [
     *       {
     *         text: "TC1",
     *         nodes: [
     *           {
     *             text: 'http%3A%2F%2Fdemoaut.katalon.com%2F.png'
     *           },
     *           {
     *             text: 'http%3A%2F%2Fdemoaut.katalon.com§1.png'
     *           }
     *         ]
     *       },
     *       {
     *         text: "TC2,
     *         nodes: [
     *           {
     *             text: 'http%3A%2F%2Fdemoaut.katalon.com§atoz.png'
     *           }
     *         ]
     *       }
     *     ]
     *   },
     *   {
     *     text: "TS2/20180612_111256",
     *     nodes: [
     *       {
     *         text: "TC1",
     *         nodes: [
     *           {
     *             text:  'http%3A%2F%2Fdemoaut.katalon.com%2F.png'
     *           }
     *         ]
     *       }
     *     ]
     *   }
     * ]
     * }
     * </pre>
     * @return
     */
    String toBootstrapTreeviewData() {
        StringBuilder sb = new StringBuilder()
        sb.append('[')
        def count = 0
        List<TSuiteResult> tSuiteResultsOrdered = this.getTSuiteResultsSortedByTSuiteTimestampReverseOrder()
        for (TSuiteResult tSuiteResult : tSuiteResultsOrdered) {
            if (count > 0) { sb.append(',') }
            count += 1
            sb.append(tSuiteResult.toBootstrapTreeviewData())
        }
        sb.append(']')
        return sb.toString()
    }


    String htmlFragmensOfMaterialsAsModal() {
        StringBuilder sb = new StringBuilder()
        List<Material> mates = this.getMaterials()
        for (Material mate : mates) {
            sb.append(mate.toHtmlAsModalWindow())
        }
        return sb.toString()
    }

}
