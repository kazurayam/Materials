package com.kazurayam.materials.repository

import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.impl.MaterialImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.FileSystemLoopException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime

import static java.nio.file.FileVisitResult.CONTINUE
import static java.nio.file.FileVisitResult.TERMINATE

/**
 *
 */
final class RepositoryFileVisitor extends SimpleFileVisitor<Path> {

    private static Logger logger_ = LoggerFactory.getLogger(RepositoryFileVisitor.class)

    private RepositoryRoot repoRoot_

    private TSuiteName tSuiteName_
    private TExecutionProfile tExecutionProfile_
    private TSuiteTimestamp tSuiteTimestamp_
    private TSuiteResult tSuiteResult_
    private TCaseName tCaseName_
    private TCaseResult tCaseResult_

    private int subdirDepth_ = 0
    private Stack<TreeLayer> directoryTransition_

    RepositoryFileVisitor(RepositoryRoot repoRoot) {
        Objects.requireNonNull(repoRoot)
        repoRoot_ = repoRoot
        directoryTransition_ = new Stack<TreeLayer>()
        directoryTransition_.push(TreeLayer.INIT)
    }

    /**
     * Invoked for a directory before entries in the directory are visited.
     */
    @Override
    FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        def from = directoryTransition_.peek()
        switch (from) {
            case TreeLayer.INIT :
                directoryTransition_.push(TreeLayer.ROOT)
                logger_.debug("#preVisitDirectory visiting ${dir} as ROOT")
                return CONTINUE

            case TreeLayer.ROOT :
                directoryTransition_.push(TreeLayer.TESTSUITE)
                logger_.debug("#preVisitDirectory visiting ${dir} as TESTSUITE")
                tSuiteName_ = new TSuiteName(dir)
                return CONTINUE

            case TreeLayer.TESTSUITE:
                directoryTransition_.push(TreeLayer.EXECPROFILE)
                logger_.debug("#preVisitDirectory visiting ${dir} as EXECPROFILE")
                tExecutionProfile_ = new TExecutionProfile(dir)
                return CONTINUE

            case TreeLayer.EXECPROFILE :
                directoryTransition_.push(TreeLayer.TIMESTAMP)
                logger_.debug("#preVisitDirectory visiting ${dir} as TIMESTAMP")
                LocalDateTime ldt = TSuiteTimestamp.parse(dir.getFileName().toString())
                if (ldt != null) {
                    tSuiteTimestamp_ = new TSuiteTimestamp(ldt)
                    Objects.requireNonNull(tSuiteName_, "tSuiteName_ must not be null")
                    Objects.requireNonNull(tExecutionProfile_, "tExecutionProfile_ must not be null")
                    Objects.requireNonNull(tSuiteTimestamp_, "tSuiteTimestamp_ must not be null")
                    tSuiteResult_ = TSuiteResult.newInstance(tSuiteName_, tExecutionProfile_, tSuiteTimestamp_)
                    tSuiteResult_ = tSuiteResult_.setParent(repoRoot_)
                    if (tSuiteResult_ == null) {
                        throw new IllegalStateException("tSuiteResult_ is null when "
                                + "tSuiteName=\"${tSuiteName_}\""
                                + ", tExecutionProfile=\"${tExecutionProfile_}\""
                                + ", tSuiteTimestamp=\"${tSuiteTimestamp_}\""
                                + ", repoRoot=\"${repoRoot_}\"")
                    }
                    repoRoot_.addTSuiteResult(tSuiteResult_)
                } else {
                    logger_.warn("#preVisitDirectory ${dir} is ignored,"
                            + " as it's fileName '${dir.getFileName()}' is not compliant to"
                            + " the TSuiteTimestamp format (${TSuiteTimestamp.DATE_TIME_PATTERN})")
                }
                return CONTINUE

            case TreeLayer.TIMESTAMP :
                directoryTransition_.push(TreeLayer.TESTCASE)
                logger_.debug("#preVisitDirectory visiting ${dir} as TESTCASE")
                tCaseName_ = new TCaseName(dir)
                tCaseResult_ = tSuiteResult_.ensureTCaseResult(tCaseName_)
                return CONTINUE

            case TreeLayer.TESTCASE :
                directoryTransition_.push(TreeLayer.SUBDIR)
                logger_.debug("#preVisitDirectory visiting ${dir} as SUBDIR(${subdirDepth_})")
                subdirDepth_ += 1
                return CONTINUE

            case TreeLayer.SUBDIR :
                logger_.debug("#preVisitDirectory visiting ${dir} as SUBDIR(${subdirDepth_})")
                subdirDepth_ += 1
                return CONTINUE

            default:
                logger_.error("#preVisitDirectory visiting ${dir} as uknown layer")
                return TERMINATE
        }
    }

    /**
     * Invoked for a directory after entries in the directory, and all of their descendants, have been visited.
     */
    @Override
    FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
        def to = directoryTransition_.peek()
        switch (to) {
            case TreeLayer.SUBDIR :
                logger_.debug("#postVisitDirectory leaving ${dir} as SUBDIR(${subdirDepth_})")
                subdirDepth_ -= 1
                if (subdirDepth_ == 0) {
                    directoryTransition_.pop()
                }
                return CONTINUE

            case TreeLayer.TESTCASE :
                logger_.debug("#postVisitDirectory leaving ${dir} as TESTCASE")
                // resolve the lastModified property of the TCaseResult
                LocalDateTime lastModified = resolveLastModifiedOfTCaseResult(tCaseResult_)
                tCaseResult_.setLastModified(lastModified)
                logger_.debug("#postVisitDirectory set lastModified=${lastModified} to ${tCaseResult_.getTCaseName()}")
                // resolve the length property of the TCaseResult
                long length = resolveLengthOfTCaseResult(tCaseResult_)
                tCaseResult_.setSize(length)
                //
                directoryTransition_.pop()
                return CONTINUE

            case TreeLayer.TIMESTAMP :
                logger_.debug("#postVisitDirectory leaving ${dir} as TIMESTAMP")
                if (tSuiteResult_ != null) {
                    // resolve the lastModified property of the TSuiteResult
                    LocalDateTime lastModified = resolveLastModifiedOfTSuiteResult(tSuiteResult_)
                    //tSuiteResult_.setLastModified(lastModified)
                    logger_.debug("#postVisitDirectory set lastModified=${lastModified} to" +
                            " ${tSuiteResult_.getId().getTSuiteName()}/${tSuiteResult_.getId().getTSuiteTimestamp().format()}")
                    // resolve the length property of the TSuiteResult
                    long length = resolveLengthOfTSuiteResult(tSuiteResult_)
                }
                directoryTransition_.pop()
                return CONTINUE

            case TreeLayer.EXECPROFILE :
                logger_.debug("#postVisitDirectory leaving ${dir} as EXECPROFILE")
                directoryTransition_.pop()
                return CONTINUE

            case TreeLayer.TESTSUITE :
                logger_.debug("#postVisitDirectory leaving ${dir} as TESTSUITE")
                directoryTransition_.pop()
                return CONTINUE

            case TreeLayer.ROOT :
                logger_.debug("#postVisitDirectory leaving ${dir} as ROOT")
                directoryTransition_.pop()
                return CONTINUE

            default:
                logger_.error("#postVisitDirectory leaving ${dir} as unknown")
                return TERMINATE
        }
    }

    /**
     * Invoked for a file in a directory.
     */
    @Override
    FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        switch (directoryTransition_.peek()) {
            case TreeLayer.ROOT :
                logger_.debug("#visitFile ${file} in ROOT; this file is ignored")
                return CONTINUE

            case TreeLayer.TESTSUITE :
                logger_.debug("#visitFile ${file} in TESTSUITE; this file is ignored")
                return CONTINUE

            case TreeLayer.TESTSUITE :
                logger_.debug("#visitFile ${file} in EXECPROFILE; this file is ignored")
                return CONTINUE

            case TreeLayer.TIMESTAMP :
                logger_.debug("#visitFile ${file} in TIMESTAMP; this file is ignored")
                return CONTINUE

            case TreeLayer.TESTCASE :
            case TreeLayer.SUBDIR :
                Material material = new MaterialImpl(tCaseResult_, file)
                material.setLastModified(file.toFile().lastModified())
                material.setLength(file.toFile().length())
                material.setDescription(tCaseResult_.getParent().getTSuiteTimestamp().format())
                tCaseResult_.addMaterial(material)
                logger_.debug("#visitFile ${file} in TESTCASE, tCaseResult=${tCaseResult_.toString()}")
                return CONTINUE

            default:
                logger_.error("visitFile ${file} in unknown")
                return TERMINATE
        }
    }


    /**
     * Invoked for a file that could not be visited.
     * 
     * See https://github.com/kazurayam/Materials/issues/2 why we deal with
     * FileSystemLoopException explicitly.
     */
    @Override
    FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        if (exc instanceof FileSystemLoopException) {
            //logger_.warn("circular link was detected: " + file)
            System.err.println("[RepositoryFileVisitor#visitFileFailed] circular link was detected: "
                + file + ", which will be ignored")
        } else {
            //logger_.warn("unable to process file: " + file)
            System.err.println("[RepositoryFileVisitor#visitFileFailed] unable to process file: "
                + file + ", which will be ignored")
        }
        return CONTINUE
    }


    // helpers

    /**
     *
     * @param a instance of TCaseResult
     * @return LocalDateTime for TCaseResult's lastModified property
     */
    private LocalDateTime resolveLastModifiedOfTCaseResult(TCaseResult tCaseResult) {
        Objects.requireNonNull(tCaseResult, "tCaseResult must not be null")
        LocalDateTime lastModified = LocalDateTime.MIN
        List<Material> materials = tCaseResult.getMaterialList()
        for (Material mate : materials) {
            if (mate.getLastModified() > lastModified) {
                lastModified = mate.getLastModified()
            }
        }
        return lastModified
    }

    /**
     *
     * @param an instance of TSuiteResult
     * @return LocalDateTime for TSuiteResult's lastModified property
     */
    private LocalDateTime resolveLastModifiedOfTSuiteResult(TSuiteResult tSuiteResult) {
        Objects.requireNonNull(tSuiteResult, "tSuiteResult must not be null")
        LocalDateTime lastModified = LocalDateTime.MIN
        List<TCaseResult> tCaseResults = tSuiteResult.getTCaseResultList()
        for (TCaseResult tcr : tCaseResults) {
            if (tcr.getLastModified() > lastModified) {
                lastModified = tcr.getLastModified()
            }
        }
        return lastModified
    }
    
    
    /**
     * @param tCaseResult an instance of TCaseResult
     * @return sum of length of Materials contained in the TCaseResult
     */
    private long resolveLengthOfTCaseResult(TCaseResult tCaseResult) {
        Objects.requireNonNull(tCaseResult, "tCaseResult must not be null")
        long length = 0
        List<Material> materials = tCaseResult.getMaterialList()
        for (Material mate : materials) {
            length += mate.getLength()
        }
        return length
    }
    
    /**
     * @param tSuiteResult an instance of TSuiteResult
     * @return sum of length of Materials contained in the TSuiteResult
     */
    private long resolveLengthOfTSuiteResult(TSuiteResult tSuiteResult) {
        Objects.requireNonNull(tSuiteResult, "tSuiteResult must not be null")
        long length = 0
        List<TCaseResult> tCaseResults = tSuiteResult.getTCaseResultList()
        for (TCaseResult tcr :  tCaseResults) {
            length += tcr.getSize()
        }
        return length
    }

}
