package com.kazurayam.materials.repository


import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp
import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

final class TreeBranchScanner {

    static Logger logger_ = LoggerFactory.getLogger(TreeBranchScanner.class)

    private RepositoryRoot repoRoot_

    TreeBranchScanner(RepositoryRoot repoRoot) {
        Objects.requireNonNull(repoRoot, "repoRoot must not be null")
        this.repoRoot_ = repoRoot
    }

    void scan(TSuiteResult tSuiteResult) {
        Objects.requireNonNull(tSuiteResult, "tSuiteResult must not be null")
        Files.walkFileTree(
                tSuiteResult.getTSuiteTimestampDirectory(),
                new HashSet<>(),
                Integer.MAX_VALUE,
                new TreeBranchVisitor(repoRoot_, tSuiteResult)
        )
    }

    RepositoryRoot getRepositoryRoot() {
        return repoRoot_
    }

    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TreeBranchScanner":{')
        sb.append('"repoRoot:"' + repoRoot_.toJsonText())
        sb.append('}}')
        return sb.toString()
    }

    public static void main(String[] args) {
        logger_.info("#main " + "Hello, I am TreeBranchScanner.")
        Path baseDir = Paths.get(System.getProperty('user.dir') + '/src/test/fixture/Materials')
        TreeTrunkScanner trunkScanner = new TreeTrunkScanner(baseDir)
        trunkScanner.scan()
        RepositoryRoot repoRoot = trunkScanner.getRepositoryRoot()
        TSuiteResult skeltalTsr = repoRoot.getTSuiteResult(
                new TSuiteName("TS1"),
                new TExecutionProfile("CURA_ProductionEnv"),
                new TSuiteTimestamp("20180810_140105")
        )
        assert skeltalTsr.getMaterialList().size() == 0
        //
        TreeBranchScanner branchScanner = new TreeBranchScanner(repoRoot)
        branchScanner.scan(skeltalTsr)
        TSuiteResult stuffedTsr = repoRoot.getTSuiteResult(
                new TSuiteName("TS1"),
                new TExecutionProfile("CURA_ProductionEnv"),
                new TSuiteTimestamp("20180810_140105")
        )
        assert stuffedTsr.getMaterialList().size() > 0
        logger_.info("#main tsr2=" + JsonOutput.prettyPrint(stuffedTsr.toJsonText()))
    }
}
