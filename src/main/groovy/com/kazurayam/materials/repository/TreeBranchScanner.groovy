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

    TreeBranchScanner(Path baseDir) {
        Objects.requireNonNull(baseDir, "baseDir must not be null")
        if (!Files.exists(baseDir)) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        if (!Files.isDirectory(baseDir)) {
            throw new IllegalArgumentException("${baseDir} is not a directory")
        }
        repoRoot_ = new RepositoryRoot(baseDir)
    }

    void scan(Path tSuiteTimestampDirectory) {
        Objects.requireNonNull(tSuiteTimestampDirectory, "tSuiteTimestampDirectory must not be null")
        if ( ! Files.exists(tSuiteTimestampDirectory) ) {
            throw new IllegalArgumentException("${tSuiteTimestampDirectory} does not exist")
        }
        Files.walkFileTree(
                tSuiteTimestampDirectory,
                new HashSet<>(),
                Integer.MAX_VALUE,
                new TreeBranchVisitor(repoRoot_, tSuiteTimestampDirectory)
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
        RepositoryRoot rr1 = trunkScanner.getRepositoryRoot()
        TSuiteResult tsr1 = rr1.getTSuiteResult(
                new TSuiteName("TS1"),
                new TExecutionProfile("CURA_ProductionEnv"),
                new TSuiteTimestamp("20180810_140105")
        )
        TreeBranchScanner branchScanner = new TreeBranchScanner(baseDir)
        branchScanner.scan(tsr1.getTSuiteTimestampDirectory())
        RepositoryRoot rr2 = branchScanner.getRepositoryRoot()
        TSuiteResult tsr2 = rr2.getTSuiteResult(
                new TSuiteName("TS1"),
                new TExecutionProfile("CURA_ProductionEnv"),
                new TSuiteTimestamp("20180810_140105")
        )
        List<Material> mateList = tsr2.getMaterialList()
        assert mateList.size() > 0
        logger_.info("#main tsr2=" + JsonOutput.prettyPrint(tsr2.toJsonText()))
    }
}
