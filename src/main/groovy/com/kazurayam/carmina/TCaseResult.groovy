package com.kazurayam.carmina

import java.nio.file.Path

/**
 *
 */
class TCaseResult {

    private TSuiteResult parent
    private TCaseName tcName
    private Path tcDir
    private List<TargetURL> targetURLs
    private TCaseStatus tcStatus

    // --------------------- constructors and initializer ---------------------
    TCaseResult(TCaseName tcName) {
        this.tcName = tcName
        this.targetURLs = new ArrayList<TargetURL>()
        this.tcStatus = TCaseStatus.TO_BE_EXECUTED
    }

    // --------------------- properties getter & setters ----------------------
    TCaseResult setParent(TSuiteResult parent) {
        this.parent = parent
        this.tcDir = parent.getTsTimestampDir().resolve(this.tcName.toString())
        return this
    }

    TSuiteResult getParent() {
        return this.parent
    }

    TSuiteResult getTsResult() {
        return this.getParent()
    }

    TCaseName getTcName() {
        return tcName
    }

    Path getTcDir() {
        return tcDir
    }

    void setTcStatus(String tcStatus) {
        assert tcStatus != null
        TCaseStatus tcs = TCaseStatus.valueOf(tcStatus)  // this may throw IllegalArgumentException
        this.setTcStatus(tcs)
    }

    void setTcStatus(TCaseStatus tcStatus) {
        assert tcStatus != null
        this.tcStatus = tcStatus
    }

    TCaseStatus getTcStatus() {
        return this.tcStatus
    }

    // --------------------- create/add/get child nodes ----------------------
    TargetURL findOrNewTargetURL(URL url) {
        TargetURL ntp = this.getTargetURL(url)
        if (ntp == null) {
            ntp = new TargetURL(url).setParent(this)
            this.targetURLs.add(ntp)
        }
        return ntp
    }

    TargetURL getTargetURL(URL url) {
        for (TargetURL tp : this.targetURLs) {
            // you MUST NOT evaluate 'tp.getUrl() == url'
            // because it will take more than 10 seconds for DNS Hostname resolution
            if (tp.getUrl().toString() == url.toString()) {
                return tp
            }
        }
        return null
    }

    List<TargetURL> getTargetURLs() {
        return this.targetURLs
    }

    void addTargetURL(TargetURL targetPage) {
        boolean found = false
        for (TargetURL tp : this.targetURLs) {
            if (tp == targetPage) {
                found = true
            }
        }
        if (!found) {
            this.targetURLs.add(targetPage)
        }
    }

    // -------------------------- helpers -------------------------------------

    // ------------------ overriding Object properties ------------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) {
        //    return true
        //}
        if (!(obj instanceof TCaseResult)) {
            return false
        }
        TCaseResult other = (TCaseResult) obj
        if (this.tcName == other.getTcName()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        return this.tcName.hashCode()
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TcResult":{')
        sb.append('"tcName":"'   + Helpers.escapeAsJsonText(this.tcName.toString())   + '",')
        sb.append('"tcDir":"'    + Helpers.escapeAsJsonText(this.tcDir.toString())    + '",')
        sb.append('"tcStatus":"' + this.tcStatus.toString() + '",')
        sb.append('"targetURLs":[')
        def count = 0
        for (TargetURL tp : this.targetURLs) {
            if (count > 0) { sb.append(',') }
            sb.append(tp.toJson())
            count += 1
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }
}


