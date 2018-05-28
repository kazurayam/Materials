package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.Specification

class ScreenshotRepositorySpec extends Specification {

    private static Path workdir

    def setupSpec() {
        workdir = Paths.get("./build/tmp/${ScreenshotRepository.getName()}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }


    def testGetInstanceFull() {
        setup:
        ScreenshotRepository sr = ScreenshotRepository.getInstance(workdir, "testGetInstanceFull")
        expect:
        sr != null
        sr.getBaseDirPath() == workdir
        sr.getTestSuiteResult().getTestSuiteId() == 'testGetInstanceFull'
    }
}

