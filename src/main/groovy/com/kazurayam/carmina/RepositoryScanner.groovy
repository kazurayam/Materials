package com.kazurayam.carmina

import java.nio.file.Path

/**
 * RepositoryScanner scans file system tree under the baseDir directory, and it builds object trees of
 * TSuiteResult + TCaseResult + TargetURL + MaterialWrapper
 *
 * @author kazurayam
 *
 */
class RepositoryScanner {

    private Path baseDir

    RepositoryScanner(Path baseDir) {
        this.baseDir = baseDir
    }

    List<TSuiteResult> scan() {
        throw new UnsupportedOperationException('TODO')
    }
}
