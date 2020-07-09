package com.kazurayam.materials.repository

import com.kazurayam.materials.Material
import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput

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
        TSuiteResult tsr1 = repoRoot.getTSuiteResult(
                new TSuiteName("TS1"),
                new TExecutionProfile("CURA_ProductionEnv"),
                new TSuiteTimestamp("20180810_140105")
        )
        assert tsr1.getMaterialList().size() == 0
        //
        TreeBranchScanner branchScanner = new TreeBranchScanner(repoRoot)
        branchScanner.scan(tsr1.getTSuiteTimestampDirectory())
        TSuiteResult tsr2 = repoRoot.getTSuiteResult(
                new TSuiteName("TS1"),
                new TExecutionProfile("CURA_ProductionEnv"),
                new TSuiteTimestamp("20180810_140105")
        )
        assert tsr2.getMaterialList().size() > 0
        logger_.info("#main tsr2=" + JsonOutput.prettyPrint(tsr2.toJsonText()))
    }
}
